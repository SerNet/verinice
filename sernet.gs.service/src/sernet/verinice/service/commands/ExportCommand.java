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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;
import de.sernet.sync.risk.Risk;
import de.sernet.sync.sync.SyncRequest;
import de.sernet.sync.sync.SyncRequest.SyncVnaSchemaVersion;
import sernet.gs.service.CollectionUtil;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.sync.StreamFactory;
import sernet.verinice.service.sync.VeriniceArchive;
import sernet.verinice.service.sync.VnaSchemaVersion;

/**
 * Creates an VNA or XML representation for the given list of CnATreeElements.
 * 
 * @author <andreas[at]becker[dot]name>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class ExportCommand extends GenericCommand implements IChangeLoggingCommand {
    private static final Logger log = Logger.getLogger(ExportCommand.class);

    // Configuration fields set by client
    private final List<CnATreeElement> elements;
    private final String sourceId;
    private boolean reImport = false;
    private boolean exportRiskAnalysis = true;
    private Integer exportFormat;
    private Map<String, String> entityTypesBlackList;
    private Map<Class, Class> entityClassBlackList;

    // Result fields
    private byte[] result;
    private List<CnATreeElement> changedElements;
    private final String stationId;

    // Fields used on server only
    private transient Set<CnALink> linkSet;
    private transient Set<Attachment> attachmentSet;
    private transient Set<Integer> riskAnalysisIdSet;
    private transient Set<EntityType> exportedEntityTypes;
    private transient Set<String> exportedTypes;
    private transient Set<Integer> exportedElementIds;
    private transient Map<Integer, List<Attachment>> attachmentsByElementId = null;
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    private transient Map<Integer, Collection<CnATreeElement>> elementsByParentId;

    public ExportCommand(final List<CnATreeElement> elements, final String sourceId,
            final boolean reImport) {
        this(elements, sourceId, reImport, SyncParameter.EXPORT_FORMAT_DEFAULT);
    }

    public ExportCommand(final List<CnATreeElement> elements, final String sourceId,
            final boolean reImport, final Integer exportFormat) {
        this.elements = elements;
        this.sourceId = sourceId;
        this.reImport = reImport;
        if (exportFormat != null) {
            this.exportFormat = exportFormat;
        } else {
            this.exportFormat = SyncParameter.EXPORT_FORMAT_DEFAULT;
        }
        this.attachmentSet = new HashSet<>();
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    private void createFields() {
        this.changedElements = new LinkedList<>();
        this.linkSet = new HashSet<>();
        this.attachmentSet = new HashSet<>();
        this.exportedElementIds = new HashSet<>();
        this.riskAnalysisIdSet = new HashSet<>();
        this.exportedTypes = new HashSet<>();
        this.exportedEntityTypes = new HashSet<>();
        this.elementsByParentId = new HashMap<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            createFields();
            SyncRequest syncRequest = export();
            Risk risk = exportRiskAnalyses();
            if (isReImport()) {
                if (log.isInfoEnabled()) {
                    log.info("Prepare reimport is enabled. Saving the IDS of "
                            + changedElements.size() + " elements ...");
                }
                saveChangedElements();
            }

            if (isVeriniceArchive()) {
                result = createVeriniceArchive(syncRequest, risk);
            } else {
                try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    ExportFactory.marshal(syncRequest, bos);
                    result = bos.toByteArray();
                }
            }
        } catch (final RuntimeException re) {
            log.error("Runtime exception while exporting", re);
            throw re;
        } catch (final Exception e) {
            log.error("Exception while exporting", e);
            throw new RuntimeCommandException("Exception while exporting", e);
        } finally {
            elementsByParentId = null;
            attachmentsByElementId = null;
        }

    }

    /**
     * Export (i.e. "create XML representation of" the given cnATreeElement and
     * its successors. For this, child elements are exported recursively. All
     * elements that have been processed are returned as a list of
     * {@code syncObject}s with their respective attributes, represented as
     * {@code syncAttribute}s.
     * 
     * @return XML representation of elements
     * @throws CommandException
     */
    private SyncRequest export() throws CommandException {

        final SyncVnaSchemaVersion formatVersion = createVersionData();

        final SyncData syncData = new SyncData();

        if (log.isInfoEnabled()) {
            log.info("Exporting elements...");
        }

        for (final CnATreeElement element : elements) {
            loadData(element.getScopeId());
            CnATreeElement elementWithProperties = elementsByParentId.get(element.getParentId())
                    .stream().filter(e -> e.getDbId().equals(element.getDbId())).findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Requested object with db ID " + element.getDbId() + " not found."));
            SyncObject exported = exportElement(elementWithProperties);
            syncData.getSyncObject().add(exported);
        }

        if (log.isInfoEnabled()) {
            log.info("Exporting links...");
        }
        elementsByParentId.clear();
        exportLinks(syncData);

        final SyncMapping syncMapping = new SyncMapping();
        createMapping(syncMapping.getMapObjectType());

        final SyncRequest syncRequest = new SyncRequest();
        syncRequest.setSourceId(sourceId);
        syncRequest.setSyncData(syncData);
        syncRequest.setSyncMapping(syncMapping);
        syncRequest.setSyncVnaSchemaVersion(formatVersion);

        return syncRequest;
    }

    private void loadData(Integer scopeId) {
        loadElements(scopeId);
        attachmentsByElementId = loadAttachments(scopeId);
    }

    private void loadElements(Integer scopeId) {
        @SuppressWarnings("unchecked")
        List<Integer> elementIDs = getDao().findByCriteria(DetachedCriteria
                .forClass(CnATreeElement.class).add(Restrictions.eq("scopeId", scopeId))
                .setProjection(Projections.property("id")));
        Thread loadElementsThread = new Thread(() -> CollectionUtil
                .partition(elementIDs, IDao.QUERY_MAX_ITEMS_IN_LIST).forEach(partition -> {
                    DetachedCriteria criteria = DetachedCriteria.forClass(CnATreeElement.class)
                            .add(Restrictions.in("id", partition));
                    RetrieveInfo retrieveInfo = RetrieveInfo.getPropertyInstance();
                    retrieveInfo.configureCriteria(criteria);
                    @SuppressWarnings("unchecked")
                    List<CnATreeElement> elementsToCache = getDao().findByCriteria(criteria);
                    elementsToCache.forEach(element -> elementsByParentId
                            .computeIfAbsent(element.getParentId(), parentId -> new LinkedList<>())
                            .add(element));
                }), "export-" + scopeId + "-load-elements");
        loadElementsThread.start();

        loadLinks(elementIDs);
        try {
            loadElementsThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while seeding caches for scopeId " + scopeId,
                    e);
        }
    }

    private Map<Integer, List<Attachment>> loadAttachments(Integer scopeId) {
        @NonNull
        IBaseDao<Attachment, Serializable> attachmentDao = getDaoFactory().getDAO(Attachment.class);
        @SuppressWarnings("unchecked")
        List<Attachment> attachments = attachmentDao.findByCriteria(
                DetachedCriteria.forClass(Attachment.class).createAlias("cnATreeElement", "element")
                        .add(Restrictions.eq("element.scopeId", scopeId)));
        return attachments.stream().collect(
                Collectors.groupingBy(attachment -> attachment.getCnATreeElement().getDbId()));
    }

    private void loadLinks(List<Integer> elementIDs) {
        @NonNull
        IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
        CollectionUtil.partition(elementIDs, IDao.QUERY_MAX_ITEMS_IN_LIST).forEach(partition -> {
            DetachedCriteria criteria = DetachedCriteria.forClass(CnALink.class)
                    .add(Restrictions.in("id.dependantId", partition));
            linkSet.addAll(linkDao.findByCriteria(criteria));
            criteria = DetachedCriteria.forClass(CnALink.class)
                    .add(Restrictions.in("id.dependencyId", partition));
            linkSet.addAll(linkDao.findByCriteria(criteria));
        });
    }

    private SyncVnaSchemaVersion createVersionData() {

        final VnaSchemaVersion vnaSchemaVersion = getCommandService().getVnaSchemaVersion();
        final SyncVnaSchemaVersion formatVersion = new SyncVnaSchemaVersion();

        // Initialize the data transfer object
        formatVersion.setVnaSchemaVersion(vnaSchemaVersion.getVnaSchemaVersion());
        final List<String> compatibleVersion = formatVersion.getCompatibleVersions();
        compatibleVersion.addAll(vnaSchemaVersion.getCompatibleSchemaVersions());

        return formatVersion;
    }

    private Risk exportRiskAnalyses() {
        if (!isRiskAnalysis()) {
            return null;
        }
        final RiskAnalysisExporter exporter = new RiskAnalysisExporter();
        exporter.setCommandService(getCommandService());
        exporter.setRiskAnalysisIdSet(riskAnalysisIdSet);
        exporter.run();
        return exporter.getRisk();
    }

    private boolean isRiskAnalysis() {
        return isExportRiskAnalysis() && !riskAnalysisIdSet.isEmpty();
    }

    public boolean isExportRiskAnalysis() {
        return exportRiskAnalysis;
    }

    public void setExportRiskAnalysis(final boolean exportRiskAnalysis) {
        this.exportRiskAnalysis = exportRiskAnalysis;
    }

    private SyncObject exportElement(final CnATreeElement element) throws CommandException {
        final ExportTask task = new ExportTask(element);
        configureTask(task);
        SyncObject syncObject = task.export();
        getValuesFromTask(task);
        syncObject.getChildren().addAll(exportChildren(task.getElement()));
        return syncObject;
    }

    private void exportLinks(final SyncData syncData) {
        for (final CnALink link : linkSet) {
            if (!exportedElementIds.contains(link.getId().getDependantId())) {
                log.warn("Dependant of link not found. Check access rights. " + link.getId());
                continue;
            }
            if (!exportedElementIds.contains(link.getId().getDependencyId())) {
                log.warn("Dependency of link not found. Check access rights. " + link.getId());
                continue;
            }
            ExportFactory.transform(link, syncData.getSyncLink());
        }
    }

    private Collection<? extends SyncObject> exportChildren(final CnATreeElement element)
            throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Call exportChildren in ExportCommand hashcode " + this.hashCode()
                    + "for object " + element.getTitle());
        }
        final Collection<CnATreeElement> children = getElementChildren(element);
        if (FinishedRiskAnalysis.TYPE_ID.equals(element.getTypeId())) {
            children.addAll(getRiskAnalysisOrphanElements(element));
        }

        if (children.isEmpty()) {
            return Collections.emptySet();
        }
        final List<SyncObject> exportedChildren = new ArrayList<>(children.size());

        for (final CnATreeElement child : children) {
            if (log.isDebugEnabled()) {
                log.debug("Create export job for child " + child.getDbId());
            }
            final ExportTask task = new ExportTask(child);
            configureTask(task);

            SyncObject exportedChild = task.export();
            if (exportedChild != null) {
                exportedChildren.add(exportedChild);
                if (checkElement(child)) {
                    exportedChild.getChildren().addAll(exportChildren(task.getElement()));
                }
            }
            getValuesFromTask(task);
        }

        return exportedChildren;
    }

    protected Collection<CnATreeElement> getElementChildren(final CnATreeElement element) {
        return elementsByParentId.getOrDefault(element.getDbId(), Collections.emptySet());

    }

    private boolean checkElement(final CnATreeElement element) {
        return getEntityTypesBlackList().get(element.getTypeId()) == null
                && getEntityClassBlackList().get(element.getClass()) == null;
    }

    private Set<CnATreeElement> getRiskAnalysisOrphanElements(final CnATreeElement element)
            throws CommandException {
        final Set<CnATreeElement> returnValue = new HashSet<>();
        FindRiskAnalysisListsByParentID loader = new FindRiskAnalysisListsByParentID(
                element.getDbId());
        loader = getCommandService().executeCommand(loader);
        FinishedRiskAnalysisLists lists = loader.getFoundLists();
        if (lists != null) {
            returnValue.addAll(lists.getAssociatedGefaehrdungen());
        }
        return returnValue;
    }

    /**
     * Creates the verinice archive after createXmlData() was called.
     * 
     * @return the verinice archive as byte[]
     * @throws CommandException
     */
    private byte[] createVeriniceArchive(SyncRequest syncRequest, Risk risk)
            throws CommandException {
        try (final ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
            try (final ZipOutputStream zipOut = new ZipOutputStream(byteOut)) {

                ExportFactory.createZipEntry(zipOut, VeriniceArchive.VERINICE_XML, syncRequest);
                if (isRiskAnalysis()) {
                    ExportFactory.createZipEntry(zipOut, VeriniceArchive.RISK_XML, risk);
                }
                ExportFactory.createZipEntry(zipOut, VeriniceArchive.DATA_XSD,
                        StreamFactory.getDataXsdAsStream());
                ExportFactory.createZipEntry(zipOut, VeriniceArchive.MAPPING_XSD,
                        StreamFactory.getMappingXsdAsStream());
                ExportFactory.createZipEntry(zipOut, VeriniceArchive.SYNC_XSD,
                        StreamFactory.getSyncXsdAsStream());
                ExportFactory.createZipEntry(zipOut, VeriniceArchive.RISK_XSD,
                        StreamFactory.getRiskXsdAsStream());
                ExportFactory.createZipEntry(zipOut, VeriniceArchive.README_TXT,
                        StreamFactory.getReadmeAsStream());

                for (final Attachment attachment : getAttachmentSet()) {
                    LoadAttachmentFile command = new LoadAttachmentFile(attachment.getDbId(), true);
                    command = getCommandService().executeCommand(command);
                    if (command.getAttachmentFile() != null
                            && command.getAttachmentFile().getFileData() != null) {
                        ExportFactory.createZipEntry(zipOut,
                                ExportFactory.createZipFileName(attachment),
                                command.getAttachmentFile().getFileData());
                    }
                    command.setAttachmentFile(null);
                }
                zipOut.closeEntry();
            }
            return byteOut.toByteArray();
        } catch (final IOException e) {
            log.error("Error while creating zip output stream", e);
            throw new RuntimeCommandException(e);
        }
    }

    private void saveChangedElements() {
        int number = changedElements.size();
        int i = 0;
        for (CnATreeElement element : changedElements) {
            getDao().merge(element);
            if (i % 50 == 0) { // Same as the JDBC batch size
                flushAndClearHibernateSession();
            }
            i++;
            if (log.isDebugEnabled()) {
                log.debug(i + "/" + number + " elements saved");
            }
        }
    }

    public void flushAndClearHibernateSession() {
        // flush a batch of inserts and release memory:
        getDao().flush();
        getDao().clear();
        if (log.isDebugEnabled()) {
            log.debug("Hibernate session cleared.");
        }
    }

    /**
     * Adds SyncMapping for all EntityTypes that have been exported. This is
     * going to be an identity mapping.
     * 
     * @param mapObjectTypeList
     */
    private void createMapping(final List<MapObjectType> mapObjectTypeList) {
        for (final EntityType entityType : exportedEntityTypes) {
            if (entityType != null) {
                // Add <mapObjectType> element for this entity type to
                // <syncMapping>:
                final MapObjectType mapObjectType = new MapObjectType();

                mapObjectType.setIntId(entityType.getId());
                mapObjectType.setExtId(entityType.getId());

                final List<MapAttributeType> mapAttributeTypes = mapObjectType
                        .getMapAttributeType();
                for (final PropertyType propertyType : entityType.getAllPropertyTypes()) {
                    // Add <mapAttributeType> for this property type to current
                    // <mapObjectType>:
                    final MapAttributeType mapAttributeType = new MapAttributeType();

                    mapAttributeType.setExtId(propertyType.getId());
                    mapAttributeType.setIntId(propertyType.getId());

                    mapAttributeTypes.add(mapAttributeType);
                }

                mapObjectTypeList.add(mapObjectType);
            }
        }
        // For all exported objects that had no entity type (e.g. category
        // objects)
        // we create a simple 1-to-1 mapping using their type id.
        for (final String typeId : exportedTypes) {
            final MapObjectType mapObjectType = new MapObjectType();
            mapObjectType.setIntId(typeId);
            mapObjectType.setExtId(typeId);

            mapObjectTypeList.add(mapObjectType);
        }
    }

    private void configureTask(final ExportTask task) {
        task.setCommandService(getCommandService());
        task.setAttachments(attachmentsByElementId.get(task.getElement().getDbId()));
        task.setAttachmentDao(getDaoFactory().getDAO(Attachment.class));
        task.setHuiTypeFactory(getHuiTypeFactory());
        task.setSourceId(sourceId);
        task.setVeriniceArchive(isVeriniceArchive());
        task.setReImport(isReImport());
        task.setEntityTypesBlackList(getEntityTypesBlackList());
        task.setEntityClassBlackList(getEntityClassBlackList());
    }

    private void getValuesFromTask(final ExportTask exportTask) {
        attachmentSet.addAll(exportTask.getAttachmentSet());
        exportedEntityTypes.addAll(exportTask.getExportedEntityTypes());
        exportedTypes.addAll(exportTask.getExportedTypes());
        changedElements.addAll(exportTask.getChangedElementList());
        final CnATreeElement element = getElementFromTask(exportTask);
        if (element != null) {
            exportedElementIds.add(element.getDbId());
            if (FinishedRiskAnalysis.TYPE_ID.equals(element.getTypeId())) {
                riskAnalysisIdSet.add(element.getDbId());
            }
        }
    }

    private CnATreeElement getElementFromTask(final ExportTask exportTask) {
        if (exportTask == null) {
            return null;
        }
        return exportTask.getElement();
    }

    private boolean isVeriniceArchive() {
        return SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV.equals(exportFormat);
    }

    private boolean isReImport() {
        return reImport;
    }

    private Map<String, String> getEntityTypesBlackList() {
        if (entityTypesBlackList == null) {
            entityTypesBlackList = createDefaultEntityTypesBlackList();
        }
        return entityTypesBlackList;
    }

    private Map<Class, Class> getEntityClassBlackList() {
        if (entityClassBlackList == null) {
            entityClassBlackList = createDefaultEntityClassBlackList();
        }
        return entityClassBlackList;
    }

    private Map<String, String> createDefaultEntityTypesBlackList() {
        final Map<String, String> blacklist = new HashMap<>();
        if (!isExportRiskAnalysis()) {
            blacklist.put(FinishedRiskAnalysis.TYPE_ID, FinishedRiskAnalysis.TYPE_ID);
        }
        return blacklist;
    }

    private Map<Class, Class> createDefaultEntityClassBlackList() {
        final Map<Class, Class> blacklist = new HashMap<>();
        if (!isExportRiskAnalysis()) {
            blacklist.put(RisikoMassnahmenUmsetzung.class, RisikoMassnahmenUmsetzung.class);
        }
        return blacklist;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if (dao == null) {
            dao = createDao();
        }
        return dao;
    }

    private IBaseDao<CnATreeElement, Serializable> createDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    public byte[] getResult() {
        return result;
    }

    protected HUITypeFactory getHuiTypeFactory() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    public Integer getExportFormat() {
        if (exportFormat == null) {
            exportFormat = SyncParameter.EXPORT_FORMAT_DEFAULT;
        }
        return exportFormat;
    }

    public void setExportFormat(final Integer exportFormat) {
        this.exportFormat = exportFormat;
    }

    public Set<Attachment> getAttachmentSet() {
        if (attachmentSet == null) {
            attachmentSet = new HashSet<>();
        }
        return attachmentSet;
    }

}
