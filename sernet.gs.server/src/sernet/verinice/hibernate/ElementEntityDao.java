/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.hibernate;

import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElementEntity;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.IElementEntityDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.InheritLogger;

/**
 * Special dao for saving the entity of a CnaTreeElement but not the
 * CnaTreeElement itself.
 * 
 * This dao is used when a verinice user is saving an element in
 * the verinice element editor, see command: {@link UpdateElementEntity}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementEntityDao extends TreeElementDao<Entity, Integer> implements IElementEntityDao{

    private static final InheritLogger LOG_INHERIT = InheritLogger.getLogger(ElementEntityDao.class);
    
    public ElementEntityDao() {
        super(Entity.class);
    }

    /**
     * This method is saving the entity of a CnaTreeElement but not the
     * CnaTreeElement itself. 
     * 
     * After saving the entity business impact inheritance is started by
     * calling fireChange.
     * 
     * @see sernet.verinice.interfaces.IElementEntityDao#mergeEntityOfElement(sernet.verinice.model.common.CnATreeElement, boolean)
     */
    public CnATreeElement mergeEntityOfElement(CnATreeElement element, boolean fireChange) {
        if(LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("mergeEntityOfElement");
        }
        
        Entity entity = element.getEntity();
        entity = super.merge(entity);
        element.setEntity(entity);

        if(!getHibernateTemplate().contains(element)) {  
            getHibernateTemplate().load(element, element.getDbId());
        }
        
        if (fireChange) {
            fireChange(element);
        }

        return element;
    }
    
}
