/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.filter;

import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.verinice.model.bp.Proceeding;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Utility class to determine whether to show a threat according to its scope's
 * proceeding.
 */
public final class ThreatByProceedingFilterUtil {

    /**
     * Check whether the threat should be shown when filtering according to its
     * scope's proceeding.
     *
     * This method is pretty costly since it triggers database calls.
     */
    public static boolean showThreatWhenProceedingFilterIsEnabled(BpThreat threat) {
        CnATreeElement scope = CnATreeElementScopeUtils.getScope(threat);
        if (!(scope instanceof ItNetwork)) {
            return true;
        }
        ItNetwork itNetwork = (ItNetwork) scope;
        Proceeding proceeding = itNetwork.getProceeding();
        if (proceeding == null) {
            return true;
        }
        CnATreeElement element = Retriever.retrieveElement(threat,
                new RetrieveInfo().setLinksUpProperties(true));

        Set<CnALink> linksUpFromThreat = element.getLinksUp();
        if (linksUpFromThreat.isEmpty()) {
            return true;
        }
        for (CnALink cnALink : linksUpFromThreat) {
            if (BpRequirement.REL_BP_REQUIREMENT_BP_THREAT.equals(cnALink.getRelationId())) {
                BpRequirement requirement = (BpRequirement) cnALink.getDependant();
                SecurityLevel requirementSecurityLevel = requirement.getSecurityLevel();
                if (proceeding.requires(requirementSecurityLevel)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ThreatByProceedingFilterUtil() {

    }

}
