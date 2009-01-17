package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

/**
 * Create and save new element of type type to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class CreateLink<T extends CnALink, U extends CnATreeElement, V extends CnATreeElement> extends GenericCommand {

	private U dragged;
	private V target;
	private CnALink link;

	public CreateLink(V target, U dragged) {
		this.target = target;
		this.dragged = dragged;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao 
			= (IBaseDao<T, Serializable>) getDaoFactory().getDAO(CnALink.class);
		
		IBaseDao<U, Serializable> dao2 
		= (IBaseDao<U, Serializable>) getDaoFactory().getDAO(dragged.getClass());

		IBaseDao<V, Serializable> dao3 
		= (IBaseDao<V, Serializable>) getDaoFactory().getDAO(target.getClass());
		
		dragged = dao2.merge(dragged);
		target = dao3.merge(target);
		
		link = new CnALink(target, dragged);
		// make sure parent object is loaded for tree display:
		link.getParent().getParent();
	}

	public CnALink getLink() {
		return link;
	}


}
