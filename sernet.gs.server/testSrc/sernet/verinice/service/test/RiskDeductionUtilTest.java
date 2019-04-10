/**
 * 
 */
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
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

    private BpThreat bpThreat;
    private BpRequirement bpRequirement;
    private Safeguard safeguard;
    private Application application;
    private DefaultRiskConfiguration riskConfiguration;
    private SafeguardGroup safeguardGroup;

    @Transactional
    @Rollback(true)
    @Test
    public void testBasicRiskDeduction() throws Exception {
        createTestModel();

        List<Frequency> frequencies = riskConfiguration.getFrequencies();
        List<Impact> impacts = riskConfiguration.getImpacts();

        for (Impact impact : impacts) {
            for (Frequency frequency : frequencies) {
                safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, impact.getId());
                safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, frequency.getId());

                safeguard = update(safeguard);
                bpThreat = reloadElement(bpThreat);
                String frequencyWithAdditionalSafeguards = bpThreat
                        .getFrequencyWithAdditionalSafeguards();
                String impactWithAdditionalSafeguards = bpThreat
                        .getImpactWithAdditionalSafeguards();

                assertEquals(frequency.getId(), frequencyWithAdditionalSafeguards);
                assertEquals(impact.getId(), impactWithAdditionalSafeguards);
            }
        }
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testBasicRiskDeductionSafeguardFrequencyNull() throws Exception {
        createTestModel();

        String frequencyWithoutAdditionalSafeguards = bpThreat
                .getFrequencyWithoutAdditionalSafeguards();
        String impactWithoutAdditionalSafeguards = bpThreat.getImpactWithoutAdditionalSafeguards();

        List<Frequency> frequencies = riskConfiguration.getFrequencies();
        List<Impact> impacts = riskConfiguration.getImpacts();
        Frequency frequency = frequencies.get(0);
        Impact impact = impacts.get(1);
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, impact.getId());
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, frequency.getId());

        safeguard = update(safeguard);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impact.getId(), bpThreat.getImpactWithAdditionalSafeguards());

        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, impact.getId());
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, null);

        safeguard = update(safeguard);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequencyWithoutAdditionalSafeguards,
                bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impactWithoutAdditionalSafeguards,
                bpThreat.getImpactWithAdditionalSafeguards());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testBasicRiskDeductionSafeguardImpactNull() throws Exception {
        createTestModel();

        String frequencyWithoutAdditionalSafeguards = bpThreat
                .getFrequencyWithoutAdditionalSafeguards();
        String impactWithoutAdditionalSafeguards = bpThreat.getImpactWithoutAdditionalSafeguards();

        List<Frequency> frequencies = riskConfiguration.getFrequencies();
        List<Impact> impacts = riskConfiguration.getImpacts();
        Frequency frequency = frequencies.get(1);
        Impact impact = impacts.get(1);
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, impact.getId());
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, frequency.getId());

        safeguard = update(safeguard);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impact.getId(), bpThreat.getImpactWithAdditionalSafeguards());

        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, null);
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, frequency.getId());

        safeguard = update(safeguard);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequencyWithoutAdditionalSafeguards,
                bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impactWithoutAdditionalSafeguards,
                bpThreat.getImpactWithAdditionalSafeguards());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testBasicRiskDeductionRequirementImpactNull() throws Exception {
        createTestModel();

        String frequencyWithoutAdditionalSafeguards = bpThreat
                .getFrequencyWithoutAdditionalSafeguards();
        String impactWithoutAdditionalSafeguards = bpThreat.getImpactWithoutAdditionalSafeguards();

        List<Frequency> frequencies = riskConfiguration.getFrequencies();
        List<Impact> impacts = riskConfiguration.getImpacts();
        Frequency frequency = frequencies.get(1);
        Impact impact = impacts.get(1);
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, impact.getId());
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, frequency.getId());

        safeguard = update(safeguard);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impact.getId(), bpThreat.getImpactWithAdditionalSafeguards());

        safeguard.getEntity().setFlag(Safeguard.PROP_REDUCE_RISK, false);
        bpRequirement.getEntity().setFlag(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK, false);
        bpRequirement.setSimpleProperty(BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT, null);
        bpRequirement = update(bpRequirement);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequencyWithoutAdditionalSafeguards,
                bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impactWithoutAdditionalSafeguards,
                bpThreat.getImpactWithAdditionalSafeguards());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testBasicRiskDeductionRequirementFrequencyNull() throws Exception {
        createTestModel();

        String frequencyWithoutAdditionalSafeguards = bpThreat
                .getFrequencyWithoutAdditionalSafeguards();
        String impactWithoutAdditionalSafeguards = bpThreat.getImpactWithoutAdditionalSafeguards();

        List<Frequency> frequencies = riskConfiguration.getFrequencies();
        List<Impact> impacts = riskConfiguration.getImpacts();
        Frequency frequency = frequencies.get(1);
        Impact impact = impacts.get(1);
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, impact.getId());
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, frequency.getId());

        safeguard = update(safeguard);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impact.getId(), bpThreat.getImpactWithAdditionalSafeguards());

        safeguard.getEntity().setFlag(Safeguard.PROP_REDUCE_RISK, false);
        bpRequirement.getEntity().setFlag(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK, false);
        bpRequirement.setSimpleProperty(BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY, null);
        bpRequirement = update(bpRequirement);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequencyWithoutAdditionalSafeguards,
                bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impactWithoutAdditionalSafeguards,
                bpThreat.getImpactWithAdditionalSafeguards());
    }

    // private void dumpCnATreeElement(CnATreeElement e) {
    //
    // System.out.println("--"+e.getClass().getSimpleName()+"--");
    // Map<String, PropertyList> typedPropertyLists =
    // e.getEntity().getTypedPropertyLists();
    // Set<Entry<String, PropertyList>> entrySet =
    // typedPropertyLists.entrySet();
    // for (Entry<String, PropertyList> entry : entrySet) {
    // System.out.println(entry.getKey()+"->"+printList(entry.getValue()));
    // }
    //
    // }
    //
    // private String printList(PropertyList value) {
    // StringBuilder stringBuilder = new StringBuilder();
    // if(value.getProperties()!=null || value.getProperties().size()==1) {
    // return value.getProperties().get(0).getPropertyValue();
    // }
    // return "";
    // }

    @Transactional
    @Rollback(true)
    @Test
    public void testBasicRiskDeduction3Safeguards() throws Exception {
        createTestModel();
        riskConfiguration = DefaultRiskConfiguration.getInstance();
        Safeguard safeguard1 = createSafeguard(safeguardGroup);
        Safeguard safeguard2 = createSafeguard(safeguardGroup);
        safeguard1.setTitel("Test-Safeguard1");
        safeguard1.getEntity().setFlag(Safeguard.PROP_REDUCE_RISK, true);
        safeguard1.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT,
                riskConfiguration.getImpacts().get(2).getId());
        safeguard1.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY,
                riskConfiguration.getFrequencies().get(2).getId());
        safeguard1 = update(safeguard1);
        safeguard2.setTitel("Test-Safeguard2");
        safeguard2.getEntity().setFlag(Safeguard.PROP_REDUCE_RISK, true);
        safeguard2.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT,
                riskConfiguration.getImpacts().get(3).getId());
        safeguard2.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY,
                riskConfiguration.getFrequencies().get(3).getId());
        safeguard2 = update(safeguard2);

        createLink(bpRequirement, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(bpRequirement, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        List<Frequency> frequencies = riskConfiguration.getFrequencies();
        List<Impact> impacts = riskConfiguration.getImpacts();
        Frequency frequency = frequencies.get(0);
        Impact impact = impacts.get(1);
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, impact.getId());
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, frequency.getId());

        safeguard = update(safeguard);
        bpThreat = reloadElement(bpThreat);

        assertEquals(frequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(impact.getId(), bpThreat.getImpactWithAdditionalSafeguards());

        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, impact.getId());
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, null);

        safeguard = update(safeguard);
        bpThreat = reloadElement(bpThreat);

        assertEquals(riskConfiguration.getFrequencies().get(2).getId(),
                bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(riskConfiguration.getImpacts().get(2).getId(),
                bpThreat.getImpactWithAdditionalSafeguards());
    }

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
        assertEquals(null, bpThreat.getRiskWithAdditionalSafeguards());

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
        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(null, bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getImpactWithAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithAdditionalSafeguards());

        bpThreat.setImpactWithoutAdditionalSafeguards(firstImpact.getId());
        RiskDeductionUtil.deduceRisk(bpThreat);

        Risk expectedRisk = riskConfiguration.getRisk(firstFrequency, firstImpact);

        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(firstImpact.getId(), bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(firstImpact.getId(), bpThreat.getImpactWithAdditionalSafeguards());
        assertEquals(expectedRisk.getId(), bpThreat.getRiskWithoutAdditionalSafeguards());
        assertEquals(expectedRisk.getId(), bpThreat.getRiskWithAdditionalSafeguards());

        bpThreat.setImpactWithoutAdditionalSafeguards(null);
        RiskDeductionUtil.deduceRisk(bpThreat);

        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(null, bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getImpactWithAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithAdditionalSafeguards());

    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionIfOnlySafeguardStrengthFrequencyIsSet() throws Exception {

        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup bpThreatGroup = createBpThreatGroup(itNetwork);
        BpThreat bpThreat = createBpThreat(bpThreatGroup);

        RiskConfiguration riskConfiguration = DefaultRiskConfiguration.getInstance();
        Frequency firstFrequency = riskConfiguration.getFrequencies().get(0);
        Impact firstImpact = riskConfiguration.getImpacts().get(0);
        Frequency lastFrequency = riskConfiguration.getFrequencies()
                .get(riskConfiguration.getFrequencies().size() - 1);
        BpRequirementGroup bpRequirementGroup = createRequirementGroup(itNetwork);
        BpRequirement bpRequirement = createBpRequirement(bpRequirementGroup);
        createLink(bpRequirement, bpThreat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        bpRequirement.getEntity().setFlag(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK, true);
        bpRequirement.setSimpleProperty(BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY,
                firstFrequency.getId());

        bpThreat.setFrequencyWithoutAdditionalSafeguards(lastFrequency.getId());
        bpThreat.setImpactWithoutAdditionalSafeguards(firstImpact.getId());
        RiskDeductionUtil.deduceRisk(bpThreat);

        Risk expectedRiskWithoutAdditionalSafeguards = riskConfiguration.getRisk(lastFrequency,
                firstImpact);
        Risk expectedRiskWithAdditionalSafeguards = riskConfiguration.getRisk(firstFrequency,
                firstImpact);

        assertEquals(lastFrequency.getId(), bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(firstImpact.getId(), bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(firstImpact.getId(), bpThreat.getImpactWithAdditionalSafeguards());
        assertEquals(expectedRiskWithoutAdditionalSafeguards.getId(),
                bpThreat.getRiskWithoutAdditionalSafeguards());
        assertEquals(expectedRiskWithAdditionalSafeguards.getId(),
                bpThreat.getRiskWithAdditionalSafeguards());

    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionIfOnlySafeguardStrengthImpactIsSet() throws Exception {

        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup bpThreatGroup = createBpThreatGroup(itNetwork);
        BpThreat bpThreat = createBpThreat(bpThreatGroup);

        RiskConfiguration riskConfiguration = DefaultRiskConfiguration.getInstance();
        Frequency firstFrequency = riskConfiguration.getFrequencies().get(0);
        Impact firstImpact = riskConfiguration.getImpacts().get(0);
        Impact lastImpact = riskConfiguration.getImpacts()
                .get(riskConfiguration.getImpacts().size() - 1);
        BpRequirementGroup bpRequirementGroup = createRequirementGroup(itNetwork);
        BpRequirement bpRequirement = createBpRequirement(bpRequirementGroup);
        createLink(bpRequirement, bpThreat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        bpRequirement.getEntity().setFlag(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK, true);
        bpRequirement.setSimpleProperty(BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT,
                firstImpact.getId());

        bpThreat.setFrequencyWithoutAdditionalSafeguards(firstFrequency.getId());
        bpThreat.setImpactWithoutAdditionalSafeguards(lastImpact.getId());
        RiskDeductionUtil.deduceRisk(bpThreat);

        Risk expectedRiskWithoutAdditionalSafeguards = riskConfiguration.getRisk(firstFrequency,
                lastImpact);
        Risk expectedRiskWithAdditionalSafeguards = riskConfiguration.getRisk(firstFrequency,
                firstImpact);

        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(lastImpact.getId(), bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(firstImpact.getId(), bpThreat.getImpactWithAdditionalSafeguards());
        assertEquals(expectedRiskWithoutAdditionalSafeguards.getId(),
                bpThreat.getRiskWithoutAdditionalSafeguards());
        assertEquals(expectedRiskWithAdditionalSafeguards.getId(),
                bpThreat.getRiskWithAdditionalSafeguards());

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
        assertEquals(firstFrequency.getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(lastImpact.getId(), bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(lastImpact.getId(), bpThreat.getImpactWithAdditionalSafeguards());
        assertEquals(expectedRisk.getId(), bpThreat.getRiskWithoutAdditionalSafeguards());
        assertEquals(expectedRisk.getId(), bpThreat.getRiskWithAdditionalSafeguards());

        bpThreat.setFrequencyWithoutAdditionalSafeguards(null);
        RiskDeductionUtil.deduceRisk(bpThreat);
        assertEquals(null, bpThreat.getFrequencyWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getFrequencyWithAdditionalSafeguards());
        assertEquals(lastImpact.getId(), bpThreat.getImpactWithoutAdditionalSafeguards());
        assertEquals(lastImpact.getId(), bpThreat.getImpactWithAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithoutAdditionalSafeguards());
        assertEquals(null, bpThreat.getRiskWithAdditionalSafeguards());

    }

    // private void dumpCnATreeElement(CnATreeElement e) {
    //
    // System.out.println("--"+e.getClass().getSimpleName()+"--");
    // Map<String, PropertyList> typedPropertyLists =
    // e.getEntity().getTypedPropertyLists();
    // Set<Entry<String, PropertyList>> entrySet =
    // typedPropertyLists.entrySet();
    // for (Entry<String, PropertyList> entry : entrySet) {
    // System.out.println(entry.getKey()+"->"+printList(entry.getValue()));
    // }
    //
    // }
    //
    // private String printList(PropertyList value) {
    // StringBuilder stringBuilder = new StringBuilder();
    // if(value.getProperties()!=null || value.getProperties().size()==1) {
    // return value.getProperties().get(0).getPropertyValue();
    // }
    // return "";
    // }

    private void createTestModel() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization(); // new
                                                         // ItNetwork(bpModel);
        riskConfiguration = DefaultRiskConfiguration.getInstance();
        itNetwork.setRiskConfiguration(riskConfiguration);
        itNetwork = update(itNetwork);
        List<Frequency> frequencies = riskConfiguration.getFrequencies();
        List<Impact> impacts = riskConfiguration.getImpacts();
        String lastImpact = impacts.get(impacts.size() - 1).getId();
        String lastFrequency = frequencies.get(frequencies.size() - 1).getId();

        BpThreatGroup bpThreatGroup = createBpThreatGroup(itNetwork);
        bpThreat = createBpThreat(bpThreatGroup);
        bpThreat.setTitel("Test-Threat");
        bpThreat = update(bpThreat);

        ApplicationGroup applicationGroup = createBpApplicationGroup(itNetwork);
        application = createBpApplication(applicationGroup);
        BpRequirementGroup bpRequirementGroup = createRequirementGroup(application);
        bpRequirement = createBpRequirement(bpRequirementGroup);

        safeguardGroup = createSafeguardGroup(application);
        safeguard = createSafeguard(safeguardGroup);

        bpRequirement.setTitel("Test-Requirement");
        bpRequirement.setDeductionOfImplementation(true);
        bpRequirement.getEntity().setFlag(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK, true);
        safeguard.setTitel("Test-Safeguard");
        safeguard.getEntity().setFlag(Safeguard.PROP_REDUCE_RISK, true);
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, lastImpact);
        safeguard.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, lastFrequency);

        bpThreat.setImpactWithoutAdditionalSafeguards(lastImpact);
        bpThreat.setFrequencyWithoutAdditionalSafeguards(lastFrequency);
        bpThreat = update(bpThreat);

        safeguard = update(safeguard);
        bpRequirement = update(bpRequirement);

        createLink(bpRequirement, application, BpRequirement.REL_BP_REQUIREMENT_BP_APPLICATION);
        createLink(bpRequirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(bpRequirement, bpThreat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        createLink(bpThreat, application, BpThreat.REL_BP_THREAT_BP_APPLICATION);

        application = reloadElement(application);
        bpThreat = reloadElement(bpThreat);
        safeguard = reloadElement(safeguard);
        bpRequirement = reloadElement(bpRequirement);
    }

}
