/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.IHuiControlFactory;
import sernet.snutils.AssertException;
import sernet.snutils.FormInputParser;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.bp.rcp.bcm.BCMUiUtils;
import sernet.verinice.bp.rcp.risk.ui.FrequencyConfigurator;
import sernet.verinice.bp.rcp.risk.ui.ImpactConfigurator;
import sernet.verinice.bp.rcp.risk.ui.RiskConfigurationUpdateResultDialog;
import sernet.verinice.bp.rcp.risk.ui.RiskMatrixConfigurator;
import sernet.verinice.bp.rcp.risk.ui.RiskUiUtils;
import sernet.verinice.bp.rcp.risk.ui.RiskValuesConfigurator;
import sernet.verinice.bp.rcp.risk.ui.StackConfigurator;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.RiskPropertyValue;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateResult;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.Preferences;
import sernet.verinice.service.bp.risk.RiskService;
import sernet.verinice.service.commands.crud.LoadElementForEditor;

/**
 * Editor for all BSI elements with attached HUI entities.
 *
 * Uses the HUI framework to edit all properties defined in the entity's xml
 * description (SNCA.xml)
 *
 * @author koderman[at]sernet[dot]de
 *
 */
public class BSIElementEditorMultiPage extends MultiPageEditorPart {

    private static final Logger LOG = Logger.getLogger(BSIElementEditorMultiPage.class);

    public static final String EDITOR_ID = "sernet.gs.ui.rcp.main.bsi.editors.bsielementeditor"; //$NON-NLS-1$
    private HitroUIComposite huiComposite;

    private boolean isModelModified = false;
    private ITask task;
    private Map<String, String> changedElementProperties = new HashMap<>();
    private @NonNull RiskConfiguration riskConfigurationState = DefaultRiskConfiguration
            .getInstance();

    private RiskService riskService;
    private RightsServiceClient rightsService;
    private Boolean isWriteAllowed = null;

    public static final String SAMT_PERSPECTIVE_ID = "sernet.verinice.samt.rcp.SamtPerspective"; //$NON-NLS-1$
    // limit display in SAMT perspective to properties tagged as "VDA-ISA"
    // (simplified view):
    private static final String SAMT_PERSPECTIVE_DEFAULT_TAGS = "VDA-ISA"; //$NON-NLS-1$

    private ImpactConfigurator impactsConfigurator;
    private FrequencyConfigurator frequenciesConfigurator;
    private RiskValuesConfigurator riskValuesConfigurator;
    private RiskMatrixConfigurator riskMatrixConfigurator;
    private int matrixPageIndex = -1;
    private int riskCategoriesPageIndex = -1;
    private int impactsPageIndex = -1;
    private int frequenciesPageIndex = -1;

    private IEntityChangedListener modelListener = new IEntityChangedListener() {

        @Override
        public void selectionChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
            saveMultiselectElementProperties(arg0);
            modelChanged();
        }

        @Override
        public void propertyChanged(PropertyChangedEvent evt) {
            saveChangedElementProperties(evt);
            modelChanged();
        }

        private void saveChangedElementProperties(PropertyChangedEvent event) {
            if (isTaskEditorContext()
                    && StringUtils.isNotEmpty(event.getProperty().getPropertyType())) {
                PropertyType propertyType = HUITypeFactory.getInstance().getPropertyType(
                        cnAElement.getEntityType().getId(), event.getProperty().getPropertyType());
                if (propertyType.isSingleSelect()) {
                    changedElementProperties.put(event.getProperty().getPropertyType(),
                            event.getProperty().getPropertyValue());
                } else if (propertyType.isDate()) {
                    saveChangedDateElementProperties(event);
                } else if (propertyType.isMultiselect() || propertyType.isReference()) {
                    // nothing to do
                } else {
                    changedElementProperties.put(event.getProperty().getPropertyType(),
                            event.getProperty().getPropertyValue());
                }
            }
        }

        private void saveChangedDateElementProperties(PropertyChangedEvent event) {
            try {
                String date = FormInputParser.dateToString(
                        new java.sql.Date(Long.parseLong(event.getProperty().getPropertyValue())));
                changedElementProperties.put(event.getProperty().getPropertyType(), date);
            } catch (NumberFormatException | AssertException e) {
                LOG.error("Exception while getting the value of a date property", e); //$NON-NLS-1$
            }
        }

        private void saveMultiselectElementProperties(IMLPropertyType arg0) {
            if (isTaskEditorContext() && StringUtils.isNotEmpty(arg0.getId())) {
                PropertyType propertyType = HUITypeFactory.getInstance()
                        .getPropertyType(cnAElement.getEntityType().getId(), arg0.getId());
                List<Property> properties = cnAElement.getEntity()
                        .getProperties(propertyType.getId()).getProperties();

                StringBuilder sb = new StringBuilder();
                for (Property property : properties) {
                    sb.append(property.getPropertyValue()).append(","); //$NON-NLS-1$
                }
                changedElementProperties.put(propertyType.getId(), sb.toString());
            }
        }

        private void modelChanged() {
            boolean wasDirty = isDirty();
            isModelModified = true;

            if (!wasDirty) {
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        }

    };
    private CnATreeElement cnAElement;
    private LinkMaker linkMaker;
    private ChangeMetadata changeMetadata;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (!(input instanceof BSIElementEditorInput)) {
            throw new PartInitException("invalid input"); //$NON-NLS-1$
        }

        Activator.inheritVeriniceContextState();
        rightsService = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);

        setSite(site);
        setInput(input);
        setPartName(input.getName());
        if (((BSIElementEditorInput) input).isReadOnly()) {
            isWriteAllowed = false;
        }
    }

    private void initContent() {
        try {
            BSIElementEditorInput editorInput = (BSIElementEditorInput) getEditorInput();
            CnATreeElement element = editorInput.getCnAElement();
            task = editorInput.getTask();

            CnATreeElement elementWithChildren = Retriever.checkRetrieveChildren(element);
            LoadElementForEditor<CnATreeElement> command = new LoadElementForEditor<>(element);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            cnAElement = command.getElement();
            cnAElement.setChildren(elementWithChildren.getChildren());

            Entity entity = cnAElement.getEntity();
            EntityType entityType = HitroUtil.getInstance().getTypeFactory()
                    .getEntityType(entity.getEntityType());

            if (isTaskEditorContext()) {
                loadChangedElementPropertiesFromTask();
                setPartName(getPartName() + Messages.BSIElementEditor_9);
            }

            // Enable dirty listener only for writable objects:
            if (getIsWriteAllowed()) {
                // add listener to mark editor as dirty on changes:
                entity.addChangeListener(this.modelListener);
            } else {
                // do not add listener, user will never be offered to save this
                // editor, modify title to show this:
                setPartName(getPartName() + Messages.BSIElementEditor_7);
            }

            String[] tags = Preferences.getEditorTags();

            boolean strict = Activator.getDefault().getPluginPreferences()
                    .getBoolean(PreferenceConstants.HUI_TAGS_STRICT);

            // samt perspective offers a simple view, only showing properties
            // tagged with "isa":
            if (isSamtPerspective()) {
                tags = new String[] { SAMT_PERSPECTIVE_DEFAULT_TAGS };
                strict = true;
            }

            Map<String, IHuiControlFactory> overrides = RiskUiUtils
                    .createHuiControlFactories(element);

            // create view of all properties, read only or read/write:
            huiComposite.createView(entity, getIsWriteAllowed(), true, tags, strict,
                    ServiceFactory.lookupValidationService().getPropertyTypesToValidate(entity,
                            cnAElement.getDbId()),
                    Activator.getDefault().getPreferenceStore()
                            .getBoolean(PreferenceConstants.USE_VALIDATION_GUI_HINTS),
                    overrides);
            InputHelperFactory.setInputHelpers(entityType, huiComposite);
            if (Arrays.stream(tags).anyMatch("BSI-200-3"::equals)) {
                RiskUiUtils.addSelectionListener(huiComposite, cnAElement);
            }
            if (Arrays.stream(tags).anyMatch("BCM"::equals)) {
                BCMUiUtils.addSelectionListener(huiComposite, cnAElement);
                BCMUiUtils.addControlHints(huiComposite, cnAElement);
            }
            huiComposite.resetInitialFocus();

            // create in place editor for links to other objects
            // but not for simplified view:
            if (linkMaker != null) {
                linkMaker.createPartControl(getIsWriteAllowed());
                linkMaker.setInputElmt(cnAElement);
            }
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.BSIElementEditor_8);
        }
    }

    @Override
    protected void pageChange(int newPageIndex) {
        if (matrixPageIndex == newPageIndex) {
            riskMatrixConfigurator.setEditorState(riskConfigurationState);
            riskMatrixConfigurator.refresh();
        }
        if (riskCategoriesPageIndex == newPageIndex) {
            riskValuesConfigurator.setEditorState(riskConfigurationState.getRisks());
            riskValuesConfigurator.refresh();
        }
        if (frequenciesPageIndex == newPageIndex) {
            frequenciesConfigurator.setEditorState(riskConfigurationState.getFrequencies());
            frequenciesConfigurator.refresh();
        }
        if (impactsPageIndex == newPageIndex) {
            impactsConfigurator.setEditorState(riskConfigurationState.getImpacts());
            impactsConfigurator.refresh();
        }

        super.pageChange(newPageIndex);
    }

    private boolean isTaskEditorContext() {
        return task != null && task.isWithAReleaseProcess();
    }

    private void loadChangedElementPropertiesFromTask() {
        Map<String, String> changedElementPropertiesForTask = getTaskService()
                .loadChangedElementProperties(task.getId());
        for (Entry<String, String> entry : changedElementPropertiesForTask.entrySet()) {
            cnAElement.setPropertyValue(entry.getKey(), entry.getValue());
        }

        this.setPartName(EditorUtil.getEditorName(cnAElement));
        this.setTitleToolTip(EditorUtil.getEditorToolTipText(cnAElement));
        LOG.info("Loaded changes for element properties from task."); //$NON-NLS-1$
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        Activator.inheritVeriniceContextState();
        if (isTaskEditorContext()) {
            updateTaskWithChangedElementProperties();
            LOG.info("Sciped save cnAElement."); //$NON-NLS-1$
        } else {
            monitor.beginTask(Messages.BSIElementEditor_1, IProgressMonitor.UNKNOWN);
            save(true);
            Optional.ofNullable(changeMetadata).ifPresent(value -> value.setElement(cnAElement));
            // Refresh other views in background
            Job job = new RefreshJob("Refresh application...",
                    EditorUtil.getRelatedObjects(cnAElement));
            job.setRule(new RefreshJobRule());
            job.schedule();

            IEditorReference[] editorReferences = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage().getEditorReferences();
            ArrayList<IEditorReference> closeOthers = new ArrayList<>();
            BSIElementEditorInput myInput = (BSIElementEditorInput) getEditorInput();

            for (IEditorReference editorReference : editorReferences) {
                IEditorInput input;
                try {
                    if (editorReference.isPinned() || editorReference.isDirty()) {
                        continue;
                    }
                    input = editorReference.getEditorInput();
                    if (input instanceof BSIElementEditorInput) {
                        BSIElementEditorInput bsiInput = (BSIElementEditorInput) input;
                        if (!bsiInput.getId().equals(myInput.getId())) {
                            closeOthers.add(editorReference);
                        }
                    }
                } catch (PartInitException e) {
                    ExceptionUtil.log(e, Messages.BSIElementEditor_2);
                }
            }

            IEditorReference[] closeArray = closeOthers
                    .toArray(new IEditorReference[closeOthers.size()]);
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .closeEditors(closeArray, true);

            monitor.done();
        }
    }

    private void save(boolean trackChange) {
        if (!getIsWriteAllowed()) {
            ExceptionUtil.log(new IllegalStateException(), Messages.BSIElementEditor_3);
            return;
        }
        try {
            if (cnAElement.isItNetwork() && riskConfiguationIsDirty()) {
                ItNetwork itNetwork = (ItNetwork) cnAElement;
                if (riskConfiguationWasReset()) {
                    itNetwork.setRiskConfiguration(null);
                } else {
                    validateAllLabelsUniqueAndNonEmpty(riskConfigurationState);
                    itNetwork.setRiskConfiguration(riskConfigurationState);
                }
                RiskConfigurationUpdateContext updateContext = new RiskConfigurationUpdateContext(
                        cnAElement.getUuid(), riskConfigurationState,
                        frequenciesConfigurator.getDeleted(), impactsConfigurator.getDeleted(),
                        riskValuesConfigurator.getDeleted());
                RiskConfigurationUpdateResult updateResult = getRiskService()
                        .updateRiskConfiguration(updateContext);
                RiskConfigurationUpdateResultDialog.openUpdateResultDialog(updateResult);
                Stream.of(frequenciesConfigurator, impactsConfigurator, riskValuesConfigurator)
                        .forEach(StackConfigurator::reset);
            }

            // save element, refresh etc:
            if (trackChange) {
                cnAElement.getEntity()
                        .trackChange(ServiceFactory.lookupAuthService().getUsername());
            }
            CnAElementHome.getInstance().updateEntity(cnAElement);
            EditorUtil.updateDependentObjects(cnAElement);
            isModelModified = false;
            this.setPartName(EditorUtil.getEditorName(cnAElement));
            this.setTitleToolTip(EditorUtil.getEditorToolTipText(cnAElement));
            setIcon();
            firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (RiskPropertyValueLabelNotUniqueException e) {
            setActivePage(e.indexOfFirstErrorPage);
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                    Messages.BSIElementEditorMultiPage_risk_configuration_invalid,
                    Messages.BSIElementEditorMultiPage_risk_property_value_labels_not_unique);

        } catch (Exception se) {
            // close editor, loosing changes:
            ExceptionUtil.log(se, Messages.BSIElementEditor_5);
        }
    }

    private void validateAllLabelsUniqueAndNonEmpty(@NonNull RiskConfiguration riskConfiguration)
            throws RiskPropertyValueLabelNotUniqueException {
        if (!allLabelsUniqueAndNonEmpty(riskConfiguration.getFrequencies())) {
            throw new RiskPropertyValueLabelNotUniqueException(frequenciesPageIndex);
        }
        if (!allLabelsUniqueAndNonEmpty(riskConfiguration.getImpacts())) {
            throw new RiskPropertyValueLabelNotUniqueException(impactsPageIndex);
        }
        if (!allLabelsUniqueAndNonEmpty(riskConfiguration.getRisks())) {
            throw new RiskPropertyValueLabelNotUniqueException(riskCategoriesPageIndex);
        }

    }

    private static boolean allLabelsUniqueAndNonEmpty(
            List<? extends RiskPropertyValue> riskPropertyValues) {
        int numberOfUniqueNonEmptyLabels = (int) riskPropertyValues.stream()
                .map(RiskPropertyValue::getLabel).filter(s -> !s.isEmpty()).distinct().count();
        return numberOfUniqueNonEmptyLabels == riskPropertyValues.size();
    }

    private RiskService getRiskService() {
        if (riskService == null) {
            riskService = (RiskService) VeriniceContext.get(VeriniceContext.ITBP_RISK_SERVICE);
        }
        return riskService;
    }

    private void updateTaskWithChangedElementProperties() {
        if (!changedElementProperties.isEmpty()) {
            getTaskService().updateChangedElementProperties(task.getId(), changedElementProperties);
            changedElementProperties.clear();
            LOG.info("Updated task: saved changes in element properties."); //$NON-NLS-1$
        }
        isModelModified = false;
        this.setPartName(EditorUtil.getEditorName(cnAElement) + Messages.BSIElementEditor_9);
        this.setTitleToolTip(EditorUtil.getEditorToolTipText(cnAElement));
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    protected ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }

    @Override
    public void doSaveAs() {
        // not supported
    }

    private void setIcon() {
        Image icon = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
        if (cnAElement != null) {
            icon = CnAImageProvider.getImage(cnAElement, true);
        }
        setTitleImage(icon);
    }

    @Override
    public boolean isDirty() {
        if (isModelModified) {
            return true;
        }
        if (!cnAElement.isItNetwork()) {
            return false;
        }
        return riskConfiguationIsDirty();
    }

    private boolean riskConfiguationIsDirty() {
        RiskConfiguration storedRiskConfiguration = ((ItNetwork) cnAElement).getRiskConfiguration();
        return !keptDefaultRiskConfiguation() && (riskConfiguationWasReset()
                || !riskConfigurationState.deepEquals(storedRiskConfiguration));
    }

    private boolean keptDefaultRiskConfiguation() {
        RiskConfiguration storedRiskConfiguration = ((ItNetwork) cnAElement).getRiskConfiguration();
        return storedRiskConfiguration == null
                && riskConfigurationState == DefaultRiskConfiguration.getInstance();
    }

    private boolean riskConfiguationWasReset() {
        RiskConfiguration storedRiskConfiguration = ((ItNetwork) cnAElement).getRiskConfiguration();
        return storedRiskConfiguration != null
                && riskConfigurationState == DefaultRiskConfiguration.getInstance();
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setIsWriteAllowed(Boolean isWriteAllowed) {
        this.isWriteAllowed = isWriteAllowed;
    }

    public Boolean getIsWriteAllowed() {
        if (isWriteAllowed == null) {
            isWriteAllowed = createIsWriteAllowed();
        }
        return isWriteAllowed;
    }

    public Boolean createIsWriteAllowed() {
        isWriteAllowed = CnAElementHome.getInstance().isWriteAllowed(cnAElement);
        return isWriteAllowed;
    }

    private boolean isSamtPerspective() {
        IPerspectiveDescriptor perspective = getSite().getWorkbenchWindow().getActivePage()
                .getPerspective();
        // do not show linkmaker in SAMT perspective:
        return perspective.getId().equals(SAMT_PERSPECTIVE_ID);
    }

    public boolean isNotAskAndSave() {
        return true;
    }

    @Override
    public void setFocus() {
        if (getActivePage() == 0) {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage();
            IEditorPart active = page.getActiveEditor();
            if (active == this) {
                huiComposite.resetInitialFocus();
            }
        }
    }

    @Override
    public void dispose() {
        if (linkMaker != null) {
            linkMaker.dispose();
        }
        Optional.ofNullable(changeMetadata).ifPresent(Widget::dispose);
        huiComposite.closeView();
        cnAElement.getEntity().removeListener(modelListener);
        EditorRegistry.getInstance()
                .closeEditor(((BSIElementEditorInput) getEditorInput()).getId());

        super.dispose();
    }

    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     *
     */
    private final class RefreshJobRule implements ISchedulingRule {
        @Override
        public boolean contains(ISchedulingRule rule) {
            return rule.getClass() == RefreshJobRule.class;
        }

        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return rule.getClass() == RefreshJobRule.class;
        }
    }

    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     *
     */
    private final class RefreshJob extends Job {
        private List<CnATreeElement> objects;

        /**
         * @param name
         */
        private RefreshJob(String name) {
            super(name);
        }

        public RefreshJob(String name, List<CnATreeElement> dependentObjects) {
            super(name);
            objects = dependentObjects;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            monitor.setTaskName("Refresh application...");
            Activator.inheritVeriniceContextState();
            refresh();
            return Status.OK_STATUS;
        }

        private void refresh() {
            // notify all views of change:
            CnAElementFactory.getModel(cnAElement).childChanged(cnAElement);
            if (objects != null) {
                for (CnATreeElement cnATreeElement : objects) {
                    try {
                        CnAElementHome.getInstance().refresh(cnATreeElement);
                        CnAElementFactory.getModel(cnATreeElement).childChanged(cnATreeElement);
                    } catch (CommandException e) {
                        LOG.error("Error synchronizing dependent model elements."); //$NON-NLS-1$
                    }

                }
            }
        }
    }

    @Override
    protected void createPages() {
        createBsiPage();
        createLinkMakerPage();

        if (cnAElement.isItNetwork()
                && rightsService.isEnabled(ActionRightIDs.EDITRISKCONFIGURATION)) {
            ItNetwork itn = (ItNetwork) cnAElement;

            riskConfigurationState = itn.getRiskConfigurationOrDefault();

            ScrolledComposite scrolledComposite;
            scrolledComposite = createScrollableComposite(getContainer());
            riskMatrixConfigurator = new RiskMatrixConfigurator(scrolledComposite,
                    riskConfigurationState, this::riskConfigurationChanged,
                    this::restoreRiskConfiguration);
            scrolledComposite.setContent(riskMatrixConfigurator);
            matrixPageIndex = addNewPage(scrolledComposite,
                    Messages.BSIElementEditorMultiPage_page_name_risk_matrix);

            scrolledComposite = createScrollableComposite(getContainer());
            riskValuesConfigurator = new RiskValuesConfigurator(scrolledComposite,
                    riskConfigurationState.getRisks(), this::riskConfigurationChanged);
            scrolledComposite.setContent(riskValuesConfigurator);
            riskCategoriesPageIndex = addNewPage(scrolledComposite,
                    Messages.BSIElementEditorMultiPage_page_name_risk_values);

            scrolledComposite = createScrollableComposite(getContainer());
            impactsConfigurator = new ImpactConfigurator(scrolledComposite,
                    riskConfigurationState.getImpacts(), this::riskConfigurationChanged);
            scrolledComposite.setContent(impactsConfigurator);
            impactsPageIndex = addNewPage(scrolledComposite,
                    Messages.BSIElementEditorMultiPage_page_name_risk_impact);

            scrolledComposite = createScrollableComposite(getContainer());
            frequenciesConfigurator = new FrequencyConfigurator(scrolledComposite,
                    riskConfigurationState.getFrequencies(), this::riskConfigurationChanged);
            scrolledComposite.setContent(frequenciesConfigurator);
            frequenciesPageIndex = addNewPage(scrolledComposite,
                    Messages.BSIElementEditorMultiPage_page_name_risk_frequency);
        }
        if (!CnAElementHome.getInstance().isCatalogElement(cnAElement)) {
            createChangeMetadataPage();
        }

        // if opened the first time, save initialized entity:
        if (isDirty()) {
            save(false);
        }

    }

    private void riskConfigurationChanged() {
        riskConfigurationState = riskMatrixConfigurator.getEditorState().withValues(
                frequenciesConfigurator.getEditorState(), impactsConfigurator.getEditorState(),
                riskValuesConfigurator.getEditorState());
        firePropertyChange(PROP_DIRTY);
    }

    private void restoreRiskConfiguration() {
        riskConfigurationState = DefaultRiskConfiguration.getInstance();
        riskMatrixConfigurator.setEditorState(riskConfigurationState);
        riskValuesConfigurator.setEditorState(riskConfigurationState.getRisks());
        frequenciesConfigurator.setEditorState(riskConfigurationState.getFrequencies());
        impactsConfigurator.setEditorState(riskConfigurationState.getImpacts());
        // The configuration can only be reset in RiskMatrixConfigurator,
        // therefore we know this is the only composite we need no refresh here.
        // The others get refreshed on pageChange.
        riskMatrixConfigurator.refresh();
        firePropertyChange(PROP_DIRTY);
    }

    private int addNewPage(Composite page, String title) {
        int index = addPage(page);
        setPageText(index, title);
        return index;
    }

    private void createLinkMakerPage() {
        linkMaker = new LinkMaker(getContainer(), this);
        linkMaker.createPartControl(getIsWriteAllowed());
        linkMaker.setInputElmt(cnAElement);
        addNewPage(linkMaker, Messages.BSIElementEditorMultiPage_page_name_links);
    }

    private void createBsiPage() {
        huiComposite = new HitroUIComposite(getContainer(), false);
        initContent();
        setIcon();
        addNewPage(huiComposite, Messages.BSIElementEditorMultiPage_page_name_data);
    }

    private void createChangeMetadataPage() {
        changeMetadata = new ChangeMetadata(getContainer());
        changeMetadata.setElement(cnAElement);
        addNewPage(changeMetadata, Messages.BSIElementEditorMultiPage_page_name_change_metadata);
    }

    private static ScrolledComposite createScrollableComposite(Composite parent) {
        ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        return scrolledComposite;
    }

    private static class RiskPropertyValueLabelNotUniqueException extends Exception {

        private static final long serialVersionUID = 4147192368139573141L;

        private final int indexOfFirstErrorPage;

        private RiskPropertyValueLabelNotUniqueException(int indexOfFirstErrorPage) {
            this.indexOfFirstErrorPage = indexOfFirstErrorPage;
        }

    }
}
