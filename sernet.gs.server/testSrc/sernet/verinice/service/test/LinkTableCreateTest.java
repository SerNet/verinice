/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import junit.framework.Assert;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.CommandException;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class LinkTableCreateTest extends ContextConfiguration {

    @Autowired
    ObjectFactory loadDataFactory;

    @Autowired
    HUITypeFactory huiTypeFactory;

    private LoadData loadData;

    private final Logger log = Logger.getLogger(LinkTableCreateTest.class);

    @Test
    public void testChildRelation() throws Exception {

        log.info("test child releations");

        List<List<String>> expectedList = new ArrayList<>();
        expectedList.add(Arrays.asList(new String[] { "Titel_(AssetGroup)", "Titel_(Asset)", "Titel_(AG->AG->Asset)" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "", "Asset 3" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "", "Asset 4" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "Asset 1", "" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "Asset 2", "" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 2", "Asset 3", "" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 2", "Asset 4", "" }));

        List<String> extIds = Arrays.asList(new String[] { "ENTITY_1107624" });
        List<List<String>> table = loadTestData("child-relation.vlt", "child-relation.vna", "42b6e1", extIds);
        Assert.assertEquals(expectedList, table);
    }

    @Test
    public void testCnaLinkRelation() throws Exception {

        List<List<String>> expectedList = new ArrayList<>();
        expectedList.add(Arrays.asList(new String[] { "Titel(AssetGroup1)", "Titel(Asset1)", "Titel(Asset2)", "Titel(Control)", "Beschreibung(Control)", "Titel(AssetLink)" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "", "Asset 2", "", "", "Asset 1" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "", "Asset 2", "Control 1", "Does something", "" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "", "Asset 2", "Control 2", "Does something else", "" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "Asset 1", "", "", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 2", "Asset 2", "", "", "", "" }));

        List<String> extIds = Arrays.asList(new String[] { "ENTITY_1180214" });
        List<List<String>> table = loadTestData("cnalink-relation.vlt", "cnalink-relation.vna", "434455", extIds);
        Assert.assertEquals(expectedList, table);
    }

    @Test
    public void testParentRelation() throws Exception {

        List<List<String>> expectedList = new ArrayList<>();
        expectedList.add(Arrays.asList(new String[] { "Titel", "Titel", "Titel" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 1", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 2", "Asset Group 1", "Asset 1" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 2", "Asset Group 1", "Asset 1" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 2", "Asset Group 1", "Asset 2" }));
        expectedList.add(Arrays.asList(new String[] { "Asset Group 2", "Asset Group 1", "Asset 2" }));

        List<String> extIds = Arrays.asList(new String[] { "ENTITY_1109509" });
        List<List<String>> table = loadTestData("parent-relation.vlt", "parent-relation.vna", "42b6e1", extIds);
        Assert.assertEquals(expectedList, table);
    }

    @Test
    public void testPrintParentRelationInSingleRow() throws Exception {

        log.info("test print parent relation in own row");

        List<List<String>> expectedList = new ArrayList<>();

        expectedList.add(Arrays.asList(new String[] { "Title_(Assets)", "Title_(Assets)", "Title_(Asset)", "Title_(Control)" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "Control 1" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "Control 2" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 2", "Control 2" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 1", "Assets level 2", "Asset 3", "Control 1" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 1", "Assets level 2", "Asset 4", "Control 2" }));

        List<String> extIds = Arrays.asList(new String[] { "ENTITY_622592" });
        List<List<String>> table = loadTestData("parent-relations-in-one-row.vlt", "parent-relations-in-one-row.vna", "c3547b", extIds);

        if (log.isDebugEnabled()) {
            prettyPrint("expected", expectedList);
            prettyPrint("result", table);
        }

        Assert.assertEquals(expectedList, table);
    }

    @SuppressWarnings("static-access")
    @Test
    public void testListEntriesInParentGroupAndLinkedWithScenerio() throws Exception {

        log.info("list entries in parent group");

        List<List<String>> expectedList = new ArrayList<>();

        String scenario2AssetReverseName = huiTypeFactory.getInstance().getMessage("rel_incscen_asset_reversename");

        expectedList.add(Arrays.asList(new String[] { "Title_(Assets)", "Title_(Assets)", "Title_(Asset)", "Title_(Control)", "Title_(Control)", "Title", "Title_(Scenario)" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "", "", scenario2AssetReverseName, "Scenario 1" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "", "", scenario2AssetReverseName, "Scenario 2" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "Control 1", "Control 1", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "Control 1", "Control 2", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "Control 1", "Control 5", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "Control 2", "Control 1", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "Control 2", "Control 2", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 1", "Control 2", "Control 5", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 2", "", "", scenario2AssetReverseName, "Scenario 2" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 2", "Control 2", "Control 1", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 2", "Control 2", "Control 2", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 2", "Control 2", "Control 5", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 2", "Control 4", "Control 3", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 0", "Assets level 1", "Asset 2", "Control 4", "Control 4", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 1", "Assets level 2", "Asset 3", "Control 1", "Control 1", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 1", "Assets level 2", "Asset 3", "Control 1", "Control 2", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 1", "Assets level 2", "Asset 3", "Control 1", "Control 5", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 1", "Assets level 2", "Asset 4", "Control 2", "Control 1", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 1", "Assets level 2", "Asset 4", "Control 2", "Control 2", "", "" }));
        expectedList.add(Arrays.asList(new String[] { "Assets level 1", "Assets level 2", "Asset 4", "Control 2", "Control 5", "", "" }));

        List<String> extIds = Arrays.asList(new String[] { "ENTITY_622592" });
        List<List<String>> table = loadTestData("list-parent-in-controlgroup-and-linked-scenarios.vlt", "list-parent-entries-and-linked-scenarios.vna", "5ad453", extIds);

        if (log.isDebugEnabled()) {
            prettyPrint("expected", expectedList);
            prettyPrint("result", table);
        }

        Assert.assertEquals(expectedList, table);
    }

    @Test
    public void testCnaLinkOneToManyRelation() throws Exception {
        List<List<String>> expectedList = new ArrayList<>();

        expectedList.add(Arrays.asList(new String[] { "Title_(Control)", "Title_(Control)" }));
        expectedList.add(Arrays.asList(new String[] { "Control O1-1", "Control O2-1" }));
        expectedList.add(Arrays.asList(new String[] { "Control O1-1", "Control O2-2" }));
        expectedList.add(Arrays.asList(new String[] { "Control O1-1", "Control O2-3" }));
        expectedList.add(Arrays.asList(new String[] { "Control O2-1", "Control O1-1" }));
        expectedList.add(Arrays.asList(new String[] { "Control O2-2", "Control O1-1" }));
        expectedList.add(Arrays.asList(new String[] { "Control O2-3", "Control O1-1" }));

        List<String> extIds = Arrays.asList(new String[] { "ENTITY_2424832", "ENTITY_2424847" });
        List<List<String>> table = loadTestData("cnalink-one-to-many-relations.vlt", "cnalink-one-to-many-relations.vna", "a4622c", extIds);
        Assert.assertEquals(expectedList, table);

    }

    @Test
    public void testUncompletePathes() throws Exception {
        List<List<String>> expectedList = new ArrayList<>();

        expectedList.add(Arrays.asList(new String[] { "Title_(Scenario)", "Title_(Asset)", "Surname_(Person)" }));
        expectedList.add(Arrays.asList(new String[] { "Scenario 1", "Asset 1", "" }));
        expectedList.add(Arrays.asList(new String[] { "Scenario 1", "Asset 2", "Person 1" }));

        List<String> extIds = Arrays.asList(new String[] { "ENTITY_393216" });
        List<List<String>> table = loadTestData("uncomplete-paths.vlt", "uncomplete-paths.vna", "14a192", extIds);
        Assert.assertEquals(expectedList, table);

    }

    public List<List<String>> loadTestData(String vltFile, String vnaFile, String sourceId, List<String> orgExtIds) throws Exception {
        loadData = (LoadData) loadDataFactory.getObject();
        loadData.setVltFile(vltFile);
        loadData.setVnaFile(vnaFile);
        loadData.setSourceId(sourceId);
        loadData.setOrgExtIds(orgExtIds);
        loadData.setUp();
        return loadData.loadAndExecuteVLT();
    }

    @After
    public void deleteTestData() throws CommandException {
        loadData.tearDown();
    }

    private void prettyPrint(String msg, List<List<String>> list) {

        System.out.println("-------------------------------------------------------------------");
        System.out.println(msg);
        System.out.println("-------------------------------------------------------------------");

        for (List<String> row : list) {
            System.out.println(StringUtils.join(row, "\t"));
        }

        System.out.println("-------------------------------------------------------------------");
    }
}
