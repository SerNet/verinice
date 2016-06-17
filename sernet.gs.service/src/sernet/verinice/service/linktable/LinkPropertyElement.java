/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable;

import java.util.*;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LinkPropertyElement extends PropertyElement<Edge> {

    private static final Logger LOG = Logger.getLogger(LinkPropertyElement.class);

    public LinkPropertyElement() {
        super();
        result = new HashMap<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.IPathElement#load(java.lang.Object,
     * sernet.verinice.interfaces.graph.VeriniceGraph)
     */
    @Override
    public void load(Edge link, VeriniceGraph graph) {
        String propertyValue = getPropertyValue(link);
        Map<String, Object> resultMap = new HashMap<>();

        
        String parentId = String.valueOf(link.hashCode());
        
        /*
        String parentId = String.valueOf(link.getSource().getDbId());
        if (Direction.OUTGOING.equals(getDirection())) {
            parentId = String.valueOf(link.getTarget().getDbId());
        }
        */
        
        resultMap.put(parentId, propertyValue);
        if (LOG.isDebugEnabled()) {
            LOG.debug(link + "." + propertyTypeId + " = " + propertyValue + " loaded");
        }
        getResult().put(parentId, resultMap);
    }

    private String getPropertyValue(Edge link) {
        String value = null;
        String propertyTypeId = getTypeId();
        switch (propertyTypeId) {
        case CnaLinkPropertyConstants.TYPE_TITLE:
            value = getTitle(link, getDirection());
            break;
        case CnaLinkPropertyConstants.TYPE_DESCRIPTION:
            value = getDescription(link);
            break;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_C:
            value = getRiskConfidentiality(link);
            break;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_I:
            value = getRiskIntegrity(link);
            break;
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_A:
            value = getRiskAvailability(link);
            break;
        default:
            break;
        }
        return value;
    }

    private static String getTitle(Edge link, Direction direction) {
        String linkType = link.getType();
        HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(linkType);
        if (relation == null) {
            return linkType;
        }
        return (direction.equals(Direction.OUTGOING)) ? relation.getName() : relation.getReversename();
    }
    
    private static String getDescription(Edge link) {
        return link.getDescription();
    }
    
    private static String getRiskConfidentiality(Edge link) {
        return convertToString(link.getRiskConfidentiality());
    }
    
    private static String getRiskIntegrity(Edge link) {
        return convertToString(link.getRiskIntegrity());
    }
    
    private static String getRiskAvailability(Edge link) {
        return convertToString(link.getRiskAvailability());
    }
    
    private static String convertToString(Integer i) {
        String s = "";
        if(i!=null) {
            s = String.valueOf(i);
        }
        return s;
    }

    public static List<String> getAllProperties() {
        ArrayList<String> list = new ArrayList<>();
        list.add(CnaLinkPropertyConstants.TYPE_DESCRIPTION);
        list.add(CnaLinkPropertyConstants.TYPE_RISK_VALUE_A);
        list.add(CnaLinkPropertyConstants.TYPE_RISK_VALUE_C);
        list.add(CnaLinkPropertyConstants.TYPE_RISK_VALUE_I);
        list.add(CnaLinkPropertyConstants.TYPE_TITLE);

        return list;

    }
}
