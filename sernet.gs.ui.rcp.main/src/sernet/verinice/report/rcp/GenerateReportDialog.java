package sernet.verinice.report.rcp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.StringUtil;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ServiceComponent;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.reports.IReportSupplier;
import sernet.gs.ui.rcp.main.reports.ReportSupplierImpl;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ICommandCacheClient;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.interfaces.validation.IValidationService;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.model.report.ReportTemplateMetaData.ReportContext;
import sernet.verinice.service.commands.crud.LoadCnAElementByType;
import sernet.verinice.service.commands.crud.LoadCnATreeElementTitles;
import org.eclipse.swt.widgets.Text;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;

public class GenerateReportDialog extends TitleAreaDialog {
    private DataBindingContext bindingContext = new DataBindingContext();

    private static final Logger LOG = Logger.getLogger(GenerateReportDialog.class);

    private static final NumericStringComparator comparator = new NumericStringComparator();

    private static final int DEFAULT_COL_NR = 3;
    static final int DATA_SCOPE_MINIMUM_WIDTH = 200;
    static final int DATA_SCOPE_COMBO_MINIMUM_WIDTH = 500;
    private static final int MARGIN_WIDTH = 10;

    private CnATreeElement selectedScope;

    private ComboViewer comboReportType;

    private ComboViewer comboOutputFormat;

    private File outputFile;

    private ReportTemplateMetaData[] reportTemplates;

    private IOutputFormat chosenOutputFormat;

    private ReportTemplateMetaData chosenReportMetaData;

    private Integer rootElement;

    private Integer[] rootElements;

    private Combo scopeCombo;

    private List<CnATreeElement> scopes = new ArrayList<>();

    private Integer auditId = null;

    private String auditName = null;

    private List<CnATreeElement> preSelectedElments;

    private String useCase;

    private boolean isContextMenuCall;

    private boolean useDate = true;

    private String defaultFolder;

    private IReportType chosenReportType;

    // estimated size of dialog for placement (doesn't have to be exact):
    private static final int SIZE_X = 750;
    private static final int SIZE_Y = 550;

    private IReportSupplier supplier;

    private WritableValue<String> classificationHint = new WritableValue<String>();

    public GenerateReportDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.MAX);
        this.auditId = null;
        this.auditName = null;

    }

    /**
     * @param shell
     * @param reportScope
     */
    public GenerateReportDialog(Shell shell, Object reportScope) {
        this(shell);
        if (reportScope instanceof Organization || reportScope instanceof ITVerbund) {
            this.useCase = IReportType.USE_CASE_ID_GENERAL_REPORT;
        } else {
            this.useCase = IReportType.USE_CASE_ID_ALWAYS_REPORT;
        }

        CnATreeElement cnaElmt = (CnATreeElement) reportScope;
        selectedScope = cnaElmt;
        this.auditId = cnaElmt.getDbId();
        this.auditName = cnaElmt.getTitle();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting audit in report dialog: " + auditId); //$NON-NLS-1$
        }
    }

    public GenerateReportDialog(Shell shell, List<?> objects, String useCase) {
        this(shell);
        List<CnATreeElement> elmts = new ArrayList<>();
        for (Object object : objects) {
            CnATreeElement cnaElmt = (CnATreeElement) object;
            elmts.add(cnaElmt);
        }
        this.preSelectedElments = elmts;
        this.useCase = useCase;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.GenerateReportDialog_4);

        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(
                new Point(cursorLocation.x - SIZE_X / 2, cursorLocation.y - SIZE_Y / 2));
    }

    /*
     * @see
     * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        initDefaultFolder();
        IReportType[] reportTypes = ServiceComponent.getDefault().getReportService()
                .getReportTypes();
        try {
            // adding the server templates
            List<ReportTemplateMetaData> list = getSupplier()
                    .getReportTemplates(Locale.getDefault());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Locale used on system (client):\t" + Locale.getDefault().getLanguage()); //$NON-NLS-1$
                LOG.debug(list.size() + " Reporttemplates loaded from deposit folders"); //$NON-NLS-1$
            }

            reportTemplates = list.stream()
                    .sorted((template1, template2) -> comparator.compare(
                            template1.getDecoratedOutputname(), template2.getDecoratedOutputname()))
                    .toArray(ReportTemplateMetaData[]::new);
        } catch (Exception e) {
            String msg = "Error reading reports from deposit"; //$NON-NLS-1$
            ExceptionUtil.log(e, msg);
        }
        if (useCase != null) {
            filterReportTypes();
        }
        setTitle(Messages.GenerateReportDialog_0);
        StringBuilder dialogMessage = new StringBuilder();
        dialogMessage.append(Messages.GenerateReportDialog_7);
        dialogMessage.append(" "); //$NON-NLS-1$
        dialogMessage.append(Messages.GenerateReportDialog_36);
        setMessage(dialogMessage.toString());

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = MARGIN_WIDTH;
        layout.marginHeight = MARGIN_WIDTH;
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Report template group
        Group reportGroup = createGroup(composite);

        Label labelReportType = new Label(reportGroup, SWT.NONE);
        labelReportType.setText(Messages.GenerateReportDialog_1);
        GridData gdLabelReportType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdLabelReportType.widthHint = 190;
        labelReportType.setLayoutData(gdLabelReportType);

        comboReportType = new ComboViewer(reportGroup, SWT.READ_ONLY);
        comboReportType.getCombo()
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboReportType.setContentProvider(ArrayContentProvider.getInstance());
        comboReportType.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ReportTemplateMetaData) element).getDecoratedOutputname();
            }
        });
        comboReportType.setInput(reportTemplates);
        comboReportType.addSelectionChangedListener(e -> {
            if (reportTemplates.length > 0) {
                chosenReportMetaData = (ReportTemplateMetaData) e.getStructuredSelection()
                        .getFirstElement();
                chosenReportType = reportTypes[0];
            }
            setupComboOutputFormatContent();
            if (!isContextMenuCall() && !e.getSelection().isEmpty()) {
                setupComboScopes(
                        ((ReportTemplateMetaData) e.getStructuredSelection().getFirstElement())
                                .getContext());
            }
        });

        Label labelScope = new Label(reportGroup, SWT.NULL);
        GridData gdLabelScope = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdLabelScope.widthHint = 190;
        labelScope.setLayoutData(gdLabelScope);
        labelScope.setText(Messages.GenerateReportDialog_8);

        scopeCombo = new Combo(reportGroup, SWT.READ_ONLY);
        scopeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        scopeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectScope();
            }
        });

        Label labelOutputFormat = new Label(reportGroup, SWT.NONE);
        GridData gdLabelOutputFormat = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdLabelOutputFormat.widthHint = 190;
        labelOutputFormat.setLayoutData(gdLabelOutputFormat);
        labelOutputFormat.setText(Messages.GenerateReportDialog_9);

        comboOutputFormat = new ComboViewer(reportGroup, SWT.READ_ONLY);
        comboOutputFormat.setContentProvider(ArrayContentProvider.getInstance());
        comboOutputFormat.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((IOutputFormat) element).getLabel();
            }
        });
        comboOutputFormat.getCombo()
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboOutputFormat.addSelectionChangedListener(e -> {
            if (chosenReportMetaData != null) {
                chosenOutputFormat = (IOutputFormat) e.getStructuredSelection().getFirstElement();
            }
        });

        if (!isContextMenuCall()) {
            Button reset = new Button(reportGroup, SWT.RIGHT);
            reset.setText(Messages.GenerateReportDialog_40);
            reset.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    rootElement = null;
                    rootElements = null;
                    setupComboScopes(ReportContext.UNSPECIFIED);
                    comboReportType.resetFilters();
                    comboReportType.setSelection(StructuredSelection.EMPTY);
                    comboOutputFormat.setSelection(StructuredSelection.EMPTY);
                }
            });
        }
        
        Label labelClassification = new Label(reportGroup, SWT.NONE);
        labelClassification.setText(Messages.GenerateReportDialog_lblclassification);
        
        ComboViewer comboClassification = new ComboViewer(reportGroup, SWT.NONE);
        comboClassification.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboClassification.setContentProvider(ArrayContentProvider.getInstance());
        comboClassification.setInput(ServiceComponent.getDefault().getReportService().getClassificationHints());
        bindingContext.bindValue(WidgetProperties.text().observe(comboClassification.getCombo()), classificationHint, null, null);

        Label reportGroupLabel = new Label(reportGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
        reportGroupLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

        Label useDateLabel = new Label(reportGroup, SWT.NONE);
        useDateLabel.setText(Messages.GenerateReportDialog_33);

        Button useDateCheckbox = new Button(reportGroup, SWT.CHECK);
        useDateCheckbox.setSelection(true);
        GridData useDateCheckboxGridData = new GridData();
        useDateCheckboxGridData.horizontalSpan = 2;
        useDateCheckboxGridData.grabExcessHorizontalSpace = true;
        useDateCheckboxGridData.horizontalAlignment = SWT.LEFT;
        useDateCheckbox.setLayoutData(useDateCheckboxGridData);
        useDateCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useDate = ((Button) e.getSource()).getSelection();
            }
        });

        Group groupCache = new Group(composite, SWT.NULL);
        groupCache.setLayoutData(
                new GridData(GridData.FILL, SWT.TOP, true, false, DEFAULT_COL_NR, 1));
        layout = new GridLayout();
        groupCache.setLayout(layout);

        createCacheResetButton(groupCache);

        if (reportTemplates.length == 0) {
            showNoReportsExistant();
        }
        setupComboOutputFormatContent();
        setupComboScopes();

        composite.pack();
        return composite;
    }

    private Group createGroup(final Composite composite) {
        Group reportGroup = new Group(composite, SWT.NULL);
        reportGroup.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false, DEFAULT_COL_NR, 1));
        GridLayout layout = new GridLayout();
        layout.numColumns = DEFAULT_COL_NR;
        reportGroup.setLayout(layout);
        return reportGroup;
    }

    private String selectOutputFile() {
        FileDialog dlg = new FileDialog(getParentShell(), SWT.SAVE);
        ArrayList<String> extensionList = new ArrayList<>();
        if (chosenOutputFormat != null && chosenOutputFormat.getFileSuffix() != null) {
            extensionList.add("*." + chosenOutputFormat.getFileSuffix()); //$NON-NLS-1$
        }

        extensionList.add("*.*"); //$NON-NLS-1$
        dlg.setFilterExtensions(extensionList.toArray(new String[extensionList.size()]));
        dlg.setFileName(getDefaultOutputFilename());
        dlg.setOverwrite(true);
        String path = defaultFolder;

        dlg.setFilterPath(path);
        return dlg.open();

    }

    private void setupComboScopes() {
        setupComboScopes(ReportContext.UNSPECIFIED);
    }

    /**
     * Load list of scopes for user selection of top level element for report.
     */
    private void setupComboScopes(ReportContext context) {
        // check if audit was selected by context menu:
        if (this.auditId != null && isContextMenuCall()) {
            scopeCombo.removeAll();
            scopeCombo.add(this.auditName);
            rootElement = auditId;
            scopeCombo.setEnabled(false);
            scopeCombo.select(0);
            onlyShowSuitableReports(selectedScope);
            scopeCombo.redraw();
            return;
        } else if (this.preSelectedElments != null && !this.preSelectedElments.isEmpty()
                && isContextMenuCall()) {
            scopeCombo.removeAll();
            ArrayList<Integer> auditIDList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (CnATreeElement elmt : preSelectedElments) {
                sb.append(elmt.getTitle());
                if (preSelectedElments.indexOf(elmt) != preSelectedElments.size() - 1) {
                    sb.append(" & "); //$NON-NLS-1$
                }
                auditIDList.add(elmt.getDbId());
            }
            scopeCombo.add(sb.toString());
            rootElements = auditIDList.toArray(new Integer[auditIDList.size()]);
            scopeCombo.setEnabled(false);
            scopeCombo.select(0);
            scopeCombo.redraw();
            onlyShowSuitableReports(preSelectedElments.get(0));
            return;

        }

        loadScopes(context);
        List<String> scopeTitles = new ArrayList<>();

        Collections.sort(scopes, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));

        for (CnATreeElement elmt : scopes) {
            scopeTitles.add(elmt.getTitle());
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        Messages.GenerateReportDialog_16 + elmt.getDbId() + ": " + elmt.getTitle()); // $NON-NLS-2$ //$NON-NLS-1$
            }
        }
        boolean reportSupportsMultipleRootObjects = chosenReportMetaData != null
                && chosenReportMetaData.isMultipleRootObjects();
        if (reportSupportsMultipleRootObjects) {
            scopeTitles.add(0, Messages.GenerateReportDialog_37);
        }

        String[] titles = scopeTitles.toArray(new String[scopeTitles.size()]);
        scopeCombo.setItems(titles);
        if (rootElement != null) {
            boolean elementFound = false;
            for (int i = 0; i < scopes.size(); i++) {
                if (scopes.get(i).getDbId().equals(rootElement)) {
                    scopeCombo.select(reportSupportsMultipleRootObjects ? i + 1 : i);
                    elementFound = true;
                    break;
                }
            }
            if (!elementFound) {
                scopeCombo.clearSelection();
            }
        } else if (rootElements != null) {
            if (reportSupportsMultipleRootObjects) {
                scopeCombo.select(0);
            } else {
                scopeCombo.clearSelection();
            }
        }
    }

    private void setupComboOutputFormatContent() {
        if (reportTemplates.length > 0) {
            if (chosenReportMetaData != null) {
                List<IOutputFormat> supportedOutputFormats = Stream
                        .of(chosenReportMetaData.getOutputFormats())
                        .map(getDepositService()::getOutputFormat).collect(Collectors.toList());
                comboOutputFormat.setInput(supportedOutputFormats);
                comboOutputFormat
                        .setSelection(new StructuredSelection(supportedOutputFormats.get(0)));
            }
        } else {
            showNoReportsExistant();
        }
    }

    private String getDefaultOutputFilename() {
        String outputFileName = chosenReportMetaData.getOutputname();
        if (outputFileName == null || outputFileName.isEmpty()) {
            outputFileName = "unknown"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(outputFileName);
        String scopeName = StringUtil.convertToFileName(scopeCombo.getText());
        if (scopeName != null && !scopeName.isEmpty()) {
            sb.append("_").append(scopeName); //$NON-NLS-1$
        }
        if (useDate) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()); //$NON-NLS-1$
            sb.append("_").append(date); //$NON-NLS-1$
        }
        if (chosenOutputFormat != null) {
            sb.append(".").append(chosenOutputFormat.getFileSuffix()); //$NON-NLS-1$
        } else {
            sb.append(".pdf"); //$NON-NLS-1$
        }
        return StringUtil.convertToFileName(sb.toString());
    }

    @Override
    protected void okPressed() {
        try {
            if (scopeCombo.getSelectionIndex() < 0) {
                MessageDialog.openWarning(getShell(), Messages.GenerateReportDialog_5,
                        Messages.GenerateReportDialog_6);
                return;
            }
            List<Integer> scopeIds = collectScopeIds();
            IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            boolean dontShowValidationWarning = preferenceStore
                    .getBoolean(PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING);
            boolean validationsExistant = validateScopes(scopeIds);

            if (!dontShowValidationWarning && validationsExistant) {
                MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
                        getParentShell(), Messages.GenerateReportDialog_5,
                        Messages.GenerateReportDialog_21, Messages.GenerateReportDialog_23,
                        dontShowValidationWarning, preferenceStore,
                        PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING);
                preferenceStore.setValue(PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING,
                        dialog.getToggleState());

                if (!(dialog.getReturnCode() == IDialogConstants.OK_ID
                        || dialog.getReturnCode() == IDialogConstants.YES_ID)) {
                    return;
                }
            }

            String f = selectOutputFile();
            if (f != null) {
                outputFile = new File(f);
                super.okPressed();
            }
        } catch (Exception e) {
            LOG.error("Error while creating report.", e); //$NON-NLS-1$
            MessageDialog.openError(getShell(), "Error", //$NON-NLS-1$
                    "An error occurred while creating report."); //$NON-NLS-1$
        }
    }

    private List<Integer> collectScopeIds() {
        List<Integer> scopeIds = new ArrayList<>();
        if (getRootElement() != null) {
            scopeIds.add(getRootElement());
        }
        if (getRootElements() != null) {
            for (Integer scopeId : getRootElements()) {
                if (scopeId != null) {
                    scopeIds.add(scopeId);
                }
            }
        }
        return scopeIds;
    }

    private boolean validateScopes(List<Integer> scopeIds) {
        IValidationService vService = ServiceFactory.lookupValidationService();
        for (Integer scopeId : scopeIds) {
            if (!vService.getValidations(scopeId, (Integer) null).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public String getClassificationHint() {
        return classificationHint.doGetValue();
    }

    public File getOutputFile() {
        return outputFile;
    }

    public IOutputFormat getOutputFormat() {
        return chosenOutputFormat;
    }

    @Deprecated
    public IReportType getReportType() {
        return chosenReportType;
    }

    public ReportTemplateMetaData getReportMetaData() {
        return chosenReportMetaData;
    }

    /**
     * Get root element id for which the report should be created.
     */
    public Integer getRootElement() {
        return rootElement;
    }

    /**
     * Get ids of root elements, if there are more than one
     */
    public Integer[] getRootElements() {
        return (rootElements != null) ? rootElements.clone() : null;
    }

    private List<Organization> loadOrganizations() {
        LoadCnATreeElementTitles<Organization> compoundLoader = new LoadCnATreeElementTitles<>(
                Organization.class);
        try {
            compoundLoader = ServiceFactory.lookupCommandService().executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.GenerateReportDialog_19);
        }

        return compoundLoader.getElements();

    }

    private List<ItNetwork> loadItNetworks() {
        LoadCnAElementByType<ItNetwork> compoundLoader = new LoadCnAElementByType<>(
                ItNetwork.class);
        try {
            compoundLoader = ServiceFactory.lookupCommandService().executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.GenerateReportDialog_38);
        }
        return compoundLoader.getElements().stream()
                .filter(x -> !CatalogModel.TYPE_ID.equals(x.getParent().getTypeId()))
                .collect(Collectors.toList());
    }

    private List<ITVerbund> loadITVerbuende() {
        LoadCnATreeElementTitles<ITVerbund> compoundLoader = new LoadCnATreeElementTitles<>(
                ITVerbund.class);
        try {
            compoundLoader = ServiceFactory.lookupCommandService().executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.GenerateReportDialog_20);
        }
        return compoundLoader.getElements();
    }

    private void filterReportTypes() {
        ArrayList<ReportTemplateMetaData> list = new ArrayList<>();
        if (useCase != null && !useCase.equals("") && reportTemplates.length > 0) { //$NON-NLS-1$
            for (ReportTemplateMetaData data : reportTemplates) {
                /*
                 * TODO: add use case to template properties for filtering
                 */
                list.add(data);
            }
        }
        reportTemplates = list.toArray(new ReportTemplateMetaData[list.size()]);
    }

    private boolean isContextMenuCall() {
        return isContextMenuCall;
    }

    public void setContextMenuCall(boolean isContextMenuCall) {
        this.isContextMenuCall = isContextMenuCall;
    }

    private String initDefaultFolder() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        defaultFolder = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_REPORT);
        if (defaultFolder == null || defaultFolder.isEmpty()) {
            defaultFolder = System.getProperty(IVeriniceConstants.USER_HOME);
        }
        if (!defaultFolder.endsWith(System.getProperty(IVeriniceConstants.FILE_SEPARATOR))) {
            defaultFolder = defaultFolder + System.getProperty(IVeriniceConstants.FILE_SEPARATOR);
        }
        return defaultFolder;
    }

    private void createCacheResetButton(Control parent) {
        Button button = new Button((Composite) parent, SWT.PUSH);
        button.setText(Messages.GenerateReportDialog_27); // $NON-NLS-1$
        button.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, true));
        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (MessageDialog.openConfirm(getShell(), Messages.GenerateReportDialog_28,
                        Messages.GenerateReportDialog_29)) {
                    ICommandCacheClient commandCacheClient = (ICommandCacheClient) VeriniceContext
                            .get(VeriniceContext.COMMAND_CACHE_SERVICE);
                    commandCacheClient.resetCache();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });
    }

    private IReportSupplier getSupplier() {
        if (supplier == null) {
            supplier = new ReportSupplierImpl();
        }
        return supplier;
    }

    private IReportDepositService getDepositService() {
        return ServiceFactory.lookupReportDepositService();
    }

    private void showNoReportsExistant() {
        MessageDialog.openWarning(Display.getDefault().getActiveShell(),
                Messages.GenerateReportDialog_28, Messages.ReportDepositView_24);
    }

    /**
     * Select the scope aka root element and only shows reports for the same
     * context.
     */
    private void selectScope() {
        int s = scopeCombo.getSelectionIndex();
        if (chosenReportMetaData != null && chosenReportMetaData.isMultipleRootObjects()) {
            if (s == 0) {
                Integer[] roots = new Integer[scopes.size()];
                for (int i = 0; i < scopes.size(); i++) {
                    roots[i] = scopes.get(i).getDbId();
                }
                rootElements = roots;
                rootElement = null;
            } else {
                rootElement = scopes.get(s - 1).getDbId();
            }
        } else {
            rootElement = scopes.get(s).getDbId();
        }
        onlyShowSuitableReports(getSelectedScopeElement());
    }

    private CnATreeElement getSelectedScopeElement() {
        return scopes.stream().filter(x -> x.getTitle().equals(scopeCombo.getText())).findFirst()
                .orElse(null);
    }

    private void onlyShowSuitableReports(CnATreeElement element) {
        if (element == null) {
            return;
        }
        List<ReportContext> validDomains = ReportContext.getValidContexts(element);

        comboReportType.setFilters(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                ReportContext context = ((ReportTemplateMetaData) element).getContext();
                return validDomains.contains(context);
            }
        });
    }

    /**
     * Loads scopes filtered by {@code context} into {@code scopes}.
     * 
     * Loads all scopes for Optional.empty.
     */
    private void loadScopes(ReportContext context) {
        scopes.clear();
        switch (context) {
        case ISM_DS:
        case ISM_ISA:
        case ISM_ISO:
            scopes.addAll(loadOrganizations());
            break;

        case ITGS:
        case ITGS_DS:
            scopes.addAll(loadItNetworks());
            break;

        case ITGS_ALT:
            scopes.addAll(loadITVerbuende());
            break;

        case UNSPECIFIED:
        default:
            scopes.addAll(loadOrganizations());
            scopes.addAll(loadItNetworks());
            scopes.addAll(loadITVerbuende());
        }

    }
}