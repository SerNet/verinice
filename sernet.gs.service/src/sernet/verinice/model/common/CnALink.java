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
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class CnALink implements Serializable, ITypedElement {
    
    private static final InheritLogger LOG_INHERIT = InheritLogger.getLogger(CnATreeElement.class);
    
    // constants for link typeId, now replaced by relationIDs that can be defined in SNCA.xml.
    // these can still be used to differentiate link categories such as "system links" that should never be displayed to the end user
    // which is why we keep the typeId field for now.
	public static final int DEPENDANT_ON      	= 1;
	public static final int ADMINISTRATED_BY 	= 2;
//	public static final int USED_BY 			= 3;
	public static final int LOCATED_IN 		= 4;
	
	// ID for "typed element" interface, specifies this object as a link to get the correct DAO etc.
	public static final String TYPE_ID = "cnalink";
	
	// user entered comment:
	private String comment;
	
	/**
	 * Takes an object and a link and gives back the correct display name for the object on the *other* side
	 * of the link. 
	 * Which side is the other side, is determined on whether the link goes from or to the object.
	 * 
	 * @param link
	 */
	public static String getRelationObjectTitle(CnATreeElement inputElmt, CnALink link) {
	    CnATreeElement element = null;
		if (CnALink.isDownwardLink(inputElmt, link)) {
		    element = link.getDependency();
		} else {
		    element = link.getDependant();
		}
		StringBuilder sb = new StringBuilder();
		if(element instanceof IISO27kElement) {
            String abbreviation = ((IISO27kElement)element).getAbbreviation();
            if(abbreviation!=null && !abbreviation.isEmpty()) {
                sb.append(abbreviation).append(" ");
            }
        }
		String title = element.getTitle();
        if(title!=null && !title.isEmpty()) {
            sb.append(title);
        }
        return sb.toString();
	}
	
	 /**
	  * Get linked elements of specified type.
	  * 
     * @param scenario
     * @param typeId
     * @returns map of linked elements and the corresponding link
     */
    public static Map<CnATreeElement, CnALink> getLinkedElements(CnATreeElement elmt, String typeId) {
        HashMap<CnATreeElement, CnALink> result = new HashMap<CnATreeElement, CnALink>();
        
        Set<CnALink> linksDown = elmt.getLinksDown();
        for (CnALink cnALink : linksDown) {
            if (cnALink.getDependency().getTypeId().equals(typeId)){
                result.put(cnALink.getDependency(), cnALink);
            }
        }
        
        Set<CnALink> linksUp = elmt.getLinksUp();
        for (CnALink cnALink : linksUp) {
            if (cnALink.getDependant().getTypeId().equals(typeId)){
                result.put(cnALink.getDependant(), cnALink);
            }
        }
        return result;
    }
	
	/**
     * Takes an object and a link and gives back the object on the *other* side
     * of the link. 
     * Which side is the other side, is determined on whether the link goes from or to the object.
     * 
     * @param link
     */
    
	public CnATreeElement getRelationObject(CnATreeElement inputElmt, CnALink link) {
        if (CnALink.isDownwardLink(inputElmt, link)){
            return link.getDependency();
        } else {
            return link.getDependant();
        }
    }
	
	/**
	 * Returns the correct title for a link for e given element and a given link.
	 * The correct name is determined using the direction in which the link points to the object.
	 * 
	 * @param link
	 * @return
	 */
	public static String getRelationName(CnATreeElement fromElement, CnALink link) {
		HuiRelation relation = getTypeFactory().getRelation(link.getRelationId());
		String name;
		if (relation == null) {
			name = ""; //$NON-NLS-1$
		} else {
			name = isDownwardLink(fromElement, link) ? relation.getName() : relation.getReversename();
		}
		return name;
	}
	
	public static String getRelationNameReplacingEmptyNames(CnATreeElement fromElement, CnALink link) {
        HuiRelation relation = getTypeFactory().getRelation(link.getRelationId());
        String name;
        if (relation == null) {
            name = isDownwardLink(fromElement, link) ? Messages.getString("CnALink.0") : Messages.getString("CnALink.5"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            name = isDownwardLink(fromElement, link) ? relation.getName() : relation.getReversename();
        }
        return name;
    }
	
	private static HUITypeFactory getTypeFactory() {
		return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
	}
	
	/**
	 * @param link
	 * @return
	 */
	public static boolean isDownwardLink(CnATreeElement fromElement, CnALink link) {
		return fromElement.getLinksDown().contains(link);
	}
	
	public String getRelationId() {
		String typeId = "";
		if(getId()!=null && !Id.NO_TYPE.equals(getId().typeId)) {
			typeId = getId().typeId;
		}
		return typeId;
	}

	public String getComment() {
		return comment;
	}

	public static class Id implements Serializable {
		
		public static final String NO_TYPE = "NO_TYPE";
		
		private Integer dependantId;
		private Integer dependencyId;
		private String typeId;
		
		public Id() {}
		
		public Id(Integer dependantId, Integer dependencyId) {
			this(dependantId, dependencyId, ""); //$NON-NLS-1$
		}

		public Id(Integer dependantId, Integer dependencyId, String relationId) {
			this.dependantId = dependantId;
			this.dependencyId = dependencyId;
			if(relationId==null || relationId.isEmpty()) {
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

        public boolean equals(Object o) {
			if (o instanceof Id) {
				Id that = (Id)o;
				return this.dependantId.equals(that.dependantId)
					&& this.dependencyId.equals(that.dependencyId)
					&& this.typeId.equals(that.typeId);
			} 
			else {
				return false;
			}
		}
		
		public int hashCode() {
			if (dependantId == null || dependencyId == null || typeId == null){	
					return super.hashCode();
			}
			return dependantId.hashCode() + dependencyId.hashCode() + typeId.hashCode();
		}
		
	}

	private Id id;
	
	// link type category as defined by integer constant (see above). This is not the relationId specified in SNCA.xml!
	private int linkType =0;
	
	private CnATreeElement dependant;
	private CnATreeElement dependency;
    
	// links can carry risk values to be used in risk assessments:
	private Integer riskConfidentiality;
    private Integer riskIntegrity;
    private Integer riskAvailability;
	
	protected CnALink() {}
	
	

    public CnALink(CnATreeElement dependant, CnATreeElement dependency, String relationId, String comment) {
		// set linked items:
		this.dependant = dependant;
		this.dependency = dependency;
		this.comment = comment;
		
		// set IDs:
		getId().dependantId = dependant.getDbId();
		getId().dependencyId = dependency.getDbId();
		if(relationId==null || relationId.isEmpty()) {
			getId().typeId = Id.NO_TYPE;
		} else {
			getId().typeId = relationId;
		}
		
	
		// maintain bi-directional association:
		dependency.addLinkUp(this);
		dependant.addLinkDown(this);
		this.linkType = linkTypeFor(dependency);
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	protected void setLinkType(int linkType) {
		this.linkType = linkType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj instanceof CnALink) {
			CnALink that = (CnALink) obj;
			return this.getId().equals(that.getId());
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
	
	public void remove() {	    
	    if(LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("remove()...");
        }
	    
		dependant.removeLinkDown(this);
		dependency.removeLinkUp(this);
		
        if(dependency.isSchutzbedarfProvider()) {
            dependency.fireIntegritaetChanged(new CascadingTransaction());
            dependency.fireVerfuegbarkeitChanged(new CascadingTransaction());
            dependency.fireVertraulichkeitChanged(new CascadingTransaction());
        }

	}

	private int linkTypeFor(CnATreeElement target) {
		if (target instanceof Person){
			return ADMINISTRATED_BY;
		}
		if (target instanceof Raum || target instanceof Gebaeude){
			return LOCATED_IN;
		}
		return DEPENDANT_ON;
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

	public synchronized Id getId() {
		 if (this.id == null){
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
		}
		return ""; //$NON-NLS-1$
	}

    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }

    /**
     * @return the riskConfidentiality
     */
    public Integer getRiskConfidentiality() {
        return riskConfidentiality;
    }

    /**
     * @param riskConfidentiality the riskConfidentiality to set
     */
    public void setRiskConfidentiality(Integer riskConfidentiality) {
        this.riskConfidentiality = riskConfidentiality;
    }

    /**
     * @return the riskIntegrity
     */
    public Integer getRiskIntegrity() {
        return riskIntegrity;
    }

    /**
     * @param riskIntegrity the riskIntegrity to set
     */
    public void setRiskIntegrity(Integer riskIntegrity) {
        this.riskIntegrity = riskIntegrity;
    }

    /**
     * @return the riskAvailability
     */
    public Integer getRiskAvailability() {
        return riskAvailability;
    }

    /**
     * @param riskAvailability the riskAvailability to set
     */
    public void setRiskAvailability(Integer riskAvailability) {
        this.riskAvailability = riskAvailability;
    }

 
   

	
}
