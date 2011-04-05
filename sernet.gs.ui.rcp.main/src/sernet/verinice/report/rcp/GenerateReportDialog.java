package sernet.verinice.report.rcp;

import java.io.File;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;

import com.sun.xml.messaging.saaj.util.LogDomainConstants;

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

@SuppressWarnings("restriction")
public class GenerateReportDialog extends Dialog {

	private Combo comboReportType;

	private Combo comboOutputFormat;

	private Text textFile;

	private File outputFile;

	private IReportType[] reportTypes;
	
	private IOutputFormat chosenOutputFormat;
	
	private IReportType chosenReportType;

    private Integer rootElement;

    private Button openButton;

    private Text textReportFile;

    private Button openReportButton;

    private Combo scopeCombo;

    private ArrayList<CnATreeElement> scopes;

    private ArrayList<String> scopeTitles;

    private Integer auditId=null;

    private String auditName=null;
    
    // estimated size of dialog for placement (doesnt have to be exact):
    private static final int SIZE_X = 500;
    private static final int SIZE_Y = 200;
    
    private static final Logger LOG = Logger.getLogger(GenerateReportDialog.class);
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.GenerateReportDialog_0);
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-SIZE_X/2, cursorLocation.y-SIZE_Y/2));
    }
    

	public GenerateReportDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
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
            LOG.debug("Setting audit in report dialog: " + auditId);
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
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);
		

		Label labelReportType = new Label(container, SWT.NONE);
		GridData gridLabelReportType = new GridData();
		gridLabelReportType.horizontalAlignment = SWT.LEFT;
		gridLabelReportType.verticalAlignment = SWT.CENTER;
		labelReportType.setText(Messages.GenerateReportDialog_1);
		labelReportType.setLayoutData(gridLabelReportType);

		comboReportType = new Combo(container, SWT.READ_ONLY);
		GridData gridComboReportType = new GridData();
		gridComboReportType.horizontalAlignment = SWT.FILL;
		//gridComboReportType.verticalAlignment = SWT.CENTER;
		gridComboReportType.grabExcessHorizontalSpace = true;
		gridComboReportType.horizontalSpan=2;
		comboReportType.setLayoutData(gridComboReportType);

		for (IReportType rt : reportTypes) {
			comboReportType.add(rt.getLabel());
		}
		comboReportType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setupComboOutputFormatContent();
				enableFileSelection();
			}
		});
		
		Label labelReportFile = new Label(container, SWT.NONE);
        GridData gridLabelReportFile = new GridData();
        gridLabelReportFile.horizontalAlignment = SWT.LEFT;
        //gridLabelReportFile.verticalAlignment = SWT.CENTER;
        labelReportFile.setText(Messages.GenerateReportDialog_2);
        labelReportFile.setLayoutData(gridLabelReportFile);
        
        textReportFile = new Text(container, SWT.BORDER);
        GridData gridTextFile2 = new GridData();
        gridTextFile2.horizontalAlignment = SWT.FILL;
        gridTextFile2.verticalAlignment = SWT.CENTER;
        gridTextFile2.grabExcessHorizontalSpace = true;
        textReportFile.setLayoutData(gridTextFile2);
        
        openReportButton = new Button(container, SWT.PUSH);
            openReportButton.setText(Messages.GenerateReportDialog_3);
            openReportButton.addSelectionListener(new SelectionAdapter() {
              public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(getParentShell(), SWT.SAVE);
                //dlg.setFilterNames(FILTER_NAMES);
                dlg.setFilterExtensions(new String[] { "*.rptdesign", "*.rpt", "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                String fn = dlg.open();
                if (fn != null) {
                  textReportFile.setText(fn);
                }
              }
            });
            
        Label label2 = new Label(container, SWT.NULL);
        GridData gridData7 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        label2.setLayoutData(gridData7);
        label2.setText(Messages.GenerateReportDialog_8);

        scopeCombo = new Combo(container, SWT.READ_ONLY);
        GridData gridDatascopeCombo = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        
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

		Label labelOutputFormat = new Label(container, SWT.NONE);
		GridData gridLabelOutputFormat = new GridData();
		gridLabelOutputFormat.horizontalAlignment = SWT.LEFT;
		gridLabelOutputFormat.verticalAlignment = SWT.CENTER;
		labelOutputFormat.setText(Messages.GenerateReportDialog_9);
		labelOutputFormat.setLayoutData(gridLabelOutputFormat);

		comboOutputFormat = new Combo(container, SWT.READ_ONLY);
		GridData gridComboOutputFormat = new GridData();
		gridComboOutputFormat.horizontalAlignment = SWT.FILL;
		gridComboOutputFormat.verticalAlignment = SWT.CENTER;
		gridComboOutputFormat.grabExcessHorizontalSpace = true;
		gridComboOutputFormat.horizontalSpan=2;
		comboOutputFormat.setLayoutData(gridComboOutputFormat);

		Label labelFile = new Label(container, SWT.NONE);
		GridData gridLabelFile = new GridData();
		gridLabelFile.horizontalAlignment = SWT.LEFT;
		gridLabelFile.verticalAlignment = SWT.CENTER;
		labelFile.setText(Messages.GenerateReportDialog_10);
		labelFile.setLayoutData(gridLabelFile);

		textFile = new Text(container, SWT.BORDER);
		GridData gridTextFile = new GridData();
		gridTextFile.horizontalAlignment = SWT.FILL;
		gridTextFile.verticalAlignment = SWT.CENTER;
		gridTextFile.grabExcessHorizontalSpace = true;
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
		
//		  try {
//		      textFile.setText(File.createTempFile("verinice-report-", "").toString());
//	        } catch (IOException e1) {
//	            LOG.error(e1);
//	        }
		
		openButton = new Button(container, SWT.PUSH);
		    openButton.setText(Messages.GenerateReportDialog_11);
		    openButton.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		        FileDialog dlg = new FileDialog(getParentShell(), SWT.SAVE);
		        //dlg.setFilterNames(FILTER_NAMES);
		        dlg.setFilterExtensions(new String[] { "*.pdf", "*.html", "*.xls", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		        String fn = dlg.open();
		        if (fn != null) {
		          textFile.setText(fn);
		          getButton(IDialogConstants.OK_ID).setEnabled(true);
		        }
		      }
		    });
		
		comboReportType.select(0);
		setupComboOutputFormatContent();
		setupComboScopes();

		return container;
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
        for (CnATreeElement elmt : scopes) {
            scopeTitles.add(elmt.getTitle());
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.GenerateReportDialog_16 + elmt.getDbId() + ": " + elmt.getTitle()); //$NON-NLS-2$ //$NON-NLS-1$
            }
        }
        
        String[] titles = scopeTitles.toArray(new String[scopeTitles.size()]);
        Arrays.sort( titles );
        scopeCombo.setItems( titles );
        
    }

    protected void enableFileSelection() {
        boolean enable = false;
        if (reportTypes[comboReportType.getSelectionIndex()].getReportFile() != null) {
            enable = true;
        }
        textReportFile.setEnabled(enable);
        openReportButton.setEnabled(enable);
    }

    private void setupComboOutputFormatContent()
	{
		comboOutputFormat.removeAll();
		for (IOutputFormat of : reportTypes[comboReportType
				.getSelectionIndex()].getOutputFormats()) {
			comboOutputFormat.add(of.getLabel());
		};
		comboOutputFormat.select(0);
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
		
		chosenReportType.setReportFile(textReportFile.getText());
		
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
            compoundLoader = ServiceFactory.lookupCommandService()
                    .executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.GenerateReportDialog_19);
        }
        
        return compoundLoader
                .getElements();
        
    }


    /**
     * @return 
     * 
     */
    private List<ITVerbund> loadITVerbuende() {
        LoadCnATreeElementTitles<ITVerbund> compoundLoader = new LoadCnATreeElementTitles<ITVerbund>(
                ITVerbund.class);
        try {
            compoundLoader = ServiceFactory.lookupCommandService()
                    .executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.GenerateReportDialog_20);
        }
        
        return compoundLoader
                .getElements();
    }
    
	
}
