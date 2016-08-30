/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - Initial API and implementation
 *     Daniel Murygin <dm{a}sernet{dot}de> - Refactoring
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;

/**
 * This command loads entities by the database ids of the entities.
 * Together with the entities all properties of the entity are loaded.
 *
 * Hibernate configuration of an entity: Entity.hbm.xml
 *
 * @author Alexander Koderman <ak[at]sernet[dot]de>
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LoadEntitiesByIds extends GenericCommand {

    private static final long serialVersionUID = -8570078754663705003L;

    /**
     * HQL query to load the entities. The entity and the properties
     * are loaded by a single statement with joins.
     */
    private static final String HQL_QUERY = "select distinct entity from Entity entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where entity.dbId in (:dbIds)"; //$NON-NLS-1$
	
	private final Collection<Integer> entityIds;
    private List<Entity> entities;

	/**
	 * @param entityIds Database ids of entities
	 */
	public LoadEntitiesByIds(Collection<Integer> entityIds) {
	    if(entityIds==null) {
	        this.entityIds = Collections.emptyList();
	    } else {
	        this.entityIds = entityIds;
	    }
	}

	@Override
	public void execute() {		
		if(!entityIds.isEmpty()) {
		    loadEntities();	
		} else {
		    entities = Collections.emptyList();
		}		
	}

    @SuppressWarnings("unchecked")
    private void loadEntities() {
        IBaseDao<Entity, Serializable> dao = getDaoFactory().getDAO(Entity.class);
        entities = dao.findByCallback(new HibernateCallback() {
            @Override	    
        	public Object doInHibernate( Session session) throws HibernateException, SQLException {
        		Query query = session.createQuery(HQL_QUERY)
        				.setParameterList("dbIds", entityIds);
        		query.setReadOnly(true);
        		return query.list();
        	}
        });
    }

	public List<Entity> getEntities() {
		return entities;
	}

}
