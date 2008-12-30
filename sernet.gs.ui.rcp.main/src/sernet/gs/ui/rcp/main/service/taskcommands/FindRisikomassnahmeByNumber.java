package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.List;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class FindRisikomassnahmeByNumber extends GenericCommand {

	private String number;

	private RisikoMassnahme massnahme;
	
	private static final String QUERY_FIND_BY_ID = "from "
		+ RisikoMassnahme.class.getName() + " as element "
		+ "where element.number = ?";

	public FindRisikomassnahmeByNumber(String number) {
		this.number = number;
	}

	public void execute() {
		List list = getDaoFactory().getDAO(RisikoMassnahme.class).findByQuery(QUERY_FIND_BY_ID, 
				new String[] {number});
		for (Object object : list) {
			this.massnahme = (RisikoMassnahme) object;
		}
	}

	public RisikoMassnahme getMassnahme() {
		return massnahme;
	}

}
