package sernet.gs.ui.rcp.main.bsi.wizards;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;

import sernet.gs.ui.rcp.main.reports.ErgaenzendeAnalyseReport;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.MassnahmenTodoReport;
import sernet.gs.ui.rcp.main.reports.MassnahmenumsetzungReport;
import sernet.gs.ui.rcp.main.reports.ModellierungReport;
import sernet.gs.ui.rcp.main.reports.SchutzbedarfsDefinitionReport;
import sernet.gs.ui.rcp.main.reports.SchutzbedarfszuordnungReport;
import sernet.gs.ui.rcp.main.reports.StrukturanalyseReport;
import sernet.gs.ui.rcp.main.reports.VerfahrensUebersichtReport;

/**
 * WizardPage to choose the kind of report that should be generated
 * from the List of <code>IBSIReports</code>.
 * 
 * @author koderman@sernet.de
 *
 */
public class ChooseReportPage extends WizardPage {

	private Table reportsTable;
	private TableColumn nameColumn;
	private boolean initDone = false;

	protected ChooseReportPage() {
		super("Report auswählen");
		setTitle("Report auswählen");
		setDescription("Wählen Sie den Report, der erstellt werden soll.");
	}
	
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);
		setControl(container);

		final GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		reportsTable = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER  );
		reportsTable.setLayoutData(gridData);
		reportsTable.setHeaderVisible(false);
		reportsTable.setLinesVisible(false);
		
		reportsTable.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (reportsTable.getSelectionCount() > 0) {
					setPageComplete(true);
					getExportWizard().setReport((IBSIReport) 
							reportsTable.getSelection()[0].getData());
				}
			}
		});
		
		nameColumn = new TableColumn(reportsTable, SWT.LEFT);
		nameColumn.setText("Report");

		setPageComplete(false);
	
	}

	public ExportWizard getExportWizard() {
		return (ExportWizard)getWizard();
	}
	
	private void initContents() {
		if (initDone )
			return;
		initDone = true;
		
		IBSIReport report = new SchutzbedarfsDefinitionReport();
		TableItem item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new StrukturanalyseReport();
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new SchutzbedarfszuordnungReport();
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new ModellierungReport();
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new MassnahmenumsetzungReport();
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);

		report = new ErgaenzendeAnalyseReport();
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new MassnahmenTodoReport();
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new VerfahrensUebersichtReport();
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		nameColumn.pack();
		this.reportsTable.layout(true);
		
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			initContents();
		}
	}

}
