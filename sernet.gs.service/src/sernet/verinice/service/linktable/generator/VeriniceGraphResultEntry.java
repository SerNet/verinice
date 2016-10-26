/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable.generator;

import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_DESCRIPTION;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_TREATMENT;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_A;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_A_WITH_CONTROLS;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_C;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_C_WITH_CONTROLS;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_I;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_RISK_VALUE_I_WITH_CONTROLS;
import static sernet.verinice.service.linktable.CnaLinkPropertyConstants.TYPE_TITLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.service.linktable.IPropertyAdapter;
import sernet.verinice.service.linktable.PropertyAdapterFactory;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge.EdgeType;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

/**
 * Represents a matched node.
 *
 * It stores a VqlNode und {@link VqlEdge} together with the
 * {@link CnATreeElement}. So all information, which property of the
 * {@link CnATreeElement} is relevant is stored together and can be retrieved by
 * {@link #getEntries(Map)}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
final class VeriniceGraphResultEntry {

    private Edge edge;
    private CnATreeElement element;
    private VqlNode vqlNode;
    private VqlEdge vqlEdge;
    private int depth;

    private enum Direction {
        INCOMING, OUTCOMING
    }

    private static final Logger LOG = Logger.getLogger(VeriniceGraphResultEntry.class);

    // all rows this object is stored in.
    private List<VeriniceGraphResultRow> rows = new ArrayList<>();

    VeriniceGraphResultEntry(VqlNode node, VqlEdge vqlEdge, Edge edge, CnATreeElement element, int depth) {
        this.vqlEdge = vqlEdge;
        this.edge = edge;
        this.element = element;
        this.vqlNode = node;
        this.depth = depth;
    }

    String getColumnKey() {
        return vqlNode.getPath();
    }

    Map<String, String> getEntries(Map<String, String> row) {
        writeNodeToRow(row);
        writeEdgeToRow(row);
        return row;
    }

    private void writeNodeToRow(Map<String, String> row) {
        for (String propertyType : vqlNode.getPropertyTypes()) {
            IPropertyAdapter adapter = PropertyAdapterFactory.getAdapter(element);
            String propertyValue = adapter.getPropertyValue(propertyType);
            String keyInRow = vqlNode.getPathForProperty(propertyType);
            row.put(keyInRow, propertyValue);
            LOG.debug("expand final row entry: " + keyInRow + " -> " + propertyValue);
        }
    }
    
    private void writeEdgeToRow(Map<String, String> row) {
        if (vqlEdge != null) {
            for (String propertyType : vqlEdge.getPropertyTypes()) {
                String pathforProperty = vqlEdge.getPathforProperty(propertyType);
                String column = printColumn(edge, propertyType);
                row.put(pathforProperty, column);
            }
        }
    }


    private String printColumn(Edge edge, String propertyType) {
        
        String column = "";
        
        if (TYPE_RISK_VALUE_C.equals(propertyType)) {
            column = getString(edge.getRiskConfidentiality());
        }
        if (TYPE_RISK_VALUE_I.equals(propertyType)) {
            column = getString(edge.getRiskIntegrity());
        }
        if (TYPE_RISK_VALUE_A.equals(propertyType)) {
            column = getString(edge.getRiskAvailability());
        }
        if (TYPE_RISK_VALUE_C_WITH_CONTROLS.equals(propertyType)) {
            column = getString(edge.getRiskConfidentialityWithControls());
        }
        if (TYPE_RISK_VALUE_I_WITH_CONTROLS.equals(propertyType)) {
            column = getString(edge.getRiskIntegrityWithControls());
        }
        if (TYPE_RISK_VALUE_A_WITH_CONTROLS.equals(propertyType)) {
            column = getString(edge.getRiskAvailabilityWithControls());
        }
        if (TYPE_RISK_TREATMENT.equals(propertyType)) {
            if (edge.getRiskTreatment() != null) {
                column = CnALink.riskTreatmentLabels.get(edge.getRiskTreatment().name());
            } else if (isAssetAndSzenario(edge.getSource(), edge.getTarget())) {
                column = CnALink.riskTreatmentLabels.get(CnALink.RiskTreatment.UNEDITED.name());
            }
        }
        if (TYPE_DESCRIPTION.equals(propertyType)) {
            column = edge.getDescription();
        }
        if (TYPE_TITLE.equals(propertyType)) {
            column = getEdgeTitle(this.element, edge);
        }
        return column;
    }

    private String getString(Integer value) {
        if (value == null) {
            return "";
        } else {
            return String.valueOf(value);
        }
    }

    private String getEdgeTitle(CnATreeElement node, Edge edge) {
        Direction direction = getDirection(edge, node);
        return getEdgeTitle(edge, direction);
    }

    static boolean isAssetAndSzenario(CnATreeElement dependant, CnATreeElement dependency) {
        try {
            return (Asset.TYPE_ID.equals(dependant.getTypeId()) && IncidentScenario.TYPE_ID.equals(dependency.getTypeId())) 
                    || (Asset.TYPE_ID.equals(dependency.getTypeId()) && IncidentScenario.TYPE_ID.equals(dependant.getTypeId()));
        } catch (Exception e) {
            LOG.error("Error while checking link.", e); //$NON-NLS-1$
            return false;
        }
    }

    private VeriniceGraphResultEntry.Direction getDirection(Edge link, CnATreeElement node) {
        if (link.getSource() == node) {
            return Direction.INCOMING;
        }

        return Direction.OUTCOMING;
    }

    private String getEdgeTitle(Edge link, VeriniceGraphResultEntry.Direction direction) {
        String linkType = link.getType();
        HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(linkType);
        if (relation == null) {
            return linkType;
        }
        return direction.equals(Direction.OUTCOMING) ? relation.getName() : relation.getReversename();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("VeriniceGraphResultEntry [vqlNode=");
        stringBuilder.append(vqlNode);
        stringBuilder.append(", element=");
        stringBuilder.append(element);
        stringBuilder.append(", depth=");
        stringBuilder.append(depth);
        stringBuilder.append(" , vqlEdge=");
        stringBuilder.append(vqlEdge);
        stringBuilder.append(", edge=");
        stringBuilder.append(edge);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((edge == null) ? 0 : edge.hashCode());
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result + ((vqlEdge == null) ? 0 : vqlEdge.hashCode());
        result = prime * result + ((vqlNode == null) ? 0 : vqlNode.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VeriniceGraphResultEntry other = (VeriniceGraphResultEntry) obj;
        if (edge == null) {
            if (other.edge != null)
                return false;
        } else if (!edge.equals(other.edge))
            return false;
        if (element == null) {
            if (other.element != null)
                return false;
        } else if (!element.equals(other.element))
            return false;
        if (vqlEdge == null) {
            if (other.vqlEdge != null)
                return false;
        } else if (!vqlEdge.equals(other.vqlEdge))
            return false;
        if (vqlNode == null) {
            if (other.vqlNode != null)
                return false;
        } else if (!vqlNode.equals(other.vqlNode))
            return false;
        return true;
    }

    void add(VeriniceGraphResultRow row) {
        this.rows.add(row);
    }

    List<VeriniceGraphResultRow> getRows() {
        return this.rows;
    }

    public int getDepth() {
        return depth;
    }

    public CnATreeElement getCnaTreeElement() {
        return element;
    }

    public boolean isParentRelation() {
        return vqlEdge != null ? vqlEdge.getEdgeType() == EdgeType.PARENT : false;
    }
}