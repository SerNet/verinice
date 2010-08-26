package sernet.verinice.samt.rcp;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportType;

public class SimpleGenerateReportDialog extends Dialog {

	private Combo comboReportType;

	@SuppressWarnings("restriction")
    private Combo comboOutputFormat;

	private Text textFile;

	private File outputFile;

	private IReportType[] reportTypes;
	
	private IOutputFormat chosenOutputFormat;
	
	private IReportType chosenReportType;

    private Integer rootElement;

	@SuppressWarnings("restriction")
    protected SimpleGenerateReportDialog(Shell parentShell) {
		super(parentShell);

		reportTypes = ServiceComponent.getDefault().getReportService()
				.getReportTypes();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		// FIXME externalize strings

		Label labelReportType = new Label(container, SWT.NONE);
		GridData gridLabelReportType = new GridData();
		gridLabelReportType.horizontalAlignment = SWT.LEFT;
		gridLabelReportType.verticalAlignment = SWT.CENTER;
		labelReportType.setText("Report");
		labelReportType.setLayoutData(gridLabelReportType);

		comboReportType = new Combo(container, SWT.READ_ONLY);
		GridData gridComboReportType = new GridData();
		gridComboReportType.horizontalAlignment = SWT.FILL;
		gridComboReportType.verticalAlignment = SWT.CENTER;
		gridComboReportType.grabExcessHorizontalSpace = true;

		for (IReportType rt : reportTypes) {
			comboReportType.add(rt.getLabel());
		}
		comboReportType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setupComboOutputFormatContent();
			}

		});

		Label labelOutputFormat = new Label(container, SWT.NONE);
		GridData gridLabelOutputFormat = new GridData();
		gridLabelOutputFormat.horizontalAlignment = SWT.LEFT;
		gridLabelOutputFormat.verticalAlignment = SWT.CENTER;
		labelOutputFormat.setText("Datei");
		labelOutputFormat.setLayoutData(gridLabelOutputFormat);

		comboOutputFormat = new Combo(container, SWT.READ_ONLY);
		GridData gridComboOutputFormat = new GridData();
		gridComboOutputFormat.horizontalAlignment = SWT.FILL;
		gridComboOutputFormat.verticalAlignment = SWT.CENTER;
		gridComboOutputFormat.grabExcessHorizontalSpace = true;

		comboOutputFormat.setLayoutData(gridComboOutputFormat);

		Label labelFile = new Label(container, SWT.NONE);
		GridData gridLabelFile = new GridData();
		gridLabelFile.horizontalAlignment = SWT.LEFT;
		gridLabelFile.verticalAlignment = SWT.CENTER;
		labelFile.setText("Datei");
		labelFile.setLayoutData(gridLabelFile);

		textFile = new Text(container, SWT.BORDER);
		GridData gridTextFile = new GridData();
		gridTextFile.horizontalAlignment = SWT.FILL;
		gridTextFile.verticalAlignment = SWT.CENTER;
		gridTextFile.grabExcessHorizontalSpace = true;
		textFile.setText("/tmp/samt-report");
		textFile.setLayoutData(gridTextFile);
		
		comboReportType.select(0);
		setupComboOutputFormatContent();

		return container;
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
		String f = textFile.getText();

		chosenReportType = reportTypes[comboReportType.getSelectionIndex()];
		chosenOutputFormat = chosenReportType.getOutputFormats()[comboOutputFormat.getSelectionIndex()];
		
		// TODO: This just appends ".pdf" or ".html" if the existing
		// suffix does not match. Very simple and should be enhanced.
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
	
}
