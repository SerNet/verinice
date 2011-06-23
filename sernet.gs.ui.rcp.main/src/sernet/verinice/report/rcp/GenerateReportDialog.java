package sernet.verinice.report.rcp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ServiceComponent;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnATreeElementTitles;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;

public class GenerateReportDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(GenerateReportDialog.class);
    
    // manual filename mode or auto filename mode 
    private static final boolean FILENAME_MANUAL = false;
    
	private Combo comboReportType;

	private Combo comboOutputFormat;

	private Text textDir, textFile;

	private File outputFile;

	private IReportType[] reportTypes;
	
	private IOutputFormat chosenOutputFormat;
	
	private IReportType chosenReportType;

    private Integer rootElement;

    private Button openDirButton, openFileButton;

    private Text textReportTemplateFile;

    private Button openReportButton;

    private Combo scopeCombo;

    private ArrayList<CnATreeElement> scopes;

    private ArrayList<String> scopeTitles;

    private Integer auditId=null;

    private String auditName=null;
    
    private boolean userTemplate = true;

    private boolean filenameManual = FILENAME_MANUAL;
    
    // estimated size of dialog for placement (doesnt have to be exact):
    private static final int SIZE_X = 540;
    private static final int SIZE_Y = 470;
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.GenerateReportDialog_4);
        newShell.setSize(SIZE_X, SIZE_Y);
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-SIZE_X/2, cursorLocation.y-SIZE_Y/2));
    
    }
    

	public GenerateReportDialog(Shell parentShell) {
		super(parentShell);
		this.auditId=null;
        this.auditName = null;
		reportTypes = ServiceComponent.getDefault().getReportService().getReportTypes();
	}
	
	/**
     * @param shell
     * @param audit
     */
    public GenerateReportDialog(Shell shell, Audit audit) {
        this(shell);
        this.auditId=audit.getDbId();
        this.auditName = audit.getTitle();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting audit in report dialog: " + auditId); //$NON-NLS-1$
        }
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
        setTitle(Messages.GenerateReportDialog_0);
        setMessage(Messages.GenerateReportDialog_7);

        final Composite frame = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) frame.getLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        frame.setLayoutData(gd);
        
        final Composite composite = new Composite(frame, SWT.NONE);  
		layout = new GridLayout(3, false);
		composite.setLayout(layout);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        
		composite.setLayoutData(gd);

		Group reportGroup = new Group(composite, SWT.NULL);
		reportGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
		layout = new GridLayout();
        layout.numColumns = 3;
        reportGroup.setLayout(layout);
		
		Label labelReportType = new Label(reportGroup, SWT.NONE);
		GridData gridDataLabel = new GridData();
		gridDataLabel.horizontalAlignment = SWT.LEFT;
		gridDataLabel.verticalAlignment = SWT.CENTER;
		gridDataLabel.grabExcessHorizontalSpace = true;
		gridDataLabel.minimumWidth = 140;
		labelReportType.setText(Messages.GenerateReportDialog_1);
		labelReportType.setLayoutData(gridDataLabel);

		comboReportType = new Combo(reportGroup, SWT.READ_ONLY);
		GridData gridComboReportType = new GridData();
		gridComboReportType.horizontalAlignment = SWT.FILL;
		gridComboReportType.grabExcessHorizontalSpace = true;
		gridComboReportType.horizontalSpan=2;
		gridComboReportType.grabExcessHorizontalSpace = true;
		gridComboReportType.minimumWidth = 346;
		comboReportType.setLayoutData(gridComboReportType);

		for (IReportType rt : reportTypes) {
			comboReportType.add(rt.getLabel());
		}
		comboReportType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setupComboOutputFormatContent();
				enableFileSelection();
				setupOutputFilename();
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
            //dlg.setFilterNames(FILTER_NAMES);
            dlg.setFilterExtensions(new String[] { "*.rptdesign", "*.rpt", "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            String fn = dlg.open();
            if (fn != null) {
              textReportTemplateFile.setText(fn);
              setupOutputFilename();
            }
          }
        });
        
        Group scopeGroup = new Group(composite, SWT.NULL);
        scopeGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
        layout = new GridLayout();
        layout.numColumns = 2;
        scopeGroup.setLayout(layout);
            
            
        Label labelScope = new Label(scopeGroup, SWT.NULL);
        GridData gridDataScope = new GridData();
        gridDataScope.horizontalAlignment = SWT.LEFT;
        gridDataScope.verticalAlignment = SWT.CENTER;
        gridDataScope.grabExcessHorizontalSpace = true;
        gridDataScope.minimumWidth = 140;
        labelScope.setLayoutData(gridDataScope);
        labelScope.setText(Messages.GenerateReportDialog_8);

        scopeCombo = new Combo(scopeGroup, SWT.READ_ONLY);
        GridData gridDatascopeCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridDatascopeCombo.grabExcessHorizontalSpace = true;
        gridDatascopeCombo.minimumWidth = 346;
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
        groupFile.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
        layout = new GridLayout();
        layout.numColumns = 3;
        groupFile.setLayout(layout);
        
		Label labelOutputFormat = new Label(groupFile, SWT.NONE);
		GridData gridLabelOutputFormat = new GridData();
		gridLabelOutputFormat.horizontalAlignment = SWT.LEFT;
		gridLabelOutputFormat.verticalAlignment = SWT.CENTER;
		gridLabelOutputFormat.grabExcessHorizontalSpace = true;
		gridLabelOutputFormat.minimumWidth = 140;
		labelOutputFormat.setText(Messages.GenerateReportDialog_9);
		labelOutputFormat.setLayoutData(gridLabelOutputFormat);

		comboOutputFormat = new Combo(groupFile, SWT.READ_ONLY);
		GridData gridComboOutputFormat = new GridData();
		gridComboOutputFormat.horizontalAlignment = SWT.FILL;
		gridComboOutputFormat.verticalAlignment = SWT.CENTER;
		gridComboOutputFormat.grabExcessHorizontalSpace = true;
		gridComboOutputFormat.horizontalSpan=2;
		gridComboOutputFormat.grabExcessHorizontalSpace = true;
		gridComboOutputFormat.minimumWidth = 346;
		comboOutputFormat.setLayoutData(gridComboOutputFormat);
		comboOutputFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setupOutputFilename();
            }
     
        });
		
		Label labelDir = new Label(groupFile, SWT.NONE);
        GridData gridLabelFile = new GridData();
        gridLabelFile.horizontalAlignment = SWT.LEFT;
        gridLabelFile.verticalAlignment = SWT.CENTER;
        gridLabelFile.grabExcessHorizontalSpace = true;
        gridLabelFile.minimumWidth = 140;
        labelDir.setText(Messages.GenerateReportDialog_12);
        labelDir.setLayoutData(gridLabelFile);

        textDir = new Text(groupFile, SWT.BORDER);
        GridData gridTextFile = new GridData();
        gridTextFile.horizontalAlignment = SWT.FILL;
        gridTextFile.verticalAlignment = SWT.CENTER;
        gridTextFile.grabExcessHorizontalSpace = true;
        textDir.setLayoutData(gridTextFile);
        
        textDir.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        
        textDir.setEditable(!FILENAME_MANUAL);
        
        openDirButton = new Button(groupFile, SWT.PUSH);
        openDirButton.setText(Messages.GenerateReportDialog_11);
        openDirButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dlg = new DirectoryDialog(getParentShell());

                // Set the initial filter path according
                // to anything they've selected or typed in
                dlg.setFilterPath(textDir.getText());

                // Change the title bar text
                dlg.setText(Messages.GenerateReportDialog_13);

                // Customizable message displayed in the dialog
                dlg.setMessage(Messages.GenerateReportDialog_14);

                // Calling open() will open and run the dialog.
                // It will return the selected directory, or
                // null if user cancels
                String dir = dlg.open();
                if (dir != null) {
                    textDir.setText(dir);
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                    setupOutputFilename();
                }
            }
        });
        
        openDirButton.setEnabled(!FILENAME_MANUAL);
        
        Label labelFilenameManual = new Label(groupFile, SWT.NONE);
        labelFilenameManual.setText(Messages.GenerateReportDialog_15);
        labelFilenameManual.setLayoutData(gridLabelFile);
         
        final Button checkFilenameManual = new Button(groupFile, SWT.CHECK);
        checkFilenameManual.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
        checkFilenameManual.setSelection(false);
    
        checkFilenameManual.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                filenameManual = checkFilenameManual.getSelection();
                enableFileDialog(filenameManual);
            }
        });  

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
	        dlg.setFilterPath(textFile.getText());
	        //dlg.setFilterNames(FILTER_NAMES);
	        chosenReportType = reportTypes[comboReportType.getSelectionIndex()];
	        chosenOutputFormat = chosenReportType.getOutputFormats()[comboOutputFormat.getSelectionIndex()];
	        ArrayList<String> extensionList = new ArrayList<String>();
	        if(chosenOutputFormat!=null && chosenOutputFormat.getFileSuffix()!=null) {
	            extensionList.add("*." + chosenOutputFormat.getFileSuffix()); //$NON-NLS-1$
	        }
	        extensionList.add("*.*"); //$NON-NLS-1$
	        dlg.setFilterExtensions(extensionList.toArray(new String[extensionList.size()])); 
	        String fn = dlg.open();
	        if (fn != null) {
	          textFile.setText(fn);
	          getButton(IDialogConstants.OK_ID).setEnabled(true);
	        }
	      }
	    });
		
		openFileButton.setEnabled(FILENAME_MANUAL);
		
		comboReportType.select(0);
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
        textDir.setEditable(!filenameManual);
        openDirButton.setEnabled(!filenameManual);
        if(filenameManual) {
            textDir.setText(""); //$NON-NLS-1$
        }
    }


    /**
     * Load list of scopes for user selection of top level element for report.
     */
    private void setupComboScopes() {
        // check if audit was selected by context menu:
        if (this.auditId != null) {
            scopeCombo.removeAll();
            scopeCombo.add(this.auditName);
            rootElement=auditId;
            scopeCombo.setEnabled(true);
            scopeCombo.select(0);
            scopeCombo.redraw();
            return;
        }
        
        scopes = new ArrayList<CnATreeElement>();
        scopeTitles = new ArrayList<String>();
        
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
        userTemplate = false;
        if (reportTypes[comboReportType.getSelectionIndex()].getReportFile() != null) {
            userTemplate = true;
        }
        textReportTemplateFile.setEnabled(userTemplate);
        openReportButton.setEnabled(userTemplate);
    }

    private void setupComboOutputFormatContent()
	{
		comboOutputFormat.removeAll();
		for (IOutputFormat of : reportTypes[comboReportType.getSelectionIndex()].getOutputFormats()) {
			comboOutputFormat.add(of.getLabel());
		};
		comboOutputFormat.select(0);
	}
    
    /**
     * 
     */
    protected void setupOutputFilename() {
        if(!filenameManual) {          
            String reportLabel = null;
            if(userTemplate) {
                reportLabel = getLabelFromTemplateFile(textReportTemplateFile.getText());
            }
            chosenReportType = reportTypes[comboReportType.getSelectionIndex()];
            if(!userTemplate && chosenReportType!=null) {
                reportLabel = chosenReportType.getLabel();
            }
            String filename = convertToFileName(reportLabel);
            chosenOutputFormat = chosenReportType.getOutputFormats()[comboOutputFormat.getSelectionIndex()];
            String suffix = chosenOutputFormat.getFileSuffix();
            String dir = System.getProperty("user.dir"); //$NON-NLS-1$
            if(textDir!=null && textDir.getText()!=null && !textDir.getText().isEmpty()) {
                dir = textDir.getText();
            }
            if(filename!=null && !filename.isEmpty()) {
                textFile.setText(dir + File.separatorChar + filename + "." + suffix); //$NON-NLS-1$
            }
        }
    }

	/**
     * @param text
     */
    private String getLabelFromTemplateFile(String filePath) {
        String label = null;
        if(filePath!=null && !filePath.isEmpty() && filePath.contains(".")) { //$NON-NLS-1$
            int indexOfSeparator = filePath.lastIndexOf(File.separatorChar);
            if(indexOfSeparator!=-1) {
                filePath = filePath.substring(indexOfSeparator+1);
            }
            label = filePath.substring(0, filePath.lastIndexOf('.'));
        }
        return label;
    }


    @Override
	protected void okPressed() {
	    if (textFile.getText().length()==0 || scopeCombo.getSelectionIndex()<0) {
	        MessageDialog.openWarning(getShell(), Messages.GenerateReportDialog_5, Messages.GenerateReportDialog_6);
	        return;
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

		outputFile = new File(f);

		super.okPressed();
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
        }
        return filename;
    }
    
	
}
