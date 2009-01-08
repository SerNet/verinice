package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.SchutzbedarfAdapter;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.Property;

public class ProtectionLevelChanged extends GenericCommand {

	private SchutzbedarfAdapter entityChangedListener;
	private boolean setbyUser;
	private Property prop;

	public ProtectionLevelChanged(SchutzbedarfAdapter entityChangedListener, Property prop, boolean setByUser) {
		this.entityChangedListener = entityChangedListener;
		this.prop = prop;
		this.setbyUser = setByUser;
	}

	public void execute() {
		CnATreeElement parent = entityChangedListener.getParent();
		IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOForObject(parent);
		parent = (CnATreeElement) dao.merge(parent);
		entityChangedListener.setParent(parent);
		
		entityChangedListener.fireSchutzbedarfChanged(prop, setbyUser);
		entityChangedListener.fireSchutzbedarfBegruendungChanged(prop, setbyUser);
	}

}
