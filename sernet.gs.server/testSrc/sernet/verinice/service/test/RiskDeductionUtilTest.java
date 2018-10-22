/**
 * 
 */
package sernet.verinice.service.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyList;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.bp.risk.DefaultRisk;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Link;
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
				String frequencyWithAdditionalSafeguards = bpThreat.getFrequencyWithAdditionalSafeguards();
				String impactWithAdditionalSafeguards = bpThreat.getImpactWithAdditionalSafeguards();
				
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
		
		String frequencyWithoutAdditionalSafeguards = bpThreat.getFrequencyWithoutAdditionalSafeguards();
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
		
		assertEquals(frequencyWithoutAdditionalSafeguards, bpThreat.getFrequencyWithAdditionalSafeguards());
		assertEquals(impactWithoutAdditionalSafeguards, bpThreat.getImpactWithAdditionalSafeguards());
	}

    @Transactional
    @Rollback(true)
	@Test
	public void testBasicRiskDeductionSafeguardImpactNull() throws Exception {
		createTestModel();
		
		String frequencyWithoutAdditionalSafeguards = bpThreat.getFrequencyWithoutAdditionalSafeguards();
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
		
		assertEquals(frequencyWithoutAdditionalSafeguards, bpThreat.getFrequencyWithAdditionalSafeguards());
		assertEquals(impactWithoutAdditionalSafeguards, bpThreat.getImpactWithAdditionalSafeguards());
	}

    @Transactional
    @Rollback(true)
	@Test
	public void testBasicRiskDeductionRequiermentImpactNull() throws Exception {
		createTestModel();
		
		String frequencyWithoutAdditionalSafeguards = bpThreat.getFrequencyWithoutAdditionalSafeguards();
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
		
		bpRequirement.getEntity().setFlag(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK, false);
		bpRequirement.setSimpleProperty(BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT, null);
		bpRequirement = update(bpRequirement);
		bpThreat = reloadElement(bpThreat);
		
		assertEquals(frequencyWithoutAdditionalSafeguards, bpThreat.getFrequencyWithAdditionalSafeguards());
		assertEquals(impactWithoutAdditionalSafeguards, bpThreat.getImpactWithAdditionalSafeguards());
	}

    @Transactional
    @Rollback(true)
	@Test
	public void testBasicRiskDeductionRequiermentFrequnecyNull() throws Exception {
		createTestModel();
		
		String frequencyWithoutAdditionalSafeguards = bpThreat.getFrequencyWithoutAdditionalSafeguards();
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
		
		bpRequirement.getEntity().setFlag(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK, false);
		bpRequirement.setSimpleProperty(BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY, null);
		bpRequirement = update(bpRequirement);
		bpThreat = reloadElement(bpThreat);
		
		assertEquals(frequencyWithoutAdditionalSafeguards, bpThreat.getFrequencyWithAdditionalSafeguards());
		assertEquals(impactWithoutAdditionalSafeguards, bpThreat.getImpactWithAdditionalSafeguards());
	}

    
//	private void dumpCnATreeElement(CnATreeElement e) {
//		
//		System.out.println("--"+e.getClass().getSimpleName()+"--");
//		Map<String, PropertyList> typedPropertyLists = e.getEntity().getTypedPropertyLists();
//		Set<Entry<String, PropertyList>> entrySet = typedPropertyLists.entrySet();
//		for (Entry<String, PropertyList> entry : entrySet) {
//			System.out.println(entry.getKey()+"->"+printList(entry.getValue()));
//		}
//		
//	}
//
//	private String printList(PropertyList value) {
//		StringBuilder stringBuilder = new StringBuilder();
//		if(value.getProperties()!=null || value.getProperties().size()==1) {
//			return value.getProperties().get(0).getPropertyValue();
//		}
//		return "";
//	}

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
		safeguard1.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, riskConfiguration.getImpacts().get(2).getId());
		safeguard1.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, riskConfiguration.getFrequencies().get(2).getId());
		safeguard1 = update(safeguard1);
		safeguard2.setTitel("Test-Safeguard2");
		safeguard2.getEntity().setFlag(Safeguard.PROP_REDUCE_RISK, true);
		safeguard2.setSimpleProperty(Safeguard.PROP_STRENGTH_IMPACT, riskConfiguration.getImpacts().get(3).getId());
		safeguard2.setSimpleProperty(Safeguard.PROP_STRENGTH_FREQUENCY, riskConfiguration.getFrequencies().get(3).getId());
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
		
		assertEquals(riskConfiguration.getFrequencies().get(2).getId(), bpThreat.getFrequencyWithAdditionalSafeguards());
		assertEquals(riskConfiguration.getImpacts().get(2).getId(), bpThreat.getImpactWithAdditionalSafeguards());
	}

	//	private void dumpCnATreeElement(CnATreeElement e) {
	//		
	//		System.out.println("--"+e.getClass().getSimpleName()+"--");
	//		Map<String, PropertyList> typedPropertyLists = e.getEntity().getTypedPropertyLists();
	//		Set<Entry<String, PropertyList>> entrySet = typedPropertyLists.entrySet();
	//		for (Entry<String, PropertyList> entry : entrySet) {
	//			System.out.println(entry.getKey()+"->"+printList(entry.getValue()));
	//		}
	//		
	//	}
	//
	//	private String printList(PropertyList value) {
	//		StringBuilder stringBuilder = new StringBuilder();
	//		if(value.getProperties()!=null || value.getProperties().size()==1) {
	//			return value.getProperties().get(0).getPropertyValue();
	//		}
	//		return "";
	//	}
	
		private void createTestModel() throws CommandException {
			ItNetwork itNetwork = createNewBPOrganization();  //new ItNetwork(bpModel);
			riskConfiguration = DefaultRiskConfiguration.getInstance();
			itNetwork.setRiskConfiguration(riskConfiguration);
			itNetwork = update(itNetwork);
			List<Frequency> frequencies = riskConfiguration.getFrequencies();
			List<Impact> impacts = riskConfiguration.getImpacts();
			String lastImpact = impacts.get(impacts.size()-1).getId();
			String lastFrequency = frequencies.get(frequencies.size()-1).getId();
	
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
			
			bpRequirement.setTitel("Test-Requierment");
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
