package sernet.gs.ui.rcp.main.bsi.model;

import java.util.List;
import java.util.Map;

import sernet.gs.ui.rcp.main.bsi.views.chart.MassnahmeCount;

public interface IMassnahmenDAO {

	Map<String, Integer> getUmsetzungenSummary();

	Map<String, Integer> getNotCompletedStufenSummary();

	Map<String, Integer> getCompletedStufenSummary();

	Map<String, Integer> getNotCompletedZyklusSummary();

	Map<String, Integer> getCompletedZyklusSummary();

	Map<String, Integer> getSchichtenSummary();

	Map<String, Integer> getCompletedSchichtenSummary();

}
