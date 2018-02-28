package sernet.verinice.report.rcp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.NumericStringComparator;
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
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.service.commands.crud.LoadCnATreeElementTitles;

public class GenerateReportDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(GenerateReportDialog.class);

    // manual filename mode or auto filename mode
    private static final boolean FILENAME_MANUAL = true;

    private static final NumericStringComparator comparator = new NumericStringComparator();

    private Combo comboReportType;

    private Combo comboOutputFormat;

    private Text textFile;

    private File outputFile;

    private ReportTemplateMetaData[] reportTemplates;

    private IReportType[] reportTypes;

    private IOutputFormat chosenOutputFormat;

    private ReportTemplateMetaData chosenReportMetaData;

    private Integer rootElement;

    private Integer[] rootElements;

    private Button openFileButton;

    private Combo scopeCombo;

    private List<CnATreeElement> scopes;

    private Integer auditId = null;

    private String auditName = null;

    private List<CnATreeElement> preSelectedElments;

    private String useCase;

    private boolean isContextMenuCall;

    private boolean useDefaultFolder = true;

    private boolean useDate = true;

    private String defaultFolder;

    private String defaultTemplateFolder;

    private IReportType chosenReportType;

    // estimated size of dialog for placement (doesnt have to be exact):
    private static final int SIZE_X = 750;
    private static final int SIZE_Y = 550;

    private IReportSupplier supplier;

    static final int DEFAULT_COL_NR = 3;

    static final int DATA_SCOPE_MINIMUM_WIDTH = 200;
    static final int DATA_SCOPE_COMBO_MINIMUM_WIDTH = 500;
    static final int MARGIN_WIDTH = 10;

    public GenerateReportDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.auditId = null;
        this.auditName = null;
        reportTypes = ServiceComponent.getDefault().getReportService().getReportTypes();
        try {
            // adding the server templates
            List<ReportTemplateMetaData> list = getSupplier()
                    .getReportTemplates(Locale.getDefault().getLanguage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Locale used on system (client):\t" + Locale.getDefault().getLanguage());
                LOG.debug(list.size() + " Reporttemplates loaded from deposit folders");
            }
            sortList(list);
            reportTemplates = list.toArray(new ReportTemplateMetaData[list.size()]);
        } catch (Exception e) {
            String msg = "Error reading reports from deposit";
            ExceptionUtil.log(e, msg);
        }
    }

    public GenerateReportDialog(Shell parentShell, String useCase) {
        this(parentShell);
        this.useCase = useCase;
    }

    /**
     * @param shell
     * @param reportScope
     */
    public GenerateReportDialog(Shell shell, Object reportScope) {
        this(shell);
        if (reportScope instanceof Audit) {
            this.useCase = IReportType.USE_CASE_ID_AUDIT_REPORT;
        } else if (reportScope instanceof Organization || reportScope instanceof ITVerbund) {
            this.useCase = IReportType.USE_CASE_ID_GENERAL_REPORT;
        } else {
            this.useCase = IReportType.USE_CASE_ID_ALWAYS_REPORT;
        }
        CnATreeElement cnaElmt = (CnATreeElement) reportScope;

        this.auditId = cnaElmt.getDbId();
        this.auditName = cnaElmt.getTitle();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting audit in report dialog: " + auditId); //$NON-NLS-1$
        }
    }

    public GenerateReportDialog(Shell shell, List<?> objects) {
        this(shell);
        List<CnATreeElement> elmts = new ArrayList<>();
        for (Object object : objects) {
            CnATreeElement cnaElmt = (CnATreeElement) object;
            elmts.add(cnaElmt);
        }
        this.preSelectedElments = elmts;
    }

    public GenerateReportDialog(Shell shell, List<?> objects, String useCase) {
        this(shell, objects);
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
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        initDefaultFolder();

        if (useCase != null) {
            filterReportTypes();
        }
        setTitle(Messages.GenerateReportDialog_0);
        StringBuilder dialogMessage = new StringBuilder();
        dialogMessage.append(Messages.GenerateReportDialog_7);
        dialogMessage.append(" ");
        dialogMessage.append(Messages.GenerateReportDialog_36);
        setMessage(dialogMessage.toString());

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = MARGIN_WIDTH;
        layout.marginHeight = MARGIN_WIDTH;
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // GridData for all labels
        GridData gridDataLabel = createLabelGridData();
        // GridData for all combo boxes
        GridData gridDataCombo = createComboGridData();
        // GridData for all text fields
        GridData gridDataText = createTextGridData();

        // Report template group
        Group reportGroup = createGroup(composite);

        Label labelReportType = new Label(reportGroup, SWT.NONE);
        labelReportType.setText(Messages.GenerateReportDialog_1);
        labelReportType.setLayoutData(gridDataLabel);

        comboReportType = new Combo(reportGroup, SWT.READ_ONLY);
        comboReportType.setLayoutData(gridDataCombo);

        reportTemplates = fillReportCombo();

        comboReportType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                if (reportTemplates.length > 0) {
                    chosenReportMetaData = reportTemplates[comboReportType.getSelectionIndex()];
                    chosenReportType = reportTypes[0];
                }

                setupComboOutputFormatContent();
                setupComboScopes();
            }
        });

        // Scope group
        Group scopeGroup = createGroup(composite);

        Label labelScope = new Label(scopeGroup, SWT.NULL);
        labelScope.setLayoutData(gridDataLabel);
        labelScope.setText(Messages.GenerateReportDialog_8);

        scopeCombo = new Combo(scopeGroup, SWT.READ_ONLY);
        scopeCombo.setLayoutData(gridDataCombo);
        scopeCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                getButton(IDialogConstants.OK_ID).setEnabled(true);
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
            }
        });

        Group groupFile = new Group(composite, SWT.NULL);
        groupFile.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, true, false, DEFAULT_COL_NR, 1));
        layout = new GridLayout();
        layout.numColumns = DEFAULT_COL_NR;
        groupFile.setLayout(layout);

        Label labelOutputFormat = new Label(groupFile, SWT.NONE);
        labelOutputFormat.setText(Messages.GenerateReportDialog_9);
        labelOutputFormat.setLayoutData(gridDataLabel);

        comboOutputFormat = new Combo(groupFile, SWT.READ_ONLY);
        comboOutputFormat.setLayoutData(gridDataCombo);
        comboOutputFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (chosenReportMetaData != null) {
                    chosenOutputFormat = getDepositService().getOutputFormat(chosenReportMetaData
                            .getOutputFormats()[comboOutputFormat.getSelectionIndex()]);
                }
                setupOutputFilepath();
            }

        });

        Label labelFile = new Label(groupFile, SWT.NONE);
        labelFile.setText(Messages.GenerateReportDialog_10);
        labelFile.setLayoutData(gridDataLabel);

        textFile = new Text(groupFile, SWT.BORDER);
        textFile.setLayoutData(gridDataText);

        textFile.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        textFile.setEditable(FILENAME_MANUAL);

        openFileButton = new Button(groupFile, SWT.PUSH);
        openFileButton.setText(Messages.GenerateReportDialog_11);
        openFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectOutputFile();
            }
        });

        Label useDateLabel = new Label(groupFile, SWT.NONE);
        useDateLabel.setText(Messages.GenerateReportDialog_33);
        useDateLabel.setLayoutData(gridDataLabel);

        Button useDateCheckbox = new Button(groupFile, SWT.CHECK);
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

        Label useDefaultFolderLabel = new Label(groupFile, SWT.NONE);
        useDefaultFolderLabel.setText(Messages.GenerateReportDialog_26);
        useDefaultFolderLabel.setLayoutData(gridDataLabel);

        Button useDefaultFolderButton = new Button(groupFile, SWT.CHECK);
        useDefaultFolderButton.setSelection(true);
        GridData useDefaultFolderButtonGridData = new GridData();
        useDefaultFolderButtonGridData.horizontalSpan = 2;
        useDefaultFolderButtonGridData.grabExcessHorizontalSpace = true;
        useDefaultFolderButtonGridData.horizontalAlignment = SWT.LEFT;
        useDefaultFolderButton.setLayoutData(useDefaultFolderButtonGridData);
        useDefaultFolderButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useDefaultFolder = ((Button) e.getSource()).getSelection();
            }
        });

        Group groupCache = new Group(composite, SWT.NULL);
        groupCache.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false, DEFAULT_COL_NR, 1));
        layout = new GridLayout();
        layout.numColumns = 2;
        groupCache.setLayout(layout);

        createCacheResetButton(groupCache);

        openFileButton.setEnabled(FILENAME_MANUAL);

        comboReportType.select(0);
        if (reportTemplates.length > 0) {
            chosenReportType = reportTypes[0];
            chosenReportMetaData = reportTemplates[comboReportType.getSelectionIndex()];
        } else {
            showNoReportsExistant();
        }
        setupComboOutputFormatContent();
        setupComboScopes();

        composite.pack();
        return composite;
    }

    /**
     * adds decorators ( "(L)" or "(S)" ) as a prefix to the report names, sorts
     * alphabetically: (locally stored templates before server-sided templates)
     * adds sorted name list to the reportCombo
     * 
     * @return sorted Array (for access later on)
     */
    private ReportTemplateMetaData[] fillReportCombo() {

        Arrays.sort(reportTemplates, new Comparator<ReportTemplateMetaData>() {
            @Override
            public int compare(ReportTemplateMetaData template1, ReportTemplateMetaData template2) {
                return comparator.compare(template1.getDecoratedOutputname(),
                        template2.getDecoratedOutputname());
            }
        });

        for (ReportTemplateMetaData data : reportTemplates) {
            comboReportType.add(data.getDecoratedOutputname());
        }

        return reportTemplates;
    }

    protected GridData createTextGridData() {
        GridData gridDataText = new GridData();
        gridDataText.horizontalAlignment = SWT.FILL;
        gridDataText.verticalAlignment = SWT.CENTER;
        gridDataText.grabExcessHorizontalSpace = true;
        return gridDataText;
    }

    protected GridData createComboGridData() {
        GridData gridDataCombo = new GridData();
        gridDataCombo.horizontalAlignment = SWT.FILL;
        gridDataCombo.grabExcessHorizontalSpace = true;
        gridDataCombo.horizontalSpan = 2;
        gridDataCombo.minimumWidth = DATA_SCOPE_COMBO_MINIMUM_WIDTH;
        return gridDataCombo;
    }

    protected GridData createLabelGridData() {
        GridData gridDataLabel = new GridData();
        gridDataLabel.horizontalAlignment = SWT.LEFT;
        gridDataLabel.verticalAlignment = SWT.CENTER;
        gridDataLabel.grabExcessHorizontalSpace = true;
        gridDataLabel.minimumWidth = DATA_SCOPE_MINIMUM_WIDTH;
        return gridDataLabel;
    }

    protected Group createGroup(final Composite composite) {
        GridLayout layout;
        Group reportGroup = new Group(composite, SWT.NULL);
        reportGroup.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false, DEFAULT_COL_NR, 1));
        layout = new GridLayout();
        layout.numColumns = DEFAULT_COL_NR;
        reportGroup.setLayout(layout);
        return reportGroup;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void selectTemplateFile() {
        FileDialog dlg = new FileDialog(getParentShell(), SWT.OPEN);
        String path;
        if (defaultTemplateFolder != null && !defaultTemplateFolder.isEmpty()) {
            path = defaultTemplateFolder;
        } else {
            path = System.getProperty(IVeriniceConstants.USER_HOME); // $NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Template file dialog path set to: " + path); //$NON-NLS-1$
        }
        dlg.setFilterPath(path);
        dlg.setFilterExtensions(new String[] { "*.rptdesign", "*.rpt", "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        dlg.open();
    }

    public void selectOutputFile() {
        FileDialog dlg = new FileDialog(getParentShell(), SWT.SAVE);
        ArrayList<String> extensionList = new ArrayList<>();
        if (chosenOutputFormat != null && chosenOutputFormat.getFileSuffix() != null) {
            extensionList.add("*." + chosenOutputFormat.getFileSuffix()); //$NON-NLS-1$
        }
        extensionList.add("*.*"); //$NON-NLS-1$
        dlg.setFilterExtensions(extensionList.toArray(new String[extensionList.size()]));
        dlg.setFileName(getDefaultOutputFilename());
        dlg.setOverwrite(true);
        String path;
        if (isFilePath()) {
            path = getOldFolderPath();
        } else {
            path = defaultFolder;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("File dialog path set to: " + path); //$NON-NLS-1$
        }
        dlg.setFilterPath(path);
        String fn = dlg.open();
        if (fn != null) {
            textFile.setText(fn);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
    }

    boolean isFilePath() {
        return textFile != null && textFile.getText() != null && !textFile.getText().isEmpty();
    }

    private String getOldFolderPath() {
        return getFolderFromPath(textFile.getText());
    }

    private String getFolderFromPath(String path) {
        String returnPath = null;
        if (path != null && path.indexOf(File.separatorChar) != -1) {
            returnPath = path.substring(0, path.lastIndexOf(File.separatorChar) + 1);
        }
        return returnPath;
    }

    /**
     * @param filenameManual2
     */
    protected void enableFileDialog(boolean filenameManual) {
        textFile.setEditable(filenameManual);
        openFileButton.setEnabled(filenameManual);
    }

    /**
     * Load list of scopes for user selection of top level element for report.
     */
    private void setupComboScopes() {
        scopes = new ArrayList<>();

        // check if audit was selected by context menu:
        if (this.auditId != null && isContextMenuCall()) {
            scopeCombo.removeAll();
            scopeCombo.add(this.auditName);
            rootElement = auditId;
            scopeCombo.setEnabled(false);
            scopeCombo.select(0);
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
                    sb.append(" & ");
                }
                auditIDList.add(elmt.getDbId());
            }
            scopeCombo.add(sb.toString());
            rootElements = auditIDList.toArray(new Integer[auditIDList.size()]);
            scopeCombo.setEnabled(false);
            scopeCombo.select(0);
            scopeCombo.redraw();
            return;

        }
        // call is initiated from applicationbar, so let user choose from all
        // accessible scopes

        scopes.addAll(loadScopes());
        scopes.addAll(loadITVerbuende());
        scopes.addAll(loadItNetworks());

        List<String> scopeTitles = new ArrayList<>();

        Collections.sort(scopes, new Comparator<CnATreeElement>() {
            @Override
            public int compare(CnATreeElement o1, CnATreeElement o2) {
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
        });

        for (CnATreeElement elmt : scopes) {
            scopeTitles.add(elmt.getTitle());
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        Messages.GenerateReportDialog_16 + elmt.getDbId() + ": " + elmt.getTitle()); // $NON-NLS-2$ //$NON-NLS-1$
            }
        }
        if (chosenReportMetaData != null && chosenReportMetaData.isMultipleRootObjects()) {
            scopeTitles.add(0, Messages.GenerateReportDialog_37);
        }

        String[] titles = scopeTitles.toArray(new String[scopeTitles.size()]);
        scopeCombo.setItems(titles);

    }

    private void setupComboOutputFormatContent() {
        comboOutputFormat.removeAll();
        if (reportTemplates.length > 0) {
            for (IOutputFormat of : getDepositService().getOutputFormats(
                    reportTemplates[comboReportType.getSelectionIndex()].getOutputFormats())) {
                comboOutputFormat.add(of.getLabel());
            }
            comboOutputFormat.select(0);
            if (chosenReportMetaData != null) {
                chosenOutputFormat = getDepositService().getOutputFormat(chosenReportMetaData
                        .getOutputFormats()[comboOutputFormat.getSelectionIndex()]);
            }
        } else {
            showNoReportsExistant();
            return;
        }
    }

    protected String setupDirPath() {
        String currentPath = textFile.getText();
        String path = currentPath;
        if (currentPath != null && !currentPath.isEmpty()) {
            int lastSlash = currentPath
                    .lastIndexOf(System.getProperty(IVeriniceConstants.FILE_SEPARATOR));
            if (lastSlash != -1) {
                path = currentPath.substring(0, lastSlash + 1);
            } else {
                path = currentPath.substring(0, lastSlash);
            }
            if (!currentPath.equals(path)) {
                textFile.setText(path);
            }
        }

        return path;
    }

    protected void setupOutputFilepath() {
        String currentPath = textFile.getText();
        if (currentPath != null && !currentPath.isEmpty() && chosenOutputFormat != null) {
            int lastDot = currentPath.lastIndexOf('.');
            String path;
            if (lastDot != -1) {
                path = currentPath.substring(0, lastDot + 1) + chosenOutputFormat.getFileSuffix();
            } else {
                path = currentPath + chosenOutputFormat.getFileSuffix();
            }
            if (!currentPath.equals(path)) {
                textFile.setText(path);
            }
        }

    }

    protected String getDefaultOutputFilename() {
        String outputFileName = chosenReportMetaData.getOutputname();
        if (outputFileName == null || outputFileName.isEmpty()) {
            outputFileName = "unknown";
        }
        StringBuilder sb = new StringBuilder(outputFileName);
        String scopeName = convertToFileName(scopeCombo.getText());
        if (scopeName != null && !scopeName.isEmpty()) {
            sb.append("_").append(scopeName);
        }
        if (useDate) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            sb.append("_").append(date);
        }
        if (chosenOutputFormat != null) {
            sb.append(".").append(chosenOutputFormat.getFileSuffix());
        } else {
            sb.append(".pdf");
        }
        return convertToFileName(sb.toString());
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean isReportTemplate() {
        // return chosenReportType != null &&
        // chosenReportType.getId().equals(IReportType.USER_REPORT_ID);
        return true;
    }

    @Override
    protected void okPressed() {
        try {
            if (textFile.getText().length() == 0 || scopeCombo.getSelectionIndex() < 0) {
                MessageDialog.openWarning(getShell(), Messages.GenerateReportDialog_5,
                        Messages.GenerateReportDialog_6);
                return;
            }
            List<Integer> scopeIds = new ArrayList<>(0);
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
            IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            boolean dontShow = preferenceStore
                    .getBoolean(PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING);
            IValidationService vService = ServiceFactory.lookupValidationService();
            boolean validationsExistant = false;
            for (Integer scopeId : scopeIds) {
                if (!vService.getValidations(scopeId, (Integer) null).isEmpty()) {
                    validationsExistant = true;
                    break;
                }
            }

            if (!dontShow && validationsExistant) {
                MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
                        getParentShell(), Messages.GenerateReportDialog_5,
                        Messages.GenerateReportDialog_21, Messages.GenerateReportDialog_23,
                        dontShow, preferenceStore,
                        PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING);
                preferenceStore.setValue(PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING,
                        dialog.getToggleState());

                if (!(dialog.getReturnCode() == IDialogConstants.OK_ID
                        || dialog.getReturnCode() == IDialogConstants.YES_ID)) {
                    return;
                }
            }

            String f = textFile.getText();
            if (reportTemplates.length > 0) {
                chosenReportMetaData = reportTemplates[comboReportType.getSelectionIndex()];
            } else {
                showNoReportsExistant();
                return;
            }
            chosenOutputFormat = getDepositService().getOutputFormat(
                    chosenReportMetaData.getOutputFormats()[comboOutputFormat.getSelectionIndex()]);

            // This just appends the chosen report's extension if the existing
            // suffix does not match. Could be enhanced.
            if (!f.endsWith(chosenOutputFormat.getFileSuffix())) {
                f += "." + chosenOutputFormat.getFileSuffix(); //$NON-NLS-1$
            }

            String currentPath = setupDirPath();
            if (useDefaultFolder) {
                Activator.getDefault().getPreferenceStore()
                        .setValue(PreferenceConstants.DEFAULT_FOLDER_REPORT, currentPath);
            }
            outputFile = new File(f);
        } catch (Exception e) {
            LOG.error("Error while creating report.", e);
            MessageDialog.openError(getShell(), "Error",
                    "An error occurred while creating report.");
            return;
        }
        super.okPressed();
    }

    @Override
    protected void cancelPressed() {
        resetFormValues();
        super.cancelPressed();
    }

    private void resetFormValues() {
        if (preSelectedElments != null) {
            preSelectedElments = null;
        }
        if (auditId != null) {
            auditId = null;
        }
        setupComboScopes();
        comboReportType.select(0);
        if (reportTemplates.length > 0) {
            chosenReportMetaData = reportTemplates[comboReportType.getSelectionIndex()];
        }
    }

    public File getOutputFile() {
        return outputFile;
    }

    public IOutputFormat getOutputFormat() {
        return chosenOutputFormat;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public IReportType getReportType() {
        return chosenReportType;
    }

    public ReportTemplateMetaData getReportMetaData() {
        return chosenReportMetaData;
    }

    /**
     * Get root element id for which the report should be created.
     * 
     * @return
     */
    public Integer getRootElement() {
        return rootElement;
    }

    /**
     * Get ids of root elements, if there are more than one
     * 
     * @return
     */
    public Integer[] getRootElements() {
        return (rootElements != null) ? rootElements.clone() : null;
    }

    private List<Organization> loadScopes() {
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
        LoadCnATreeElementTitles<ItNetwork> compoundLoader = new LoadCnATreeElementTitles<>(
                ItNetwork.class);
        try {
            compoundLoader = ServiceFactory.lookupCommandService().executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.GenerateReportDialog_38);
        }
        return compoundLoader.getElements();
    }

    /**
     * @return
     * 
     */
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

    private static String convertToFileName(String label) {
        String filename = "scope"; //$NON-NLS-1$
        if (label != null) {
            filename = label.replace(' ', '_');
            filename = filename.replace("ä", "\u00E4"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ü", "\u00FC"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ö", "\u00F6"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ä", "\u00C4"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ü", "\u00DC"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ö", "\u00D6"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ß", "\u00DF"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("\\", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(";", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("<", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(">", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("|", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("/", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return filename;
    }

    private void filterReportTypes() {
        ArrayList<ReportTemplateMetaData> list = new ArrayList<>();
        if (useCase != null && !useCase.equals("") && reportTemplates.length > 0) {
            for (ReportTemplateMetaData data : reportTemplates) {
                /*
                 * TODO: add use case to template properties for filtering
                 */
                list.add(data);
            }
        }
        reportTemplates = list.toArray(new ReportTemplateMetaData[list.size()]);
    }

    public boolean isContextMenuCall() {
        return isContextMenuCall;
    }

    public void setContextMenuCall(boolean isContextMenuCall) {
        this.isContextMenuCall = isContextMenuCall;
    }

    private String initDefaultFolder() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        defaultFolder = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_REPORT);
        defaultTemplateFolder = prefs.getString(PreferenceConstants.DEFAULT_TEMPLATE_FOLDER_REPORT);
        if (defaultFolder == null || defaultFolder.isEmpty()) {
            defaultFolder = System.getProperty(IVeriniceConstants.USER_HOME);
        }
        if (defaultTemplateFolder == null || defaultTemplateFolder.isEmpty()) {
            defaultTemplateFolder = System.getProperty(IVeriniceConstants.USER_HOME);
        }
        if (!defaultFolder.endsWith(System.getProperty(IVeriniceConstants.FILE_SEPARATOR))) {
            defaultFolder = defaultFolder + System.getProperty(IVeriniceConstants.FILE_SEPARATOR);
        }
        if (!defaultTemplateFolder
                .endsWith(System.getProperty(IVeriniceConstants.FILE_SEPARATOR))) {
            defaultTemplateFolder = defaultTemplateFolder
                    + System.getProperty(IVeriniceConstants.FILE_SEPARATOR);
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
                } else {
                    return;
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

    private void sortList(List<ReportTemplateMetaData> list) {
        Collections.sort(list, new Comparator<ReportTemplateMetaData>() {

            @Override
            public int compare(ReportTemplateMetaData o1, ReportTemplateMetaData o2) {
                NumericStringComparator nsc = new NumericStringComparator();
                return nsc.compare(o1.getOutputname(), o2.getOutputname());
            }
        });
    }

}
