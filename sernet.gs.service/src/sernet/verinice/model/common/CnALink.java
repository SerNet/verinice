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
package sernet.verinice.model.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.InheritLogger;

/**
 * Association class for links between items.
 * 
 * This entity class is mapped to database by Hibernate.
 * Check Hibernate configuration in file CnALink.hbm.xml.
 * 
 * @author koderman[at]sernet[dot]de
 */
@SuppressWarnings("serial")
public class CnALink implements Serializable, ITypedElement {

    private static final InheritLogger logInherit = InheritLogger.getLogger(CnALink.class);

    public static final Map<String, String> riskTreatmentLabels;
    static {
        riskTreatmentLabels = new Hashtable<>();
        riskTreatmentLabels.put(CnALink.RiskTreatment.ACCEPT.name(), Messages.getString("CnALink.RiskTreatment_ACCEPT")); //$NON-NLS-1$
        riskTreatmentLabels.put(CnALink.RiskTreatment.AVOID.name(), Messages.getString("CnALink.RiskTreatment_AVOID")); //$NON-NLS-1$
        riskTreatmentLabels.put(CnALink.RiskTreatment.MODIFY.name(), Messages.getString("CnALink.RiskTreatment_MODIFY")); //$NON-NLS-1$
        riskTreatmentLabels.put(CnALink.RiskTreatment.TRANSFER.name(), Messages.getString("CnALink.RiskTreatment_TRANSFER")); //$NON-NLS-1$
        riskTreatmentLabels.put(CnALink.RiskTreatment.UNEDITED.name(), Messages.getString("CnALink.RiskTreatment_UNEDITED")); //$NON-NLS-1$
    }
    
    // constants for link typeId, now replaced by relationIDs that can be
    // defined in SNCA.xml.
    // these can still be used to differentiate link categories such as "system
    // links" that
    // should never be displayed to the end user which is why we keep the typeId
    // field for now.
    public static final int DEPENDANT_ON = 1;
    public static final int ADMINISTRATED_BY = 2;
    public static final int LOCATED_IN = 4;

    // ID for "typed element" interface, specifies this object as a link to get
    // the correct DAO etc.
    public static final String TYPE_ID = "cnalink";

    private Id id;

    // link type category as defined by integer constant (see above). This is
    // not the relationId specified in SNCA.xml!
    private int linkType = 0;

    private CnATreeElement dependant;
    private CnATreeElement dependency;

    // links can carry risk values to be used in risk assessments:
    private Integer riskConfidentiality;
    private Integer riskIntegrity;
    private Integer riskAvailability;
    
    // risk values with controls
    private Integer riskConfidentialityWithControls;
    private Integer riskIntegrityWithControls;
    private Integer riskAvailabilityWithControls;

    public enum RiskTreatment {ACCEPT,TRANSFER,MODIFY,AVOID,UNEDITED};
    private String riskTreatmentValue = null;

    // user entered comment:
    private String comment;

    protected CnALink() {
    }

    public CnALink(CnATreeElement dependant, CnATreeElement dependency, String relationId,
            String comment) {
        // set linked items:
        this.dependant = dependant;
        this.dependency = dependency;
        this.comment = comment;

        // set IDs:
        getId().dependantId = dependant.getDbId();
        getId().dependencyId = dependency.getDbId();
        if (relationId == null || relationId.isEmpty()) {
            getId().typeId = Id.NO_TYPE;
        } else {
            getId().typeId = relationId;
        }

        // maintain bi-directional association:
        dependency.addLinkUp(this);
        dependant.addLinkDown(this);
        this.linkType = linkTypeFor(dependency);
    }

    /**
     * Takes an object / element and a link/relation and gives back the correct display name for
     * the element on the *other* side of the link. Which side is the other side,
     * is determined on whether the link goes from or to the param inputElmt.
     * 
     * @param inputElement Source element of the link
     * @param link The link 
     */
    public static String getRelationObjectTitle(CnATreeElement inputElement, CnALink link) {
        CnATreeElement element;
        if (CnALink.isDownwardLink(inputElement, link)) {
            element = link.getDependency();
        } else {
            element = link.getDependant();
        }
        StringBuilder sb = new StringBuilder();
        if (element instanceof IISO27kElement) {
            String abbreviation = ((IISO27kElement) element).getAbbreviation();
            if (abbreviation != null && !abbreviation.isEmpty()) {
                sb.append(abbreviation).append(" ");
            }
        }
        String title = element.getTitle();
        if (title != null && !title.isEmpty()) {
            sb.append(title);
        }
        return sb.toString();
    }

    /**
     * Returns all elements which are linked to param element of type which the
     * given type id.
     * 
     * @param element A verinice element
     * @param typeId A type id from SNCA.xml
     * @returns map of linked elements and the corresponding link
     */
    public static Map<CnATreeElement, CnALink> getLinkedElements(CnATreeElement element, String typeId) {
        HashMap<CnATreeElement, CnALink> result = new HashMap<>();

        Set<CnALink> linksDown = element.getLinksDown();
        for (CnALink cnALink : linksDown) {
            if (cnALink.getDependency().getTypeId().equals(typeId)) {
                result.put(cnALink.getDependency(), cnALink);
            }
        }

        Set<CnALink> linksUp = element.getLinksUp();
        for (CnALink cnALink : linksUp) {
            if (cnALink.getDependant().getTypeId().equals(typeId)) {
                result.put(cnALink.getDependant(), cnALink);
            }
        }
        return result;
    }

    /**
     * Takes an element / object and a link and gives back the object on the *other* side
     * of the link. Which side is the other side, is determined on whether the
     * link goes from or to the param element.
     * 
     * @param inputElement Source element of the link
     * @param link The link
     * @return The destination element of the link
     */

    public static CnATreeElement getRelationObject(CnATreeElement inputElement, CnALink link) {
        if (CnALink.isDownwardLink(inputElement, link)) {
            return link.getDependency();
        } else {
            return link.getDependant();
        }
    }

    /**
     * Returns the correct title for a link for e given element and a given
     * link. The correct name is determined using the direction in which the
     * link points to the object.
     * 
     * @param inputElement Source element of the link
     * @param link The link
     * @return The name of the link
     */
    public static String getRelationName(CnATreeElement fromElement, CnALink link) {
        HuiRelation relation = getTypeFactory().getRelation(link.getRelationId());
        String name;
        if (relation == null) {
            name = ""; //$NON-NLS-1$
        } else {
            name = isDownwardLink(fromElement, link) ? relation.getName()
                    : relation.getReversename();
        }
        return name;
    }

    public static String getRelationNameReplacingEmptyNames(CnATreeElement fromElement,
            CnALink link) {
        HuiRelation relation = getTypeFactory().getRelation(link.getRelationId());
        String name;
        if (relation == null) {
            name = isDownwardLink(fromElement, link) ? Messages.getString("CnALink.0") //$NON-NLS-1$
                    : Messages.getString("CnALink.5"); //$NON-NLS-1$
        } else {
            name = isDownwardLink(fromElement, link) ? relation.getName()
                    : relation.getReversename();
        }
        return name;
    }
    
    /**
     * @param element An element
     * @param link A link
     * @return True if element is the source of the link
     */
    public static boolean isDownwardLink(CnATreeElement element, CnALink link) {
        return element.getLinksDown().contains(link);
    }

    private static HUITypeFactory getTypeFactory() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }

    public String getRelationId() {
        String typeId = "";
        if (getId() != null && !Id.NO_TYPE.equals(getId().typeId)) {
            typeId = getId().typeId;
        }
        return typeId;
    }

    protected void setLinkType(int linkType) {
        this.linkType = linkType;
    }

    private int linkTypeFor(CnATreeElement target) {
        if (target instanceof Person) {
            return ADMINISTRATED_BY;
        }
        if (target instanceof Raum || target instanceof Gebaeude) {
            return LOCATED_IN;
        }
        return DEPENDANT_ON;
    }

    public synchronized Id getId() {
        if (this.id == null) {
            this.id = new Id();
        }
        return id;
    }

    public synchronized void setId(Id id) {
        this.id = id;
    }

    public int getLinkType() {
        return linkType;
    }

    public String getTitle() {
        return typeTitle() + dependency.getTitle();
    }

    private String typeTitle() {
        switch (linkType) {
        case DEPENDANT_ON:
            return Messages.getString("CnALink.0"); //$NON-NLS-1$
        case ADMINISTRATED_BY:
            return Messages.getString("CnALink.4"); //$NON-NLS-1$
        case LOCATED_IN:
            return Messages.getString("CnALink.1"); //$NON-NLS-1$
        default: 
            return ""; //$NON-NLS-1$
        }    
    }

    /*
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CnALink) {
            CnALink that = (CnALink) obj;
            return this.getId().equals(that.getId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    public void remove() {
        logInherit.debug("remove()...");

        dependant.removeLinkDown(this);
        dependency.removeLinkUp(this);

        if (dependency.isProtectionRequirementsProvider()) {
            dependency.fireIntegritaetChanged(new CascadingTransaction());
            dependency.fireVerfuegbarkeitChanged(new CascadingTransaction());
            dependency.fireVertraulichkeitChanged(new CascadingTransaction());
            dependency.fireValueChanged(new CascadingTransaction());
        }
    }

    public CnATreeElement getDependant() {
        return dependant;
    }

    public void setDependant(CnATreeElement dependant) {
        this.dependant = dependant;
    }

    public CnATreeElement getDependency() {
        return dependency;
    }

    public void setDependency(CnATreeElement dependency) {
        this.dependency = dependency;
    }

    public Integer getRiskConfidentiality() {
        return riskConfidentiality;
    }

    public void setRiskConfidentiality(Integer riskConfidentiality) {
        this.riskConfidentiality = riskConfidentiality;
    }

    public Integer getRiskIntegrity() {
        return riskIntegrity;
    }

    public void setRiskIntegrity(Integer riskIntegrity) {
        this.riskIntegrity = riskIntegrity;
    }

    public Integer getRiskAvailability() {
        return riskAvailability;
    }

    public void setRiskAvailability(Integer riskAvailability) {
        this.riskAvailability = riskAvailability;
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
        String value = getRiskTreatmentValue();
        if (value == null) {
            return null;
        } else {
            return RiskTreatment.valueOf(value);
        }
    }
    
    public void setRiskTreatment(RiskTreatment riskTreatment) {
        if (riskTreatment==null || riskTreatment == RiskTreatment.UNEDITED) {
            setRiskTreatmentValue(null);
        } else {
            setRiskTreatmentValue(riskTreatment.name());
        }
    }
    
    private String getRiskTreatmentValue() {
        return riskTreatmentValue;
    }

    private void setRiskTreatmentValue(String riskTreatmentValue) {
        this.riskTreatmentValue = riskTreatmentValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public static String[] getRiskTreatmentOptions() {
        RiskTreatment[] states = RiskTreatment.values();
        String[] names = new String[states.length];
        for (int i = 0; i < states.length; i++) {
            names[i] = states[i].name();
        }
        return names;
    }

    public static class Id implements Serializable {

        public static final String NO_TYPE = "NO_TYPE";

        private Integer dependantId;
        private Integer dependencyId;
        private String typeId;

        public Id() {
            // nothing to do
        }

        public Id(Integer dependantId, Integer dependencyId) {
            this(dependantId, dependencyId, ""); //$NON-NLS-1$
        }

        // link is generated from dependant to dependency
        // dependant => dependency is downward link
        // dependency => dependant is updward link
        public Id(Integer dependantId, Integer dependencyId, String relationId) {
            this.dependantId = dependantId;
            this.dependencyId = dependencyId;
            if (relationId == null || relationId.isEmpty()) {
                typeId = NO_TYPE;
            } else {
                typeId = relationId;
            }
        }

        public Integer getDependantId() {
            return dependantId;
        }

        public Integer getDependencyId() {
            return dependencyId;
        }

        public String getTypeId() {
            return typeId;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Id) {
                Id that = (Id) o;
                return this.dependantId.equals(that.dependantId)
                        && this.dependencyId.equals(that.dependencyId)
                        && this.typeId.equals(that.typeId);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            if (dependantId == null || dependencyId == null || typeId == null) {
                return super.hashCode();
            }
            return dependantId.hashCode() + dependencyId.hashCode() + typeId.hashCode();
        }

        @Override
        public String toString() {
            return "Id [dependantId=" + dependantId + ", dependencyId=" + dependencyId + ", typeId="
                    + typeId + "]";
        }

    }

}
