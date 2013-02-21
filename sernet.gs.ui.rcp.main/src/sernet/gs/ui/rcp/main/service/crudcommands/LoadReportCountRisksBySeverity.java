package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.iso27k.service.IRiskAnalysisService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Process;

/**
 * Starting with all process for a given scope, load all linked assets and their
 * linked scenarios. Returns totsla numbetr of risks by color (red, green, yellow).
 * 
 * 
 */
public class LoadReportCountRisksBySeverity extends GenericCommand implements ICachedCommand{

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
    
    private String ciaRelevantProperty;
    
    private boolean resultInjectedFromCache = false;

    public LoadReportCountRisksBySeverity(Integer rootElement, char riskType, int numberOfYellowLevels) {
        this.rootElmt = rootElement;
        this.riskType = riskType;
        this.numYellowFields = numberOfYellowLevels;
        for(int i=0; i < countRYG.length; i++) {
            countRYG[i]=0;
        }
    }

    public void execute() {
        if(!resultInjectedFromCache){
            try {
                // determine max and tolerable risk values. initialize matrices to save value counts:
                Organization org = (Organization) getDaoFactory().getDAO(Organization.TYPE_ID).findById(rootElmt);

                switch (this.riskType) {
                case 'c':
                    tolerableRisk = org.getNumericProperty(PROP_ORG_RISKACCEPT_C);
                    ciaRelevantProperty = "scenario_value_method_confidentiality";
                    break;
                case 'i':
                    tolerableRisk = org.getNumericProperty(PROP_ORG_RISKACCEPT_I);
                    ciaRelevantProperty = "scenario_value_method_integrity";
                    break;
                case 'a':
                    tolerableRisk = org.getNumericProperty(PROP_ORG_RISKACCEPT_A);
                    ciaRelevantProperty = "scenario_value_method_availability";
                    break;
                default:
                    break;
                }

                // load risk values from elements (following links to process, asset, scenario)

                LoadReportElements command = new LoadReportElements(Process.TYPE_ID, rootElmt, true);
                command = getCommandService().executeCommand(command);
                if (command.getElements() == null || command.getElements().size() == 0) {
                    return;
                }
                List<CnATreeElement> elements = command.getElements();

                for (CnATreeElement process : elements) {
                    LoadReportLinkedElements cmnd2 = new LoadReportLinkedElements(Asset.TYPE_ID, process.getDbId(), true, false);
                    cmnd2 = getCommandService().executeCommand(cmnd2);
                    List<CnATreeElement> assets = cmnd2.getElements();
                    for (CnATreeElement asset : assets) {
                        LoadReportLinkedElements cmnd3 = new LoadReportLinkedElements(IncidentScenario.TYPE_ID, asset.getDbId());
                        cmnd3 = getCommandService().executeCommand(cmnd3);
                        List<CnATreeElement> scenarios = cmnd3.getElements();
                        for (CnATreeElement scenario : scenarios) {
                            if(scenario.getEntity().getProperties(ciaRelevantProperty).getProperty(0).getPropertyValue().equals("1")){
                                countRisk(scenario, asset);
                            }
                        }
                    }
                }

            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }
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
        default:
            break;
        }
        
        return row;
    }

  

    /**
     * @param riskC
     * @param tolerableRisk2
     */
    private void increaseCount(int risk) {
        if (risk > tolerableRisk){
            countRYG[0]++; // red
        } else if (risk < tolerableRisk-numYellowFields+1) {
            countRYG[2]++; // green
        } else {
            countRYG[1]++; // yellow
        }
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

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        cacheID.append(String.valueOf(riskType));
        cacheID.append(String.valueOf(numYellowFields));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        if(result instanceof Integer[]){
            Integer[] array = (Integer[])result;
            countRYG[0] = array[0];
            countRYG[1] = array[1];
            countRYG[2] = array[2];
            resultInjectedFromCache = true;
        }
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return (countRYG != null) ? countRYG.clone() : null;
    }
  
}
