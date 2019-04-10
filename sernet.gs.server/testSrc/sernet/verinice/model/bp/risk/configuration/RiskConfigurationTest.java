package sernet.verinice.model.bp.risk.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;

public class RiskConfigurationTest {

    @Test
    public void testConfigurationWithValues() {
        List<Frequency> frequencies = Arrays.asList(new Frequency("01", "01", "first"),
                new Frequency("02", "02", "second"), new Frequency("03", "03", "third"));
        List<Impact> impacts = Arrays.asList(new Impact("01", "01", "first"),
                new Impact("02", "02", "second"), new Impact("03", "03", "third"),
                new Impact("03", "03", "fourth"));
        List<Risk> risks = Arrays.asList(new Risk("01", "01", "first", null),
                new Risk("02", "02", "second", null));

        RiskConfiguration newConfiguration = DefaultRiskConfiguration.getInstance()
                .withValues(frequencies, impacts, risks);

        assertFalse("expected a new instance",
                DefaultRiskConfiguration.getInstance() == newConfiguration);

        assertEquals("first", newConfiguration.getRisk("01", "01").getDescription());
        assertEquals("first", newConfiguration.getRisk("02", "01").getDescription());
        assertEquals("first", newConfiguration.getRisk("03", "01").getDescription());
        assertEquals("first", newConfiguration.getRisk("01", "02").getDescription());
        assertEquals("first", newConfiguration.getRisk("02", "02").getDescription());
        assertEquals("second", newConfiguration.getRisk("03", "02").getDescription());
        assertEquals("second", newConfiguration.getRisk("01", "03").getDescription());
        assertEquals("second", newConfiguration.getRisk("02", "03").getDescription());
        assertNull("deleted risk values shall not be referenced",
                newConfiguration.getRisk("03", "03"));
    }

    @Test
    public void testConfigurationWithImpactValueAdded() {
        List<Frequency> frequencies = DefaultRiskConfiguration.getInstance().getFrequencies();
        List<Impact> impacts = new ArrayList<>(DefaultRiskConfiguration.getInstance().getImpacts());
        impacts.add(new Impact("impact05", "fifth", "the fifth"));
        List<Risk> risks = DefaultRiskConfiguration.getInstance().getRisks();

        RiskConfiguration newConfiguration = DefaultRiskConfiguration.getInstance()
                .withValues(frequencies, impacts, risks);

        assertFalse("expected a new instance",
                DefaultRiskConfiguration.getInstance() == newConfiguration);

        assertEquals("risk01", newConfiguration.getRisk("frequency01", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency02", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency03", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency04", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency01", "impact02").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency02", "impact02").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency03", "impact02").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency04", "impact02").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency01", "impact03").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency02", "impact03").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency03", "impact03").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency04", "impact03").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency01", "impact04").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency02", "impact04").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency03", "impact04").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency04", "impact04").getId());
        assertEquals(null, newConfiguration.getRisk("frequency01", "impact05"));
        assertEquals(null, newConfiguration.getRisk("frequency02", "impact05"));
        assertEquals(null, newConfiguration.getRisk("frequency03", "impact05"));
        assertEquals(null, newConfiguration.getRisk("frequency04", "impact05"));
    }

    @Test
    public void testConfigurationWithFrequencyValueAdded() {
        List<Frequency> frequencies = new ArrayList<>(
                DefaultRiskConfiguration.getInstance().getFrequencies());
        frequencies.add(new Frequency("frequency05", "fifth", "the fifth"));
        List<Impact> impacts = DefaultRiskConfiguration.getInstance().getImpacts();
        List<Risk> risks = DefaultRiskConfiguration.getInstance().getRisks();

        RiskConfiguration newConfiguration = DefaultRiskConfiguration.getInstance()
                .withValues(frequencies, impacts, risks);

        assertFalse("expected a new instance",
                DefaultRiskConfiguration.getInstance() == newConfiguration);

        assertEquals("risk01", newConfiguration.getRisk("frequency01", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency02", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency03", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency04", "impact01").getId());
        assertEquals(null, newConfiguration.getRisk("frequency05", "impact01"));
        assertEquals("risk01", newConfiguration.getRisk("frequency01", "impact02").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency02", "impact02").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency03", "impact02").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency04", "impact02").getId());
        assertEquals(null, newConfiguration.getRisk("frequency05", "impact02"));
        assertEquals("risk02", newConfiguration.getRisk("frequency01", "impact03").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency02", "impact03").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency03", "impact03").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency04", "impact03").getId());
        assertEquals(null, newConfiguration.getRisk("frequency05", "impact03"));
        assertEquals("risk02", newConfiguration.getRisk("frequency01", "impact04").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency02", "impact04").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency03", "impact04").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency04", "impact04").getId());
        assertEquals(null, newConfiguration.getRisk("frequency05", "impact04"));
    }

    @Test
    public void testConfigurationWithRiskValueAdded() {
        List<Frequency> frequencies = DefaultRiskConfiguration.getInstance().getFrequencies();
        List<Impact> impacts = DefaultRiskConfiguration.getInstance().getImpacts();
        List<Risk> risks = new ArrayList<>(DefaultRiskConfiguration.getInstance().getRisks());
        risks.add(new Risk("risk05", "fifth", "the fifth", null));

        RiskConfiguration newConfiguration = DefaultRiskConfiguration.getInstance()
                .withValues(frequencies, impacts, risks);

        assertFalse("expected a new instance",
                DefaultRiskConfiguration.getInstance() == newConfiguration);

        assertEquals("risk01", newConfiguration.getRisk("frequency01", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency02", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency03", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency04", "impact01").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency01", "impact02").getId());
        assertEquals("risk01", newConfiguration.getRisk("frequency02", "impact02").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency03", "impact02").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency04", "impact02").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency01", "impact03").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency02", "impact03").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency03", "impact03").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency04", "impact03").getId());
        assertEquals("risk02", newConfiguration.getRisk("frequency01", "impact04").getId());
        assertEquals("risk03", newConfiguration.getRisk("frequency02", "impact04").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency03", "impact04").getId());
        assertEquals("risk04", newConfiguration.getRisk("frequency04", "impact04").getId());
    }
}
