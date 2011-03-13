package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.dialect.function.CastFunction;

import com.sun.xml.messaging.saaj.util.LogDomainConstants;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
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
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.Process;

/**
 * Load Process name, CIA, calculated total risk.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadReportProcessesWithRisk extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadReportProcessesWithRisk.class);
    
   private List<List<String>> result = new ArrayList<List<String>>();
   
   public static final String[] COLUMNS = {
     "dbid",
     "abbrev",
     "name",
     "C",  
     "I",  
     "A",  
     "risk",  
   };
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(LoadReportProcessesWithRisk.class);
        }
        return log;
    }

	private String typeId = Process.TYPE_ID;
    private Integer rootElement;
    private ArrayList<CnATreeElement> elements;

    
    public LoadReportProcessesWithRisk( Integer rootElement) {
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
        if (command.getElements() == null || command.getElements().size()==0) {
            this.elements = new ArrayList<CnATreeElement>(0);
            return;
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
	    
	    
	    Collections.sort(elements, new Comparator<CnATreeElement>() {
            @Override
            public int compare(CnATreeElement o1, CnATreeElement o2) {
                NumericStringComparator comparator = new NumericStringComparator();
                return comparator.compare(o1.getTitle(), o2.getTitle());
            }
        });

       try {
        calculateRisk();
    } catch (CommandException e) {
        throw new RuntimeCommandException(e);
    }
	    
//	    IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
//	    RetrieveInfo ri = new RetrieveInfo();
//	    ri.setProperties(true);
//	    ri.setParent(true);
//	    ri.setPermissions(true);
//	    ri.setParentPermissions(true);
//	    ri.setChildren(true);
//	    ri.setChildren(true);
//	    HydratorUtil.hydrateElements(dao, elements, ri);

	}

	

   

    /**
     * @param elements2
     * @throws CommandException 
     */
    private void calculateRisk() throws CommandException {
        for (CnATreeElement cnATreeElement : elements) {
            if (cnATreeElement.getTypeId().equals(Process.TYPE_ID)) {
                LoadReportLinkedElements loadAssets = new LoadReportLinkedElements(Asset.TYPE_ID, cnATreeElement.getDbId(), true);
                loadAssets = getCommandService().executeCommand(loadAssets);
                List<CnATreeElement> assets = loadAssets.getElements();
                
                int totalRisk=0;
                for (CnATreeElement asset : assets) {
                    LoadReportElementWithLinks command2 = new LoadReportElementWithLinks("incident_scenario", asset.getDbId());
                    command2 = getCommandService().executeCommand(command2);
                    List<CnALink> links = command2.getLinkList();
                    
                    for (CnALink link : links) {
                        totalRisk += link.getRiskConfidentiality();
                        totalRisk += link.getRiskIntegrity();
                        totalRisk += link.getRiskAvailability();
                    }
                }
                
                getLog().debug("Total risk for process " + cnATreeElement.getDbId() + ": " + totalRisk);
                
                ArrayList<String> row = new ArrayList<String>();
                AssetValueAdapter process = new AssetValueAdapter(cnATreeElement);
                row.add(Integer.toString(cnATreeElement.getDbId()));
                row.add(cnATreeElement.getEntity().getSimpleValue(Process.PROP_ABBR));
                row.add(cnATreeElement.getEntity().getSimpleValue(Process.PROP_NAME));
                row.add(Integer.toString(process.getVertraulichkeit()));
                row.add(Integer.toString(process.getIntegritaet()));
                row.add(Integer.toString(process.getVerfuegbarkeit()));
                row.add(Integer.toString(totalRisk));
                result.add(row);
            }
        }
    }

    
  

    /**
     * @return the result
     */
    public List<List<String>> getResult() {
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
