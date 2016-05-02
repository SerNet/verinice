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
package sernet.verinice.service.linktable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Path element in a column path definition which loads the types
 * of the links of an element.
 * Delimiter for this path element is: IPathElement.DELIMITER_LINK_TYPE (:)
 * See GenericDataModel for a description of column path definitions.
 *
 * @see GenericDataModel
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkTypeElement extends BaseElement<CnATreeElement,Edge> {

    private static final Logger LOG = Logger.getLogger(LinkTypeElement.class);

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#load(sernet.verinice.model.common.CnATreeElement, sernet.verinice.interfaces.graph.VeriniceGraph)
     */
    @Override
    public void load(CnATreeElement parent, VeriniceGraph graph) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading links of " + parent.getTitle() + ", type id: " + getElementTypeId() + "...");
        }
        String parentId = String.valueOf(parent.getDbId());
        Map<String, Object> result = new HashMap<>();
        Set<Edge> edgeSet = graph.getEdgesByElementType(parent, getElementTypeId());
        defineDirection(parent, edgeSet);
        for (Edge edge : edgeSet) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Edge loaded, type: " + edge.getType());
            }
            /*
            CnATreeElement target = getTarget(parent, edge);
            String id = String.valueOf(target.getDbId());
            String label = getLabel(edge.getType(), isDownward(parent, edge));
            result.put(id, label);  
            */         
            getChild().setDirection(getDirection());
            getChild().load(edge,graph);
            String id = String.valueOf(edge.getSource().getDbId());
            if(Direction.OUTGOING.equals(getDirection())) {
                id = String.valueOf(edge.getTarget().getDbId());
            }
            result.put(id, getChild().getResult());
  
        }
        getResult().put(parentId, result);
    }

    protected void defineDirection(CnATreeElement element, Set<Edge> edgeSet) {
        if(edgeSet!=null && !edgeSet.isEmpty()) {
            setDirection(isOutgoing(element,edgeSet.iterator().next()) ? Direction.OUTGOING : Direction.INCOMING);
        } else {
            setDirection(null);
        }
    }

    private static boolean isOutgoing(CnATreeElement source, Edge edge) {
        return edge.getSource().equals(source);
    }

    
  

}
