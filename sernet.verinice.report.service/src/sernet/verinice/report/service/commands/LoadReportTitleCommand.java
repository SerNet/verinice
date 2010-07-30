package sernet.verinice.report.service.commands;

import sernet.verinice.interfaces.GenericCommand;

/**
 * Loads and returns the report's title.
 * 
 * TODO: Somehow allow setting and storing the report's title in the application and then
 * provide access to that value via this command.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class LoadReportTitleCommand extends GenericCommand {

	public String getResult() {
		return "<h1>Information Technologie (IT)</h1><h1>Security Assessment at VW TEST - Company 1</h1><h1>Final Report</h1>";
	}

	@Override
	public void execute() {
		// TODO: Implement me.
	}

}
