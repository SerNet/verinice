/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.snutils.DBException;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class HUITypeFactoryTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(HUITypeFactoryTest.class);
    private static URL url;

    @Resource(name = "huiTypeFactory")
    private HUITypeFactory huiTypeFactory;

    /**
     * @throws MalformedURLException
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws MalformedURLException {
        // LOG.info(new File("").getAbsolutePath());
        String filePath = new File("").getAbsolutePath() + "/testSrc/SNCA.xml";
        url = Paths.get(filePath).toUri().toURL();

    }
    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getEntityType(java.lang.String)}
     * .
     */
    @Test
    public void testGetEntityType() {

        for (String typeID : huiTypeFactory.getAllTypeIds()) {
            String gottenTypeID = huiTypeFactory.getEntityType(typeID).getId();
            assertEquals("Wrong typeID", typeID, gottenTypeID);
        }
    }


    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getPossibleRelations(java.lang.String, java.lang.String)}
     * . Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getPossibleRelationsFrom(java.lang.String)}
     * . Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getPossibleRelationsTo(java.lang.String)}
     * .
     */
    @Test
    public void testGetPossibleRelations() {
        Set<String> testTypeIds;
        for (String fromTypeID : huiTypeFactory.getAllTypeIds()) {
            testTypeIds = huiTypeFactory.getAllTypeIds();
            for (String toTypeID : testTypeIds) {
                Set<HuiRelation> relations = huiTypeFactory.getPossibleRelations(fromTypeID,
                        toTypeID);
                assertTrue("Relations from " + fromTypeID + " does not contain all relations from "
                        + fromTypeID + " to " + toTypeID,
                        huiTypeFactory.getPossibleRelationsFrom(fromTypeID).containsAll(relations));
                assertTrue("Relations to " + toTypeID + " does not contain all relations from "
                        + fromTypeID + " to " + toTypeID,
                        huiTypeFactory.getPossibleRelationsTo(toTypeID).containsAll(relations));
            }
        }
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#createInstance(java.net.URL)}
     * .
     * 
     * @throws DBException
     */
    @Test
    public void testCreateInstance() throws DBException {
        HUITypeFactory factory = HUITypeFactory.createInstance(url);
        // assertTrue(factory.equals(huiTypeFactory));
        // TODO rmotza needed to be equals???
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        HUITypeFactory.getInstance();
        // fail("Not yet implemented"); // TODO rmotza correctnes?
    }

    /**
     * Test method for {@link sernet.hui.common.connect.HUITypeFactory#getAllTypeIds()}.
     */
    @Test
    public void testGetAllTypeIds() {
        LOG.info(huiTypeFactory.getAllTypeIds());
        // fail("Not yet implemented"); // TODO rmotza correctnes?
    }

    /**
     * Test method for {@link sernet.hui.common.connect.HUITypeFactory#getAllEntityTypes()}.
     */
    @Test
    public void testGetAllEntityTypes() {
        LOG.info(huiTypeFactory.getAllEntityTypes());
        // fail("Not yet implemented"); // TODO rmotza correctnes?
    }

    /**
     * Test method for {@link sernet.hui.common.connect.HUITypeFactory#getURLPropertyTypes()}.
     */
    @Test
    public void testGetURLPropertyTypes() {
        if (LOG.isInfoEnabled()) {
            LOG.info(huiTypeFactory.getURLPropertyTypes());
        }
        // fail("Not yet implemented"); // TODO rmotza correctnes?
    }

    /**
     * Test method for {@link sernet.hui.common.connect.HUITypeFactory#getAllTags()}.
     */
    @Test
    public void testGetAllTags() {
        if (LOG.isInfoEnabled()) {
            LOG.info(huiTypeFactory.getAllTags());
        }
        // fail("Not yet implemented"); // TODO rmotza correctnes?
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getMessage(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testGetMessageStringStringBoolean() {
        /*
         * correct string
         * 
         */
        assertEquals(" (!)", huiTypeFactory.getMessage("", null, false));
        assertEquals(" (!)", huiTypeFactory.getMessage("", "", false));
        assertEquals("", huiTypeFactory.getMessage("", null, true));
        assertEquals("", huiTypeFactory.getMessage("", "", true));

        // fail("Not yet implemented"); // TODO rmotza finish
    }


    /**
     * Test method for {@link sernet.hui.common.connect.HUITypeFactory#isDependency(sernet.hui.common.multiselectionlist.IMLPropertyOption)}.
     */
    @Test
    public void testIsDependency() {
        fail("Not yet implemented"); // TODO rmotza
    }

    /**
     * Test method for {@link sernet.hui.common.connect.HUITypeFactory#getRelation(java.lang.String)}.
     */
    @Test
    public void testGetRelation() {
        // what type IDs?
        // for (String typeID : huiTypeFactory.getAllTypeIds()) {
        // LOG.info(huiTypeFactory.getRelation(typeID));
        // }
        fail("Not yet implemented"); // TODO rmotza

    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getPropertyType(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetPropertyType() {
        fail("Not yet implemented"); // TODO rmotza
    }


}
