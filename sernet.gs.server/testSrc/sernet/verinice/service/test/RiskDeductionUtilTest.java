/**
 * 
 */
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.service.bp.risk.RiskDeductionUtil;

/**
 * @author urszeidler
 *
 */
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class RiskDeductionUtilTest extends AbstractModernizedBaseProtection {

    @Transactional
    @Rollback(true)
    @Test
    public void testRiskDeductionWithSparseConfig() throws Exception {
        ItNetwork itNetwork = createNewBPOrganization();

        Frequency frequency = new Frequency("frequency1", "Default frequency",
                "Default frequency of occurrence");
        Impact impact = new Impact("impact1", "Default impact", "Default impact");
        Risk risk = new Risk("risk1", "Default risk", "Default risk", null);

        RiskConfiguration riskConfiguration = new RiskConfiguration(
                Collections.singletonList(frequency), Collections.singletonList(impact),
                Collections.singletonList(risk), new Integer[][] { new Integer[] { -1 } });
        itNetwork.setRiskConfiguration(riskConfiguration);
        itNetwork = update(itNetwork);

        BpThreatGroup bpThreatGroup = createBpThreatGroup(itNetwork);
        BpThreat bpThreat = createBpThreat(bpThreatGroup);
        bpThreat.setTitel("Test-Threat");
        bpThreat.setFrequencyWithoutAdditionalSafeguards("frequency1");
        bpThreat.setImpactWithoutAdditionalSafeguards("impact1");
        bpThreat = update(bpThreat);

        RiskDeductionUtil.deduceRisk(bpThreat);
        assertEquals(null, bpThreat.getRiskWithoutAdditionalSafeguards());

    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionOfFrequencyWithSafeguards() throws Exception {

        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup bpThreatGroup = createBpThreatGroup(itNetwork);
        BpThreat bpThreat = createBpThreat(bpThreatGroup);

        RiskConfiguration riskConfiguration = DefaultRiskConfiguration.getInstance();
        Frequency firstFrequency = riskConfiguration.getFrequencies().get(0);
        Impact firstImpact = riskConfiguration.getImpacts().get(0);

        bpThreat.setFrequencyWithoutAdditionalSafeguards(firstFrequency.getId());
        RiskDeductionUtil.deduceRisk(bpThreat);

        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithoutAdditionalSafeguards());

        bpThreat.setImpactWithoutAdditionalSafeguards(firstImpact.getId());
        RiskDeductionUtil.deduceRisk(bpThreat);

        Risk expectedRisk = riskConfiguration.getRisk(firstFrequency, firstImpact);

        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(firstImpact.getId(), bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(expectedRisk.getId(), bpThreat.getRiskWithoutAdditionalSafeguards());

        bpThreat.setImpactWithoutAdditionalSafeguards(null);
        RiskDeductionUtil.deduceRisk(bpThreat);

        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithoutAdditionalSafeguards());

    }

    @Transactional
    @Rollback(true)
    @Test
    public void setAndUnsetRiskProperties() throws Exception {

        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup bpThreatGroup = createBpThreatGroup(itNetwork);
        BpThreat bpThreat = createBpThreat(bpThreatGroup);

        RiskConfiguration riskConfiguration = DefaultRiskConfiguration.getInstance();
        Frequency firstFrequency = riskConfiguration.getFrequencies().get(0);
        Impact lastImpact = riskConfiguration.getImpacts()
                .get(riskConfiguration.getImpacts().size() - 1);

        bpThreat.setFrequencyWithoutAdditionalSafeguards(firstFrequency.getId());
        bpThreat.setImpactWithoutAdditionalSafeguards(lastImpact.getId());
        RiskDeductionUtil.deduceRisk(bpThreat);

        Risk expectedRisk = riskConfiguration.getRisk(firstFrequency, lastImpact);

        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(lastImpact.getId(), bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(expectedRisk.getId(), bpThreat.getRiskWithoutAdditionalSafeguards());

        bpThreat.setFrequencyWithoutAdditionalSafeguards(null);
        RiskDeductionUtil.deduceRisk(bpThreat);
        assertEquals(null, bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(lastImpact.getId(), bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithoutAdditionalSafeguards());

    }

}
