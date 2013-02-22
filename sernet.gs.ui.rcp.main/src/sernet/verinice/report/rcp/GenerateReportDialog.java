package sernet.verinice.report.rcp;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ServiceComponent;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnATreeElementTitles;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.interfaces.validation.IValidationService;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;

public class GenerateReportDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(GenerateReportDialog.class);
    
    // manual filename mode or auto filename mode 
    private static final boolean FILENAME_MANUAL = true;
    
	private Combo comboReportType;

	private Combo comboOutputFormat;

	private Text textFile;

	private File outputFile;

	private IReportType[] reportTypes;
	
	private IOutputFormat chosenOutputFormat;
	
	private IReportType chosenReportType;

    private Integer rootElement;
    
    private Integer[] rootElements;

    private Button openFileButton;

    private Text textReportTemplateFile;

    private Button openReportButton;

    private Combo scopeCombo;

    private List<CnATreeElement> scopes;

    private Integer auditId=null;

    private String auditName=null;
    
	private List<CnATreeElement> preSelectedElments;
	
	private String useCase;
	
	private boolean isContextMenuCall;
	
	private boolean useCache = true;
	
	private boolean useDefaultFolder = true;
	
	private String defaultFolder;
	
    // estimated size of dialog for placement (doesnt have to be exact):
    private static final int SIZE_X = 700;
    private static final int SIZE_Y = 470;

	public GenerateReportDialog(Shell parentShell) {
		super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.auditId=null;
        this.auditName = null;
		reportTypes = ServiceComponent.getDefault().getReportService().getReportTypes();
	}
	
	public GenerateReportDialog(Shell parentShell, String useCase){
		this(parentShell);
		this.useCase = useCase;
	}
	
	/**
     * @param shell
     * @param reportScope
     */
    public GenerateReportDialog(Shell shell, Object reportScope) {
        this(shell);
        if(reportScope instanceof Audit){
            this.useCase = IReportType.USE_CASE_ID_AUDIT_REPORT;
        } else if(reportScope instanceof Organization || reportScope instanceof ITVerbund) {
            this.useCase = IReportType.USE_CASE_ID_GENERAL_REPORT;
        } else {
            this.useCase = IReportType.USE_CASE_ID_ALWAYS_REPORT;
        }
        CnATreeElement cnaElmt = (CnATreeElement) reportScope;
        
        
        this.auditId=cnaElmt.getDbId();
        this.auditName = cnaElmt.getTitle();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting audit in report dialog: " + auditId); //$NON-NLS-1$
        }
    }
    
    public GenerateReportDialog(Shell shell, List<Object> objects) {
    	this(shell);
    	List<CnATreeElement> elmts = new ArrayList<CnATreeElement>();
    	for (Object object : objects) {
			CnATreeElement cnaElmt = (CnATreeElement) object;
			elmts.add(cnaElmt);
		}
    	this.preSelectedElments = elmts;
    }
    
    public GenerateReportDialog(Shell shell, List<Object> objects, String useCase){
    	this(shell, objects);
    	this.useCase = useCase;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.GenerateReportDialog_4);
        newShell.setSize(SIZE_X, SIZE_Y);
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-SIZE_X/2, cursorLocation.y-SIZE_Y/2));
    
    }


    /* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
    @Override
	protected void createButtonsForButtonBar(Composite parent) {
	    super.createButtonsForButtonBar(parent);
	    getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

    @Override
	protected Control createDialogArea(Composite parent) {
        final int dataScopeMinimumWidth = 140;
        final int dataScopeComboMinimumWidth = 346;
        final int marginWidth = 10;
        final int defaultColNr = 3;
        
        getDefaultFolder();
        
        if(useCase != null){
            filterReportTypes();
        }
        setTitle(Messages.GenerateReportDialog_0);
        setMessage(Messages.GenerateReportDialog_7);

        final Composite frame = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) frame.getLayout();
        layout.marginWidth = marginWidth;
        layout.marginHeight = marginWidth;
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        frame.setLayoutData(gd);
        
        final Composite composite = new Composite(frame, SWT.NONE);  
		layout = new GridLayout(defaultColNr, false);
		composite.setLayout(layout);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        
		composite.setLayoutData(gd);

		Group reportGroup = new Group(composite, SWT.NULL);
		reportGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, defaultColNr, 1));
		layout = new GridLayout();
        layout.numColumns = defaultColNr;
        reportGroup.setLayout(layout);
		
		Label labelReportType = new Label(reportGroup, SWT.NONE);
		GridData gridDataLabel = new GridData();
		gridDataLabel.horizontalAlignment = SWT.LEFT;
		gridDataLabel.verticalAlignment = SWT.CENTER;
		gridDataLabel.grabExcessHorizontalSpace = true;
		gridDataLabel.minimumWidth = dataScopeMinimumWidth;
		labelReportType.setText(Messages.GenerateReportDialog_1);
		labelReportType.setLayoutData(gridDataLabel);

		comboReportType = new Combo(reportGroup, SWT.READ_ONLY);
		GridData gridComboReportType = new GridData();
		gridComboReportType.horizontalAlignment = SWT.FILL;
		gridComboReportType.grabExcessHorizontalSpace = true;
		gridComboReportType.horizontalSpan=2;
		gridComboReportType.grabExcessHorizontalSpace = true;
		gridComboReportType.minimumWidth = dataScopeComboMinimumWidth;
		comboReportType.setLayoutData(gridComboReportType);

		for (IReportType rt : reportTypes) {
			comboReportType.add(rt.getLabel());
		}
		comboReportType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			    chosenReportType = reportTypes[comboReportType.getSelectionIndex()];
                if(reportTypes[comboReportType.getSelectionIndex()].getId().equals("user") && !reportTypes[comboReportType.getSelectionIndex()].getReportFile().equals("")){
                    chosenReportType.setReportFile(""); // forget before chosen user-report template
                }
				setupComboOutputFormatContent();
				enableFileSelection();
			}
     
		});
		
		Label labelReportFile = new Label(reportGroup, SWT.NONE);
        GridData gridLabelReportFile = new GridData();
        gridLabelReportFile.horizontalAlignment = SWT.LEFT;
        labelReportFile.setText(Messages.GenerateReportDialog_2);
        labelReportFile.setLayoutData(gridLabelReportFile);
        
        textReportTemplateFile = new Text(reportGroup, SWT.BORDER);
        GridData gridTextFile2 = new GridData();
        gridTextFile2.horizontalAlignment = SWT.FILL;
        gridTextFile2.verticalAlignment = SWT.CENTER;
        gridTextFile2.grabExcessHorizontalSpace = true;
        textReportTemplateFile.setLayoutData(gridTextFile2);
        
        openReportButton = new Button(reportGroup, SWT.PUSH);
        openReportButton.setText(Messages.GenerateReportDialog_3);
        openReportButton.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent event) {
            FileDialog dlg = new FileDialog(getParentShell(), SWT.SAVE);
            dlg.setFilterExtensions(new String[] { "*.rptdesign", "*.rpt", "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            String fn = dlg.open();
            if (fn != null) {
              textReportTemplateFile.setText(fn);
            }
          }
        });
        
        Group scopeGroup = new Group(composite, SWT.NULL);
        scopeGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, defaultColNr, 1));
        layout = new GridLayout();
        layout.numColumns = 2;
        scopeGroup.setLayout(layout);
            
            
        Label labelScope = new Label(scopeGroup, SWT.NULL);
        GridData gridDataScope = new GridData();
        gridDataScope.horizontalAlignment = SWT.LEFT;
        gridDataScope.verticalAlignment = SWT.CENTER;
        gridDataScope.grabExcessHorizontalSpace = true;
        gridDataScope.minimumWidth = dataScopeMinimumWidth;
        labelScope.setLayoutData(gridDataScope);
        labelScope.setText(Messages.GenerateReportDialog_8);

        scopeCombo = new Combo(scopeGroup, SWT.READ_ONLY);
        GridData gridDatascopeCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridDatascopeCombo.grabExcessHorizontalSpace = true;
        gridDatascopeCombo.minimumWidth = dataScopeComboMinimumWidth;
        scopeCombo.setLayoutData(gridDatascopeCombo);
        
        scopeCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                getButton(IDialogConstants.OK_ID).setEnabled(true);
                int s = scopeCombo.getSelectionIndex();
                rootElement = scopes.get(s).getDbId();
            }
        });

        Group groupFile = new Group(composite, SWT.NULL);
        groupFile.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, defaultColNr, 1));
        layout = new GridLayout();
        layout.numColumns = defaultColNr;
        groupFile.setLayout(layout);
        
		Label labelOutputFormat = new Label(groupFile, SWT.NONE);
		GridData gridLabelOutputFormat = new GridData();
		gridLabelOutputFormat.horizontalAlignment = SWT.LEFT;
		gridLabelOutputFormat.verticalAlignment = SWT.CENTER;
		gridLabelOutputFormat.grabExcessHorizontalSpace = true;
		gridLabelOutputFormat.minimumWidth = dataScopeMinimumWidth;
		labelOutputFormat.setText(Messages.GenerateReportDialog_9);
		labelOutputFormat.setLayoutData(gridLabelOutputFormat);

		comboOutputFormat = new Combo(groupFile, SWT.READ_ONLY);
		GridData gridComboOutputFormat = new GridData();
		gridComboOutputFormat.horizontalAlignment = SWT.FILL;
		gridComboOutputFormat.verticalAlignment = SWT.CENTER;
		gridComboOutputFormat.grabExcessHorizontalSpace = true;
		gridComboOutputFormat.horizontalSpan=2;
		gridComboOutputFormat.grabExcessHorizontalSpace = true;
		gridComboOutputFormat.minimumWidth = dataScopeComboMinimumWidth;
		comboOutputFormat.setLayoutData(gridComboOutputFormat);
		comboOutputFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(chosenReportType!=null) {
                    chosenOutputFormat = chosenReportType.getOutputFormats()[comboOutputFormat.getSelectionIndex()];
                }
                setupOutputFilepath();
            }
     
        });
		
        GridData gridLabelFile = new GridData();
        gridLabelFile.horizontalAlignment = SWT.LEFT;
        gridLabelFile.verticalAlignment = SWT.CENTER;
        gridLabelFile.grabExcessHorizontalSpace = true;
        gridLabelFile.minimumWidth = dataScopeMinimumWidth;
        
        GridData gridTextFile = new GridData();
        gridTextFile.horizontalAlignment = SWT.FILL;
        gridTextFile.verticalAlignment = SWT.CENTER;
        gridTextFile.grabExcessHorizontalSpace = true;       

		Label labelFile = new Label(groupFile, SWT.NONE);
		labelFile.setText(Messages.GenerateReportDialog_10);
		labelFile.setLayoutData(gridLabelFile);

		textFile = new Text(groupFile, SWT.BORDER);
		textFile.setLayoutData(gridTextFile);
		
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
		    public void widgetSelected(SelectionEvent event) {
		        FileDialog dlg = new FileDialog(getParentShell(), SWT.SAVE);
		        dlg.setFilterPath(defaultFolder + textFile.getText());
		        ArrayList<String> extensionList = new ArrayList<String>();
		        if(chosenOutputFormat!=null && chosenOutputFormat.getFileSuffix()!=null) {
		            extensionList.add("*." + chosenOutputFormat.getFileSuffix()); //$NON-NLS-1$
		        }
		        extensionList.add("*.*"); //$NON-NLS-1$
		        dlg.setFilterExtensions(extensionList.toArray(new String[extensionList.size()])); 
		        dlg.setFileName(getDefaultOutputFilename());
		        String fn = dlg.open();
		        if (fn != null) {
		            textFile.setText(fn);
		            getButton(IDialogConstants.OK_ID).setEnabled(true);
		        }
		       }
		});

		Button useDefaultFolderButton = new Button(groupFile, SWT.CHECK);
        useDefaultFolderButton.setText(Messages.GenerateReportDialog_26);
        useDefaultFolderButton.setSelection(true);        
        GridData  useDefaultFolderButtonGridData = new GridData();
        useDefaultFolderButtonGridData.horizontalSpan = 2;
        useDefaultFolderButtonGridData.grabExcessHorizontalSpace = true;
        useDefaultFolderButtonGridData.horizontalAlignment = GridData.FILL;
        useDefaultFolderButtonGridData.verticalAlignment = SWT.RIGHT;
        useDefaultFolderButton.setLayoutData(useDefaultFolderButtonGridData);
        useDefaultFolderButton.addSelectionListener(new SelectionAdapter() {
        
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                useDefaultFolder = ((Button)e.getSource()).getSelection();
            }
            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);

            }
        });
		
        Group groupCache = new Group(composite, SWT.NULL);
        groupCache.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, defaultColNr, 1));
        layout = new GridLayout();
        layout.numColumns = 2;
        groupCache.setLayout(layout);
        
        gridLabelFile.horizontalAlignment = SWT.LEFT;
        gridLabelFile.verticalAlignment = SWT.CENTER;
        gridLabelFile.grabExcessHorizontalSpace = true;
        gridLabelFile.minimumWidth = dataScopeMinimumWidth;
        
        Button useCacheButton = new Button(groupCache, SWT.CHECK);
        useCacheButton.setText(Messages.GenerateReportDialog_25);
		useCacheButton.setSelection(true);
	    GridData  useCacheButtonGridData = new GridData();
	    useCacheButtonGridData.horizontalSpan = 2;
	    useCacheButtonGridData.grabExcessHorizontalSpace = true;
	    useCacheButtonGridData.horizontalAlignment = GridData.FILL;
	    useCacheButtonGridData.verticalAlignment = SWT.RIGHT;
	    useCacheButton.setLayoutData(useCacheButtonGridData);
	    useCacheButton.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                useCache = ((Button)e.getSource()).getSelection();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
	    
		openFileButton.setEnabled(FILENAME_MANUAL);
		
		comboReportType.select(0);
		chosenReportType = reportTypes[comboReportType.getSelectionIndex()];
		setupComboOutputFormatContent();
		setupComboScopes();
		
		frame.pack(); 
		return frame;
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
        // check if audit was selected by context menu:
        if (this.auditId != null && isContextMenuCall()){
            scopeCombo.removeAll();
            scopeCombo.add(this.auditName);
            rootElement=auditId;
            scopeCombo.setEnabled(true);
            scopeCombo.select(0);
            scopeCombo.redraw();
            return;
        } else if(this.preSelectedElments != null && this.preSelectedElments.size() > 0 && isContextMenuCall()) {
            scopeCombo.removeAll();
            ArrayList<Integer> auditIDList = new ArrayList<Integer>();
            StringBuilder sb = new StringBuilder();
            for(CnATreeElement elmt : preSelectedElments){
            	sb.append(elmt.getTitle());
            	if(preSelectedElments.indexOf(elmt) != preSelectedElments.size() - 1){
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
        
        scopes = new ArrayList<CnATreeElement>();

        List<String> scopeTitles = new ArrayList<String>();
        
        scopes.addAll(loadScopes());
        scopes.addAll(loadITVerbuende());

        Collections.sort(scopes, new Comparator<CnATreeElement>() {
            @Override
            public int compare(CnATreeElement o1, CnATreeElement o2) {
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
        });
        
        for (CnATreeElement elmt : scopes) {
            scopeTitles.add(elmt.getTitle());
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.GenerateReportDialog_16 + elmt.getDbId() + ": " + elmt.getTitle()); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
            }
        }
        
        String[] titles = scopeTitles.toArray(new String[scopeTitles.size()]);
        scopeCombo.setItems( titles );
        
    }

    protected void enableFileSelection() {
        boolean userTemplate = false;
        if (reportTypes[comboReportType.getSelectionIndex()].getReportFile() == null || reportTypes[comboReportType.getSelectionIndex()].getReportFile().equals("")) {
            userTemplate = true;
        }
        textReportTemplateFile.setEnabled(userTemplate);
        openReportButton.setEnabled(userTemplate);
    }

    private void setupComboOutputFormatContent(){
		comboOutputFormat.removeAll();
		for (IOutputFormat of : reportTypes[comboReportType.getSelectionIndex()].getOutputFormats()) {
			comboOutputFormat.add(of.getLabel());
		}
		comboOutputFormat.select(0);
		if(chosenReportType!=null) {
            chosenOutputFormat = chosenReportType.getOutputFormats()[comboOutputFormat.getSelectionIndex()];    
        }
	}
    
    /**
     * 
     */
    protected String setupDirPath() { 
        String currentPath = textFile.getText();
        String path = currentPath;
        if(currentPath!=null && !currentPath.isEmpty()) {
            int lastSlash = currentPath.lastIndexOf(System.getProperty("file.separator"));
            if(lastSlash!=-1) {
                path = currentPath.substring(0,lastSlash+1);
            }
            else{
                path = currentPath.substring(0,lastSlash);
            }   
        }
        if(!currentPath.equals(path)) {
            textFile.setText(path);
        }
        return path; 
    }
    
    protected void setupOutputFilepath() { 
        String currentPath = textFile.getText();
        String path = currentPath;
        if(currentPath!=null && !currentPath.isEmpty() && chosenOutputFormat!=null) {
            int lastDot = currentPath.lastIndexOf('.');
            if(lastDot!=-1) {
                path = currentPath.substring(0,lastDot+1) + chosenOutputFormat.getFileSuffix();
            } else {
                path = currentPath + chosenOutputFormat.getFileSuffix();
            }
        }
        if(!currentPath.equals(path)) {
            textFile.setText(path);
        }
    }
    
    protected String getDefaultOutputFilename() {
        String outputFileName = chosenReportType.getReportFile();
        if(outputFileName == null || outputFileName.equals("")){
            outputFileName = "unknown";
        }
        String scopeName = scopeCombo.getText().replaceAll("[^a-zA-Z]", "");
        StringBuilder sb = new StringBuilder(outputFileName).append("_").append(scopeName);
        if(chosenOutputFormat!=null) {
            sb.append(".").append(chosenOutputFormat.getFileSuffix());
        }
        return sb.toString();
    }


    @Override
    protected void okPressed() {
        if (textFile.getText().length()==0 || scopeCombo.getSelectionIndex()<0) {
            MessageDialog.openWarning(getShell(), Messages.GenerateReportDialog_5, Messages.GenerateReportDialog_6);
            return;
        }
        List<Integer> scopeIds = new ArrayList<Integer>(0);
        scopeIds.add(getRootElement());
        if(getRootElements() != null){
            scopeIds.addAll(Arrays.asList(getRootElements()));
        }
        IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        boolean dontShow = preferenceStore.getBoolean(PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING);
        IValidationService vService = ServiceFactory.lookupValidationService();
        boolean validationsExistant = false;
        for(Integer scopeId : scopeIds){
            if(vService.getValidations(scopeId, (Integer)null).size() > 0){
                validationsExistant = true;
                break;
            }
        }

        if(!dontShow && validationsExistant){
            MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(getParentShell(), Messages.GenerateReportDialog_5, Messages.GenerateReportDialog_21, Messages.GenerateReportDialog_23, dontShow, preferenceStore, PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING);
            preferenceStore.setValue(PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING, dialog.getToggleState());

            if(!(dialog.getReturnCode()==IDialogConstants.OK_ID || dialog.getReturnCode()==IDialogConstants.YES_ID)){
                return;
            }
        }

        String f = textFile.getText();
        chosenReportType = reportTypes[comboReportType.getSelectionIndex()];
        chosenOutputFormat = chosenReportType.getOutputFormats()[comboOutputFormat.getSelectionIndex()];

        chosenReportType.setReportFile(textReportTemplateFile.getText());

        // This just appends the chosen report's extension if the existing
        // suffix does not match. Could be enhanced.
        if (!f.endsWith(chosenOutputFormat.getFileSuffix())) {
            f += "." + chosenOutputFormat.getFileSuffix(); //$NON-NLS-1$
        }

        String currentPath = setupDirPath();
        defaultFolder = currentPath;
        Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.DEFAULT_FOLDER_REPORT, currentPath);
        outputFile = new File(f);
        resetScopeCombo();
        super.okPressed();
    }
    
    @Override
    protected void cancelPressed(){
        resetScopeCombo();
        super.cancelPressed();
    }
    
    private void resetScopeCombo(){
        if(preSelectedElments != null){
            preSelectedElments = null;
        }
        if(auditId != null){
            auditId = null;
        }
        setupComboScopes();
    }

	public File getOutputFile() {
		return outputFile;
	}

	public IOutputFormat getOutputFormat()
	{
		return chosenOutputFormat;
	}

	public IReportType getReportType() {
		return chosenReportType;
	}

    /**
     * Get root element id for which the report should be created.
     * @return
     */
    public Integer getRootElement() {
        return rootElement;
    }
    
    /**
     * Get ids of root elements, if there are more than one
     * @return
     */
    public Integer[] getRootElements(){
    	return (rootElements != null) ? rootElements.clone() : null;
    }
    
    private List<Organization> loadScopes() {
        LoadCnATreeElementTitles<Organization> compoundLoader = new LoadCnATreeElementTitles<Organization>(
                Organization.class);
        try {
            compoundLoader = ServiceFactory.lookupCommandService().executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.GenerateReportDialog_19);
        }
        
        return compoundLoader.getElements();
        
    }


    /**
     * @return 
     * 
     */
    private List<ITVerbund> loadITVerbuende() {
        LoadCnATreeElementTitles<ITVerbund> compoundLoader = new LoadCnATreeElementTitles<ITVerbund>(
                ITVerbund.class);
        try {
            compoundLoader = ServiceFactory.lookupCommandService().executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.GenerateReportDialog_20);
        }
        return compoundLoader.getElements();
    }
    
    public static String convertToFileName(String label) {
        String filename = ""; //$NON-NLS-1$
        if(label!=null) {
            filename = label.replace(' ', '_');
            filename = filename.replace("ä", "ae"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ü", "ue"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ö", "oe"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ä", "Ae"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ü", "Ue"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ö", "Oe"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ß", "ss"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("\\", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(";", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("<", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(">", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("|", ""); //$NON-NLS-1$ //$NON-NLS-2$
           }
        return filename;
    }
    
    private void filterReportTypes(){
		ArrayList<IReportType> list = new ArrayList<IReportType>();
		if(useCase != null && !useCase.equals("")){
			for(IReportType rt : reportTypes){
				if(rt.getUseCaseID().equals(useCase) || rt.getUseCaseID().equals(IReportType.USE_CASE_ID_ALWAYS_REPORT)){
					list.add(rt);
				}
			}
		}
		reportTypes = list.toArray(new IReportType[list.size()]);
    }

    public boolean isContextMenuCall() {
        return isContextMenuCall;
    }

    public void setContextMenuCall(boolean isContextMenuCall) {
        this.isContextMenuCall = isContextMenuCall;
    }
    
    public boolean getUseReportCache(){
        return useCache;
    }
    public boolean getUseDefaultFolder(){
        return useDefaultFolder;
    }
    private String getDefaultFolder(){
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
         defaultFolder = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_REPORT);
         if(defaultFolder != null && !defaultFolder.isEmpty() && !defaultFolder.endsWith(System.getProperty("file.separator"))){
             defaultFolder=defaultFolder+System.getProperty("file.separator"); 
         }        
        return defaultFolder; 
    }
}
