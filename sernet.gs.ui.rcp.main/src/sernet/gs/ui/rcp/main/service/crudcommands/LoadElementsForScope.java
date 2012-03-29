package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sun.xml.messaging.saaj.util.LogDomainConstants;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
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
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

/**
 * Load all elements of given type in same scope as a given element.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadElementsForScope extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadElementsForScope.class);
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(LoadElementsForScope.class);
        }
        return log;
    }

	private String typeId;
    private Integer anyElement;
    private ArrayList<CnATreeElement> elements;
    
    public LoadElementsForScope(String typeId, Integer anyElement) {
	    this.typeId = typeId;
	    this.anyElement = anyElement;
	}
	
	public void execute() {
	    getLog().debug("LoadElements for root_object " + anyElement);
	    
	    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {anyElement});
	    try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
	    CnATreeElement elmtInScope = command.getElements().get(0);
	    CnATreeElement root = findRoot(elmtInScope);

	    //if typeId is that of the root object, just return it itself. else look for children:
	    ArrayList<CnATreeElement> items = new ArrayList<CnATreeElement>();
	    if (this.typeId.equals(root.getTypeId())) {
	        this.elements = items;
	        this.elements.add(root);
	    }
	    else {
	        getElements(typeId, items, root);
	        this.elements = items;
	    }
	    
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
     * @param elmtInScope
     * @return
     */
    private CnATreeElement findRoot(CnATreeElement elmtInScope) {
        if (elmtInScope.getTypeId().equals(Organization.TYPE_ID) ||
            elmtInScope.getTypeId().equals(ITVerbund.TYPE_ID))
            return elmtInScope;
        return findRoot(elmtInScope.getParent());
    }

    /**
     * @return the elements
     */
    public List<CnATreeElement> getElements() {
        return elements;
    }

    public void getElements(String typeFilter, List<CnATreeElement> items, CnATreeElement parent) {
        for (CnATreeElement child : parent.getChildren()) {
            if (typeFilter != null && typeFilter.length()>0) {
                if (child.getTypeId().equals(typeFilter)) {
                    items.add(child);
                    child.getParent().getTitle();
                }
            } else {
                items.add(child);
                child.getParent().getTitle();
            }
            getElements(typeFilter, items, child);
        }
        
    }
	


}
