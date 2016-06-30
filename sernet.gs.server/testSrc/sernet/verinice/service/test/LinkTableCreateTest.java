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

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import junit.framework.Assert;
import sernet.verinice.interfaces.CommandException;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class LinkTableCreateTest extends ContextConfiguration{

    @Autowired
    ObjectFactory loadDataFactory;
    private LoadData loadData;

    @Test
    public void testChildRelation() throws Exception{
        List<List<String>> table = loadTestData("child-relation.vlt", "child-relation.vna", "42b6e1", "ENTITY_1107624");
        int expectedTableSize = 7;
        Assert.assertEquals(expectedTableSize, table.size());
    }

    @Test
    public void testCnaLinkRelation() throws Exception{
        List<List<String>> table = loadTestData("cnalink-relation.vlt", "cnalink-relation.vna", "3a6c5f", "ENTITY_1108858");
        int expectedTableSize = 4;
        Assert.assertEquals(expectedTableSize, table.size());
    }

    @Test
    public void testParentRelation() throws Exception{
        List<List<String>> table = loadTestData("parent-relation.vlt", "parent-relation.vna", "42b6e1", "ENTITY_1109509");
        int expectedTableSize = 4;
        Assert.assertEquals(expectedTableSize, table.size());
    }


    public List<List<String>> loadTestData(String vltFile, String vnaFile, String sourceId, String extId) throws Exception{
        loadData = (LoadData) loadDataFactory.getObject();
        loadData.setVltFile(vltFile);
        loadData.setVnaFile(vnaFile);
        loadData.setSourceId(sourceId);
        loadData.setExtIdOrg(extId);
        loadData.setUp();
        return loadData.loadAndExecuteVLT();
    }

    @After
    public void deleteTestData() throws CommandException{
        loadData.tearDown();
    }
}
