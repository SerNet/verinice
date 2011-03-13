package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sun.xml.messaging.saaj.util.LogDomainConstants;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * Load all elements of given type linked to the given root element.
 */
public class LoadReportLinkedElements extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadReportLinkedElements.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadReportLinkedElements.class);
        }
        return log;
    }

    // get these types of elements:
	private String typeId;
	
	// starting element:
    private Integer rootElement;
    
    // the result:
    private List<CnATreeElement> elements;
    
    // recurse down all levels of links to emelents of given type?
    private boolean goDeep = false;
    
    
    public LoadReportLinkedElements(String typeId, Integer rootElement, boolean goDeep) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	    this.goDeep= goDeep;
	}

    public LoadReportLinkedElements(String typeId, Integer rootElement) {
        this(typeId, rootElement, false);
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
	    
	    CascadingTransaction ta = new CascadingTransaction();
	    ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>();
	    
	    elements = getLinkedElements(ta, root, result);
	    
	    
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
	private List<CnATreeElement> getLinkedElements(CascadingTransaction ta, CnATreeElement root, ArrayList<CnATreeElement> result) {
        // FIXME externalize strings in SNCA.xml!
        for (CnALink link : root.getLinksDown()) {
            if (link.getDependency().getTypeId().equals(this.typeId)) {
                if (!ta.hasBeenVisited(link.getDependency())) {
                    try {
                        ta.enter(link.getDependency());
                    } catch (TransactionAbortedException e) {
                        return result;
                    }
                    result.add(link.getDependency());
                    if (goDeep)
                        getLinkedElements(ta, link.getDependency(), result);
                }
            }
        }
        for (CnALink link : root.getLinksUp()) {
            if (link.getDependant().getTypeId().equals(this.typeId)) {
                if (!ta.hasBeenVisited(link.getDependant())) {
                    try {
                        ta.enter(link.getDependant());
                    } catch (TransactionAbortedException e) {
                        return result;
                    }
                    result.add(link.getDependant());
                    if (goDeep)
                        getLinkedElements(ta, link.getDependant(), result);
                }
            }
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Loaded " + result.size() + " assets for process " + this.rootElement);
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
