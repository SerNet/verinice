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

    private CnATreeElement treeElement;
    private static int riskLevel;
    private static String imagePath;

    @Override
    public void decorate(Object element, IDecoration decoration) {
        Activator.inheritVeriniceContextState();
        treeElement = (CnATreeElement) element;
        if (prefEnabled() && isApplicable()) {
            castTreeElement();
            setImagePath();
            decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(imagePath));
        }
    }

    private boolean prefEnabled() {
        return Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.SHOW_RISK_ANALYSIS_DECORATOR);
    }

    private boolean isApplicable() {
        if (treeElement instanceof Asset ||
            treeElement instanceof IncidentScenario ||
            treeElement instanceof Threat ||
            treeElement instanceof Vulnerability) {
            return true;
        }
        return false;
    }
    
    private void castTreeElement() {
        if (treeElement instanceof Asset) {
            setRiskLevelForAsset();
        } else if (treeElement instanceof IncidentScenario) {
            setRiskLevelForIncidentScenario();
        } else if (treeElement instanceof Threat) {
            setRiskLevelForThreat();
        } else if (treeElement instanceof Vulnerability) {
            setRiskLevelForVulnerability();
        }
    }

    private void setRiskLevelForAsset() {
        Entity treeElementEntity = ((Asset) treeElement).getEntity();
        int riskValueConfidentiality = Integer.parseInt(
                treeElementEntity.getSimpleValue("asset_riskvalue_c"));
        int riskValueIntegrity = Integer.parseInt(
                treeElementEntity.getSimpleValue("asset_riskvalue_i"));
        int riskValueAvailability = Integer.parseInt(
                treeElementEntity.getSimpleValue("asset_riskvalue_a"));
        // Possible range is 0..8
        int riskValueMax = NumberUtils.max(
                riskValueConfidentiality, riskValueIntegrity, riskValueAvailability);
        setRiskLevelForAssetOrIncidentScenario(riskValueMax);
    }
    
    private void setRiskLevelForIncidentScenario() {
        treeElement = (IncidentScenario) treeElement;
        int riskValueIncidentScenario = Integer.parseInt(
                treeElement.getEntity().getSimpleValue("incscen_likelihood"));
        setRiskLevelForAssetOrIncidentScenario(riskValueIncidentScenario);
    }
    
    private void setRiskLevelForAssetOrIncidentScenario(int riskValue) {
        if (riskValue > RISK_THRESHOLD_HIGH) {
            riskLevel = COMBINED_RISK_VALUE_HIGH;
        } else if (riskValue > RISK_THRESHOLD_MEDIUM) {
            riskLevel = COMBINED_RISK_VALUE_MEDIUM;
        } else {
            riskLevel = COMBINED_RISK_VALUE_LOW;
        }
    } 

    private void setRiskLevelForThreat() {
        treeElement = (Threat) treeElement;
        int riskValueThreatLikelihood = Integer.parseInt(
                treeElement.getEntity().getSimpleValue("threat_likelihood"));
        int riskValueThreatImpact = Integer.parseInt(
                treeElement.getEntity().getSimpleValue("threat_impact"));
        int riskValue = Math.max(riskValueThreatLikelihood, riskValueThreatImpact);
        if (riskValue > RISK_THRESHOLD_HIGH_THREAT) {
            riskLevel = COMBINED_RISK_VALUE_HIGH;
        } else if (riskValue > RISK_THRESHOLD_MEDIUM_THREAT) {
            riskLevel = COMBINED_RISK_VALUE_MEDIUM;
        } else {
            riskLevel = COMBINED_RISK_VALUE_LOW;
        }
    }

    private void setRiskLevelForVulnerability() {
        treeElement = (Vulnerability) treeElement;
        int riskValue = Integer.parseInt(
                treeElement.getEntity().getSimpleValue("vulnerability_level"));
        if (riskValue > RISK_THRESHOLD_HIGH_VULNERABILITY) {
            riskLevel = COMBINED_RISK_VALUE_HIGH;
        } else if (riskValue > RISK_THRESHOLD_MEDIUM_VULNERABILITY) {
            riskLevel = COMBINED_RISK_VALUE_MEDIUM;
        } else {
            riskLevel = COMBINED_RISK_VALUE_LOW;
        }
    }

    private void setImagePath() {
        if (riskLevel == COMBINED_RISK_VALUE_HIGH) {
            imagePath = IMAGE_PATH_RED;
        } else if (riskLevel == COMBINED_RISK_VALUE_MEDIUM) {
            imagePath = IMAGE_PATH_YELLOW;
        } else if (riskLevel == COMBINED_RISK_VALUE_LOW) {
            imagePath = IMAGE_PATH_GREEN;
        } else {
            imagePath = IMAGE_PATH_EMPTY;
        }
    }
}