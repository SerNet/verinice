package sernet.gs.ui.rcp.main.common.model;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;

/**
 * Association class for links between items.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class CnALink implements Serializable {

	public static final int DEPENDANT_ON      	= 1;
	public static final int ADMINISTRATED_BY 	= 2;
//	public static final int USED_BY 			= 3;
	public static final int LOCATED_IN 		= 4;
	
	
	public static class Id implements Serializable {
		private Integer dependantId;
		private Integer dependencyId;
		
		public Id() {}
		
		public Id(Integer dependantId, Integer dependencyId) {
			this.dependantId = dependantId;
			this.dependencyId = dependencyId;
		}
		
		public boolean equals(Object o) {
			if (o != null && o instanceof Id) {
				Id that = (Id)o;
				return this.dependantId.equals(that.dependantId)
					&& this.dependencyId.equals(that.dependencyId);
			} 
			else {
				return false;
			}
		}
		
		public int hashCode() {
			return dependantId.hashCode() + dependencyId.hashCode();
		}
		
	}

	private Id id;
	
	private int linkType =0;
	
	private CnATreeElement dependant;
	private CnATreeElement dependency;
	
	protected CnALink() {}
	
	public CnALink(CnATreeElement dependant, CnATreeElement dependency) {
		// set linked items:
		this.dependant = dependant;
		this.dependency = dependency;
		
		// set IDs:
		getId().dependantId = dependant.getDbId();
		getId().dependencyId = dependency.getDbId();
	
		// maintain bi-directional association:
		dependency.addLinkUp(this);
		dependant.addLinkDown(this);
		this.linkType = linkTypeFor(dependency);
		
		// update target:
		dependency.getLinkChangeListener().integritaetChanged();
		dependency.getLinkChangeListener().verfuegbarkeitChanged();
		dependency.getLinkChangeListener().vertraulichkeitChanged();
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj != null && obj instanceof CnALink) {
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
		dependant.removeLinkDown(this);
		dependency.removeLinkUp(this);

		// update target:
		dependency.getLinkChangeListener().integritaetChanged();
		dependency.getLinkChangeListener().verfuegbarkeitChanged();
		dependency.getLinkChangeListener().vertraulichkeitChanged();
	}

	private int linkTypeFor(CnATreeElement target) {
		if (target instanceof Person)
			return ADMINISTRATED_BY;
		if (target instanceof Raum || target instanceof Gebaeude)
			return LOCATED_IN;
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
		 if (this.id == null)
			 this.id = new Id();
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public int getLinkType() {
		return linkType;
	}

	public void setLinkType(int linkType) {
		this.linkType = linkType;
	}

	public String getTitle() {
		return typeTitle() + dependency.getTitel();
	}

	private String typeTitle() {
		switch (linkType) {
		case DEPENDANT_ON:
			return "abhängig von: ";
		case ADMINISTRATED_BY:
			return "zuständig: ";
//		case USED_BY:
//			return Messages.CnALink_used;
		case LOCATED_IN:
			return "Standort: "; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	public LinkKategorie getParent() {
		return dependant.getLinks();
	}

	
	
}
