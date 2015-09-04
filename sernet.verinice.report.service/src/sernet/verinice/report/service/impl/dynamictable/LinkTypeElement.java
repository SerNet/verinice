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

import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
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
public class LinkTypeElement implements IPathElement {

    private static final Logger LOG = Logger.getLogger(LinkTypeElement.class);
    
    private String targetTypeId;

    private Map<String,Map<String, Object>> result;
    
    public LinkTypeElement() {
        super();
        result = new HashMap<String,Map<String, Object>>();
    }

    public LinkTypeElement(String targetTypeId) {
        this();
        this.targetTypeId = targetTypeId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#load(sernet.verinice.model.common.CnATreeElement, sernet.verinice.interfaces.graph.VeriniceGraph)
     */
    @Override
    public void load(CnATreeElement parent, VeriniceGraph graph) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading links of " + parent.getTitle() + ", type id: " + getTargetTypeId() + "...");
        }
        String parentId = String.valueOf(parent.getDbId());
        Map<String, Object> result = new HashMap<String, Object>();
        Set<Edge> edgeSet = graph.getEdgesByElementType(parent, getTargetTypeId());
        for (Edge edge : edgeSet) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Edge loaded, type: " + edge.getType());
            }
            CnATreeElement target = getTarget(parent, edge);
            String id = String.valueOf(target.getDbId());
            String label = getLabel(edge.getType(), isDownward(parent, edge));
            result.put(id, label);
        }
        getResult().put(parentId, result);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#createResultMap(java.util.Map, java.lang.String)
     */
    @Override
    public Map<String, String> createResultMap(Map<String, String> map, String dbIds) {
        Set<String> childKeySet = getResult().keySet();
        for (String childKey : childKeySet) {
            if(dbIds==null || dbIds.endsWith(childKey)) {
                Map<String,Object> resultMap = getResult().get(childKey);
                Set<String> resultKeySet = resultMap.keySet();
                for (String resultKey : resultKeySet) {
                    String newKey = (dbIds==null) ? resultKey : dbIds + RESULT_KEY_SEPERATOR + resultKey;
                    String label = (String) resultMap.get(resultKey);
                    map.put(newKey, label);
                }
            }
        }
        return map;
    }

    

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#getResult()
     */
    @Override
    public Map<String, Map<String, Object>> getResult() {
        return result;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#setTypeId(java.lang.String)
     */
    @Override
    public void setTypeId(String typeId) {
        this.targetTypeId = typeId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#getChild()
     */
    @Override
    public IPathElement getChild() {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#setChild(sernet.verinice.report.service.impl.dynamictable.IPathElement)
     */
    @Override
    public void setChild(IPathElement nextElement) {
    }
    
    public String getTargetTypeId() {
        return targetTypeId;
    }
    
    private CnATreeElement getTarget(CnATreeElement source, Edge edge) {
        CnATreeElement edgeSource = edge.getSource();
        CnATreeElement edgeTarget = edge.getTarget();
        return (edgeSource.equals(source)) ? edgeTarget : edgeSource;       
    }
    
    private boolean isDownward(CnATreeElement source, Edge edge) {
        return (edge.getSource().equals(source));       
    }
    
    private String getLabel(String linkType, boolean isDownward) {
        HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(linkType);
        if(relation==null) {
            return linkType;
        }
        return (isDownward) ? relation.getName() : relation.getReversename();
    }

}
