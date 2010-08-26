package sernet.verinice.samt.rcp;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
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

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnATreeElementTitles;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

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
    
    // estimated size of dialog for placement (doesnt have to be exact):
    private static final int SIZE_X = 500;
    private static final int SIZE_Y = 200;
    
    private static final Logger LOG = Logger.getLogger(GenerateReportDialog.class);
    
    @SuppressWarnings("restriction")
    @Override
    protected void configureShell(Shell newShell) {
        // FIXME externalize strings
        super.configureShell(newShell);
        newShell.setText("Generate report");
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-SIZE_X/2, cursorLocation.y-SIZE_Y/2));
    }
    

	protected GenerateReportDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);

		reportTypes = ServiceComponent.getDefault().getReportService()
				.getReportTypes();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("restriction")
    @Override
	protected void createButtonsForButtonBar(Composite parent) {
	    super.createButtonsForButtonBar(parent);
	    getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	@SuppressWarnings("restriction")
    @Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);
		
		// FIXME externalize strings

		Label labelReportType = new Label(container, SWT.NONE);
		GridData gridLabelReportType = new GridData();
		gridLabelReportType.horizontalAlignment = SWT.LEFT;
		gridLabelReportType.verticalAlignment = SWT.CENTER;
		labelReportType.setText("Choose Report:");
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
        labelReportFile.setText("Report File:");
        labelReportFile.setLayoutData(gridLabelReportFile);
        
        textReportFile = new Text(container, SWT.BORDER);
        GridData gridTextFile2 = new GridData();
        gridTextFile2.horizontalAlignment = SWT.FILL;
        gridTextFile2.verticalAlignment = SWT.CENTER;
        gridTextFile2.grabExcessHorizontalSpace = true;
        textReportFile.setLayoutData(gridTextFile2);
        
        openReportButton = new Button(container, SWT.PUSH);
            openReportButton.setText("Browse...");
            openReportButton.addSelectionListener(new SelectionAdapter() {
              public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(getParentShell(), SWT.SAVE);
                //dlg.setFilterNames(FILTER_NAMES);
                dlg.setFilterExtensions(new String[] { "*.rptdesign", "*.rpt", "*.xml", "*.*" });
                String fn = dlg.open();
                if (fn != null) {
                  textReportFile.setText(fn);
                }
              }
            });
            
        Label label2 = new Label(container, SWT.NULL);
        GridData gridData7 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        label2.setLayoutData(gridData7);
        label2.setText("Top level element:");

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
		labelOutputFormat.setText("Output Format");
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
		labelFile.setText("Output File");
		labelFile.setLayoutData(gridLabelFile);

		textFile = new Text(container, SWT.BORDER);
		GridData gridTextFile = new GridData();
		gridTextFile.horizontalAlignment = SWT.FILL;
		gridTextFile.verticalAlignment = SWT.CENTER;
		gridTextFile.grabExcessHorizontalSpace = true;
		textFile.setLayoutData(gridTextFile);
		
//		  try {
//		      textFile.setText(File.createTempFile("verinice-report-", "").toString());
//	        } catch (IOException e1) {
//	            LOG.error(e1);
//	        }
		
		openButton = new Button(container, SWT.PUSH);
		    openButton.setText("Browse...");
		    openButton.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		        FileDialog dlg = new FileDialog(getParentShell(), SWT.SAVE);
		        //dlg.setFilterNames(FILTER_NAMES);
		        dlg.setFilterExtensions(new String[] { "*.pdf", "*.html", "*.xls", "*.*" });
		        String fn = dlg.open();
		        if (fn != null) {
		          textFile.setText(fn);
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
    @SuppressWarnings("restriction")
    private void setupComboScopes() {
        scopes = new ArrayList<CnATreeElement>();
        scopeTitles = new ArrayList<String>();
        
        scopes.addAll(loadScopes());
        scopes.addAll(loadITVerbuende());
        for (CnATreeElement elmt : scopes) {
            scopeTitles.add(elmt.getTitle());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loaded top level element with ID " + elmt.getDbId() + ": " + elmt.getTitle());
            }
        }
        
        scopeTitles.toArray(new String[scopeTitles.size()]);
        scopeCombo.setItems( scopeTitles.toArray(new String[scopeTitles.size()]) );
        
    }


    /**
     * 
     */
    @SuppressWarnings("restriction")
    protected void enableFileSelection() {
        boolean enable = false;
        if (reportTypes[comboReportType.getSelectionIndex()].getReportFile() != null) {
            enable = true;
        }
        textReportFile.setEnabled(enable);
        openReportButton.setEnabled(enable);
    }

    @SuppressWarnings("restriction")
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
		String f = textFile.getText();

		chosenReportType = reportTypes[comboReportType.getSelectionIndex()];
		chosenOutputFormat = chosenReportType.getOutputFormats()[comboOutputFormat.getSelectionIndex()];
		
		// This just appends the chosen report's extension if the existing
		// suffix does not match. Could be enhanced.
		if (!f.endsWith(chosenOutputFormat.getFileSuffix())) {
			f += "." + chosenOutputFormat.getFileSuffix();
		}

		outputFile = new File(f);

		super.okPressed();
	}

	File getOutputFile() {
		return outputFile;
	}

	IOutputFormat getOutputFormat()
	{
		return chosenOutputFormat;
	}

	IReportType getReportType() {
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
            ExceptionUtil.log(e, "Error loading scopes.");
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
            ExceptionUtil.log(e, "Error loading IT-networks.");
        }
        
        return compoundLoader
                .getElements();
    }
    
	
}
