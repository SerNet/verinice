/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Moritz Reiter <mr[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.connect.Entity;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * Creates an overlay Decorator (green, yellow or red dot) for Assets, Scenarios, Threats and
 * Vulnerabilities based on different risk analysis values.
 * 
 * @author Moritz Reiter <mr[at]sernet[dot]de>
 */
public class RiskAnalysisDecorator extends LabelProvider implements ILightweightLabelDecorator {
    
    private static final Logger LOG = Logger.getLogger(RiskAnalysisDecorator.class);
    
    private static final int NO_RISK_VALUE = -1;
    
    private static final int COMBINED_RISK_VALUE_LOW = 0;
    private static final int COMBINED_RISK_VALUE_MEDIUM = 1;
    private static final int COMBINED_RISK_VALUE_HIGH = 2;

    // Range is 0..8
    private static final int RISK_THRESHOLD_MEDIUM = 2;
    private static final int RISK_THRESHOLD_HIGH = 5;
    
    // Range is 0..5
    private static final int RISK_THRESHOLD_MEDIUM_THREAT = 1;
    private static final int RISK_THRESHOLD_HIGH_THREAT = 3;
    
    // Range is 0..3
    private static final int RISK_THRESHOLD_MEDIUM_VULNERABILITY = 0;
    private static final int RISK_THRESHOLD_HIGH_VULNERABILITY = 1;
    
    private static final String IMAGE_PATH_GREEN = "overlays/dot_green.png";
    private static final String IMAGE_PATH_YELLOW = "overlays/dot_yellow.png";
    private static final String IMAGE_PATH_RED = "overlays/dot_red.png";
    private static final String IMAGE_PATH_EMPTY = "overlays/empty.png";

    @Override
    public void decorate(Object element, IDecoration decoration) {
        Activator.inheritVeriniceContextState();
        CnATreeElement treeElement = null;
        if(element instanceof CnATreeElement){
            treeElement = (CnATreeElement) element; 
            if (prefEnabled() && isApplicable(treeElement)) {
                int riskLevel = getRiskLevel(treeElement);
                if(riskLevel != NO_RISK_VALUE){
                    decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(getImagePath(riskLevel)));
                }
            }
        }
    }

    private boolean prefEnabled() {
        return Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.SHOW_RISK_ANALYSIS_DECORATOR);
    }

    private boolean isApplicable(CnATreeElement element) {
        if (Asset.TYPE_ID.equals(element.getTypeId()) ||
            IncidentScenario.TYPE_ID.equals(element.getTypeId()) ||
            Threat.TYPE_ID.equals(element.getTypeId()) ||
            Vulnerability.TYPE_ID.equals(element.getTypeId())) {
                return true;
        }
        return false;
    }

    private int getRiskLevel(CnATreeElement element) {
        try{
            if (Asset.TYPE_ID.equals(element.getTypeId())) {
                return getRiskLevelForAsset(element);
            } else if (IncidentScenario.TYPE_ID.equals(element.getTypeId())) {
                return getRiskLevelForIncidentScenario(element);
            } else if (Threat.TYPE_ID.equals(element.getTypeId())) {
                return getRiskLevelForThreat(element);
            } else if (Vulnerability.TYPE_ID.equals(element.getTypeId())) {
                return getRiskLevelForVulnerability(element);
            }
        } catch (NumberFormatException e){
            LOG.warn("Error on parsing risklevel for element" + element.getUuid(), e);
        }
        return NO_RISK_VALUE;
    }

    private int getRiskLevelForAsset(CnATreeElement element)  throws NumberFormatException{
        Entity treeElementEntity = element.getEntity();
        try{
            int riskValueConfidentiality = Integer.parseInt(
                    treeElementEntity.getSimpleValue("asset_riskvalue_c"));
            int riskValueIntegrity = Integer.parseInt(
                    treeElementEntity.getSimpleValue("asset_riskvalue_i"));
            int riskValueAvailability = Integer.parseInt(
                    treeElementEntity.getSimpleValue("asset_riskvalue_a"));
            // Possible range is 0..8
            int riskValueMax = NumberUtils.max(
                    riskValueConfidentiality, riskValueIntegrity, riskValueAvailability);
            return getRiskLevelForAssetOrIncidentScenario(riskValueMax);
        } catch (NumberFormatException e){
            throw e;
        }
    }
    
    private int getRiskLevelForIncidentScenario(CnATreeElement element) throws NumberFormatException {
        try{
            int riskValueIncidentScenario = Integer.parseInt(
                    element.getEntity().getSimpleValue("incscen_likelihood"));
            return getRiskLevelForAssetOrIncidentScenario(riskValueIncidentScenario);
        } catch (NumberFormatException e){
            throw e;
        }
    }
    
    private int getRiskLevelForAssetOrIncidentScenario(int riskValue) {
        if (riskValue > RISK_THRESHOLD_HIGH) {
            return COMBINED_RISK_VALUE_HIGH;
        } else if (riskValue > RISK_THRESHOLD_MEDIUM) {
            return COMBINED_RISK_VALUE_MEDIUM;
        } else {
            return COMBINED_RISK_VALUE_LOW;
        }
    } 

    private int getRiskLevelForThreat(CnATreeElement element) throws NumberFormatException{
        try{
            int riskValueThreatLikelihood = Integer.parseInt(
                    element.getEntity().getSimpleValue("threat_likelihood"));
            int riskValueThreatImpact = Integer.parseInt(
                    element.getEntity().getSimpleValue("threat_impact"));
            int riskValue = Math.max(riskValueThreatLikelihood, riskValueThreatImpact);
            if (riskValue > RISK_THRESHOLD_HIGH_THREAT) {
                return COMBINED_RISK_VALUE_HIGH;
            } else if (riskValue > RISK_THRESHOLD_MEDIUM_THREAT) {
                return COMBINED_RISK_VALUE_MEDIUM;
            } else {
                return COMBINED_RISK_VALUE_LOW;
            }
        } catch (NumberFormatException e){
            throw e;
        }
    }

    private int getRiskLevelForVulnerability(CnATreeElement element) throws NumberFormatException{
        try{
            int riskValue = Integer.parseInt(
                    element.getEntity().getSimpleValue("vulnerability_level"));
            if (riskValue > RISK_THRESHOLD_HIGH_VULNERABILITY) {
                return COMBINED_RISK_VALUE_HIGH;
            } else if (riskValue > RISK_THRESHOLD_MEDIUM_VULNERABILITY) {
                return COMBINED_RISK_VALUE_MEDIUM;
            } else {
                return COMBINED_RISK_VALUE_LOW;
            }
        } catch (NumberFormatException e){
            throw e;
        }
    }

    private String getImagePath(int riskLevel) {
        switch(riskLevel){
            case COMBINED_RISK_VALUE_HIGH:return IMAGE_PATH_RED;
            case COMBINED_RISK_VALUE_MEDIUM:return IMAGE_PATH_YELLOW;
            case COMBINED_RISK_VALUE_LOW:return IMAGE_PATH_GREEN;
            default: return IMAGE_PATH_EMPTY;
        }
    }
}