package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class LoadChildrenForExpansion extends GenericCommand {


	private CnATreeElement parent;

	public LoadChildrenForExpansion(CnATreeElement parent) {
		this.parent = parent;
	}
	
	public void execute() {
		Logger.getLogger(this.getClass()).debug("Loading children for " + parent.getTitel());
		IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOForObject(parent);
		dao.reload(parent, parent.getDbId());
		hydrate(parent);
		
		Set<CnATreeElement> children = parent.getChildren();
		for (CnATreeElement child : children) {
			hydrate(child);
		}
	}

	private void hydrate(CnATreeElement element) {
		if (element == null)
			return;
		
		if (element instanceof MassnahmenUmsetzung) {
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) element;
			mn.getKapitelValue();
			mn.getTitel();
			mn.getUmsetzung();
			return;
		}
		
		
		HydratorUtil.hydrateElement(getDaoFactory().getDAOForObject(element), 
				element, true);

		if (element instanceof BausteinUmsetzung) {
			BausteinUmsetzung bst = (BausteinUmsetzung) element;
			bst.getKapitel();
			
			Set<CnATreeElement> massnahmen = bst.getChildren();
			for (CnATreeElement massnahme : massnahmen) {
				hydrate(massnahme);
			}
		}
		
		if (element.getLinksDown().size() > 0 ) {
			Set<CnALink> links = element.getLinks().getChildren();
			for (CnALink link : links) {
				link.getTitle();
				link.getDependency().getUuid();
			}
		}
	}
	

	public CnATreeElement getElementWithChildren() {
		return parent;
	}

	

}
