package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class LinkKategorie implements Serializable  {
	
	private CnATreeElement parent;

	public LinkKategorie(CnATreeElement parent) {
		this.parent = parent;
	}
	
	private LinkKategorie() {
		
	}
	
	public String getTitle() {
		return "Abhängigkeiten"; //$NON-NLS-1$
	}



	public Set<CnALink> getChildren() {
		return parent.getLinksDown();
	}



	public CnATreeElement getParent() {
		return parent;
	}

}
