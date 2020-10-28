/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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
package sernet.verinice.bp.rcp.risk.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import sernet.gs.ui.rcp.main.AbstractRequiresHUITypeFactoryTest;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.common.CnALink;

public class RiskUiUtilsTest extends AbstractRequiresHUITypeFactoryTest {

    // tests for threat decorators
    @Test
    public void no_risk_info_yields_unknown_effective_risk() {
        BpThreat threat = new BpThreat(null);
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threat);
        assertEquals(EffectiveRisk.UNKNOWN, risk);
    }

    @Test
    public void risk_wo_additional_safeguards_is_used_if_treatment_option_is_unspecified() {
        BpThreat threat = new BpThreat(null);
        threat.setRiskWithoutAdditionalSafeguards("risk1");
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threat);
        assertEquals("risk1", risk.getRiskId());
    }

    @Test
    public void risk_with_additional_safeguards_is_used_if_treatment_option_is_reduce() {
        BpThreat threat = new BpThreat(null);
        threat.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_REDUCTION);
        threat.setRiskWithAdditionalSafeguards("risk1");
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threat);
        assertEquals("risk1", risk.getRiskId());
    }

    @Test
    public void risk_is_unknown_if_treatment_option_is_reduce_and_reduced_risk_is_unspecified() {
        BpThreat threat = new BpThreat(null);
        threat.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_REDUCTION);
        threat.setRiskWithoutAdditionalSafeguards("risk1");
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threat);
        assertEquals(EffectiveRisk.UNKNOWN, risk);
    }

    @Test
    public void risk_avoidance_yields_treated_effective_risk() {
        BpThreat threat = new BpThreat(null);
        threat.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_AVOIDANCE);
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threat);
        assertEquals(EffectiveRisk.TREATED, risk);
    }

    @Test
    public void risk_transfer_yields_treated_effective_risk() {
        BpThreat threat = new BpThreat(null);
        threat.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_TRANSFER_OF_RISK);
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threat);
        assertEquals(EffectiveRisk.TREATED, risk);
    }

    @Test
    public void risk_acceptance_yields_treated_effective_risk() {
        BpThreat threat = new BpThreat(null);
        threat.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_ACCEPTANCE);
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threat);
        assertEquals(EffectiveRisk.TREATED, risk);
    }

    // tests for threat group decorators
    @Test
    public void no_effective_risk_for_empty_group() {
        BpThreatGroup group = new BpThreatGroup(null);
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(group);
        assertNull(risk);
    }

    @Test
    public void highest_specified_risk_is_used() {
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        threat1.setRiskWithoutAdditionalSafeguards("risk1");
        assertEquals("risk1", RiskUiUtils.getEffectiveRisk(group).getRiskId());
        BpThreat threat2 = new BpThreat(group);
        group.addChild(threat2);
        threat2.setRiskWithoutAdditionalSafeguards("risk2");
        assertEquals("risk2", RiskUiUtils.getEffectiveRisk(group).getRiskId());
    }

    @Test
    public void only_unspecified_children() {
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        assertEquals(EffectiveRisk.UNKNOWN, RiskUiUtils.getEffectiveRisk(group));
    }

    @Test
    public void one_specified_and_one_unspecified_child() {
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        BpThreat threat2 = new BpThreat(group);
        group.addChild(threat2);
        threat2.setRiskWithoutAdditionalSafeguards("risk2");
        assertEquals(EffectiveRisk.UNKNOWN, RiskUiUtils.getEffectiveRisk(group));
    }

    @Test
    public void only_acceptance_children() {
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        threat1.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_ACCEPTANCE);
        assertEquals(EffectiveRisk.TREATED, RiskUiUtils.getEffectiveRisk(group));
    }

    @Test
    public void unspecified_and_accepted_children() {
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        threat1.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_ACCEPTANCE);
        BpThreat threat2 = new BpThreat(group);
        group.addChild(threat2);
        assertEquals(EffectiveRisk.UNKNOWN, RiskUiUtils.getEffectiveRisk(group));
    }

    @Test
    public void only_contains_group() {
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreatGroup subgroup = new BpThreatGroup(group);
        group.addChild(subgroup);
        assertNull(RiskUiUtils.getEffectiveRisk(group));
    }

    @Test
    public void contains_group_and_child() {
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat = new BpThreat(group);
        group.addChild(threat);
        threat.setRiskWithAdditionalSafeguards("risk1");
        BpThreatGroup subgroup = new BpThreatGroup(group);
        group.addChild(subgroup);
        assertNull(RiskUiUtils.getEffectiveRisk(group));
    }

    // tests for target object decorators
    @Test
    public void no_effective_risk_if_risk_analysis_is_not_required() {
        ItSystem itSystem = new ItSystem(null);
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(itSystem);
        assertNull(risk);
    }

    @Test
    public void unknown_effective_risk_if_there_are_no_linked_risks() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.getEntity().setFlag(ItSystem.PROP_RISKANALYSIS_NECESSARY, true);
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(itSystem);
        assertEquals(EffectiveRisk.UNKNOWN, risk);
    }

    @Test
    public void highest_specified_risk_from_linked_threats_is_used() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.getEntity().setFlag(ItSystem.PROP_RISKANALYSIS_NECESSARY, true);
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        threat1.setRiskWithoutAdditionalSafeguards("risk1");
        new CnALink(threat1, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        assertEquals("risk1", RiskUiUtils.getEffectiveRisk(itSystem).getRiskId());

        BpThreat threat2 = new BpThreat(group);
        group.addChild(threat2);
        threat2.setRiskWithoutAdditionalSafeguards("risk2");
        new CnALink(threat2, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        assertEquals("risk2", RiskUiUtils.getEffectiveRisk(itSystem).getRiskId());
    }

    @Test
    public void only_unspecified_risks_in_linked_threats() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.getEntity().setFlag(ItSystem.PROP_RISKANALYSIS_NECESSARY, true);
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        new CnALink(threat1, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        assertEquals(EffectiveRisk.UNKNOWN, RiskUiUtils.getEffectiveRisk(itSystem));
    }

    @Test
    public void only_accepted_risks_in_linked_threats() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.getEntity().setFlag(ItSystem.PROP_RISKANALYSIS_NECESSARY, true);
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        threat1.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_ACCEPTANCE);
        new CnALink(threat1, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        assertEquals(EffectiveRisk.TREATED, RiskUiUtils.getEffectiveRisk(itSystem));
    }

    @Test
    public void unspecified_and_accepted_risks_in_linked_threats() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.getEntity().setFlag(ItSystem.PROP_RISKANALYSIS_NECESSARY, true);
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        group.addChild(threat1);
        new CnALink(threat1, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        BpThreat threat2 = new BpThreat(group);
        group.addChild(threat2);
        threat2.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_ACCEPTANCE);
        new CnALink(threat2, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        assertEquals(EffectiveRisk.UNKNOWN, RiskUiUtils.getEffectiveRisk(itSystem));
    }

    @Test
    public void high_and_unspecified_risks_in_linked_threats() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.getEntity().setFlag(ItSystem.PROP_RISKANALYSIS_NECESSARY, true);
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        threat1.setRiskWithoutAdditionalSafeguards("risk1");
        group.addChild(threat1);
        new CnALink(threat1, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        BpThreat threat2 = new BpThreat(group);
        group.addChild(threat2);
        new CnALink(threat2, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        assertEquals(EffectiveRisk.UNKNOWN, RiskUiUtils.getEffectiveRisk(itSystem));
    }

    @Test
    public void high_and_accepted_risks_in_linked_threats() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.getEntity().setFlag(ItSystem.PROP_RISKANALYSIS_NECESSARY, true);
        BpThreatGroup group = new BpThreatGroup(null);
        BpThreat threat1 = new BpThreat(group);
        threat1.setRiskWithoutAdditionalSafeguards("risk1");
        group.addChild(threat1);
        new CnALink(threat1, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        BpThreat threat2 = new BpThreat(group);
        group.addChild(threat2);
        threat2.setSimpleProperty(BpThreat.PROP_RISK_TREATMENT_OPTION,
                BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_ACCEPTANCE);
        new CnALink(threat2, itSystem, BpThreat.REL_BP_THREAT_BP_ITSYSTEM, null);
        assertEquals("risk1", RiskUiUtils.getEffectiveRisk(itSystem).getRiskId());
    }

}
