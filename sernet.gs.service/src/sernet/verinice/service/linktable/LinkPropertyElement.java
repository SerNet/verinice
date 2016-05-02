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

import java.util.HashMap;
import java.util.Map;

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
public class LinkPropertyElement extends PropertyElement implements IPathElement<Edge, EndOfPathElement> {

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

        String id = String.valueOf(link.getSource().getDbId());
        if (Direction.OUTGOING.equals(getDirection())) {
            id = String.valueOf(link.getTarget().getDbId());
        }

        resultMap.put(id, propertyValue);
        if (LOG.isDebugEnabled()) {
            LOG.debug(link + "." + propertyTypeId + " = " + propertyValue + " loaded");
        }
        getResult().put(id, resultMap);
    }

    private String getPropertyValue(Edge link) {
        return getLabel(link.getType(), getDirection());
    }

    private static String getLabel(String linkType, Direction direction) {
        HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(linkType);
        if (relation == null) {
            return linkType;
        }
        return (direction.equals(Direction.OUTGOING)) ? relation.getName() : relation.getReversename();
    }
}
