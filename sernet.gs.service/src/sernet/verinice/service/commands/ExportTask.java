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

import org.apache.log4j.Logger;

import de.sernet.sync.data.SyncFile;
import de.sernet.sync.data.SyncObject;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ExportTask {

    private static final Logger LOG = Logger.getLogger(ExportTask.class);

    private List<Attachment> attachments = null;

    private IBaseDao<Attachment, Serializable> attachmentDao;

    private HUITypeFactory huiTypeFactory;

    private CnATreeElement element;

    private Set<Attachment> attachmentSet;

    private Set<String> exportedTypes;

    private Set<EntityType> exportedEntityTypes;

    private List<CnATreeElement> changedElementList;

    private boolean reImport;

    private boolean veriniceArchive;

    private Map<String, String> entityTypesBlackList;

    private Map<Class, Class> entityClassBlackList;

    private String sourceId;

    private ICommandService commandService;

    private ExportReferenceTypes exportReferenceTypes;

    public ExportTask(CnATreeElement element) {
        this.element = element;
    }

    /**
     * Export (i.e. "create XML representation of" the given cnATreeElement and
     * its successors. For this, child elements are exported recursively. All
     * elements that have been processed are returned as a list of
     * {@code syncObject}s with their respective attributes, represented as
     * {@code syncAttribute}s.
     * 
     * @param element
     * @return List<Element>
     * @throws CommandException
     */
    public SyncObject export() {

        exportReferenceTypes = new ExportReferenceTypes(commandService);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Exporting element: " + element.getUuid());
        }

        /*
         * Export the given CnATreeElement, if it is NOT blacklisted (i.e. an IT
         * network or category element) AND, if we should restrict the exported
         * objects to certain entity types, this element's entity type IS
         * allowed:
         **/

        String typeId = element.getTypeId();
        if (checkElement(element)) {

            String extId = ExportFactory.createExtId(element);

            exportReferenceTypes.addReference2ExtId(element.getDbId(), extId);

            SyncObject syncObject = new SyncObject();
            syncObject.setExtId(extId);
            syncObject.setExtObjectType(typeId);
            if (element.getIconPath() != null) {
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
                ExportFactory.transform(entity, attributes, typeId, huiTypeFactory,
                        exportReferenceTypes);
            }

            EntityType entityType = element.getEntityType();
            if (entityType != null) {
                // Add the elements EntityType to the set of exported
                // EntityTypes in order to
                // use it for the mapping generation later on.
                getExportedEntityTypes().add(entityType);
            } else {
                // Instance has no EntityType. This is fine but still some
                // mapping
                // information is needed. We save the typeId for later then.
                getExportedTypes().add(typeId);
            }

            if (veriniceArchive) {
                // export attachments of the element
                exportAttachments(syncObject);
                getExportedEntityTypes().add(huiTypeFactory.getEntityType(Attachment.TYPE_ID));
            }

            /**
             * Save source id to re-import element later
             */
            if (reImport) {
                element.setSourceId(sourceId);
                if (element.getExtId() == null) {
                    element.setExtId(extId);
                }
                getChangedElementList().add(element);
            }
            return syncObject;
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Element is not exported: Type " + typeId + ", uuid: " + element.getUuid());
        }
        return null;
    }

    private void exportAttachments(SyncObject syncObject) {
        if (attachments != null) {
            List<SyncFile> fileListXml = syncObject.getFile();
            for (Attachment attachment : attachments) {
                SyncFile syncFile = new SyncFile();
                Entity entity = attachment.getEntity();
                String extId = ExportFactory.createExtId(attachment);
                syncFile.setExtId(extId);
                syncFile.setFile(ExportFactory.createZipFileName(attachment));
                ExportFactory.transform(entity, syncFile.getSyncAttribute(), Attachment.TYPE_ID,
                        huiTypeFactory, exportReferenceTypes);
                fileListXml.add(syncFile);
                if (reImport) {
                    attachment.setExtId(extId);
                    attachment.setSourceId(sourceId);
                    attachmentDao.saveOrUpdate(attachment);
                }
            }
            // Add all files to attachment set, to export file data
            // later
            getAttachmentSet().addAll(attachments);

        }

    }

    private boolean checkElement(CnATreeElement element) {
        return (entityTypesBlackList == null
                || entityTypesBlackList.get(element.getTypeId()) == null)
                && (entityClassBlackList == null
                        || entityClassBlackList.get(element.getClass()) == null);
    }

    public Set<Attachment> getAttachmentSet() {
        if (attachmentSet == null) {
            attachmentSet = new HashSet<>();
        }
        return attachmentSet;
    }

    public Set<EntityType> getExportedEntityTypes() {
        if (exportedEntityTypes == null) {
            exportedEntityTypes = new HashSet<>();
        }
        return exportedEntityTypes;
    }

    public Set<String> getExportedTypes() {
        if (exportedTypes == null) {
            exportedTypes = new HashSet<>();
        }
        return exportedTypes;
    }

    public List<CnATreeElement> getChangedElementList() {
        if (changedElementList == null) {
            changedElementList = new LinkedList<>();
        }
        return changedElementList;
    }

    public void setVeriniceArchive(boolean veriniceArchive) {
        this.veriniceArchive = veriniceArchive;
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

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    public void setHuiTypeFactory(HUITypeFactory huiTypeFactory) {
        this.huiTypeFactory = huiTypeFactory;
    }

    public void setAttachmentDao(IBaseDao<Attachment, Serializable> attachmentDao) {
        this.attachmentDao = attachmentDao;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public CnATreeElement getElement() {
        return element;
    }
}
