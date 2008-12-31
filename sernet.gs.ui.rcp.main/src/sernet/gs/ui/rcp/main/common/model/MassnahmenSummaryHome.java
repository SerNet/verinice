package sernet.gs.ui.rcp.main.common.model;

import java.util.Map;

import javax.management.NotCompliantMBeanException;

import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedLayerSummary;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedStepsSummary;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedZyklusSummary;
import sernet.gs.ui.rcp.main.service.statscommands.IncompleteStepsSummary;
import sernet.gs.ui.rcp.main.service.statscommands.IncompleteZyklusSummary;
import sernet.gs.ui.rcp.main.service.statscommands.LayerSummary;
import sernet.gs.ui.rcp.main.service.statscommands.UmsetzungSummary;

public class MassnahmenSummaryHome {

	public Map<String, Integer> getNotCompletedZyklusSummary() {
		IncompleteZyklusSummary command = new IncompleteZyklusSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedZyklusSummary() {
		CompletedZyklusSummary command = new CompletedZyklusSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getSchichtenSummary() {
		LayerSummary command = new LayerSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedSchichtenSummary() {
		CompletedLayerSummary command = new CompletedLayerSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getNotCompletedStufenSummary() {
		IncompleteStepsSummary command = new IncompleteStepsSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedStufenSummary() {
		CompletedStepsSummary command = new CompletedStepsSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getUmsetzungenSummary() {
		UmsetzungSummary command = new UmsetzungSummary();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

}
