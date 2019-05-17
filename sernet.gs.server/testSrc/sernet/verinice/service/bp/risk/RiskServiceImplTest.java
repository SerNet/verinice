package sernet.verinice.service.bp.risk;

import java.util.Collections;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import junit.framework.Assert;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
@Transactional
public class RiskServiceImplTest extends AbstractModernizedBaseProtection {

    @Resource(name = "itbpRiskService")
    private RiskService riskService;

    private static final DefaultRiskConfiguration defaultRiskConfiguration = DefaultRiskConfiguration
            .getInstance();
    private static int numberOfFrequenciesInDefaultConfig = defaultRiskConfiguration
            .getFrequencies().size();
    private static int numberOfImpactsInDefaultConfig = defaultRiskConfiguration.getImpacts()
            .size();
    private static int numberOfRiskCategoriesInDefaultConfig = defaultRiskConfiguration.getRisks()
            .size();

    private static final Frequency highestFrequencyInDefaultConfig = defaultRiskConfiguration
            .getFrequencies().get(numberOfFrequenciesInDefaultConfig - 1);
    private static final Impact highestImpactInDefaultConfig = defaultRiskConfiguration.getImpacts()
            .get(numberOfImpactsInDefaultConfig - 1);
    private static final Risk highestRiskCategoryInDefaultConfig = defaultRiskConfiguration
            .getRisks().get(numberOfRiskCategoriesInDefaultConfig - 1);

    @Test
    public void cacheIsUpdatedWhenUpdatingConfiguration() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        Assert.assertEquals(null, riskService.findRiskConfiguration(itNetwork.getDbId()));

        RiskConfiguration newRiskConfiguration = DefaultRiskConfiguration.getInstance().withRisk(
                highestFrequencyInDefaultConfig, highestImpactInDefaultConfig,
                defaultRiskConfiguration.getRisks().get(2));

        RiskConfigurationUpdateContext riskConfigurationUpdateContext = new RiskConfigurationUpdateContext(
                itNetwork.getUuid(), newRiskConfiguration, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        riskService.updateRiskConfiguration(riskConfigurationUpdateContext);
        Assert.assertEquals(newRiskConfiguration,
                riskService.findRiskConfiguration(itNetwork.getDbId()));
    }

    @Test
    public void riskValuesAreRemovedWhenFrequencyIsRemovedFromConfiguration()
            throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup threatGroup = createBpThreatGroup(itNetwork);
        BpThreat threat = createBpThreat(threatGroup);
        threat.setFrequencyWithoutAdditionalSafeguards(highestFrequencyInDefaultConfig.getId());
        threat.setImpactWithoutAdditionalSafeguards(highestImpactInDefaultConfig.getId());
        RiskDeductionUtil.deduceRisk(threat);
        threat = update(threat);
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithAdditionalSafeguards());

        RiskConfiguration newRiskConfiguration = defaultRiskConfiguration.withValues(
                defaultRiskConfiguration.getFrequencies().subList(0,
                        numberOfFrequenciesInDefaultConfig - 1),
                defaultRiskConfiguration.getImpacts(), defaultRiskConfiguration.getRisks());

        RiskConfigurationUpdateContext riskConfigurationUpdateContext = new RiskConfigurationUpdateContext(
                itNetwork.getUuid(), newRiskConfiguration,
                Collections.singletonList(highestFrequencyInDefaultConfig), Collections.emptyList(),
                Collections.emptyList());
        riskService.updateRiskConfiguration(riskConfigurationUpdateContext);

        threat = reloadElement(threat);
        Assert.assertEquals(null, threat.getFrequencyWithoutAdditionalSafeguards());
        Assert.assertEquals(null, threat.getFrequencyWithAdditionalSafeguards());
        Assert.assertEquals(null, threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(null, threat.getRiskWithAdditionalSafeguards());
    }

    @Test
    public void riskValuesAreRemovedWhenRiskIsRemovedFromConfiguration() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup threatGroup = createBpThreatGroup(itNetwork);
        BpThreat threat = createBpThreat(threatGroup);
        threat.setFrequencyWithoutAdditionalSafeguards(highestFrequencyInDefaultConfig.getId());
        threat.setImpactWithoutAdditionalSafeguards(highestImpactInDefaultConfig.getId());
        RiskDeductionUtil.deduceRisk(threat);
        threat = update(threat);
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithAdditionalSafeguards());

        RiskConfiguration newRiskConfiguration = defaultRiskConfiguration.withValues(
                defaultRiskConfiguration.getFrequencies(), defaultRiskConfiguration.getImpacts(),
                defaultRiskConfiguration.getRisks().subList(0,
                        numberOfRiskCategoriesInDefaultConfig - 1));

        RiskConfigurationUpdateContext riskConfigurationUpdateContext = new RiskConfigurationUpdateContext(
                itNetwork.getUuid(), newRiskConfiguration, Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(highestRiskCategoryInDefaultConfig));
        riskService.updateRiskConfiguration(riskConfigurationUpdateContext);

        threat = reloadElement(threat);
        Assert.assertEquals(null, threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(null, threat.getRiskWithAdditionalSafeguards());
    }

    @Test
    public void riskValuesAreRemovedWhenValueIsRemovedFromRiskMatrix() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup threatGroup = createBpThreatGroup(itNetwork);
        BpThreat threat = createBpThreat(threatGroup);
        threat.setFrequencyWithoutAdditionalSafeguards(highestFrequencyInDefaultConfig.getId());
        threat.setImpactWithoutAdditionalSafeguards(highestImpactInDefaultConfig.getId());
        RiskDeductionUtil.deduceRisk(threat);
        threat = update(threat);
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithAdditionalSafeguards());

        RiskConfiguration newRiskConfiguration = defaultRiskConfiguration
                .withRisk(highestFrequencyInDefaultConfig, highestImpactInDefaultConfig, null);

        RiskConfigurationUpdateContext riskConfigurationUpdateContext = new RiskConfigurationUpdateContext(
                itNetwork.getUuid(), newRiskConfiguration, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        riskService.updateRiskConfiguration(riskConfigurationUpdateContext);

        threat = reloadElement(threat);
        Assert.assertEquals(null, threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(null, threat.getRiskWithAdditionalSafeguards());
    }

    @Test
    public void riskValuesAreChangedWhenValueIsChangedInRiskMatrix() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup threatGroup = createBpThreatGroup(itNetwork);
        BpThreat threat = createBpThreat(threatGroup);
        threat.setFrequencyWithoutAdditionalSafeguards(highestFrequencyInDefaultConfig.getId());
        threat.setImpactWithoutAdditionalSafeguards(highestImpactInDefaultConfig.getId());
        RiskDeductionUtil.deduceRisk(threat);
        threat = update(threat);
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithAdditionalSafeguards());

        RiskConfiguration newRiskConfiguration = defaultRiskConfiguration.withRisk(
                highestFrequencyInDefaultConfig, highestImpactInDefaultConfig,
                defaultRiskConfiguration.getRisks().get(0));

        RiskConfigurationUpdateContext riskConfigurationUpdateContext = new RiskConfigurationUpdateContext(
                itNetwork.getUuid(), newRiskConfiguration, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        riskService.updateRiskConfiguration(riskConfigurationUpdateContext);

        threat = reloadElement(threat);
        Assert.assertEquals(newRiskConfiguration.getRisks().get(0).getId(),
                threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(newRiskConfiguration.getRisks().get(0).getId(),
                threat.getRiskWithAdditionalSafeguards());
    }

    @Test
    public void riskValuesAreAppliedWhenValueIsSpecifiedInRiskMatrix() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        RiskConfiguration newRiskConfiguration = defaultRiskConfiguration
                .withRisk(highestFrequencyInDefaultConfig, highestImpactInDefaultConfig, null);

        RiskConfigurationUpdateContext riskConfigurationUpdateContext = new RiskConfigurationUpdateContext(
                itNetwork.getUuid(), newRiskConfiguration, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        riskService.updateRiskConfiguration(riskConfigurationUpdateContext);

        BpThreatGroup threatGroup = createBpThreatGroup(itNetwork);
        BpThreat threat = createBpThreat(threatGroup);
        threat.setFrequencyWithoutAdditionalSafeguards(highestFrequencyInDefaultConfig.getId());
        threat.setImpactWithoutAdditionalSafeguards(highestImpactInDefaultConfig.getId());
        RiskDeductionUtil.deduceRisk(threat);
        threat = update(threat);
        Assert.assertEquals(null, threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(null, threat.getRiskWithoutAdditionalSafeguards(),
                threat.getRiskWithAdditionalSafeguards());

        riskConfigurationUpdateContext = new RiskConfigurationUpdateContext(itNetwork.getUuid(),
                defaultRiskConfiguration, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList());
        riskService.updateRiskConfiguration(riskConfigurationUpdateContext);

        threat = reloadElement(threat);
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithoutAdditionalSafeguards());
        Assert.assertEquals(highestRiskCategoryInDefaultConfig.getId(),
                threat.getRiskWithAdditionalSafeguards());
    }
}
