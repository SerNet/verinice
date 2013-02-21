package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.iso27k.service.IRiskAnalysisService;
import sernet.verinice.iso27k.service.RiskAnalysisServiceImpl;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.AssetValueService;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Process;

/**
 * Starting with all process for a given scope, load all linked assets and their
 * linked scenarios. Returns a list with the following columns:
 * 
 * - probability (from scenario) - impact C (from asset) - impact I (from asset)
 * - impact A (from asset), tolerable levels (by scope: same number in all rows),
 * 
 * 
 */
public class LoadReportAllRisksForScope extends GenericCommand implements ICachedCommand{
    
    public static final String[] COLUMNS = new String[] { 
        "Probability", 
        "Impact C", 
        "Impact I",
        "Impact A" ,
        "RISK_C",
        "RISK_I",
        "RISK_A",
        "TOLERABLE_C",
        "TOLERABLE_I",
        "TOLERABLE_A",
        "Scenario",
        "Asset"
        };

    public static final String PROP_ORG_RISKACCEPT_C = "org_riskaccept_confid";
    public static final String PROP_ORG_RISKACCEPT_I = "org_riskaccept_integ";
    public static final String PROP_ORG_RISKACCEPT_A = "org_riskaccept_avail";
    
    
    // present for backwards compatibility, some reports still use the constants in this Class:
    public static final int RISK_PRE_CONTROLS               = IRiskAnalysisService.RISK_PRE_CONTROLS;
    public static final int RISK_WITH_IMPLEMENTED_CONTROLS  = IRiskAnalysisService.RISK_WITH_IMPLEMENTED_CONTROLS;
    public static final int RISK_WITH_ALL_CONTROLS          = IRiskAnalysisService.RISK_WITH_ALL_CONTROLS;
        
    private Integer rootElmt;
    private List<List<String>> result;
    private RiskMatrix riskMatrixC;
    private RiskMatrix riskMatrixI;
    private RiskMatrix riskMatrixA;
    
    private transient Set<Integer> seenScenarios;
    private transient Set<Integer> seenAssets;

    private int tolerableC;

    private int tolerableI;

    private int tolerableA;

    private boolean distinct;

    private int riskType = IRiskAnalysisService.RISK_PRE_CONTROLS;
   
    private boolean resultInjectedFromCache = false;

    public LoadReportAllRisksForScope(Integer rootElement) {
        this(rootElement, false);
    }

    public LoadReportAllRisksForScope(Integer rootElement, boolean distinct, int riskType) {
        this.rootElmt = rootElement;
        result = new ArrayList<List<String>>();
        this.distinct = distinct;
        this.riskType = riskType;
    }

    public LoadReportAllRisksForScope(Integer rootElement, boolean distinct) {
        this(rootElement, distinct, IRiskAnalysisService.RISK_PRE_CONTROLS);
    }
        
    @SuppressWarnings({ "unchecked" })
    public void execute() {
        try {
            // determine max and tolerable risk values. initialize matrices to save value counts:
            Organization org = (Organization) getDaoFactory().getDAO(Organization.TYPE_ID).findById(rootElmt);
            
            HUITypeFactory huiTypeFactory = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
            PropertyType type = huiTypeFactory.getPropertyType(IncidentScenario.TYPE_ID, RiskAnalysisServiceImpl.PROP_SCENARIO_PROBABILITY);
            int probMax = type.getMaxValue();

            int ciaMax = huiTypeFactory.getPropertyType(Asset.TYPE_ID, Asset.TYPE_ID+AssetValueService.CONFIDENTIALITY).getMaxValue();
            riskMatrixC = new RiskMatrix(probMax, ciaMax);

            ciaMax = huiTypeFactory.getPropertyType(Asset.TYPE_ID, Asset.TYPE_ID+AssetValueService.INTEGRITY).getMaxValue();
            riskMatrixI = new RiskMatrix(probMax, ciaMax);

            ciaMax = huiTypeFactory.getPropertyType(Asset.TYPE_ID, Asset.TYPE_ID+AssetValueService.AVAILABILITY).getMaxValue();
            riskMatrixA = new RiskMatrix(probMax, ciaMax);

            if(!resultInjectedFromCache){

                tolerableC = org.getNumericProperty(PROP_ORG_RISKACCEPT_C);
                tolerableI = org.getNumericProperty(PROP_ORG_RISKACCEPT_I);
                tolerableA = org.getNumericProperty(PROP_ORG_RISKACCEPT_A);

                // load risk values from elements (following links to process, asset, scenario)

                seenScenarios = new HashSet<Integer>();
                seenAssets = new HashSet<Integer>();
                
                LoadReportElements command = new LoadReportElements(Process.TYPE_ID, rootElmt, true);
                command = getCommandService().executeCommand(command);
                if (command.getElements() == null || command.getElements().size() == 0) {
                    result = new ArrayList<List<String>>(0);
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
                            // only add distinct values: (starting from different processes, recursion can lead to the same element multiple times)
                            if (!distinct) {
                                // just add it:
                                result.add(makeRow(scenario, asset));
                            }
                            else if (  ! (seenScenarios.contains(scenario.getDbId()) && seenAssets.contains(asset.getDbId())) )  {
                                result.add(makeRow(scenario, asset));
                            }

                        }
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
     * @throws CommandException 
     */
    @SuppressWarnings("restriction")
    private List<String> makeRow(CnATreeElement scenario, CnATreeElement asset) throws CommandException {
        seenScenarios.add(scenario.getDbId());
        seenAssets.add(asset.getDbId());

        ArrayList<String> row = new ArrayList<String>();
        AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);
        RiskAnalysisServiceImpl raService = new RiskAnalysisServiceImpl();
        
        int probability = 0;
        switch (this.riskType) {
        case IRiskAnalysisService.RISK_PRE_CONTROLS:
            probability = scenario.getNumericProperty(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY);
            break;
        case IRiskAnalysisService.RISK_WITH_IMPLEMENTED_CONTROLS:
            probability = scenario.getNumericProperty(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
            break;
        case IRiskAnalysisService.RISK_WITH_ALL_CONTROLS:
            probability = scenario.getNumericProperty(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
            break;
        default:
            break;
        }
        
        Integer impactC = 0;
        Integer impactI = 0;
        Integer impactA = 0;
        impactC = valueAdapter.getVertraulichkeit();
        impactI = valueAdapter.getIntegritaet();
        impactA = valueAdapter.getVerfuegbarkeit();
        
        boolean isCRelevant = false;
        boolean isIRelevant = false;
        boolean isARelevant = false;
        
        isCRelevant = scenario.getEntity().getProperties("scenario_value_method_confidentiality").getProperty(0).getPropertyValue().equals("1");
        isIRelevant = scenario.getEntity().getProperties("scenario_value_method_integrity").getProperty(0).getPropertyValue().equals("1");
        isARelevant = scenario.getEntity().getProperties("scenario_value_method_availability").getProperty(0).getPropertyValue().equals("1");
            
        Integer[] reducedImpact = raService.applyControlsToImpact(riskType, asset, impactC, impactI, impactA);
        if (reducedImpact != null) {
            impactC = reducedImpact[0];
            impactI = reducedImpact[1];
            impactA = reducedImpact[2];
        }
        
        
        // prob. / impact:
        row.add(Integer.toString(probability));
        row.add(Integer.toString(impactC));
        row.add(Integer.toString(impactI));
        row.add(Integer.toString(impactA));
        
        int riskC = probability + impactC;
        int riskI = probability + impactI;
        int riskA = probability + impactA;
        
        // risk values:
        row.add(Integer.toString(riskC));
        row.add(Integer.toString(riskI));
        row.add(Integer.toString(riskA));
        
        // tolerable values:
        row.add(Integer.toString(tolerableC));
        row.add(Integer.toString(tolerableI));
        row.add(Integer.toString(tolerableA));
        
        row.add(scenario.getTitle());
        row.add(asset.getTitle());
        
        if(isCRelevant){
            riskMatrixC.increaseCount(probability, impactC);
        }
        if(isIRelevant){
            riskMatrixI.increaseCount(probability, impactI);
        }
        if(isARelevant){
            riskMatrixA.increaseCount(probability, impactA);
        }
        return row;
    }

  

   

    /**
     * @return the result
     */
    public List<List<String>> getResult() {
        return result;
    }

   
    
    public Integer[][] getCountC() {
        return riskMatrixC.map;
    }
    
    public Integer[][] getCountI() {
        return riskMatrixI.map;
    }
 
    public Integer[][] getCountA() {
        return riskMatrixA.map;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        cacheID.append(String.valueOf(distinct));
        cacheID.append(String.valueOf(riskType));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        if(result instanceof Object[]){
            HUITypeFactory huiTypeFactory = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
            PropertyType type = huiTypeFactory.getPropertyType(IncidentScenario.TYPE_ID, RiskAnalysisServiceImpl.PROP_SCENARIO_PROBABILITY);
            int probMax = type.getMaxValue();

            int ciaMax = huiTypeFactory.getPropertyType(Asset.TYPE_ID, Asset.TYPE_ID+AssetValueService.CONFIDENTIALITY).getMaxValue();
            riskMatrixC = new RiskMatrix(probMax, ciaMax);

            ciaMax = huiTypeFactory.getPropertyType(Asset.TYPE_ID, Asset.TYPE_ID+AssetValueService.INTEGRITY).getMaxValue();
            riskMatrixI = new RiskMatrix(probMax, ciaMax);

            ciaMax = huiTypeFactory.getPropertyType(Asset.TYPE_ID, Asset.TYPE_ID+AssetValueService.AVAILABILITY).getMaxValue();
            riskMatrixA = new RiskMatrix(probMax, ciaMax);
            Object[] array = (Object[])result;
            this.result = (ArrayList<List<String>>)array[0];
            riskMatrixC.map = (Integer[][])array[1];
            riskMatrixI.map = (Integer[][])array[2];
            riskMatrixA.map = (Integer[][])array[3];
            resultInjectedFromCache = true;
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        Object[] cacheableResult = new Object[4];
        cacheableResult[0] = this.result;
        cacheableResult[1] = riskMatrixC.map;
        cacheableResult[2] = riskMatrixI.map;
        cacheableResult[3] = riskMatrixA.map;
        return cacheableResult;
    }
    
}
