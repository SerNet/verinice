package sernet.verinice.service.bp.risk;

import java.util.Collections;

import javax.annotation.Resource;

import org.junit.Test;

import junit.framework.Assert;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

public class RiskServiceImplTest extends AbstractModernizedBaseProtection {

    @Resource(name = "itbpRiskService")
    private RiskService riskService;

    @Test
    public void cacheIsUpdatedWhenUpdatingConfiguration() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        Assert.assertEquals(null, riskService.findRiskConfiguration(itNetwork.getDbId()));
        DefaultRiskConfiguration defaultRiskConfiguration = DefaultRiskConfiguration.getInstance();

        RiskConfiguration newRiskConfiguration = DefaultRiskConfiguration.getInstance().withRisk(
                defaultRiskConfiguration.getFrequencies().get(3),
                defaultRiskConfiguration.getImpacts().get(3),
                defaultRiskConfiguration.getRisks().get(2));

        RiskConfigurationUpdateContext riskConfigurationUpdateContext = new RiskConfigurationUpdateContext(
                itNetwork.getUuid(), newRiskConfiguration, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        riskService.updateRiskConfiguration(riskConfigurationUpdateContext);
        Assert.assertEquals(newRiskConfiguration,
                riskService.findRiskConfiguration(itNetwork.getDbId()));
    }

}
