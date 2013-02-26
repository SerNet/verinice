/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.service.NotifyingThread;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import de.sernet.sync.data.SyncFile;
import de.sernet.sync.data.SyncObject;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ExportThread extends NotifyingThread {

    private static final Logger LOG = Logger.getLogger(ExportThread.class);
    
    private static final Object LOCK = new Object();
    
    private Cache cache = null;
    
    private ICommandService commandService;
    
    private IBaseDao<CnATreeElement, Serializable> dao;
    
    private IBaseDao<Attachment, Serializable> attachmentDao;
    
    private HUITypeFactory huiTypeFactory;
    
    private ExportTransaction transaction;
    
    private Set<CnALink> linkSet;
    
    private Set<Attachment> attachmentSet;

    private Set<String> exportedTypes;
    
    private Set<EntityType> exportedEntityTypes;
    
    private List<CnATreeElement> changedElementList;
    
    private Object exportFormat;

    private boolean reImport;
    
    private boolean veriniceArchive;
    
    private Map<String,String> entityTypesBlackList;
    
    private Map<Class,Class> entityClassBlackList;
    
    private String sourceId;
    
    
    public ExportThread(ExportTransaction transaction) {
        super();
        this.transaction = transaction;
    }

    /* (non-Javadoc)
     * @see sernet.gs.service.NotifyingThread#doRun()
     */
    @Override
    public void doRun() {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting export job asyncronly...");
            }
            ServerInitializer.inheritVeriniceContextState();
            export();
        } catch (CommandException e) {
            LOG.error("Error while exporting", e);
        }

    }

    /**
     * Export (i.e. "create XML representation of" the given cnATreeElement
     * and its successors. For this, child elements are exported recursively.
     * All elements that have been processed are returned as a list of
     * {@code syncObject}s with their respective attributes, represented
     * as {@code syncAttribute}s.
     * 
     * @param element
     * @return List<Element>
     * @throws CommandException 
     */
    public void export() throws CommandException {
        CnATreeElement element = transaction.getElement();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Exporting element: " + element.getUuid());
        } 
        
        /**
         * Export the given CnATreeElement, if it is NOT blacklisted (i.e. an IT network
         * or category element) AND, if we should restrict the exported objects to certain
         * entity types, this element's entity type IS allowed:
         **/
        
        String typeId = element.getTypeId();
        if(checkElement(element)) {
            element = hydrate( element );
            transaction.setElement(element);
            
            String extId = ExportFactory.createExtId(element);
            SyncObject syncObject = new SyncObject();
            syncObject.setExtId(extId);
            syncObject.setExtObjectType(typeId);
            if(element.getIconPath()!=null) {
                syncObject.setIcon(element.getIconPath());
            }

            List<de.sernet.sync.data.SyncAttribute> attributes = syncObject.getSyncAttribute();
            
            /**
             * Retrieve all properties from the entity:
             */     
            Entity entity = element.getEntity();
            // Category instance may have no Entity attached to it
            // For those we do not store any property values.
            if (entity != null) {
                ExportFactory.transform(entity, attributes, typeId, getHuiTypeFactory());
                
                // Add the elements EntityType to the set of exported EntityTypes in order to
                // use it for the mapping generation later on.
                getExportedEntityTypes().add(element.getEntityType());
            }
            else {
                // Instance has no EntityType. This is fine but still some mapping
                // information is needed. We save the typeId for later then.
                getExportedTypes().add(typeId);
            }
            // add links to linkSet
            addLinks(element);
                    
            if(isVeriniceArchive()) {
                // export attachments of the element
                exportAttachments(element, syncObject);
                getExportedEntityTypes().add(getHuiTypeFactory().getEntityType(Attachment.TYPE_ID));
            }
            
            transaction.setTarget(syncObject);
            
            /**
             * Save source and external id to re-import element later
             */
            if(isReImport()) {
                // TODO: what happens if external and source id already set?
                element.setextId(extId);
                element.setSourceId(getSourceId());
                getDao().merge(element);
                getChangedElementList().add(element);
            }
        } else if(LOG.isDebugEnabled()) { // else if(checkElement(element))
            LOG.debug("Element is not exported: Type " + typeId + ", uuid: " + element.getUuid());
        }       
    }
    
    private CnATreeElement hydrate(CnATreeElement element)
    { 
        if (element == null){
            return element;
        }
        CnATreeElement elementFromCache = getElementFromCache(element);
        if(elementFromCache!=null) {
            return elementFromCache;
        }  
        
        RetrieveInfo ri = RetrieveInfo.getPropertyChildrenInstance();
        ri.setLinksDown(true);
        ri.setLinksUp(true);
        element = getDao().retrieve(element.getDbId(), ri);
        
        cacheElement(element);       
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Element: " + element.getTitle() + " hydrated, UUID: " + element.getUuid());
        }
             
        return element;
    }
    
    private CnATreeElement getElementFromCache(CnATreeElement element) {
        CnATreeElement fromCache = null;
        synchronized (LOCK) {
            if(Status.STATUS_ALIVE.equals(cache.getStatus())) {
                Element cachedElement = getCache().get(element.getUuid());
                if(cachedElement!=null) {
                    fromCache = (CnATreeElement) cachedElement.getValue();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Element from cache: " + element.getTitle() + ", UUID: " + element.getUuid());
                    }
                }
            } else {
                LOG.warn("Cache is not alive. Can't put element to cache, uuid: " + element.getUuid());
            }
        }
        return fromCache;
    }

    private void cacheElement(CnATreeElement element) {
        synchronized (LOCK) {
            if(Status.STATUS_ALIVE.equals(cache.getStatus())) {
                getCache().put(new Element(element.getUuid(), element));
            } else {
                LOG.warn("Cache is not alive. Can't put element to cache, uuid: " + element.getUuid());
            }
        }
    }
    
    private void exportAttachments(CnATreeElement element, SyncObject syncObject) throws CommandException {
        if(element!=null) {
            LoadAttachments command = new LoadAttachments(element.getDbId());
            command = getCommandService().executeCommand(command);
            List<Attachment> fileList = command.getAttachmentList();
            List<SyncFile> fileListXml = syncObject.getFile();
            for (Attachment attachment : fileList) {
                SyncFile syncFile = new SyncFile();
                Entity entity = attachment.getEntity();
                String extId = ExportFactory.createExtId(attachment);
                syncFile.setExtId(extId);
                syncFile.setFile(ExportFactory.createZipFileName(attachment));
                ExportFactory.transform(entity, syncFile.getSyncAttribute(), Attachment.TYPE_ID, getHuiTypeFactory());
                fileListXml.add(syncFile);
                if(isReImport()) {
                    attachment.setExtId(extId);
                    attachment.setSourceId(getSourceId());
                    getAttachmentDao().saveOrUpdate(attachment);
                }
            }
            // Add all files to attachment set, to export file data later
            getAttachmentSet().addAll(fileList);
        }     
    }
    
    private void addLinks(CnATreeElement element) {
        try {
            getLinkSet().addAll(element.getLinksDown());
            getLinkSet().addAll(element.getLinksUp());
        } catch (Exception e) {
            LOG.error("error while getting links of element: " + element.getTitle() + "(" + element.getTypeId() + "), UUID: " + element.getUuid(), e);
        }
    }
    
    private boolean checkElement(CnATreeElement element) {
        return (getEntityTypesBlackList() == null || getEntityTypesBlackList().get(element.getTypeId()) == null)
         && (getEntityClassBlackList() == null || getEntityClassBlackList().get(element.getClass()) == null);
    }
    
    public SyncObject getSyncObject() {
        return getTransaction().getTarget();
    }
    
    public Set<CnALink> getLinkSet() {
        if(linkSet==null) {
            linkSet = new HashSet<CnALink>();
        }
        return linkSet;
    }
    
    public Set<Attachment> getAttachmentSet() {
        if(attachmentSet==null) {
            attachmentSet = new HashSet<Attachment>();
        }
        return attachmentSet;
    }
   
    public ICommandService getCommandService() {
        return commandService;
    }


    public ExportTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(ExportTransaction transaction) {
        this.transaction = transaction;
    }

    public void setLinkSet(Set<CnALink> linkSet) {
        this.linkSet = linkSet;
    }

    public void setAttachmentSet(Set<Attachment> attachmentSet) {
        this.attachmentSet = attachmentSet;
    }
    
    public Set<EntityType> getExportedEntityTypes() {
        if(exportedEntityTypes==null) {
            exportedEntityTypes = new HashSet<EntityType>();
        }
        return exportedEntityTypes;
    }

    public Set<String> getExportedTypes() {
        if(exportedTypes==null) {
            exportedTypes = new HashSet<String>();
        }
        return exportedTypes;
    }
    
    public void setExportedEntityTypes(Set<EntityType> exportedEntityTypes) {
        this.exportedEntityTypes = exportedEntityTypes;
    }

    public void setExportedTypes(Set<String> exportedTypes) {
        this.exportedTypes = exportedTypes;
    }

    public List<CnATreeElement> getChangedElementList() {
        if(changedElementList==null) {
            changedElementList = new LinkedList<CnATreeElement>();
        }
        return changedElementList;
    }

    public void setChangedElementList(List<CnATreeElement> changedElementList) {
        this.changedElementList = changedElementList;
    }
    
    public Object getExportFormat() {
        return exportFormat;
    }

    public boolean isReImport() {
        return reImport;
    }

    public boolean isVeriniceArchive() {
        return veriniceArchive;
    }

    public void setVeriniceArchive(boolean veriniceArchive) {
        this.veriniceArchive = veriniceArchive;
    }

    public Map<String, String> getEntityTypesBlackList() {
        return entityTypesBlackList;
    }
   
    public Map<Class, Class> getEntityClassBlackList() {
        return entityClassBlackList;
    }

    public void setExportFormat(Object exportFormat) {
        this.exportFormat = exportFormat;
    }

    public void setReImport(boolean reImport) {
        this.reImport = reImport;
    }

    public void setEntityTypesBlackList(Map<String, String> entityTypesBlackList) {
        this.entityTypesBlackList = entityTypesBlackList;
    }

    public void setEntityClassBlackList(Map<Class, Class> entityClassBlackList) {
        this.entityClassBlackList = entityClassBlackList;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }


    protected HUITypeFactory getHuiTypeFactory() {
        return huiTypeFactory;
    }
    
    public void setHuiTypeFactory(HUITypeFactory huiTypeFactory) {
        this.huiTypeFactory = huiTypeFactory;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return dao;
    }


    public void setDao(IBaseDao<CnATreeElement, Serializable> dao) {
        this.dao = dao;
    }

    public IBaseDao<Attachment, Serializable> getAttachmentDao() {
        return attachmentDao;
    }


    public void setAttachmentDao(IBaseDao<Attachment, Serializable> attachmentDao) {
        this.attachmentDao = attachmentDao;
    }


    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
}
