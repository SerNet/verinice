/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm@sernet.de>.
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
 *     Alexander Koderman
 *     Benjamin Weißenfels <bw@sernet.de>
 *     Daniel Murygin <dm[at]sernet[dot]de> 
 ******************************************************************************/
package sernet.verinice.service.risk;

import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ProtectionRequirementsValueAdapter;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.RetrieveCnATreeElement;

/**
 * Helper class for executing a risk analysis.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RiskAnalysisHelperImpl implements RiskAnalysisHelper {
    
    private static final transient Logger LOG = Logger.getLogger(RiskAnalysisHelperImpl.class);

    /**
     * Reduce impact levels by all controls applied to this asset.
     * 
     * @param asset
     * @param impactC
     * @param impactI
     * @param impactA
     * @throws CommandException 
     */
    @Override
    public Integer[] applyControlsToImpact(int riskType, CnATreeElement rawElement, Integer impactC, Integer impactI, Integer impactA)  {
        if (riskType == RISK_PRE_CONTROLS){
            return null; // do nothing
        }
        CnATreeElement element = Retriever.checkRetrieveLinks(rawElement, true);
        
        Map<CnATreeElement, CnALink> linkedElements = CnALink.getLinkedElements(element, Control.TYPE_ID);
        
        Integer impactC0 = Integer.valueOf(impactC.intValue());
        Integer impactI0 = Integer.valueOf(impactI.intValue());
        Integer impactA0 = Integer.valueOf(impactA.intValue());
        
        
        switch (riskType) {
        case RISK_WITH_IMPLEMENTED_CONTROLS:
            for (CnATreeElement control : linkedElements.keySet()) {
                control = Retriever.checkRetrieveElement(control);
                if (Control.isImplemented(control.getEntity())) {
                    impactC0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_CONFIDENTIALITY);
                    impactI0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_INTEGRITY);
                    impactA0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_AVAILABILITY);
                } 
            }
            break;
        case RISK_WITH_ALL_CONTROLS:
            for (CnATreeElement control : linkedElements.keySet()) {
                control = Retriever.checkRetrieveElement(control);
                impactC0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_CONFIDENTIALITY);
                impactI0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_INTEGRITY);
                impactA0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_AVAILABILITY);
            }
            break;
        case RISK_WITHOUT_NA_CONTROLS:
            for (CnATreeElement control : linkedElements.keySet()) {
                control = Retriever.checkRetrieveElement(control);
                if (Control.isPlanned(control.getEntity())) {
                    impactC0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_CONFIDENTIALITY);
                    impactI0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_INTEGRITY);
                    impactA0 -= control.getNumericProperty(Control.PROP_EFFECTIVENESS_AVAILABILITY);
                }
            }
            break;
        default: // do nothing
            break;
        }

        impactC0 = (impactC0.intValue() < 0) ? Integer.valueOf(0) : impactC0;
        impactI0 = (impactI0.intValue() < 0) ? Integer.valueOf(0) : impactI0;
        impactA0 = (impactA0.intValue() < 0) ? Integer.valueOf(0) : impactA0;

        return new Integer[] { impactC0, impactI0, impactA0 };
    }

    /**
     * Computes if a given risk (given by asset & scenario) is red, yellow or
     * green
     */
    @Override
    public int getRiskColor(CnATreeElement asset, CnATreeElement scenario, char riskType, int numOfYellowFields, String probType) {
        ProtectionRequirementsValueAdapter valueAdapter = new ProtectionRequirementsValueAdapter(asset);

        int probability = scenario.getNumericProperty(probType);
        int riskControlState;
        if (probType.equals(RiskAnalysisHelper.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS)) {
            riskControlState = RiskAnalysisHelper.RISK_WITH_IMPLEMENTED_CONTROLS;
        } else if (probType.equals(RiskAnalysisHelper.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS)) {
            riskControlState = RiskAnalysisHelper.RISK_WITH_ALL_CONTROLS;
        } else {
            riskControlState = RiskAnalysisHelper.RISK_PRE_CONTROLS;
        }

        int impactC = valueAdapter.getConfidentiality();
        int impactI = valueAdapter.getIntegrity();
        int impactA = valueAdapter.getAvailability();
        Integer[] reducedImpact = applyControlsToImpact(riskControlState, asset, impactC, impactI, impactA);
        if (reducedImpact != null) {
            impactC = reducedImpact[0];
            impactI = reducedImpact[1];
            impactA = reducedImpact[2];
        }

        // prob. / impact:
        int riskC = probability + impactC;
        int riskI = probability + impactI;
        int riskA = probability + impactA;

        int riskColour = 0;
        // risk values:
        switch (riskType) {
        case 'c':
            riskColour = getRiskColor(riskC, getTolerableRisks(asset, 'c'), numOfYellowFields);
            break;
        case 'i':
            riskColour = getRiskColor(riskI, getTolerableRisks(asset, 'i'), numOfYellowFields);
            break;
        case 'a':
            riskColour = getRiskColor(riskA, getTolerableRisks(asset, 'a'), numOfYellowFields);
            break;
        default: // do nothing
            break;
        }
        return riskColour;
    }
    
    private int getTolerableRisks(CnATreeElement elmt, char riskType) {
        try {
            RetrieveCnATreeElement command = new RetrieveCnATreeElement(Organization.TYPE_ID,
                    elmt.getScopeId(), RetrieveInfo.getPropertyInstance());
            CnATreeElement organization = getCommandService()
                    .executeCommand(command).getElement();
            if (organization instanceof Organization) {
                switch (riskType) {
                case 'c':
                    return organization.getNumericProperty(Organization.PROP_RISKACCEPT_CONFID);
                case 'i':
                    return organization.getNumericProperty(Organization.PROP_RISKACCEPT_INTEG);
                case 'a':
                    return organization.getNumericProperty(Organization.PROP_RISKACCEPT_AVAIL);
                default:
                    return 0;
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while getting tolerable risk", e);
        }
        return 0;
    }
    
    private int getRiskColor(int risk, int tolerableRisk, int numOfYellowFields){
        if(risk > tolerableRisk){
            return RiskAnalysisHelper.RISK_COLOR_RED;
        } else if(risk < tolerableRisk-numOfYellowFields+1){
            return RiskAnalysisHelper.RISK_COLOR_GREEN;
        } else {
            return RiskAnalysisHelper.RISK_COLOR_YELLOW;
        }
    }
    
    public String getColorString(int colorValue){
        switch(colorValue){
            case RiskAnalysisHelper.RISK_COLOR_GREEN:
                return "green";
            case RiskAnalysisHelper.RISK_COLOR_YELLOW:
                return "yellow";
            case RiskAnalysisHelper.RISK_COLOR_RED:
                return "red";
            default: 
                return "noColourDefined";
        }
    }
    
    public static ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }

}
