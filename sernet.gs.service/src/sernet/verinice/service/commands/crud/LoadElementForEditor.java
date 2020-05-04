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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.crud;

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

public class LoadElementForEditor<T extends CnATreeElement> extends GenericCommand {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(LoadElementForEditor.class);

    private T element;
    private Integer dbId;
    private String typeId;

    public LoadElementForEditor(T element) {
        // slim down for transfer:
        dbId = element.getDbId();
        typeId = element.getTypeId();
    }

    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("execute, dbId: " + dbId);
        }
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
        element = (T) dao.findById(dbId);
        HydratorUtil.hydrateElement(dao, element, false);
        Set<CnALink> linksDown = element.getLinksDown();
        for (CnALink cnALink : linksDown) {
            for (PropertyList pl : cnALink.getDependency().getEntity().getTypedPropertyLists()
                    .values()) {
                Hibernate.initialize(pl.getProperties());
            }
        }
        Set<CnALink> linksUp = element.getLinksUp();
        for (CnALink cnALink : linksUp) {
            for (PropertyList pl : cnALink.getDependant().getEntity().getTypedPropertyLists()
                    .values()) {
                Hibernate.initialize(pl.getProperties());
            }
        }
    }

    public T getElement() {
        return element;
    }

}
