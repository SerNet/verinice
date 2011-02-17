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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

@SuppressWarnings("serial")
public class LoadChildrenForExpansion extends GenericCommand {
	
	private static final Logger log = Logger.getLogger(LoadChildrenForExpansion.class);

	private CnATreeElement parent;
	private Integer dbId;
	
	private Set<Class<?>> filteredClasses;

    private String typeId;

	public LoadChildrenForExpansion(CnATreeElement parent) {
		this(parent, new HashSet<Class<?>>());
	}

	public LoadChildrenForExpansion(CnATreeElement parent, Set<Class<?>> filteredClasses) {
		// slim down for transfer:
		dbId = parent.getDbId();
		typeId = parent.getTypeId();
		this.parent = null;
		this.filteredClasses = filteredClasses;
	}
	
	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
		
		RetrieveInfo ri = new RetrieveInfo();
		ri.setChildren(true).setChildrenProperties(true).setProperties(true).setLinksDown(false).setLinksUp(false);
		parent = dao.retrieve(dbId,ri);
		if(parent!=null) {
			hydrate(parent);
			
			if (log.isDebugEnabled() && !filteredClasses.isEmpty())
				log.debug("Skipping the following model classes: " + filteredClasses);
			
			Set<CnATreeElement> children = parent.getChildren();
			for (CnATreeElement child : children) {
				if (!filteredClasses.contains(child.getClass()))
					hydrate(child);
			}
		}
	}

	private void hydrate(CnATreeElement element) {
		if (element == null)
			return;
		
		
		if (element instanceof MassnahmenUmsetzung) {
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) element;
			mn.getKapitelValue();
			mn.getTitle();	
			mn.getUmsetzung();
			mn.getUrl();
			mn.getStand();
			return;
		}
		
		RetrieveInfo ri = null;
		ri = new RetrieveInfo();
		ri.setChildren(true).setLinksDown(false);
		if (element instanceof BausteinUmsetzung) {
			ri.setChildrenProperties(true).setInnerJoin(true);
		}
		
		HydratorUtil.hydrateElement(getDaoFactory().getDAO(element.getTypeId()), element, ri);
		
		// initialize all children:
		if (element instanceof FinishedRiskAnalysis
				|| element instanceof GefaehrdungsUmsetzung) {
			Set<CnATreeElement> children = element.getChildren();
			for (CnATreeElement child : children) {
				hydrate(child);
			}
		}
		
		if (element instanceof BausteinUmsetzung) {
			IBaseDao<? extends BausteinUmsetzung, Serializable> dao = getDaoFactory().getDAO(BausteinUmsetzung.class);
			
			BausteinUmsetzung bst = (BausteinUmsetzung) element;
			bst.getKapitel();
			Set<CnATreeElement> massnahmen = bst.getChildren();
			for (CnATreeElement massnahme : massnahmen) {
				hydrate(massnahme);
			}
		}
		
		/*
		log.debug("Hydrating links down...");
		if (element.getLinksDown().size() > 0 ) {
			Set<CnALink> links = element.getLinks().getChildren();
			for (CnALink link : links) {
				link.getTitle();
				link.getDependency().getUuid();
			}
		}
		log.debug("Links down hydrated");
		*/
		
	}
	
	public CnATreeElement getElementWithChildren() {
		return parent;
	}
}
