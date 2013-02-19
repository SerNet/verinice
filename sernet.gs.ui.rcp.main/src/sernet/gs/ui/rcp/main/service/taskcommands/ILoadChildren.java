package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.List;

import sernet.verinice.interfaces.ICommand;
import sernet.verinice.model.common.CnATreeElement;

public interface ILoadChildren extends ICommand{

	void execute();

	List<CnATreeElement> getGebaeudeList();

	List<CnATreeElement> getRaumList();

	List<CnATreeElement> getClienteList();

	List<CnATreeElement> getServerList();

	List<CnATreeElement> getNetzList();

	List<CnATreeElement> getAnwendungList();

	List<CnATreeElement> getPersonList();
	
	List<CnATreeElement> getTkKomponenteList();

    List<CnATreeElement> getSonstItList();

	void setId(Integer id);

}