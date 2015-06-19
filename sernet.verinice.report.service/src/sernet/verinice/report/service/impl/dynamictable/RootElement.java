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
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RootElement extends BaseElement {
    
    private static final Logger LOG = Logger.getLogger(RootElement.class);
    
    public RootElement(String typeId) {
        super(typeId);
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#load(sernet.verinice.interfaces.graph.VeriniceGraph)
     */
    @Override
    public void load(CnATreeElement parent, VeriniceGraph graph) {
        Set<CnATreeElement> elements = graph.getElements(getElementTypeId()); 
        Map<String, Object> result = new HashMap<String, Object>();
        for (CnATreeElement element : elements) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(element.getTitle() + "  loaded");
            }
            getChild().load(element,graph);
            String id = String.valueOf(element.getDbId());
            result.put(id, getChild().getResult());
        }
        getResult().put("ROOT", result);       
    }

}
