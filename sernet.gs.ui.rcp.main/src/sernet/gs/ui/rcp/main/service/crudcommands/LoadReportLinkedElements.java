package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Load all elements of given type linked to the given root element.
 */
public class LoadReportLinkedElements extends GenericCommand {


	private String typeId;
    private Integer rootElement;
    private List<CnATreeElement> elements;
    
    public LoadReportLinkedElements(String typeId, Integer rootElement) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	}
	
	public void execute() {
	    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
	    try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
        if (command.getElements() == null || command.getElements().size()==0) {
            elements = new ArrayList<CnATreeElement>(0);
            return;
        }
	    CnATreeElement root = command.getElements().get(0);
	    
	    elements = getLinkedElements(root);
	    
	    IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
	    RetrieveInfo ri = new RetrieveInfo();
	    ri.setProperties(true);
//	    ri.setParent(true);
//	    ri.setPermissions(true);
//	    ri.setParentPermissions(true);
//	    ri.setChildren(true);
//	    ri.setChildren(true);
	    HydratorUtil.hydrateElements(dao, elements, ri);

	}

	/**
     * @param root
     * @param typeId2
     * @return
     */
    private List<CnATreeElement> getLinkedElements(CnATreeElement root) {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>();
        for (CnALink link : root.getLinksDown()) {
            if (link.getDependency().getTypeId().equals(this.typeId))
                result.add(link.getDependency());
        }
        for (CnALink link : root.getLinksUp()) {
            if (link.getDependant().getTypeId().equals(this.typeId))
                result.add(link.getDependant());
        }
        return result;
    }

    /**
     * @return the elements
     */
    public List<CnATreeElement> getElements() {
        return elements;
    }

  
	


}
