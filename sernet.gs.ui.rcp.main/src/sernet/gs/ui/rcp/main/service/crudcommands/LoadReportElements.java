package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * Load elements for reports. All properties  will be initialized to avoid lazy initialization exceptions.
 * Result list will be sorted using <code>NumericStringComparator</code>. 
 * This correctly sorts "M 1.101" *after* "M 1.2" which cannot be accomplished by the comparators normally available in BIRT.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadReportElements extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadReportElements.class);
    
   
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(LoadReportElements.class);
        }
        return log;
    }

	private String typeId;
    private Integer rootElement;
    private ArrayList<CnATreeElement> elements;
    
    private boolean useScopeID = false;
    
    public LoadReportElements(String typeId, Integer rootElement) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	}

    public LoadReportElements(String typeId, Integer rootElement, boolean useScopeID){
        this(typeId, rootElement);
        this.useScopeID = useScopeID;
    }
   
    
	public void execute() {
	    getLog().debug("LoadReportElements for root_object " + rootElement);

	    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
	    try {
	        command = getCommandService().executeCommand(command);
	    } catch (CommandException e) {
	        throw new RuntimeCommandException(e);
	    }
	    if (command.getElements() == null || command.getElements().size()==0) {
	        this.elements = new ArrayList<CnATreeElement>(0);
	        return;
	    }
	    CnATreeElement root = command.getElements().get(0);

	    if(!useScopeID || !hasScopeID(root)){
	        
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


	    } else {
	        elements = new ArrayList<CnATreeElement>(0);
	        if(root instanceof Organization || root instanceof ITVerbund){
	            try {
	                LoadCnAElementByScopeId scopeCommand = new LoadCnAElementByScopeId(rootElement, typeId);
                    scopeCommand = getCommandService().executeCommand(scopeCommand);
                    elements.addAll(scopeCommand.getResults());
                } catch (CommandException e) {
                    log.error("Error while retrieving elements via scopeid", e);
                }
	        }
	    }
	    if(elements != null){
    	    Collections.sort(elements, new Comparator<CnATreeElement>() {
    	        @Override
    	        public int compare(CnATreeElement o1, CnATreeElement o2) {
    	            NumericStringComparator comparator = new NumericStringComparator();
    	            return comparator.compare(o1.getTitle(), o2.getTitle());
    	        }
    	    });
	    }
	}

	
	private boolean hasScopeID(CnATreeElement root){
	    return (root instanceof ITVerbund) || (root instanceof Organization);
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
