/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.reports.AllItemsReport;
import sernet.gs.ui.rcp.main.reports.ErgaenzendeAnalyseReport;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.MassnahmenTodoReport;
import sernet.gs.ui.rcp.main.reports.MassnahmenumsetzungReport;
import sernet.gs.ui.rcp.main.reports.ModellierungReport;
import sernet.gs.ui.rcp.main.reports.SchutzbedarfsDefinitionReport;
import sernet.gs.ui.rcp.main.reports.SchutzbedarfszuordnungReport;
import sernet.gs.ui.rcp.main.reports.SelfAssessmentReport;
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

		Properties reportProperties = loadReportProperties();
		
		IBSIReport report = new SchutzbedarfsDefinitionReport(reportProperties);
		TableItem item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new StrukturanalyseReport(reportProperties);
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new SchutzbedarfszuordnungReport(reportProperties);
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new ModellierungReport(reportProperties);
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new MassnahmenumsetzungReport(reportProperties);
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);

		report = new ErgaenzendeAnalyseReport(reportProperties);
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new MassnahmenTodoReport(reportProperties);
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new VerfahrensUebersichtReport(reportProperties);
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new AllItemsReport(reportProperties);
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);
		
		report = new SelfAssessmentReport();
		item = new TableItem(reportsTable, SWT.NULL);
		item.setText(0, report.getTitle());
		item.setData(report);

		nameColumn.pack();
		this.reportsTable.layout(true);
		
	}
	
	private Properties loadReportProperties() {
    		try {
    			Properties reportProperties = new Properties();
    			File config = new File(CnAWorkspace.getInstance().getConfDir() + File.separator
    					+ IBSIReport.PROPERTY_FILE);
    			FileInputStream is = new FileInputStream(config);
	    		if (is != null) {
	    			reportProperties.load(is);
	    			is.close();
	    			return reportProperties;
	    		} else {
	    			Logger.getLogger(this.getClass())
	    				.error("Konnte Report Default-Felder nicht laden.");
	    		}
			} catch (IOException e) {
				Logger.getLogger(
						this.getClass()).error("Konnte Report Default-Felder nicht laden.", e);
			}
			return null;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			initContents();
		}
	}

}
