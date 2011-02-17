package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

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
    

    /**
     * @return the result
     */
    public ArrayList<CnATreeElement> getResult() {
        return result;
    }

   

  
	


}
