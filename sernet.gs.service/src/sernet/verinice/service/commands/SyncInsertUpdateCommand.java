/*******************************************************************************
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Andreas Becker <andreas.r.becker[at]rub[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster[a]tarent[dot]de> - removal of JDom API use
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.iso27k.LoadImportObjectsHolder;
import sernet.verinice.service.iso27k.LoadModel;
import sernet.verinice.service.sync.VeriniceArchive;
import de.sernet.sync.data.SyncAttribute;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncFile;
import de.sernet.sync.data.SyncLink;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;

/**
 * This command is used as a sub-command of {@link SyncCommand} to insert and update
 * elements during the import process.
 * 
 * It's not a standalone-command.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings({ "serial", "restriction" })
public class SyncInsertUpdateCommand extends GenericCommand implements IAuthAwareCommand  {

    private transient Logger log = Logger.getLogger(SyncInsertUpdateCommand.class);

    private static final int FLUSH_LEVEL = 50;
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SyncInsertUpdateCommand.class);
        }
        return log;
    }
    
    private transient Logger logrt = Logger.getLogger(SyncInsertUpdateCommand.class.getName() + ".rt");

    public Logger getLogrt() {
        if (logrt == null) {
            logrt = Logger.getLogger(SyncInsertUpdateCommand.class.getName() + ".rt");
        }
        return logrt;
    }

    private String sourceId;
    private boolean sourceIdExists;
    private transient SyncMapping syncMapping;
    private transient SyncData syncData;
    private String userName;

    private SyncParameter parameter;
    
    private List<String> errorList;

    private int inserted = 0, updated = 0, merged = 0;
    
    private long globalStart = 0;
    
    private Map<Class,CnATreeElement> containerMap = new HashMap<Class,CnATreeElement>(2);

    private Set<CnATreeElement> elementSet = new HashSet<CnATreeElement>();
    
    private transient Map<String, CnATreeElement> idElementMap = new HashMap<String, CnATreeElement>();

    private transient Map<String, Attachment> attachmentMap;
    
    private transient IAuthService authService;
    
    private transient Map<Class,IBaseDao> daoMap = new HashMap<Class, IBaseDao>();
    
    public SyncInsertUpdateCommand(
    		String sourceId, 
    		SyncData syncData, 
    		SyncMapping syncMapping,
    		String userName,
    		SyncParameter parameter,
    		List<String> errorList) {
        super();
        this.sourceId = sourceId;
        this.syncData = syncData;
        this.syncMapping = syncMapping;
        this.userName = userName;
        this.parameter = parameter;
        this.errorList = errorList;
        attachmentMap = new HashMap<String, Attachment>();
    }

    /**
     * Processes the given <syncData> and <syncMapping> elements in order to
     * insert and/or update objects in(to) the database, according to the flags
     * insert & update.
     * 
     * If there already exists an ITVerbund from a past sync session, this one
     * (identified by its sourceID) will be used; otherwise this creates a new
     * one within the BSIModel.
     * 
     * @throws InvalidRequestException
     * @throws CommandException
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            if (getLogrt().isDebugEnabled()) {
                globalStart = System.currentTimeMillis();
            }
            merged = 0;
            CheckSourceId checkSourceId = new CheckSourceId(sourceId);
            checkSourceId = getCommandService().executeCommand(checkSourceId);
            sourceIdExists = checkSourceId.exists();
            if (sourceIdExists && getLog().isDebugEnabled()) {
                getLog().debug("Source-Id exists in DB: " + sourceId);
            }
            List<SyncObject> soList = syncData.getSyncObject();
            
            for (SyncObject so : soList) {
                importObject(null, so);        
            } // for <syncObject>
            if (getLogrt().isDebugEnabled()) {
                getLogrt().debug("Elements: " + merged);
            }
            for (SyncLink syncLink : syncData.getSyncLink()) {
                importLink(syncLink);
            }
            finalizeDaos();
        } catch (RuntimeException e) {
            getLog().error("RuntimeException while importing", e);
            throw e;
        } catch (Exception e) {
            getLog().error("Exception while importing", e);
            throw new RuntimeCommandException(e);
        }
    }

    /**
     * 
     */
    private void finalizeDaos() {
        daoMap.clear();
    }

    private void importObject(CnATreeElement parent, SyncObject so) throws CommandException {
        String extId = so.getExtId();
        String extObjectType = so.getExtObjectType();
        long start = 0;
        if (getLogrt().isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Importing element type: " + extObjectType + ", extId: " + extId + "...");
        }
        
        boolean setAttributes = false;

        MapObjectType mot = getMap(extObjectType);

        if (mot == null) {
            final String message = "Could not find mapObjectType-Element for XML type: " + extObjectType;
            getLog().error(message);
            errorList.add(message);
            return;
        }

        // this element "knows", which huientitytype is applicable and
        // how the associated properties have to be mapped!
        String veriniceObjectType = mot.getIntId();
        
        CnATreeElement elementInDB = null;
        if(sourceIdExists) {
            elementInDB = findDbElement(sourceId, extId, true, true);
        }
        
        if (elementInDB != null) {
            if (parameter.isUpdate()) {
                /*** UPDATE: ***/
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element found in db: updating, uuid: " + elementInDB.getUuid());
                }
                if(parent==null) {
                    // Org. or IT-Verbund was found
                    parent = elementInDB.getParent();
                }
                if(parameter.isIntegrate()) {
                    elementInDB.setSourceId(null);
                    elementInDB.setExtId(null);
                }
                setAttributes = true;
                updated++;
            } else {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element found in db, update disabled, uuid: " + elementInDB.getUuid());
                }
                // do not update this object's attributes!
                setAttributes = false;
            }
        }
       
        Class clazz = CnATypeMapper.getClassFromTypeId(veriniceObjectType);
        IBaseDao<CnATreeElement, Serializable> dao = getDao(clazz);      
        
        parent = (parent == null) ? accessContainer(clazz) : parent;
        
        // If no previous object was found in the database and the 'insert'
        // flag is given, create a new object.
        if (elementInDB == null && parameter.isInsert()) {                          
            try {
                // create new object in db...
                elementInDB =createElement(parent, clazz);
                 
                // ...and set its sourceId and extId:
                if(!parameter.isIntegrate()) {
                    elementInDB.setSourceId(sourceId);
                    elementInDB.setExtId(extId);
                }
                
                if(elementInDB instanceof Organization || elementInDB instanceof ITVerbund) {
                    addElement(elementInDB);
                }
                
                setAttributes = true;
                inserted++;
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element inserted, uuid: " + elementInDB.getUuid());
                }
            } catch (Exception e) {
                getLog().error("Error while inserting element, type: " + extObjectType + ", extId: " + extId, e);
                errorList.add("Konnte " + veriniceObjectType + "-Objekt nicht erzeugen.");
            }
        }

        /*
         * Now if we should update an existing object or created a new object,
         * set the associated attributes:
         */
        if (null != elementInDB && setAttributes) {
            if(so.getIcon()!=null && !so.getIcon().isEmpty()) {
                elementInDB.setIconPath(so.getIcon());
            }
            // for all <syncAttribute>-Elements below current
            // <syncObject>...
            HUITypeFactory huiTypeFactory = getHuiTypeFactory();
            for (SyncAttribute sa : so.getSyncAttribute()) {
                String attrExtId = sa.getName();
                List<String> attrValues = sa.getValue();

                MapAttributeType mat = getMapAttribute(mot, attrExtId);

                String attrIntId = null;
                if (mat == null) {
                    final String message = "Could not find mapObjectType-Element for XML attribute type: " + attrExtId + " of type: " + extObjectType + ". Using extern-id.";
                    getLog().warn(message);
                    attrIntId = attrExtId;
                } else {
                    attrIntId = mat.getIntId();                  
                }
                elementInDB.getEntity().importProperties(huiTypeFactory,attrIntId, attrValues);
                addElement(elementInDB);
            } // for <syncAttribute>
            elementInDB = dao.merge(elementInDB);
            parent.addChild(elementInDB);
            elementInDB.setParentAndScope(parent);
            
            // set the scope id of orgs. and it-verbunds.
            if(elementInDB instanceof Organization || elementInDB instanceof ITVerbund) {
                elementInDB.setScopeId(elementInDB.getDbId());
            }
     
            merged++;
            if(merged % FLUSH_LEVEL == 0 ) {
               long flushstart = 0;
               if (getLogrt().isDebugEnabled()) {
                   flushstart = System.currentTimeMillis();
               }
               dao.flush();
               dao.clear();
               if (getLogrt().isDebugEnabled()) {
                   long time = System.currentTimeMillis() - flushstart;
                   getLogrt().debug("Flushed, runtime: " + time + " ms");
               }
            }
        } // if( null != ... )

        if(isVeriniceArchive()) {
            importFileList(elementInDB, so.getFile());
        }
        
        if(elementInDB!=null) {
            idElementMap.put(extId, elementInDB);
        }
        
        if (getLogrt().isDebugEnabled()) {
            long cur = System.currentTimeMillis();
            long time = cur - start;
            long globalTime = cur - globalStart;
            long a = Math.round((globalTime*1.0)/merged);
            getLogrt().debug("Element " + merged + ": " + time + "ms, ave.: " + a);
        }
        // Handle all the child objects.
        for (SyncObject child : so.getChildren()) {
            // The object that was created or modified during the course of
            // this method call is the parent for the import of the
            // child elements.
            if (getLog().isDebugEnabled() && child!=null) {
                getLog().debug("Child found, type: " + child.getExtObjectType() + ", extId: " + child.getExtId());
            }
            importObject(elementInDB, child);
        }
    }

    /**
     * @param clazz
     * @return
     */
    private <T> IBaseDao<T, Serializable>  getDao(Class clazz) {
        IBaseDao<T, Serializable> dao = daoMap.get(clazz);
        if(dao==null) {
            dao = getDaoFactory().getDAO(clazz);
            daoMap.put(clazz, dao);
        }
        return dao;
    }
    
    private CnATreeElement createElement(CnATreeElement parent, Class clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        CnATreeElement child;
        // get constructor with parent-parameter and create new object:
        if(clazz.equals(Organization.class)) {
            child = (CnATreeElement) clazz.getConstructor(CnATreeElement.class,boolean.class).newInstance(parent,false);
        } else {
            child = (CnATreeElement) clazz.getConstructor(CnATreeElement.class).newInstance(parent);
        }

        if (authService.isPermissionHandlingNeeded()) {
            addPermissions(parent,child);
        }

        return child;
    }
    
    private void addPermissions(CnATreeElement parent, CnATreeElement child) {
        // By default, inherit permissions from parent element but ITVerbund
        // instances cannot do this, as its parents (BSIModel) is not visible
        // and has no permissions. Therefore we use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new ITVerbund.
        if (child instanceof ITVerbund || child instanceof Organization) {
            addPermissions(child);           
        } else {
            child.setPermissions(Permission.clonePermissionSet(child, parent.getPermissions()));
        }
    }
    
    private void addPermissions(/*not final*/ CnATreeElement element) {
        HashSet<Permission> newperms = new HashSet<Permission>();
        newperms.add(Permission.createPermission(element, authService.getUsername(), true, true));
        element.setPermissions(newperms);
        for (CnATreeElement child : element.getChildren()) {
            addPermissions(child);
        }
    }

    /**
     * @param elementInDB
     * @param file
     * @throws CommandException 
     */
    private void importFileList(CnATreeElement elementInDB, List<SyncFile> fileList) throws CommandException {
        HUITypeFactory huiTypeFactory = getHuiTypeFactory();
        for (SyncFile fileXml : fileList) {
            LoadAttachmentByExternalId loadAttachment = new LoadAttachmentByExternalId(sourceId, fileXml.getExtId());
            loadAttachment = getCommandService().executeCommand(loadAttachment);
            Attachment attachment = loadAttachment.getAttachment();     
            if(attachment==null) { 
                attachment = new Attachment();
                attachment.setExtId(fileXml.getExtId());
                attachment.setSourceId(sourceId);
            }
            attachmentMap.put(fileXml.getFile(), attachment);
            attachment.setCnATreeElementId(elementInDB.getDbId());
            attachment.setCnAElementTitel(elementInDB.getTitle());
            attachment.setTitel(fileXml.getFile());
                   
            SaveNote command = new SaveNote(attachment);        
            command = getCommandService().executeCommand(command);
            attachment = (Attachment) command.getAddition();
            
            MapObjectType mot = getMap(Attachment.TYPE_ID);
            for (SyncAttribute sa : fileXml.getSyncAttribute()) {
                String attrExtId = sa.getName();
                List<String> attrValues = sa.getValue();
                MapAttributeType mat = getMapAttribute(mot, attrExtId);

                if (mat == null) {
                    final String message = "Could not find mapObjectType-Element for XML attribute type: " + attrExtId + " of type: " + Attachment.TYPE_ID;
                    getLog().error(message);
                    this.errorList.add(message);
                } else {
                    String attrIntId = mat.getIntId();
                    attachment.getEntity().importProperties(huiTypeFactory, attrIntId, attrValues);
                }
            }
            
            
        }   
    }
    
    /**
     * Imports all file data from a verinice Archive (zipFileData).
     * Call importFileList before calling this method! 
     * 
     * Danger: Out-of-memory trouble for large file...
     * 
     * @param zipFileData a verinice Archive
     * @throws IOException
     * @throws CommandException
     */
    public void importFileData(byte[] zipFileData) throws IOException, CommandException {
        List<String> fileNameList = new ArrayList<String>(attachmentMap.keySet());
        // Next call might result in out-of-memory trouble
        // because all attachment data from zipFileData
        // is stored in one map.
        // In case of out of memory exceptions
        // call this method in for loop.
        Map<String, byte[]> fileDataMap = VeriniceArchive.extractZipEntries(zipFileData, fileNameList);
        
        for (String fileName : fileNameList) {
            Attachment attachment = attachmentMap.get(fileName);
            IBaseDao<AttachmentFile, Serializable> dao = getDao(AttachmentFile.class);
            AttachmentFile attachmentFile = dao.findById(attachment.getDbId());
            attachmentFile.setFileData(fileDataMap.get(fileName));
            fileDataMap.remove(fileName);
            SaveAttachment saveFileCommand = new SaveAttachment(attachmentFile);
            saveFileCommand = getCommandService().executeCommand(saveFileCommand);
            saveFileCommand.clear();
        }
        fileDataMap.clear();     
    }

    /**
     * @param syncLink
     * @throws CommandException 
     */
    private void importLink(SyncLink syncLink) {
        String dependantId = syncLink.getDependant();
        String dependencyId = syncLink.getDependency();
        CnATreeElement dependant = idElementMap.get(dependantId);
        if(dependant==null) {     
        	dependant = findDbElement(this.sourceId, dependantId, true, true);
            if(dependant==null) {
                getLog().error("Can not import link. dependant not found in xml file and db, dependant ext-id: " + dependantId + " dependency ext-id: " + dependencyId);
                return;
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("dependant not found in XML file but in db, ext-id: " + dependantId);
            }
        }
        CnATreeElement dependency = idElementMap.get(dependencyId);
        if(dependency==null) { 
        	dependency = findDbElement(this.sourceId, dependencyId, true, true);
            if(dependency==null) {
                getLog().error("Can not import link. dependency not found in xml file and db, dependency ext-id: " + dependencyId + " dependant ext-id: " + dependantId);
                return;
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("dependency not found in XML file but in db, ext-id: " + dependencyId);
            }
        }
        
        CnALink link = new CnALink(dependant,dependency,syncLink.getRelationId(),syncLink.getComment());
        
        String titleDependant = "unknown";
    	String titleDependency = "unknown";
    	if (getLog().isDebugEnabled()) {     	
			try { 
				titleDependant = dependant.getTitle();
				titleDependency = dependency.getTitle();
			} catch(Exception e) {
				getLog().debug("Error while reading title.", e);
			}
    	}
    	
        if(isNew(link)) {
	        dependant.addLinkDown(link);
	        dependency.addLinkUp(link);
	        if (getLog().isDebugEnabled()) {
	        	getLog().debug("Creating new link from: " + titleDependant + " to: " + titleDependency + "...");
			}
	        getDao(CnALink.class).saveOrUpdate(link);
        } else if (getLog().isDebugEnabled()) {
        	getLog().debug("Link exists: " + titleDependant + " to: " + titleDependency);
		}
        
    }

    private boolean isNew(CnALink link) {
    	String hql = "from CnALink as link where link.id.dependantId=? and link.id.dependencyId=? and (link.id.typeId=? or link.id.typeId=?)";
    	String relationId = link.getRelationId();
    	String relationId2 = relationId;
    	if(CnALink.Id.NO_TYPE.equals(relationId)) {
    		relationId2 = "";
    	}
    	if(relationId!=null && relationId.isEmpty()) {
    		relationId2 = CnALink.Id.NO_TYPE;
    	}
    	Object[] paramArray = new Object[]{
    			link.getDependant().getDbId(),
    			link.getDependency().getDbId(),
    			relationId,
    			relationId2};
    	List result = getDao(CnALink.class).findByQuery(hql, paramArray); 	
		return result==null || result.isEmpty();
	}

	private MapObjectType getMap(String extObjectType) {
        for (MapObjectType mot : syncMapping.getMapObjectType()) {
            if (extObjectType.equals(mot.getExtId())) {
                return mot;
            }
        }

        return null;
    }

    private SyncMapping.MapObjectType.MapAttributeType getMapAttribute(MapObjectType mot, String extObjectType) {
        for (SyncMapping.MapObjectType.MapAttributeType mat : mot.getMapAttributeType()) {
            if (extObjectType.equals(mat.getExtId())) {
                return mat;
            }
        }

        return null;
    }
    
    /**
     * Query element (by externalID) from DB, which has been previously
     * synchronized from the given sourceID.
     * 
     * @param sourceId
     * @param externalId
     * @return the CnATreeElement from the query or null, if nothing was found
     * @throws RuntimeException if more than one element is found
     */
    private CnATreeElement findDbElement(String sourceId, String externalId, boolean fetchLinksDown, boolean fetchLinksUp) {
    	CnATreeElement result = null;
    	// use a new crudCommand (load by external, source id):
        LoadCnAElementByExternalID command = new LoadCnAElementByExternalID(sourceId, externalId, fetchLinksDown, fetchLinksUp);
        try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			final String message = "Error while loading element by source and externeal id";
    		log.error(message,e);
			throw new RuntimeCommandException(message,e);
		}
        List<CnATreeElement> foundElements = command.getElements();
        if (foundElements != null) {
        	if(foundElements.size()==1) {  
        		result = foundElements.get(0);
        	}
        	if(foundElements.size()>1) {  
        		final String message = "Found more than one element with source-id: " + sourceId + " and externeal-id: " + externalId;
        		log.error(message);
    			throw new RuntimeCommandException(message);
        	}
        }
        return result;
    }
   
    /**
     * If during the import action an object has to be created for which no
     * parent is available (or can be found) the artificial 'rootImportObject'
     * should be used.
     * 
     * <p>
     * This method should <em>onl</em> be called when the 'rootImportObject' is
     * definitely needed and going to be used because the root object is not
     * only created but also automatically persisted in the database. If it were
     * not used later on the user would see an object node in the object tree.
     * </p>
     * @param clazz 
     * 
     * @return
     */
    private CnATreeElement accessContainer(Class clazz) {
        // Create the importRootObject if it does not exist yet
        // and set the 'importRootObject' variable.
    	CnATreeElement container = containerMap.get(clazz);
        if (container==null) {
            LoadImportObjectsHolder cmdLoadContainer = new LoadImportObjectsHolder(clazz);
            try {
                cmdLoadContainer = getCommandService().executeCommand(cmdLoadContainer);
            } catch (CommandException e) {
                getLog().error("Error while accessinf container.", e);
                errorList.add("Fehler beim Ausführen von LoadBSIModel.");
                throw new RuntimeCommandException("Fehler beim Anlegen des Behälters für importierte Objekte.", e);
            }
            container = cmdLoadContainer.getHolder();
            if(container==null) {
                container = createContainer(clazz);
            }    
            // load the parent
            container.getParent().getTitle();
            containerMap.put(clazz,container);
        } 
        return container;
    }

    private CnATreeElement createContainer(Class clazz) {
        if(LoadImportObjectsHolder.isImplementation(clazz, IBSIStrukturElement.class)) {
            return createBsiContainer();
        } else {
            return createIsoContainer();
        }     
    }
    
    private CnATreeElement createBsiContainer() {
        LoadBSIModel cmdLoadModel = new LoadBSIModel();
        try {
            cmdLoadModel = getCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
            handleCreateContainerException(e);
        }
        BSIModel model = cmdLoadModel.getModel();
        ImportBsiGroup holder = null;
        try {
            holder = new ImportBsiGroup(model);
            addPermissions(holder);
            getDao(ImportBsiGroup.class).saveOrUpdate(holder);
        } catch (Exception e) {
            handleCreateContainerException(e);
        }
        return holder;
    }
    
    private CnATreeElement createIsoContainer() {
        LoadModel cmdLoadModel = new LoadModel();
        try {
            cmdLoadModel = getCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
            handleCreateContainerException(e);
        }
        ISO27KModel model = cmdLoadModel.getModel();
        ImportIsoGroup holder = null;
        try {
            holder = new ImportIsoGroup(model);
            addPermissions(holder);
            getDao(ImportIsoGroup.class).saveOrUpdate(holder);
        } catch (Exception e1) {
            handleCreateContainerException(e1);
        }
        return holder;
    }
    
    private void handleCreateContainerException(Exception e) {
        String message = "Fehler beim Anlegen des Behaelters für importierte Objekte.";
        getLog().error(message,e);
        errorList.add(message);
        throw new RuntimeCommandException(message,e );
    }
    
    protected void addElement(CnATreeElement element) {
        if(elementSet==null) {
            elementSet = new HashSet<CnATreeElement>();
        }
        // load the parent
        element.getParent().getTitle();
        elementSet.add(element);
    }
    
    public int getUpdated() {
        return updated;
    }

    public int getInserted() {
        return inserted;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public Map<Class, CnATreeElement> getContainerMap() {
		return containerMap;
	}

	public Set<CnATreeElement> getElementSet() {
        return elementSet;
    }

    protected String getUserName() {
		return userName;
	}

	protected void setUserName(String userName) {
		this.userName = userName;
	}
	
	private boolean isVeriniceArchive() {
        return SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV.equals(parameter.getFormat());
    }
	
	/**
     * @return the authService
     */
    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    /**
     * @param authService the authService to set
     */
    @Override
    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

    private HUITypeFactory getHuiTypeFactory() {
	    return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
	}
    

}
