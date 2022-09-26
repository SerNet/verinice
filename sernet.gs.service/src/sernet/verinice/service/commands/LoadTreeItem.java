/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementFilter;

/**
 * This command is loading items after user opens elements in the verinice
 * TreeViewer Command is called by sernet.verinice.rcp.tree.ElementLoader.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadTreeItem extends GenericCommand {

    private Integer dbId;

    private RetrieveInfo ri;

    private Map<String, Object> parameter;

    private CnATreeElement element;

    /*
     * Key: uuid Value: true if element with uuid has children, false if not
     */
    private Map<Integer, Boolean> hasChildrenMap;

    public LoadTreeItem(Integer dbId, RetrieveInfo ri) {
        this(dbId, ri, (Map<String, Object>) null);
    }

    public LoadTreeItem(Integer dbId, RetrieveInfo ri, Map<String, Object> parameter) {
        super();
        this.dbId = dbId;
        this.ri = ri;
        this.parameter = parameter;
    }

    @Override
    public void execute() {
        IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        // one select with joins specified in RetrieveInfo
        element = dao.retrieve(dbId, ri);
        ElementFilter.filterChildrenOfElement(element, parameter);

        hasChildrenMap = new HashMap<>();
        Set<CnATreeElement> children = element.getChildren();
        hasChildrenMap.put(element.getDbId(), (children != null && !children.isEmpty()));
        if (children != null) {
            for (CnATreeElement child : children) {
                Set<CnATreeElement> grandchildren = child.getChildren();
                // calling grandchildren.size() is starting the hibernate
                // initialization of set grand children
                // if grand children is set to false in RetrieveInfo
                hasChildrenMap.put(child.getDbId(),
                        (grandchildren != null && !grandchildren.isEmpty()));
            }
        }
    }

    /**
     * @return the element
     */
    public CnATreeElement getElement() {
        return element;
    }

    /**
     * Returns a map to determine if element have children. Map contains one
     * entry per child of element.
     * 
     * Key of the map: id Value of the map:: true if element with id has
     * children, false if not
     * 
     * @return the hasChildrenMap
     */
    public Map<Integer, Boolean> getHasChildrenMap() {
        return hasChildrenMap;
    }

}
