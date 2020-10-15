/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.gs.service.MapUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.commands.unify.LoadUnifyMapping;
import sernet.verinice.service.commands.unify.Unify;
import sernet.verinice.service.commands.unify.UnifyMapping;
import sernet.verinice.service.test.helper.vnaimport.VNAImportHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@Transactional
@TransactionConfiguration(transactionManager = "txManager")
public class UnifyTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(UnifyTest.class);

    private static final String VNA_FILENAME = "UnifyTest.vna";

    private static final String SOURCE_ID = "VN-872";
    private static final String CONTROL_GROUP_1_X = "ENTITY_46008";
    private static final String CONTROL_GROUP_2_X = "ENTITY_117615";

    // Ext-id of ISA-topics of ISA 2.x in test VNA with name VNA_FILENAME
    private static final String ISA_TOPIC_2_X_C_14_1 = "ENTITY_117723";

    public static final List<String> PROPERTY_TYPE_BLACKLIST = Arrays.asList(SamtTopic.PROP_DESC,
            SamtTopic.PROP_NAME, SamtTopic.PROP_VERSION, SamtTopic.PROP_WEIGHT,
            SamtTopic.PROP_OWNWEIGHT, SamtTopic.PROP_MIN1, SamtTopic.PROP_MIN2);

    @Before
    public void importData() throws IOException, CommandException, SyncParameterException {
        VNAImportHelper.importFile(VNA_FILENAME);
    }

    @Test
    public void testBlacklist() throws Exception {
        CnATreeElement element = loadElement(SOURCE_ID, ISA_TOPIC_2_X_C_14_1);
        Map<String, String> valuesBeforeUnify = loadValuesOfBlacklistProperties(element);
        doUnify();
        element = loadElement(SOURCE_ID, ISA_TOPIC_2_X_C_14_1);
        Map<String, String> valuesAfterUnify = loadValuesOfBlacklistProperties(element);
        assertTrue("Values of blacklist properties after unify are not the same as before.",
                MapUtil.compare(valuesBeforeUnify, valuesAfterUnify));
    }

    private void doUnify() throws CommandException {
        List<UnifyMapping> mappingList = loadUnifyMappings();
        Unify unifyCommand = new Unify.Builder(mappingList).build();
        unifyCommand = commandService.executeCommand(unifyCommand);
    }

    private Map<String, String> loadValuesOfBlacklistProperties(CnATreeElement element) {
        Map<String, String> valuesOfBlacklistProperties = new HashMap<String, String>();
        for (String propertyTypeId : PROPERTY_TYPE_BLACKLIST) {
            valuesOfBlacklistProperties.put(propertyTypeId,
                    element.getEntity().getSimpleValue(propertyTypeId));
        }
        return valuesOfBlacklistProperties;
    }

    private List<UnifyMapping> loadUnifyMappings() throws CommandException {
        ControlGroup controlGroupV1 = (ControlGroup) loadElement(SOURCE_ID, CONTROL_GROUP_1_X);
        assertNotNull("Control group of ISA 1.x not found.", controlGroupV1);
        ControlGroup controlGroupV2 = (ControlGroup) loadElement(SOURCE_ID, CONTROL_GROUP_2_X);
        assertNotNull("Control group of ISA 2.0 not found.", controlGroupV2);
        LoadUnifyMapping command = new LoadUnifyMapping(controlGroupV1.getUuid(),
                controlGroupV2.getUuid());
        command = commandService.executeCommand(command);
        List<UnifyMapping> mappingList = command.getMappings();
        return mappingList;
    }
}
