/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.interfaces.graph;

import java.io.Serializable;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.CnALink.RiskTreatment;

/**
 * An Edge is the representation of a link between to objects in a verinice
 * graph. A link between 2 objects can be a parent child relation or a relation
 * defined in file SNCA.xml by element "huirelation".
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Edge implements Serializable {

    private static final long serialVersionUID = -7300388773596283110L;

    public static final String RELATIVES = "relatives";

    private CnATreeElement source;
    private CnATreeElement target;
    private String type;
    private String description;
    private Integer riskConfidentiality;
    private Integer riskIntegrity;
    private Integer riskAvailability;
    private Integer riskConfidentialityWithControls;
    private Integer riskIntegrityWithControls;
    private Integer riskAvailabilityWithControls;
    private RiskTreatment riskTreatment;

    // empty constructor for JGraphT class based edge factory
    public Edge() {

    }

    public Edge(CnATreeElement parent, CnATreeElement child) {
        this(parent, child, RELATIVES);
    }

    public Edge(CnATreeElement source, CnATreeElement target, String type) {
        super();
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public CnATreeElement getSource() {
        return source;
    }

    public void setSource(CnATreeElement source) {
        this.source = source;
    }

    public CnATreeElement getTarget() {
        return target;
    }

    public void setTarget(CnATreeElement target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRiskConfidentiality() {
        return riskConfidentiality;
    }

    public void setRiskConfidentiality(Integer riskValueC) {
        this.riskConfidentiality = riskValueC;
    }

    public Integer getRiskIntegrity() {
        return riskIntegrity;
    }

    public void setRiskIntegrity(Integer riskValueI) {
        this.riskIntegrity = riskValueI;
    }

    public Integer getRiskAvailability() {
        return riskAvailability;
    }

    public void setRiskAvailability(Integer integer) {
        this.riskAvailability = integer;
    }

    public Integer getRiskConfidentialityWithControls() {
        return riskConfidentialityWithControls;
    }

    public void setRiskConfidentialityWithControls(Integer riskConfidentialityWithControls) {
        this.riskConfidentialityWithControls = riskConfidentialityWithControls;
    }

    public Integer getRiskIntegrityWithControls() {
        return riskIntegrityWithControls;
    }

    public void setRiskIntegrityWithControls(Integer riskIntegrityWithControls) {
        this.riskIntegrityWithControls = riskIntegrityWithControls;
    }

    public Integer getRiskAvailabilityWithControls() {
        return riskAvailabilityWithControls;
    }

    public void setRiskAvailabilityWithControls(Integer riskAvailabilityWithControls) {
        this.riskAvailabilityWithControls = riskAvailabilityWithControls;
    }

    public RiskTreatment getRiskTreatment() {
        return riskTreatment;
    }

    public void setRiskTreatment(RiskTreatment riskTreatment) {
        this.riskTreatment = riskTreatment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Edge " + source + "->" + target + " (" + type + ")";
    }
}
