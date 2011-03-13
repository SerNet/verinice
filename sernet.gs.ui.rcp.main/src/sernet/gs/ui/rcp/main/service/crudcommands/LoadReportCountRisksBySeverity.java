package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.iso27k.service.IRiskAnalysisService;
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
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.AssetValueService;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Process;

/**
 * Starting with all process for a given scope, load all linked assets and their
 * linked scenarios. Returns totsla numbetr of risks by color (red, green, yellow).
 * 
 * 
 */
public class LoadReportCountRisksBySeverity extends GenericCommand {

    public static final String[] COLUMNS = new String[] { 
        "COUNT_RISK_COLOR",
        "COUNT_RISK_VALUE",
        };

    public static final String PROP_ORG_RISKACCEPT_C = "org_riskaccept_confid";
    public static final String PROP_ORG_RISKACCEPT_I = "org_riskaccept_integ";
    public static final String PROP_ORG_RISKACCEPT_A = "org_riskaccept_avail";
        
    private Integer rootElmt;

    private int tolerableRisk;
    
    private Integer[] countRYG = new Integer[3];

    private char riskType;

    private int numYellowFields;

    public LoadReportCountRisksBySeverity(Integer rootElement, char riskType, int numberOfYellowLevels) {
        this.rootElmt = rootElement;
        this.riskType = riskType;
        this.numYellowFields = numberOfYellowLevels;
        for(int i=0; i < countRYG.length; i++) {
            countRYG[i]=0;
        }
    }

    public void execute() {
        try {
            // determine max and tolerable risk values. initialize matrices to save value counts:
            Organization org = (Organization) getDaoFactory().getDAO(Organization.TYPE_ID).findById(rootElmt);

            HUITypeFactory huiTypeFactory = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);

            switch (this.riskType) {
            case 'c':
                tolerableRisk = org.getNumericProperty(PROP_ORG_RISKACCEPT_C);
                break;
            case 'i':
                tolerableRisk = org.getNumericProperty(PROP_ORG_RISKACCEPT_I);
                break;
            case 'a':
                tolerableRisk = org.getNumericProperty(PROP_ORG_RISKACCEPT_A);
                break;
            }

            // load risk values from elements (following links to process, asset, scenario)
            
            LoadReportElements command = new LoadReportElements(Process.TYPE_ID, rootElmt);
            command = getCommandService().executeCommand(command);
            if (command.getElements() == null || command.getElements().size() == 0) {
                return;
            }
            List<CnATreeElement> elements = command.getElements();

            for (CnATreeElement process : elements) {
                LoadReportLinkedElements cmnd2 = new LoadReportLinkedElements(Asset.TYPE_ID, process.getDbId(), true);
                cmnd2 = getCommandService().executeCommand(cmnd2);
                List<CnATreeElement> assets = cmnd2.getElements();
                for (CnATreeElement asset : assets) {
                    LoadReportLinkedElements cmnd3 = new LoadReportLinkedElements(IncidentScenario.TYPE_ID, asset.getDbId());
                    cmnd3 = getCommandService().executeCommand(cmnd3);
                    List<CnATreeElement> scenarios = cmnd3.getElements();
                    for (CnATreeElement scenario : scenarios) {
                        countRisk(scenario, asset);
                    }
                }
            }

        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }

    }

    /**
     * @param process
     * @param asset
     * @return
     */
    private List<String> countRisk(CnATreeElement scenario, CnATreeElement asset) {
        ArrayList<String> row = new ArrayList<String>();
        AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);
        
        int probability = scenario.getNumericProperty(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY);
        
        // prob. / impact:
        int riskC = probability + valueAdapter.getVertraulichkeit();
        int riskI = probability + valueAdapter.getIntegritaet();
        int riskA = probability + valueAdapter.getVerfuegbarkeit();
        
        // risk values:
        switch (this.riskType) {
        case 'c':
            increaseCount(riskC);
            break;
        case 'i':
            increaseCount(riskI);
            break;
        case 'a':
            increaseCount(riskA);
            break;
        }
        
        return row;
    }

  

    /**
     * @param riskC
     * @param tolerableRisk2
     */
    private void increaseCount(int risk) {
        if (risk > tolerableRisk)
            countRYG[0]++; // red
        else if (risk < tolerableRisk-numYellowFields+1)
            countRYG[2]++; // green
        else
            countRYG[1]++; // yellow
    }

    /**
     * @return the result
     */
    public List<List<Object>> getResult() {
        ArrayList<List<Object>> result = new ArrayList<List<Object>>();
        result.add(Arrays.asList(new Object[] {"2RED", countRYG[0]}));
        result.add(Arrays.asList(new Object[] {"1YELLOW", countRYG[1]}));
        result.add(Arrays.asList(new Object[] {"0GREEN", countRYG[2]}));
        return result;
    }

   
  
}
