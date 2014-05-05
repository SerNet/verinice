/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.iso27k.service.IRiskAnalysisService;
import sernet.verinice.iso27k.service.RiskAnalysisServiceImpl;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * Adds more columns to the normal list of links for an element in case of scnearios: threat and vuln. levels and titles
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadReportScenarioDetails extends GenericCommand implements ICachedCommand{
    
    public static final String[] COLUMNS = new String[] {"relationName", "toAbbrev", "toElement", "riskC", "riskI", "riskA", "dbid",
        "threatLevel", "vulnLevel", "threatTitle", "vulnTitle", "threatLevelDesc", "vulnLevelDesc", 
        "treatedRiskC", "treatedRiskI", "treatedRiskA", "scenarioDbId"};

    
    
    private LoadReportElementWithLinks loadReportElementWithLinks;

    private List<List<String>> result;
    
    private boolean resultInjectedFromCache = false;
    
    private String typeId;
    
    private Integer rootElmt;

    public LoadReportScenarioDetails(String typeId, Integer rootElement) {
        loadReportElementWithLinks = new LoadReportElementWithLinks(typeId, rootElement);
        this.typeId = typeId;
        this.rootElmt = rootElement;
    }

    public LoadReportScenarioDetails(String typeId, String rootElement) {
        loadReportElementWithLinks = new LoadReportElementWithLinks(typeId, rootElement);
        this.typeId = typeId;
        this.rootElmt = Integer.valueOf(Integer.parseInt(rootElement));
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try {
                loadReportElementWithLinks = getCommandService().executeCommand(loadReportElementWithLinks);
                result = loadReportElementWithLinks.getResult();
                List<CnALink> links = loadReportElementWithLinks.getLinkList();
                int i=0;
                for (CnALink cnALink : links) {
                    addCols(cnALink, result.get(i));
                    i++;
                }

            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }
        } 
    }

    /**
     * @param cnALink
     * @param row 
     * @throws CommandException 
     */
    private void addCols(CnALink cnALink, List<String> row) throws CommandException {
        CnATreeElement scenario;
        CnATreeElement asset = null;
        
        if (cnALink.getDependant().getTypeId().equals(IncidentScenario.TYPE_ID)) {
            scenario = cnALink.getDependant();
            if (cnALink.getDependency().getTypeId().equals(Asset.TYPE_ID)) {
                asset = cnALink.getDependency();
            }
        } else {
            scenario = cnALink.getDependency();
            if (cnALink.getDependant().getTypeId().equals(Asset.TYPE_ID)) {
                asset = cnALink.getDependant();
            }
        }
        
        // get the reduced probability of the scenario:
        int probabilityWithControls = scenario.getNumericProperty(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
        
        // get the CIA deciders
        boolean isCRelevant = scenario.getEntity().getProperties("scenario_value_method_confidentiality").getProperty(0).getPropertyValue().equals("1");
        boolean isIRelevant = scenario.getEntity().getProperties("scenario_value_method_integrity").getProperty(0).getPropertyValue().equals("1");
        boolean isARelevant = scenario.getEntity().getProperties("scenario_value_method_availability").getProperty(0).getPropertyValue().equals("1");

        
        // initialize risk with base risk - with no controls:
        Integer treatedRiskC = Integer.parseInt(row.get(3));
        Integer treatedRiskI = Integer.parseInt(row.get(4));
        Integer treatedRiskA = Integer.parseInt(row.get(5));
        
        // get all controls for the asset and reduce the impact of the asset and thereby the risk:
        if (asset != null) {
            Integer impactC = 0;
            Integer impactI = 0;
            Integer impactA = 0;
            AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);
            RiskAnalysisServiceImpl ras = new RiskAnalysisServiceImpl();
            
            impactC = valueAdapter.getVertraulichkeit();
            impactI = valueAdapter.getIntegritaet();
            impactA = valueAdapter.getVerfuegbarkeit();
            
            Integer[] reducedImpact = ras.applyControlsToImpact(IRiskAnalysisService.RISK_WITH_IMPLEMENTED_CONTROLS, asset, impactC, impactI, impactA);
            if (reducedImpact != null) {
                impactC = reducedImpact[0];
                impactI = reducedImpact[1];
                impactA = reducedImpact[2];
            }
            
            treatedRiskC = probabilityWithControls + impactC;
            treatedRiskI = probabilityWithControls + impactI;
            treatedRiskA = probabilityWithControls + impactA;
        }
        
        HUITypeFactory hui = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);

        int threatLevel = scenario.getNumericProperty(IRiskAnalysisService.PROP_SCENARIO_THREAT_PROBABILITY);
        PropertyType propertyType = hui.getPropertyType(IncidentScenario.TYPE_ID, IRiskAnalysisService.PROP_SCENARIO_THREAT_PROBABILITY);
        String threatDesc = propertyType.getNameForValue(threatLevel);
        
        int vulnLevel = scenario.getNumericProperty(IRiskAnalysisService.PROP_SCENARIO_VULN_PROBABILITY);
        propertyType = hui.getPropertyType(IncidentScenario.TYPE_ID, IRiskAnalysisService.PROP_SCENARIO_VULN_PROBABILITY);
        String vulnDesc = propertyType.getNameForValue(vulnLevel);
        
        String threatTitle;
        String vulnTitle;
        
        Map<CnATreeElement, CnALink> threats = CnALink.getLinkedElements(scenario, Threat.TYPE_ID);
        Map<CnATreeElement, CnALink> vulns = CnALink.getLinkedElements(scenario, Vulnerability.TYPE_ID);
        
        if (threats.size()==0) {
               threatTitle = "";
        } else {
            // scenario may be linked to more than one threat, add all to description:
            // (for risk calculation, the highest threat will be used, see RiskAnalysisServiceImpl.java)
            StringBuilder sb = new StringBuilder();
            for (Iterator iterator = threats.keySet().iterator(); iterator.hasNext();) {
                CnATreeElement t = (CnATreeElement) iterator.next();
                sb.append(t.getTitle());
                if (iterator.hasNext()){
                    sb.append(", ");
                }
            }
            threatTitle = sb.toString();
        }
        
        if (vulns.size()==0){
            vulnTitle = "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Iterator iterator = vulns.keySet().iterator(); iterator.hasNext();) {
                CnATreeElement t = (CnATreeElement) iterator.next();
                sb.append(t.getTitle());
                if (iterator.hasNext()){
                    sb.append(", ");
                }
            }
            vulnTitle = sb.toString();
        }
        
        row.add(Integer.toString(threatLevel));
        row.add(Integer.toString(vulnLevel));
        row.add(threatTitle);
        row.add(vulnTitle);
        row.add(threatDesc);
        row.add(vulnDesc);
        row.add(isCRelevant ? treatedRiskC.toString() : "0");
        row.add(isIRelevant ? treatedRiskI.toString() : "0");
        row.add(isARelevant ? treatedRiskA.toString() : "0");
        row.add(scenario.getDbId().toString());
    }


    /**
     * @return the result
     */
    public List<List<String>> getResult() {
        return result;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(this.typeId);
        cacheID.append(String.valueOf(rootElmt));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.result;
    }

}


