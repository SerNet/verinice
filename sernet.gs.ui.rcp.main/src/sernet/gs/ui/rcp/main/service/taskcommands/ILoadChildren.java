package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.List;

import sernet.verinice.interfaces.ICommand;
import sernet.verinice.model.common.CnATreeElement;

public interface ILoadChildren extends ICommand{

	public abstract void execute();

	public abstract List<CnATreeElement> getGebaeudeList();

	public abstract List<CnATreeElement> getRaumList();

	public abstract List<CnATreeElement> getClienteList();

	public abstract List<CnATreeElement> getServerList();

	public abstract List<CnATreeElement> getNetzList();

	public abstract List<CnATreeElement> getAnwendungList();

	public abstract List<CnATreeElement> getPersonList();

	public abstract void setId(Integer id);

}