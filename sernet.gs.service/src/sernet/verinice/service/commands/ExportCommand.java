/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 *     Daniel Murygin <dm[at]sernet[dot]de> - Export to verinice archive, concurrency
 ******************************************************************************/

package sernet.verinice.service.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipOutputStream;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.service.IThreadCompleteListener;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.sync.StreamFactory;
import sernet.verinice.service.sync.VeriniceArchive;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;
import de.sernet.sync.sync.SyncRequest;

/**
 * Creates an XML representation of the given list of
 * CnATreeElements.
 * 
 * ExportCommand uses multiple threads to load data. Default number of threads is 3.
 * You can configure maximum number of threads in veriniceserver-common.xml:
 * 
 * <bean id="hibernateCommandService" class="sernet.verinice.service.HibernateCommandService">
 *  <!-- Set properties for command instances here -->
 *  <!-- Key is <COMMAND_CLASS_NAME>.<PROPERTY_NAME> -->
 *  <property name="properties">
 *      <props>
 *          <prop key="sernet.verinice.service.commands.ExportCommand.maxNumberOfThreads">5</prop>
 *      </props>
 *  </property>
 * </bean>
 * 
 * @author <andreas[at]becker[dot]name>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class ExportCommand extends ChangeLoggingCommand implements IChangeLoggingCommand
{
    private transient Logger log = Logger.getLogger(ExportCommand.class);
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ExportCommand.class);
        }
        return log;
    }
    
    private static final Integer LOCK = Integer.valueOf(0);
   
    public static final String PROP_MAX_NUMBER_OF_THREADS = "maxNumberOfThreads";
    public static final int DEFAULT_NUMBER_OF_THREADS = 3;
    
    // Configuration fields set by client
    private List<CnATreeElement> elements;
	private String sourceId;
    private boolean reImport = false;
    private Integer exportFormat;
    private Map<String,String> entityTypesBlackList;   
    private Map<Class,Class> entityClassBlackList;
	
    // Result fields
	private byte[] result;	
    private List<CnATreeElement> changedElements;
    private String stationId;
    
    // Fields used on server only
    private transient byte[] xmlData;
    private transient Set<CnALink> linkSet;
    private transient Set<Attachment> attachmentSet;
    private transient Set<EntityType> exportedEntityTypes;
    private transient Set<String> exportedTypes;    
    private transient CacheManager manager = null;
    private transient String cacheId = null;
    private transient Cache cache = null;   
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    private transient ExecutorService taskExecutor;
 
    public ExportCommand( List<CnATreeElement> elements, String sourceId, boolean reImport) {
        new ExportCommand(elements, sourceId, reImport, SyncParameter.EXPORT_FORMAT_DEFAULT);
    }
    
	public ExportCommand( List<CnATreeElement> elements, String sourceId, boolean reImport, Integer exportFormat) {
		this.elements = elements;
		this.sourceId = sourceId;
		this.reImport = reImport;
		if(exportFormat!=null) {
		    this.exportFormat = exportFormat;
		} else {
		    this.exportFormat = SyncParameter.EXPORT_FORMAT_DEFAULT;
		}
		this.attachmentSet = new HashSet<Attachment>();    
        this.stationId = ChangeLogEntry.STATION_ID;
	}
	
	private void createFields() {
        this.changedElements = new LinkedList<CnATreeElement>();
        this.linkSet = new HashSet<CnALink>();
        this.attachmentSet = new HashSet<Attachment>();
        this.exportedTypes = new HashSet<String>();
        this.exportedEntityTypes = new HashSet<EntityType>();
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.interfaces.ICommand#execute()
	 */
	@Override
    public void execute() {
	    try {
	        createFields();
    	    xmlData = export();
    		
    		if(isVeriniceArchive()) {
    		    result = createVeriniceArchive();
    		} else {
    		    result = xmlData;
    		}
	    } catch (RuntimeException re) {
            getLog().error("Runtime exception while exporting", re);
            throw re;
        } catch (Exception e) {
            getLog().error("Exception while exporting", e);
            throw new RuntimeCommandException("Exception while exporting", e);
        }
	    finally {
	        getCache().removeAll();
	    }
		
	}

	/**
     * Export (i.e. "create XML representation of" the given cnATreeElement
     * and its successors. For this, child elements are exported recursively.
     * All elements that have been processed are returned as a list of
     * {@code syncObject}s with their respective attributes, represented
     * as {@code syncAttribute}s.
     * 
     * @return XML representation of elements
     * @throws CommandException
     */
    private byte[] export() throws CommandException {	
        if (getLog().isInfoEnabled()) {
            getLog().info("Max number of threads is: " + getMaxNumberOfThreads());
        }
        
        getCache().removeAll();
        
		SyncData syncData = new SyncData();
		ExportTransaction exportTransaction = new ExportTransaction();
	
		for( CnATreeElement element : elements ) {	
		    exportTransaction.setElement(element);
		    
		    ExportThread thread = new ExportThread(exportTransaction);
            configureThread(thread);
	        thread.export();
	        getValuesFromThread(thread);
		    
			exportChildren(exportTransaction);
			syncData.getSyncObject().add(exportTransaction.getTarget());
		}
		
		for(CnALink link : linkSet) {
		    CnATreeElement dependant  = link.getDependant();
		    dependant = getFromCache(dependant);
		    link.setDependant(dependant);
		    CnATreeElement dependency  = link.getDependency();
		    dependency = getFromCache(dependency);
		    link.setDependency(dependency);
		    ExportFactory.transform(link, syncData.getSyncLink());
		}
		
		if (getLog().isDebugEnabled()) {
            Statistics s = getCache().getStatistics();
            getLog().debug("Cache size: " + s.getObjectCount() + ", hits: " + s.getCacheHits());                  
        }
		
		SyncMapping syncMapping = new SyncMapping();
		createMapping(syncMapping.getMapObjectType());
		
		SyncRequest syncRequest = new SyncRequest();
        syncRequest.setSourceId(sourceId);
        syncRequest.setSyncData(syncData);
        syncRequest.setSyncMapping(syncMapping);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ExportFactory.marshal(syncRequest, bos);
		return bos.toByteArray();
    }
    
    private void exportChildren(final ExportTransaction transaction) throws CommandException {      
        final int timeOutFactor = 40;
        CnATreeElement element = transaction.getElement();
        Set<CnATreeElement> children = element.getChildren();
        
        List<ExportTransaction> transactionList = new ArrayList<ExportTransaction>();
        
        taskExecutor = Executors.newFixedThreadPool(getMaxNumberOfThreads());
        
        for( CnATreeElement child : children ) {
            ExportTransaction childTransaction = new ExportTransaction(child);
            transactionList.add(childTransaction);
            ExportThread thread = new ExportThread(childTransaction);
            configureThread(thread);
            thread.addListener(new IThreadCompleteListener() {            
                @Override
                public void notifyOfThreadComplete(Thread thread) {
                    ExportThread exportThread = (ExportThread) thread;
                    synchronized(LOCK) {
                        if(exportThread.getSyncObject()!=null) {
                            transaction.getTarget().getChildren().add(exportThread.getSyncObject());
                        }
                        getValuesFromThread(exportThread);
                    }                 
                }
            });
            taskExecutor.execute(thread);
        }
        
        awaitTermination(transactionList.size() * timeOutFactor);
        
        if (getLog().isDebugEnabled() && transactionList.size()>0) {
            getLog().debug(transactionList.size() + " export threads finished.");
        }
        
        for( ExportTransaction childTransaction : transactionList ) {
            if(checkElement(childTransaction.getElement())) {
                exportChildren(childTransaction);
            }
        }
    }
    
    private boolean checkElement(CnATreeElement element) {
        return (getEntityTypesBlackList() == null || getEntityTypesBlackList().get(element.getTypeId()) == null)
         && (getEntityClassBlackList() == null || getEntityClassBlackList().get(element.getClass()) == null);
    }

    /**
     * Creates the verinice archive after createXmlData() was called.
     * 
     * @return the verinice archive as byte[]
     * @throws CommandException
     */
    private byte[] createVeriniceArchive() throws CommandException {
        try {        
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();        
            ZipOutputStream zipOut = new ZipOutputStream(byteOut);     
            
            ExportFactory.createZipEntry(zipOut, VeriniceArchive.VERINICE_XML, xmlData);
            ExportFactory.createZipEntry(zipOut, VeriniceArchive.DATA_XSD, StreamFactory.getDataXsdAsStream());
            ExportFactory.createZipEntry(zipOut, VeriniceArchive.MAPPING_XSD, StreamFactory.getMappingXsdAsStream());
            ExportFactory.createZipEntry(zipOut, VeriniceArchive.SYNC_XSD, StreamFactory.getSyncXsdAsStream());
            ExportFactory.createZipEntry(zipOut, VeriniceArchive.README_TXT, StreamFactory.getReadmeAsStream());
                     
            for (Attachment attachment : getAttachmentSet()) {
                LoadAttachmentFile command = new LoadAttachmentFile(attachment.getDbId());      
                command = getCommandService().executeCommand(command);
                if(command.getAttachmentFile()!=null && command.getAttachmentFile().getFileData()!=null) {
                    ExportFactory.createZipEntry(zipOut, ExportFactory.createZipFileName(attachment), command.getAttachmentFile().getFileData());                  
                }
                
            }
            
            zipOut.close();
            byteOut.close();
            return byteOut.toByteArray();
        } catch (IOException e) {
            getLog().error("Error while creating zip output stream", e);
            throw new RuntimeCommandException(e);
        }
    }
    

    /**
     * @param timeout in seconds
     */
    private void awaitTermination(int timeout) {
        final int secondsUntilTimeOut = 60;
        taskExecutor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!taskExecutor.awaitTermination(timeout, TimeUnit.SECONDS)) {
                getLog().error("Export executer timeout reached: " + timeout + "s. Terminating execution now.");
                taskExecutor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!taskExecutor.awaitTermination(secondsUntilTimeOut, TimeUnit.SECONDS)) {
                    getLog().error("Export executer did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            taskExecutor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Adds SyncMapping for all EntityTypes that have been exported. This
     * is going to be an identity mapping.
     * 
     * @param mapObjectTypeList
     */
    private void createMapping(List<MapObjectType> mapObjectTypeList) {
        for (EntityType entityType : exportedEntityTypes)
        {
            if(entityType!=null) {
                // Add <mapObjectType> element for this entity type to <syncMapping>:
                MapObjectType mapObjectType = new MapObjectType();
                
                mapObjectType.setIntId(entityType.getId());
                mapObjectType.setExtId(entityType.getId());
                
                List<MapAttributeType> mapAttributeTypes = mapObjectType.getMapAttributeType();
                for (PropertyType propertyType : entityType.getAllPropertyTypes())
                {
                    // Add <mapAttributeType> for this property type to current <mapObjectType>:
                    MapAttributeType mapAttributeType = new MapAttributeType();
                    
                    mapAttributeType.setExtId(propertyType.getId());
                    mapAttributeType.setIntId(propertyType.getId());
                    
                    mapAttributeTypes.add(mapAttributeType);
                }
                
                mapObjectTypeList.add(mapObjectType);
            }
        }
        // For all exported objects that had no entity type (e.g. category objects)
        // we create a simple 1-to-1 mapping using their type id.
        for (String typeId : exportedTypes)
        {
            MapObjectType mapObjectType = new MapObjectType();
            mapObjectType.setIntId(typeId);
            mapObjectType.setExtId(typeId);
            
            mapObjectTypeList.add(mapObjectType);
        }
    }
    
    private CnATreeElement getFromCache(CnATreeElement element) {
        Element cachedElement = getCache().get(element.getUuid());
        if(cachedElement!=null) {
            element = (CnATreeElement) cachedElement.getValue();
            if (getLog().isDebugEnabled()) {
                getLog().debug("Element from cache: " + element.getTitle() + ", UUID: " + element.getUuid());
            }
        } else {
            element = getDao().retrieve(element.getDbId(), RetrieveInfo.getPropertyInstance());
            if(element!=null) {
                getCache().put(new Element(element.getUuid(), element));
            }
        }
        return element;
    }
      
    private void configureThread(ExportThread thread) {
        thread.setCommandService(getCommandService());
        thread.setCache(getCache());
        thread.setDao(getDao());
        thread.setAttachmentDao(getDaoFactory().getDAO(Attachment.class));
        thread.setHuiTypeFactory(getHuiTypeFactory());
        thread.setSourceId(sourceId);
        thread.setVeriniceArchive(isVeriniceArchive());
        thread.setReImport(isReImport());
        thread.setEntityTypesBlackList(getEntityTypesBlackList());
        thread.setEntityClassBlackList(getEntityClassBlackList());
    }
    
    /**
     * @param exportThread
     */
    private void getValuesFromThread(ExportThread exportThread) {
        linkSet.addAll(exportThread.getLinkSet());
        attachmentSet.addAll(exportThread.getAttachmentSet());
        exportedEntityTypes.addAll(exportThread.getExportedEntityTypes());
        exportedTypes.addAll(exportThread.getExportedTypes());
        changedElements.addAll(exportThread.getChangedElementList());
    }

	private boolean isVeriniceArchive() {
	    return SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV.equals(exportFormat);
	}
	
	private boolean isReImport() {
		return reImport;
	}
  
    private Map<String,String> getEntityTypesBlackList() {
        if(entityTypesBlackList==null) {
            entityTypesBlackList = createDefaultEntityTypesBlackList();
        }
        return entityTypesBlackList;
    }
    
    private Map<Class,Class> getEntityClassBlackList() {
        if(entityClassBlackList==null) {
        	entityClassBlackList = createDefaultEntityClassBlackList();
        }
        return entityClassBlackList;
    }
    
    /**
     * @return
     */
    private Map<String, String> createDefaultEntityTypesBlackList() {
        Map<String, String> blacklist = new HashMap<String, String>();
        // BSI Risk analyses will not be imported or exported(Bug 194)
        blacklist.put(FinishedRiskAnalysis.TYPE_ID, FinishedRiskAnalysis.TYPE_ID);
        return blacklist;
    }
    
    private Map<Class, Class> createDefaultEntityClassBlackList() {
        Map<Class, Class> blacklist = new HashMap<Class, Class>();
        blacklist.put(RisikoMassnahmenUmsetzung.class, RisikoMassnahmenUmsetzung.class);
        return blacklist;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = createDao();
        }
        return dao;
    }
    
    private IBaseDao<CnATreeElement, Serializable> createDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

	public byte[] getResult() {
		return (result != null) ? result.clone() : null; 
	}
	
	@Override
    protected void finalize() throws Throwable {
	    CacheManager.getInstance().shutdown();
	    super.finalize();
	};
	
	private Cache getCache() { 	
	    if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || cache==null || !Status.STATUS_ALIVE.equals(cache.getStatus())) {
	        cache = createCache();
	    } else {
	        cache = getManager().getCache(cacheId);
	    }
	    return cache;
 	}
	
	private Cache createCache() {
	    final int maxElementsInMemory = 20000;
	    final int timeToLiveSeconds = 1800;
	    final int timeToIdleSeconds = timeToLiveSeconds;
	    cacheId = UUID.randomUUID().toString();
        cache = new Cache(cacheId, maxElementsInMemory, false, false, timeToLiveSeconds, timeToIdleSeconds);
        getManager().addCache(cache);
        return cache;
	}
	
	
	
	public CacheManager getManager() {
	    if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus())) {
	        manager = CacheManager.create();
	    }
        return manager;
    }

    protected HUITypeFactory getHuiTypeFactory() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }

	/* (non-Javadoc)
	 * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
	 */
	@Override
	public int getChangeType() {
		return ChangeLogEntry.TYPE_UPDATE;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
	 */
	@Override
	public List<CnATreeElement> getChangedElements() {
		return changedElements;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
	 */
	@Override
	public String getStationId() {
		return stationId;
	}

    public Integer getExportFormat() {
        if(exportFormat==null) {
            exportFormat = SyncParameter.EXPORT_FORMAT_DEFAULT;
        }
        return exportFormat;
    }

    public void setExportFormat(Integer exportFormat) {
        this.exportFormat = exportFormat;
    }

    public Set<Attachment> getAttachmentSet() {
        if(attachmentSet==null) {
            attachmentSet = new HashSet<Attachment>();
        }
        return attachmentSet;
    }
    
    /**
     * You can configure maximum number of threads in veriniceserver-common.xml:
     * 
     * <bean id="hibernateCommandService" class="sernet.verinice.service.HibernateCommandService">
     *  <!-- Set properties for command instances here -->
     *  <!-- Key is <COMMAND_CLASS_NAME>.<PROPERTY_NAME> -->
     *  <property name="properties">
     *      <props>
     *          <prop key="sernet.verinice.service.commands.ExportCommand.maxNumberOfThreads">5</prop>
     *      </props>
     *  </property>
     * </bean>
     * 
     * @return
     */
    private int getMaxNumberOfThreads() {
        int number = DEFAULT_NUMBER_OF_THREADS;
        Object prop = getProperties().get(PROP_MAX_NUMBER_OF_THREADS);
        if(prop!=null) {
            try {
                number = Integer.valueOf((String) prop);
            } catch( Exception e) {
                getLog().error("Error while readind max number of thread from property: " + PROP_MAX_NUMBER_OF_THREADS + ", value is: " + prop, e);
                number = DEFAULT_NUMBER_OF_THREADS;
            }
        }
        return number;
    }
    
}
