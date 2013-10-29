package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.iso27k.service.RiskAnalysisServiceImpl;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueService;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;

/**
 * 
 * Load summary: count of risks by value according to matrix
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadRiskSummary extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadNotes.class);
    private static final String TYPE_ID = "asset";
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(LoadNotes.class);
        }
        return log;
    }
    
    private RiskMatrix matrix;

    private Integer rootElement;
    
    private char ciaChoice;
    
    public LoadRiskSummary( char cia, Integer rootElement) {
        this.ciaChoice = cia;
	    this.rootElement = rootElement;
	}
	
	public void execute() {
	    int ciaMax = 0;
	    int probMax = 0;
	    getLog().debug("LoadReportElements for root_object " + rootElement);
	    
	    PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(Asset.TYPE_ID, RiskAnalysisServiceImpl.PROP_SCENARIO_PROBABILITY);
	    probMax = type.getMaxValue();
	    
	    Organization org = (Organization) getDaoFactory().getDAO(Organization.TYPE_ID).findById(rootElement);
	    switch (ciaChoice) {
        case 'c':
            ciaMax= org.getEntityType().getPropertyType(Asset.TYPE_ID + AssetValueService.CONFIDENTIALITY).getMaxValue();
            break;
        case 'i':
            ciaMax= org.getEntityType().getPropertyType(Asset.TYPE_ID + AssetValueService.INTEGRITY).getMaxValue();
            break;
        case 'a':
            ciaMax= org.getEntityType().getPropertyType(Asset.TYPE_ID + AssetValueService.AVAILABILITY).getMaxValue();
            break;
        default:
            break;
        }
	    matrix = new RiskMatrix(probMax, ciaMax);
	    
	    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
	    try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
	    CnATreeElement root = command.getElements().get(0);
	    ArrayList<CnATreeElement> items = new ArrayList<CnATreeElement>();
	    
        getElements(TYPE_ID, items, root);

        populateMatrix(items);
	}

    private void populateMatrix(List<CnATreeElement> items) {
        Integer risk =null;
        for (CnATreeElement cnATreeElement : items) {
          for (CnALink link: cnATreeElement.getLinksDown()) {
              if (link.getDependency().getTypeId().equals(IncidentScenario.TYPE_ID)) {
                  Integer prob = link.getDependency().getEntity().getInt(RiskAnalysisServiceImpl.PROP_SCENARIO_PROBABILITY);
                  switch (ciaChoice) {
                  case 'c':
                      risk = link.getRiskConfidentiality();
                      break;
                  case 'i':
                      risk = link.getRiskIntegrity();
                      break;
                  case 'a':
                      risk = link.getRiskAvailability();
                      break;
                  default:
                      break;
                  }
                  matrix.increaseCount(prob, risk);
              }
          }
        }
    }
	
	public Integer[][] getResult() {
	    return matrix.map;
	}
	
	
   

    public void getElements(String typeFilter, List<CnATreeElement> items, CnATreeElement parent) {
        for (CnATreeElement child : parent.getChildren()) {
            if (typeFilter != null && typeFilter.length()>0) {
                if (child.getTypeId().equals(typeFilter)) {
                    items.add(child);
                }
            } else {
                items.add(child);
            }
            getElements(typeFilter, items, child);
        }
        
    }
	


}
