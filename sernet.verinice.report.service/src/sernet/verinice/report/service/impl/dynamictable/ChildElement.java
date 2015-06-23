/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.verinice.report.service.impl.dynamictable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Path element in a column path definition which loads the children of an element.
 * Delimiter for this path element is: IPathElement.DELIMITER_CHILD (>)
 * See GenericDataModel for a description of column path definitions.
 * 
 * @see GenericDataModel
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ChildElement extends BaseElement {

    private static final Logger LOG = Logger.getLogger(ChildElement.class);

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#load(sernet.verinice.model.common.CnATreeElement, sernet.verinice.interfaces.graph.VeriniceGraph)
     */
    @Override
    public void load(CnATreeElement parent, VeriniceGraph graph) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading children of " + parent.getTitle() + "...");
        }
        String parentId = String.valueOf(parent.getDbId());
        Map<String, Object> result = new HashMap<String, Object>();
        Set<CnATreeElement> elements = graph.getChildren(parent);
        for (CnATreeElement element : elements) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(element.getTitle() + "  loaded");
            }
            getChild().load(element,graph);
            String id = String.valueOf(element.getDbId());
            result.put(id, getChild().getResult());
        }
        getResult().put(parentId, result);
        
    }

    
    
}
