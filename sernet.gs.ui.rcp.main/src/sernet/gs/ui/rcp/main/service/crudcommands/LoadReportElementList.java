package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

/**
 * Load lsit of elements and their databse ids for further use in reports.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadReportElementList extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadNotes.class);
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(LoadNotes.class);
        }
        return log;
    }

	private String typeId;
    private Integer rootElement;
    private ArrayList<CnATreeElement> elements;
    
    public LoadReportElementList(String typeId, Integer rootElement) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	}
	
	public void execute() {
	    getLog().debug("LoadReportElements for root_object " + rootElement);
	    
	    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
	    try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
	    CnATreeElement root = command.getElements().get(0);

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
	    
	    // load lazy fields:
	    getResult();
	}
	
	public static final String[] COLUMNS = new String[] {"elmt_id", "elmt_name"};
	/**
     * @return the result
     */
    public List<List<String>> getResult() {
        List<List<String>> result = new ArrayList<List<String>>();
        for (CnATreeElement elmt : elements) {
            List<String> row = Arrays.asList(elmt.getDbId().toString(), elmt.getTitle());
            result.add(row);
        }
        return result;
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
