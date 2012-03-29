package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads an element with all its children.
 */
public class LoadReportElementWithChildren extends GenericCommand {

	private String filterchildrenByTypeID;
    private Integer rootElement;
    ArrayList<CnATreeElement> result;
    
    public LoadReportElementWithChildren(String filerChildrenByTypeID, Integer rootElement) {
	    this.filterchildrenByTypeID = filerChildrenByTypeID;
	    this.rootElement = rootElement;
	}

    public LoadReportElementWithChildren(String typeId, String rootElement) {
        this.filterchildrenByTypeID = typeId;
        try {
            this.rootElement = Integer.parseInt(rootElement);
        } catch(NumberFormatException e) {
            this.rootElement=-1;
        }
    }
	
	public void execute() {
	    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement}); 
	    try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
        if (command.getElements() == null || command.getElements().size()==0) {
            result = new ArrayList<CnATreeElement>(0);
            return;
        }
	    CnATreeElement root = command.getElements().get(0);
	    
	    loadChildren(root);
	    sortResults();
	}

	/**
     * @param root
     * @param typeId2
     * @return
     */
    private void loadChildren(CnATreeElement root) {
        result = new ArrayList<CnATreeElement>();
        
        if (filterchildrenByTypeID == null)
            result.addAll(root.getChildren());
        else {
            for (CnATreeElement child: root.getChildren()) {
                if (child.getTypeId().equals(filterchildrenByTypeID)) {
                    result.add(child);
                }
            }
        }
    }
    
    private void sortResults(){
    	if(result != null){
    		Collections.sort(result, new Comparator<CnATreeElement>() {
				@Override
				public int compare(CnATreeElement o1, CnATreeElement o2) {
					NumericStringComparator comparator = new NumericStringComparator();
					if(o1 instanceof MassnahmenUmsetzung && o2 instanceof MassnahmenUmsetzung){
						MassnahmenUmsetzung m1 = (MassnahmenUmsetzung)o1;
						MassnahmenUmsetzung m2 = (MassnahmenUmsetzung)o2;
						return comparator.compare(m1.getKapitel(), m2.getKapitel());
					} else {
						return comparator.compare(o1.getTitle(), o2.getTitle());
					}
				}
			});
    	}
    }
    

    /**
     * @return the result
     */
    public ArrayList<CnATreeElement> getResult() {
        return result;
    }

   

  
	


}
