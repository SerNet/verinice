package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sun.xml.messaging.saaj.util.LogDomainConstants;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.iso27k.service.RiskAnalysisServiceImpl;
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
    
    private int cia_tolerable;
    private char cia_choice;
    private int cia_max;
    private int probMax;
    
    public LoadRiskSummary( char cia, Integer rootElement) {
        this.cia_choice = cia;
	    this.rootElement = rootElement;
	}
	
	public void execute() {
	    getLog().debug("LoadReportElements for root_object " + rootElement);
	    
	    PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(Asset.TYPE_ID, RiskAnalysisServiceImpl.PROP_SCENARIO_PROBABILITY);
	    probMax = type.getMaxValue();
	    
	    Organization org = (Organization) getDaoFactory().getDAO(Organization.TYPE_ID).findById(rootElement);
	    switch (cia_choice) {
        case 'c':
            cia_tolerable = org.getSchutzbedarfProvider().getVertraulichkeit();
            cia_max= org.getEntityType().getPropertyType(Asset.TYPE_ID + AssetValueService.CONFIDENTIALITY).getMaxValue();
            break;
        case 'i':
            cia_tolerable =  org.getSchutzbedarfProvider().getIntegritaet();
            cia_max= org.getEntityType().getPropertyType(Asset.TYPE_ID + AssetValueService.INTEGRITY).getMaxValue();
            break;
        case 'a':
            cia_tolerable = org.getSchutzbedarfProvider().getVerfuegbarkeit();
            cia_max= org.getEntityType().getPropertyType(Asset.TYPE_ID + AssetValueService.AVAILABILITY).getMaxValue();
            break;
        }
	    matrix = new RiskMatrix(probMax, cia_max);
	    
	    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
	    try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
	    CnATreeElement root = command.getElements().get(0);
	    ArrayList<CnATreeElement> items = new ArrayList<CnATreeElement>();
	    
        getElements(TYPE_ID, items, root);

        
        Integer risk =null;
        for (CnATreeElement cnATreeElement : items) {
          for (CnALink link: cnATreeElement.getLinksDown()) {
              if (link.getDependency().getTypeId().equals(IncidentScenario.TYPE_ID)) {
                  Integer prob = link.getDependency().getEntity().getInt(RiskAnalysisServiceImpl.PROP_SCENARIO_PROBABILITY);
                  switch (cia_choice) {
                  case 'c':
                      risk = link.getRiskConfidentiality();
                      break;
                  case 'i':
                      risk = link.getRiskIntegrity();
                      break;
                  case 'a':
                      risk = link.getRiskAvailability();
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
