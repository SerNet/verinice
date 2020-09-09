/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin
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
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.risk.RiskAnalysisConfiguration;
import sernet.verinice.service.risk.RiskAnalysisService;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * This class tests the risk analysis according to ISO 27005
 */
public class RiskAnalysisServiceOverflowTest extends BeforeEachVNAImportHelper {

    private static final String VNA_FILENAME = "RiskAnalysisServiceOverflowTest.vna";

    private static final String SOURCE_ID = "483e9b";
    private static final String EXT_ID_ORGANIZATION = "3107a3aa-efdc-49d7-bddd-7fc7e581bbbb";
    private static final String EXT_ID_ASSET = "fce4e1c2-039c-4b0d-8709-1a7347765a57";

    @Resource(name = "riskAnalysisService")
    RiskAnalysisService riskAnalysisService;

    @Test
    public void testRiskAnalysis() throws CommandException {
        Organization org = (Organization) loadElement(SOURCE_ID, EXT_ID_ORGANIZATION);
        RiskAnalysisConfiguration configuration = new RiskAnalysisConfiguration(org.getDbId());
        riskAnalysisService.runRiskAnalysis(configuration);
        Asset asset = (Asset) loadElement(SOURCE_ID, EXT_ID_ASSET);
        checkRiskValues(asset);

        Set<CnALink> links = asset.getLinksUp();
        for (CnALink link : links) {
            if (IncidentScenario.REL_INCSCEN_ASSET.equals(link.getRelationId())) {
                checkRiskValues(link);
            }
        }
    }

    public void checkRiskValues(Asset asset) {
        assertEquals(0, asset.getNumericProperty(Asset.ASSET_CONFIDENTIALITY_WITH_CONTROLS));
        assertEquals(0,
                asset.getNumericProperty(Asset.ASSET_CONFIDENTIALITY_WITH_PLANNED_CONTROLS));
        assertEquals(0,
                asset.getNumericProperty(Asset.ASSET_CONFIDENTIALITY_WITH_IMPLEMENTED_CONTROLS));
        assertEquals(7, asset.getNumericProperty(Asset.ASSET_RISK_C));
        assertEquals(4, asset.getNumericProperty(Asset.ASSET_CONTROLRISK_C));
        assertEquals(4, asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_C));
        assertEquals(4, asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C));

        assertEquals(0, asset.getNumericProperty(Asset.ASSET_INTEGRITY_WITH_CONTROLS));
        assertEquals(0, asset.getNumericProperty(Asset.ASSET_INTEGRITY_WITH_PLANNED_CONTROLS));
        assertEquals(1, asset.getNumericProperty(Asset.ASSET_INTEGRITY_WITH_IMPLEMENTED_CONTROLS));
        assertEquals(6, asset.getNumericProperty(Asset.ASSET_RISK_I));
        assertEquals(5, asset.getNumericProperty(Asset.ASSET_CONTROLRISK_I));
        assertEquals(4, asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_I));
        assertEquals(4, asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I));

        assertEquals(1, asset.getNumericProperty(Asset.ASSET_AVAILABILITY_WITH_CONTROLS));
        assertEquals(1, asset.getNumericProperty(Asset.ASSET_AVAILABILITY_WITH_PLANNED_CONTROLS));
        assertEquals(2,
                asset.getNumericProperty(Asset.ASSET_AVAILABILITY_WITH_IMPLEMENTED_CONTROLS));
        assertEquals(7, asset.getNumericProperty(Asset.ASSET_RISK_A));
        assertEquals(6, asset.getNumericProperty(Asset.ASSET_CONTROLRISK_A));
        assertEquals(5, asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_A));
        assertEquals(5, asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A));
    }

    public void checkRiskValues(CnALink link) {
        assertEquals(Integer.valueOf(7), link.getRiskConfidentiality());
        assertEquals(Integer.valueOf(4), link.getRiskConfidentialityWithControls());
        assertEquals(Integer.valueOf(6), link.getRiskIntegrity());
        assertEquals(Integer.valueOf(5), link.getRiskIntegrityWithControls());
        assertEquals(Integer.valueOf(7), link.getRiskAvailability());
        assertEquals(Integer.valueOf(6), link.getRiskAvailabilityWithControls());
    }

    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
        return new SyncParameter(true, true, true, false,
                SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }

}
