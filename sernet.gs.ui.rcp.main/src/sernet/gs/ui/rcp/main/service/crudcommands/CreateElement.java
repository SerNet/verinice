package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
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
public class CreateElement<T extends CnATreeElement> extends GenericCommand {

	private CnATreeElement container;
	private Class<T> type;
	protected T child;

	public CreateElement(CnATreeElement container, Class<T> type) {
		this.container = container;
		this.type = type;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao 
			= (IBaseDao<T, Serializable>) getDaoFactory().getDAO(type);
		try {
			// get constructor with parent-parameter and create new object:
			T child = type.getConstructor(CnATreeElement.class).newInstance(container);
			container.addChild(child);
			this.child = dao.merge(child);
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}

	public T getNewElement() {
		return child;
	}
	
	

}
