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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.PropertySelection;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.ReportGetRowsCommand;
import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.gs.ui.rcp.office.OOWrapper;
import sernet.snutils.ExceptionHandlerFactory;

/**
 * Wizard to create different kinds of reports using OpenOffice as backend.
 * 
 * @author koderman@sernet.de
 *
 */
public class ExportWizard extends Wizard implements IExportWizard {

	private ChooseReportPage chooseReportPage;
	private IBSIReport report;
	private String ooPath;
	private String templatePath;
	private ChooseExportMethodPage chooseExportMethodPage;
	private ChoosePropertiesPage choosePropertiesPage;

	private PropertySelection shownPropertyTypes;
	private String textTemplatePath;

	
	public void resetShownPropertyTypes() {
		this.shownPropertyTypes = null;
	}

	private String absolutePath(String dirPath) {
		File f  = new File(dirPath);
		return f.getAbsolutePath();
	}
	
	@Override
	public boolean performFinish() {
		try {
			
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
					doExport(mon, ooPath, templatePath);
				}
			});
			return (true);
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Fehler beim OpenOffice Export");
		} catch (InterruptedException e) {
			// do nothing
		}
		return false;
	}

	public void addPages() {
		setWindowTitle("OpenOffice Export");
		
		chooseReportPage = new ChooseReportPage();
		addPage(chooseReportPage);
		
		chooseExportMethodPage = new ChooseExportMethodPage();
		addPage(chooseExportMethodPage);
		
		choosePropertiesPage = new ChoosePropertiesPage();
		addPage(choosePropertiesPage);

	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public boolean needsProgressMonitor() {
		return true;
	}

	public void setReport(IBSIReport report) {
		this.report = report;
	}

	public IBSIReport getReport() {
		return report;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String odtPath) {
		this.templatePath = odtPath;
	}

	public String getOoPath() {
		return ooPath;
	}

	public void setOoPath(String ooPath) {
		this.ooPath = ooPath;
	}
	
	protected void doExport(IProgressMonitor mon, String ooPath, String odtPath) {
		try {
			
			ReportGetRowsCommand command = new ReportGetRowsCommand(report, shownPropertyTypes);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			ArrayList<IOOTableRow> rows = command.getRows();

			//shownPropertyTypes.printall();
			OOWrapper ooWrap = new OOWrapper(ooPath);
			mon.beginTask("Exportiere nach OpenOffice...", rows.size());
			mon.subTask("");
//			if (report instanceof TextReport) {
//				File tmp = File.createTempFile("report_tmp",".odt");
//				CnAWorkspace.getInstance().copyFile(textTemplatePath, tmp);
//				ooWrap.openDocument(tmp.getAbsolutePath());
//				ooWrap.createTextReport(getReport().getTitle(), rows, 2, mon);
//			} else {
				File tmp = File.createTempFile("report_tmp",".ods");
				CnAWorkspace.getInstance().copyFile(templatePath,tmp);
				ooWrap.openSpreadhseet(tmp.getAbsolutePath());
				ooWrap.fillSpreadsheet(getReport().getTitle(), rows, mon);
//			}
			mon.done();
		} catch (java.lang.Exception e) {
			ExceptionHandlerFactory.getDefaultHandler().handleException(e);
		}
	}

	


	/**
	 * Set the fields that will be shown as columns in the report.
	 * References HUI framework properties as defined in SNCA.xml.
	 * 
	 * @param entityTypeId for which entity should the field be displayed
	 * @param propertyTypeId this property will be displayed for entities of
	 *                       the type given above
	 */
	public void addShownProperty(String entityTypeId, String propertyTypeId) {
		if (shownPropertyTypes == null)
			shownPropertyTypes = new PropertySelection();
		shownPropertyTypes.add(entityTypeId, propertyTypeId);
	}

	public void removeShownProperty(String entityTypeId, String propertyTypeId) {
		List<String> properties = shownPropertyTypes.get(entityTypeId);
		if (properties == null)
			return;
		
		if (properties.contains(propertyTypeId))
			properties.remove(propertyTypeId);
	}

	public void setTextTemplatePath(String dirPath) {
		this.textTemplatePath = dirPath;
	}
	
	public String getTextTemplatePath() {
		return this.textTemplatePath;
	}

}
