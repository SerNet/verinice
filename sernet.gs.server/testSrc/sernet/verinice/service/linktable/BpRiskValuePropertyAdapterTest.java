package sernet.verinice.service.linktable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationCache;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

public class BpRiskValuePropertyAdapterTest extends AbstractModernizedBaseProtection {

    @Test
    public void getFrequencyLabelFromThreat() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup threatGroup = createBpThreatGroup(itNetwork, "Threats");
        BpThreat bpThreat = createThreat(threatGroup, "T1", "Threat 1");
        DefaultRiskConfiguration defaultRiskConfiguration = DefaultRiskConfiguration.getInstance();
        Frequency firstFrequency = defaultRiskConfiguration.getFrequencies().iterator().next();
        bpThreat.setFrequencyWithoutAdditionalSafeguards(firstFrequency.getId());

        BpRiskValuePropertyAdapter adapter = new BpRiskValuePropertyAdapter(bpThreat,
                new RiskConfigurationCache());

        String valueFromPropertyAdapter = adapter
                .getPropertyValue(BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS);
        assertEquals(firstFrequency.getLabel(), valueFromPropertyAdapter);
    }

}
