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

import sernet.verinice.report.service.impl.IOutputFormat;
import sernet.verinice.report.service.impl.IReportType;

public class GenerateReportDialog extends Dialog {

	private Combo comboOutputFormat;
	
	private Text textFile;

	private File outputFile;

	private IOutputFormat outputFormat;
	
	private IReportType reportType;

	protected GenerateReportDialog(Shell parentShell) {
		super(parentShell);
		
		// DEMO: Hard-code the report type. Later on use a dialog or something to chose the report
		// type.
		reportType = Activator.getDefault().getReportService().getReportTypes()[0];
		outputFormat = reportType.getOutputFormats()[0];
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
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
		
		/* TODO: Possible output formats should be put into this combobox
		 * everytime the dialog is shown.
		 */
		for (IOutputFormat of : reportType.getOutputFormats())
		{
			comboOutputFormat.add(of.getLabel());
		};
		comboOutputFormat.select(0);
		comboOutputFormat.addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e) {
				outputFormat = reportType.getOutputFormats()[comboOutputFormat.getSelectionIndex()];
			}
			
		});
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

		return container;
	}

	@Override
	protected void okPressed() {
		String f = textFile.getText();

		// TODO: This just appends ".pdf" or ".html" if the existing
		// suffix does not match. Very simple and should be enhanced.
		if (!f.endsWith(outputFormat.getFileSuffix())) {
			f += "." + outputFormat.getFileSuffix();
		}

		outputFile = new File(f);
		
		super.okPressed();
	}

	File getOutputFile() {
		return outputFile;
	}

	public IOutputFormat getOutputFormat() {
		return outputFormat;
	}

}
