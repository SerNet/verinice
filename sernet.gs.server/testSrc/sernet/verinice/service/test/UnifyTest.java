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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.commands.UnifyElement;
import sernet.verinice.service.commands.unify.Isa20Mapper;
import sernet.verinice.service.commands.unify.LoadUnifyMapping;
import sernet.verinice.service.commands.unify.Unify;
import sernet.verinice.service.commands.unify.UnifyMapping;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UnifyTest extends BeforeEachVNAImportHelper {
   
    private static final Logger LOG = Logger.getLogger(UnifyTest.class);
    
    private static final String VNA_FILENAME = "UnifyTest.vna";
    
    private static final String SOURCE_ID = "VN-872";
    private static final String CONTROL_GROUP_1_X = "ENTITY_46008";
    private static final String CONTROL_GROUP_2_0 = "ENTITY_117615";
    private static final String ISA_TOPIC_2_0_14_1 = "ENTITY_117723";
    private static final String ISA_TOPIC_2_0_14_2 = "ENTITY_117751"; 
    
    private static final String ISA_TOPIC_2_0_9_5 = "ENTITY_117973";
    private static final String ISA_TOPIC_2_0_18_4 = "ENTITY_118115";
    private static final String ISA_TOPIC_2_0_15_1 = "ENTITY_118151";
    
    private static final String TEXT_FROM_1_X = "sernet.verinice.service.test.UnifyTest";
    private static final int MATURITY_FROM_1_X_LEVEL_3 = 3;
    
    private static final Map<String, String[]> MAP_FROM_ISA_1_TO_2;
    static {
        MAP_FROM_ISA_1_TO_2 = new Hashtable<String, String[]>();
        MAP_FROM_ISA_1_TO_2.put("5.1",new String[]{"5.1"}); 
        MAP_FROM_ISA_1_TO_2.put("6.1",new String[]{"6.1"});
        MAP_FROM_ISA_1_TO_2.put("11.10",new String[]{"6.3"});
        MAP_FROM_ISA_1_TO_2.put("8.1",new String[]{"7.1"});
        MAP_FROM_ISA_1_TO_2.put("8.2",new String[]{"7.2"});
        MAP_FROM_ISA_1_TO_2.put("7.1",new String[]{"8.1"});
        MAP_FROM_ISA_1_TO_2.put("7.2",new String[]{"8.2"});
        MAP_FROM_ISA_1_TO_2.put("10.12",new String[]{"8.3"});        
        MAP_FROM_ISA_1_TO_2.put("11.6",new String[]{"9.1"});
        MAP_FROM_ISA_1_TO_2.put("11.1",new String[]{"9.2"});
        MAP_FROM_ISA_1_TO_2.put("11.2",new String[]{"9.3"});
        MAP_FROM_ISA_1_TO_2.put("11.3",new String[]{"9.4"});
        MAP_FROM_ISA_1_TO_2.put("8.3",new String[]{"9.5"});
        MAP_FROM_ISA_1_TO_2.put("12.1",new String[]{"10.1"});     
        MAP_FROM_ISA_1_TO_2.put("9.1",new String[]{"11.1"});
        MAP_FROM_ISA_1_TO_2.put("9.2",new String[]{"11.2"});
        MAP_FROM_ISA_1_TO_2.put("9.4",new String[]{"11.3"});
        MAP_FROM_ISA_1_TO_2.put("9.5",new String[]{"11.4"});
        MAP_FROM_ISA_1_TO_2.put("10.1",new String[]{"12.1"});
        MAP_FROM_ISA_1_TO_2.put("10.2",new String[]{"12.2"});
        MAP_FROM_ISA_1_TO_2.put("10.4",new String[]{"12.3"});
        MAP_FROM_ISA_1_TO_2.put("10.7",new String[]{"12.4"});
        MAP_FROM_ISA_1_TO_2.put("10.17",new String[]{"12.5"});
        MAP_FROM_ISA_1_TO_2.put("10.16",new String[]{"12.6"});
        MAP_FROM_ISA_1_TO_2.put("12.3",new String[]{"12.7"});
        MAP_FROM_ISA_1_TO_2.put("15.4",new String[]{"12.8"});
        MAP_FROM_ISA_1_TO_2.put("10.8",new String[]{"13.1"});
        MAP_FROM_ISA_1_TO_2.put("11.8",new String[]{"13.3"});
        MAP_FROM_ISA_1_TO_2.put("10.15",new String[]{"13.4"});      
        MAP_FROM_ISA_1_TO_2.put("12.2",new String[]{"14.1","14.2"});       
        MAP_FROM_ISA_1_TO_2.put("6.2",new String[]{"15.1"});
        MAP_FROM_ISA_1_TO_2.put("10.3",new String[]{"15.2"});
        MAP_FROM_ISA_1_TO_2.put("13.1",new String[]{"16.1"});
        MAP_FROM_ISA_1_TO_2.put("13.2",new String[]{"16.2"});
        MAP_FROM_ISA_1_TO_2.put("14.1",new String[]{"17.1"});
        MAP_FROM_ISA_1_TO_2.put("15.1",new String[]{"18.1"});
        MAP_FROM_ISA_1_TO_2.put("15.2",new String[]{"18.2"});
        MAP_FROM_ISA_1_TO_2.put("15.3",new String[]{"18.4"});
    }
    
    
    @Test
    public void testLoadUnifyMapping() throws Exception {
        List<UnifyMapping> mappingList = loadUnifyMappings();
        assertNotNull("Mapping list is null.", mappingList);
        assertTrue("Mapping is empty.", !mappingList.isEmpty());
        checkNumberOfMappings(mappingList);      
        for (Map.Entry<String, String[]> entry : MAP_FROM_ISA_1_TO_2.entrySet()) {
            checkMapping(mappingList,entry);
        }
    }
    
    @Test
    public void testUnify() throws Exception {
        List<UnifyMapping> mappingList = loadUnifyMappings();
        Unify unifyCommand = new Unify(mappingList,false, false, false);
        unifyCommand = commandService.executeCommand(unifyCommand);
        checkIsaTopic((SamtTopic) loadElement(SOURCE_ID, ISA_TOPIC_2_0_14_1));
        checkIsaTopic((SamtTopic) loadElement(SOURCE_ID, ISA_TOPIC_2_0_14_2));
        checkIsaTopic((SamtTopic) loadElement(SOURCE_ID, ISA_TOPIC_2_0_9_5));
        checkIsaTopic((SamtTopic) loadElement(SOURCE_ID, ISA_TOPIC_2_0_15_1));
        checkIsaTopic((SamtTopic) loadElement(SOURCE_ID, ISA_TOPIC_2_0_18_4));
    }

    private void checkIsaTopic(SamtTopic isaTopic) {
        assertEquals("Unexpected value of property: " + SamtTopic.PROP_EXTERNALNOTE, TEXT_FROM_1_X, isaTopic.getExternalNode());
        assertEquals("Unexpected value of property: " + SamtTopic.PROP_AUDIT_FINDINGS, TEXT_FROM_1_X, isaTopic.getAuditFindings());
        assertEquals("Unexpected value of property: " + SamtTopic.PROP_MATURITY, MATURITY_FROM_1_X_LEVEL_3, isaTopic.getMaturity());
    }

    private List<UnifyMapping> loadUnifyMappings() throws CommandException {
        ControlGroup controlGroupV1 = (ControlGroup) loadElement(SOURCE_ID, CONTROL_GROUP_1_X);
        assertNotNull("Control group of ISA 1.x not found.", controlGroupV1);
        ControlGroup controlGroupV2 = (ControlGroup) loadElement(SOURCE_ID, CONTROL_GROUP_2_0);
        assertNotNull("Control group of ISA 2.0 not found.", controlGroupV2);
        LoadUnifyMapping command = new LoadUnifyMapping(controlGroupV1.getUuid(),controlGroupV2.getUuid(),Isa20Mapper.ID);
        command = commandService.executeCommand(command);
        List<UnifyMapping> mappingList = command.getMappings();
        return mappingList;
    }
    
    private void checkNumberOfMappings(List<UnifyMapping> mappingList) {
        int n = 0;
        for (UnifyMapping unifyMapping : mappingList) {
            if(unifyMapping.getDestinationElements()!=null && !unifyMapping.getDestinationElements().isEmpty()) {
                n++;
            }
        }
        assertEquals("Number of mappings is wrong.",MAP_FROM_ISA_1_TO_2.size(), n);
    }

    private void checkMapping(List<UnifyMapping> mappingList, Entry<String, String[]> entry) {
        boolean mappingFound = false;
        for (UnifyMapping mapping : mappingList) {
            String number = LoadUnifyMapping.getNumberOrTitle(mapping.getSourceElement().getTitle());
            if(entry.getKey().equals(number)) {
                mappingFound = checkMapping(mapping, entry);
            }            
        }
        assertTrue("Mapping not found, source: " + entry.getKey() + ". destination: " + entry.getValue()[0], mappingFound);
    }

    private boolean checkMapping(UnifyMapping mapping, Entry<String, String[]> entry) {
        boolean mappingFound = true;           
        List<UnifyElement>  destList = mapping.getDestinationElements();
        for (UnifyElement unifyElement : destList) {
            String numberDest = LoadUnifyMapping.getNumberOrTitle(unifyElement.getTitle());
            if(Arrays.binarySearch(entry.getValue(),numberDest)<0) {
                mappingFound = false;
            }
        }
        String info = "source: " + entry.getKey() + ". destination: " + entry.getValue()[0];
        if (LOG.isDebugEnabled() && mappingFound) {
            LOG.debug("Mapping found, " + info);
        }
        assertTrue("Mapping not found, " + info, mappingFound);
        return mappingFound;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.test.helper.vnaimport.AbstractVNAImportHelper#getFilePath()
     */
    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME).getPath();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.test.helper.vnaimport.AbstractVNAImportHelper#getSyncParameter()
     */
    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
       return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }

}
