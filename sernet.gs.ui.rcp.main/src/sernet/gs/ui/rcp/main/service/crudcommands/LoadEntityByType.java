package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Entity;

public class LoadEntityByType extends GenericCommand {
	
	private static final String QUERY = "from Entity entity " +
	"join fetch entity.typedPropertyLists " +
	"where entity.entityType = ?"; //$NON-NLS-1$
	
	private String type;

	private List<Entity> entities;

	public LoadEntityByType(String type) {
		this.type = type;
	}

	public void execute() {
		IBaseDao<Entity, Serializable> dao = getDaoFactory().getDAO(Entity.class);
		entities = dao.findByQuery(QUERY, new String[] {"configuration"});
		for (Entity entity : entities) {
			HydratorUtil.hydrateEntity(dao, entity);
		}
	}

	public List<Entity> getEntities() {
		return entities;
	}
	
	

}
