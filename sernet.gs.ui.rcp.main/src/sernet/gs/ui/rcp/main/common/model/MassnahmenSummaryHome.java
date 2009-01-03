package sernet.gs.ui.rcp.main.common.model;

import java.util.Map;

import javax.management.NotCompliantMBeanException;

import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedLayerSummary;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedStepsSummary;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedZyklusSummary;
import sernet.gs.ui.rcp.main.service.statscommands.IncompleteStepsSummary;
import sernet.gs.ui.rcp.main.service.statscommands.IncompleteZyklusSummary;
import sernet.gs.ui.rcp.main.service.statscommands.LayerSummary;
import sernet.gs.ui.rcp.main.service.statscommands.UmsetzungSummary;

public class MassnahmenSummaryHome {

	public Map<String, Integer> getNotCompletedZyklusSummary() throws CommandException {
		IncompleteZyklusSummary command = new IncompleteZyklusSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedZyklusSummary() throws CommandException {
		CompletedZyklusSummary command = new CompletedZyklusSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getSchichtenSummary()throws CommandException {
		LayerSummary command = new LayerSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedSchichtenSummary()throws CommandException {
		CompletedLayerSummary command = new CompletedLayerSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getNotCompletedStufenSummary()throws CommandException {
		IncompleteStepsSummary command = new IncompleteStepsSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedStufenSummary() throws CommandException{
		CompletedStepsSummary command = new CompletedStepsSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getUmsetzungenSummary()throws CommandException {
		UmsetzungSummary command = new UmsetzungSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

}
