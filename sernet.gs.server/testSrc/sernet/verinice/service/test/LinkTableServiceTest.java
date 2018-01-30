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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.service.commands.LoadCnAElementByEntityTypeId;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.ILinkTableService;
import sernet.verinice.service.linktable.LinkTableConfiguration;
import sernet.verinice.service.linktable.LinkTableService;
import sernet.verinice.service.linktable.generator.GraphLinkedTableCreator;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkTableServiceTest extends BeforeEachVNAImportHelper {

    private static final Logger LOG = Logger.getLogger(LinkTableServiceTest.class);

    private static final String VNA_FILENAME = "LinkTableServiceTest.vna";
    private static final String VLT_FILENAME = "LinkTableServiceTest.vlt";

    private static final String SOURCE_ID = "dm_20160303";
    private static final String EXT_ID_ORG = "ENTITY_37737";
    
    private static String ALIAS1 = "auditgroup-name"; 
    private static String ALIAS2 = "person_relation"; 
    private static String ALIAS3 = "controlPerson"; 

    @Resource(name="cnaTreeElementDao")
    protected IBaseDao<CnATreeElement, Long> elementDao;

    ILinkTableService service = new LinkTableService();

    
    private static final String[] COLUMN_PATHES_ARRAY  = { 
            "auditgroup>audit.audit_name AS " + ALIAS1, 
            "incident_scenario/threat.threat_name", 
            "asset:person-iso.person-iso_name AS " + ALIAS2, 
            "samt_topic<controlgroup.controlgroup_name", 
            "threat.threat_name", 
            "incident_scenario/asset/control/person-iso.person-iso_name AS " + ALIAS3 
    }; 
 
 
    private static final String[] EXPECTED_OBJECT_TYPES = { 
      "asset", 
      "audit", 
      "auditgroup", 
      "control", 
      "controlgroup", 
      "incident_scenario", 
      "person-iso", 
      "samt_topic", 
      "threat" 
    }; 
     
    private static final String[] EXPECTED_PROPERTY_TYPES = { 
            "audit_name", 
            "controlgroup_name", 
            "person-iso_name", 
            "threat_name" 
          }; 
 
    private static final Set<String> COLUMN_PATHES; 
 
    static { 
        COLUMN_PATHES = new LinkedHashSet<>(Arrays.asList(COLUMN_PATHES_ARRAY)); 
    } 
    
    @Test
    public void testGetElementTypes() {
        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        for (String path : COLUMN_PATHES) {
            builder.addColumnPath(path);
        }
        LinkTableConfiguration configuration = builder.build();
        checkObjectTypes(new LinkedList<>(configuration.getObjectTypeIds()));
    }
    
    @Test
    public void testGetPropertyTypes() {
        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        for (String path : COLUMN_PATHES) {
            builder.addColumnPath(path);
        }
        LinkTableConfiguration configuration = builder.build();
        checkPropertyTypes(new LinkedList<>(configuration.getPropertyTypeIds()));
    }
    
    private void checkObjectTypes(List<String> objectTypeIds) { 
        Collections.sort(objectTypeIds); 
        assertArrayEquals(EXPECTED_OBJECT_TYPES, objectTypeIds.toArray()); 
    } 
     
    private void checkPropertyTypes(List<String> propertyTypeIds) { 
        Collections.sort(propertyTypeIds); 
        assertArrayEquals(EXPECTED_PROPERTY_TYPES, propertyTypeIds.toArray()); 
    } 

    
    
    
    @Test
    public void testChildParentReport() throws CommandException {
        service.setLinkTableCreator(new GraphLinkedTableCreator());
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);

        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        builder.addScopeId(org.getScopeId())
        .addColumnPath("asset<assetgroup.assetgroup_name")
        .addColumnPath("asset.asset_name");

        List<List<String>> resultTable = service.createTable(builder.build());

        List<String> assetNames = new LinkedList<String>();
        for (List<String> row : resultTable) {
            assetNames.add(row.get(1));
        }

        LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(Asset.TYPE_ID,org.getDbId());
        command = commandService.executeCommand(command);
        List<CnATreeElement> assetList = command.getElements();

        int expectedSize = resultTable.size() - 1;
        
        assertEquals("Result table has not " + assetList.size() + " rows", expectedSize, assetList.size());

        for (CnATreeElement asset : assetList) {
            assertTrue("Asset: " + asset.getTitle() + " not in result list",assetNames.contains(asset.getTitle()));
        }
    }
    
    

    @Test
    public void testSzenarioReport() throws CommandException {
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);

        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        builder.addScopeId(org.getScopeId())
        .addColumnPath("incident_scenario.incident_scenario_name")
        .addColumnPath("incident_scenario:person-iso.title")
        .addColumnPath("incident_scenario/person-iso.person-iso_name")
        .addColumnPath("incident_scenario/person-iso.person-iso_surname")
        .addColumnPath("incident_scenario/asset.asset_name")
        .addColumnPath("incident_scenario/asset:person-iso.title")
        .addColumnPath("incident_scenario/asset/person-iso.person-iso_name")
        .addColumnPath("incident_scenario/asset/person-iso.person-iso_surname")
        .addColumnPath("incident_scenario/control.control_name")
        .addColumnPath("incident_scenario/control:person-iso.title")
        .addColumnPath("incident_scenario/control/person-iso.person-iso_name")
        .addColumnPath("incident_scenario/control/person-iso.person-iso_surname");

        List<List<String>> resultTable = service.createTable(builder.build());

        checkTable(resultTable);

    }

    @Test
    public void testCreateWithVltFile() throws CommandException, IOException {
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        ILinkTableConfiguration configuration = VeriniceLinkTableIO.readLinkTableConfiguration(getVltFilePath());
        LinkTableConfiguration changedConfiguration = cloneConfiguration(configuration);
        changedConfiguration.addScopeId(org.getScopeId());

        String tempVltPath = File.createTempFile(this.getClass().getSimpleName(), ".vlt").getAbsolutePath();
        VeriniceLinkTableIO.write(changedConfiguration, tempVltPath);

        List<List<String>> resultTable = service.createTable(tempVltPath);
        checkTable(resultTable);
        FileUtils.deleteQuietly(new File(tempVltPath));
    }

    private void checkTable(List<List<String>> resultTable) {
        //assertEquals(325, resultTable.size());
        assertEquals(12, resultTable.get(0).size());

        assertEquals("Abuse of rights possible due to well-known software flaws", resultTable.get(28).get(0));
        assertEquals("modelliert durch", resultTable.get(28).get(1));
        assertEquals("Thomas", resultTable.get(28).get(2));
        assertEquals("Test", resultTable.get(28).get(3));

        assertEquals("Break-down of air conditioning", resultTable.get(32).get(0));
        assertEquals("Air-conditioners", resultTable.get(32).get(4));
        assertEquals("verantwortlich", resultTable.get(32).get(5));
        assertEquals("Janet", resultTable.get(32).get(6));
        assertEquals("Junit", resultTable.get(32).get(7));

        assertEquals("Configuration error", resultTable.get(42).get(0));
        assertEquals("12.5.1 Change control procedures", resultTable.get(42).get(8));
        assertEquals("zuständig", resultTable.get(42).get(9));
        assertEquals("Thomas", resultTable.get(42).get(10));
        assertEquals("Test", resultTable.get(42).get(11));

        assertEquals("Leak of information over networks", resultTable.get(155).get(0));
        assertEquals("12.5.4 Information leakage", resultTable.get(155).get(8));
        assertEquals("implementiert von", resultTable.get(155).get(9));
        assertEquals("Anna", resultTable.get(155).get(10));
        assertEquals("Assert", resultTable.get(155).get(11));

        assertEquals("Leak of information over networks", resultTable.get(156).get(0));
        assertEquals("12.5.4 Information leakage", resultTable.get(156).get(8));
        assertEquals("zuständig", resultTable.get(156).get(9));
        assertEquals("Thomas", resultTable.get(156).get(10));
        assertEquals("Test", resultTable.get(156).get(11));
    }

    private LinkTableConfiguration cloneConfiguration(ILinkTableConfiguration configuration) {
        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        builder.setColumnPathes(configuration.getColumnPaths())
        .setLinkTypeIds(configuration.getLinkTypeIds());
        if(configuration.getScopeIdArray()!=null) {
            builder.setScopeIds(new HashSet<>(Arrays.asList(configuration.getScopeIdArray())));
        }
        return builder.build();
    }

    protected String getVltFilePath() {
        return getFilePath(VLT_FILENAME);
    }

    @Override
    protected String getFilePath() {
        return getFilePath(VNA_FILENAME);
    }

    private String getFilePath(String fileName) {
        return this.getClass().getResource(fileName).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
       return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }
}
