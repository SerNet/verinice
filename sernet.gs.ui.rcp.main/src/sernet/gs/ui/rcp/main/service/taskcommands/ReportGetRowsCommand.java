package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.ICnaItemRow;
import sernet.gs.ui.rcp.main.reports.PropertiesRow;
import sernet.gs.ui.rcp.main.reports.PropertySelection;
import sernet.gs.ui.rcp.main.reports.Report;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.office.IOOTableRow;

public class ReportGetRowsCommand extends GenericCommand {

	private IBSIReport report;
	private PropertySelection shownPropertyTypes;
	private ArrayList<IOOTableRow> rows;


	public ReportGetRowsCommand(IBSIReport report,
			PropertySelection shownPropertyTypes) {
		this.report = report;
		this.shownPropertyTypes = shownPropertyTypes;
	}

	public void execute() {
		try {
			LoadBSIModel command = new LoadBSIModel();
			getCommandService().executeCommand(command);
			BSIModel model = command.getModel();
			((Report)report).setModel(model);
			
			// initialize items: 
			report.getItems();
			
			// convert to report rows:
			rows = report.getReport(shownPropertyTypes);
			
			// hydrate row element:
			for (IOOTableRow row : rows) {
				if (row instanceof ICnaItemRow) {
					CnATreeElement item = ((ICnaItemRow)row).getItem();
					HydratorUtil.hydrateElement(getDaoFactory().getDAOForObject(item), item, false);
				}
			}
			
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		report = null;
		shownPropertyTypes = null;
	}

	public ArrayList<IOOTableRow> getRows() {
		return rows;
	}

	

}
