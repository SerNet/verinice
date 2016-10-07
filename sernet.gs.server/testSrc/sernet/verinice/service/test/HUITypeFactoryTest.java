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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.hui.common.connect.PropertyType;
import sernet.snutils.DBException;

/**
 * Class to test HUITypeFactory.
 * 
 * There are some methods which get random subsets, because the number of items
 * to test is too big.
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class HUITypeFactoryTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(HUITypeFactoryTest.class);
    private static URL url;
    private static final String ABSOLUTE_SNCA_PATH = new File("").getAbsolutePath()
            + "/testSrc/SNCA.xml";
    private static final int MAX_NUM_TO_TEST = 100;
    private static final int ALL_ENTITY_TYPES_SIZE = 59;
    private static final int ALL_TAGS_SIZE = 18;
    private static final int ALL_URL_PROPERY_TYPES_SIZE = 31;

    @Resource(name = "huiTypeFactory")
    private HUITypeFactory huiTypeFactory;

    @BeforeClass
    public static void setUpBeforeClass() throws MalformedURLException {
        url = Paths.get(ABSOLUTE_SNCA_PATH).toUri().toURL();

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
     * ,
     * {@link sernet.hui.common.connect.HUITypeFactory#getPossibleRelationsFrom(java.lang.String)}
     * and
     * {@link sernet.hui.common.connect.HUITypeFactory#getPossibleRelationsTo(java.lang.String)}
     * .
     */
    @Test
    public void testGetPossibleRelations() {
        Set<String> testTypeIds = huiTypeFactory.getAllTypeIds();
        for (String fromTypeID : huiTypeFactory.getAllTypeIds()) {
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
    @Ignore
    public void testCreateInstance() throws DBException {
        assertEquals("HUITypeFactory should be a singleton: ", huiTypeFactory,
                HUITypeFactory.createInstance(url));
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        assertEquals("HUITypeFactory is expected to be a Singleton", huiTypeFactory,
                HUITypeFactory.getInstance());
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getAllTypeIds()}.
     */
    @Test
    public void testGetAllTypeIds() {
        Collection<String> allEntityTypeIds = huiTypeFactory.getAllTypeIds();
        HashSet<String> set = new HashSet<>(allEntityTypeIds);
        assertEquals(allEntityTypeIds.size(), set.size());
        assertEquals(ALL_ENTITY_TYPES_SIZE, set.size());
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getAllEntityTypes()}.
     */
    @Test
    public void testGetAllEntityTypes() {
        Collection<EntityType> allEntityTypes = huiTypeFactory.getAllEntityTypes();
        HashSet<EntityType> set = new HashSet<>(allEntityTypes);
        assertEquals(allEntityTypes.size(), set.size());
        assertEquals(ALL_ENTITY_TYPES_SIZE, set.size());
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getURLPropertyTypes()}.
     */
    @Test
    public void testGetURLPropertyTypes() {
        Collection<PropertyType> allURLPropertyTypes = huiTypeFactory.getURLPropertyTypes();
        HashSet<PropertyType> set = new HashSet<>(allURLPropertyTypes);
        assertEquals(allURLPropertyTypes.size(), set.size());
        assertEquals(ALL_URL_PROPERY_TYPES_SIZE, set.size());
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getAllTags()}.
     */
    @Test
    public void testGetAllTags() {
        Collection<String> allTags = huiTypeFactory.getAllTags();
        HashSet<String> set = new HashSet<>(allTags);
        assertEquals(allTags.size(), set.size());
        assertEquals(ALL_TAGS_SIZE, set.size());
    }

    /**
     * 9 Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getMessage(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testGetMessageStringStringBoolean() {

        assertEquals(" (!)", huiTypeFactory.getMessage("", null, false));
        assertEquals(" (!)", huiTypeFactory.getMessage("", "", false));
        assertEquals("", huiTypeFactory.getMessage("", null, true));
        assertEquals("", huiTypeFactory.getMessage("", "", true));
        assertTrue("Mitarbeiter".equals(huiTypeFactory.getMessage("person", "", true)) || "Employee".equals(huiTypeFactory.getMessage("person", "", true)));
        assertEquals("", huiTypeFactory.getMessage("", "", true));

        for (String typeID : huiTypeFactory.getAllTypeIds()) {

            assertTrue(!"".equals(huiTypeFactory.getMessage(typeID, "", true)));
            assertTrue(!" (!)".equals(huiTypeFactory.getMessage(typeID, "", false)));
        }
    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getRelation(java.lang.String)}
     * .
     */
    @Test
    public void testGetRelation() {

        HashSet<HuiRelation> allRelations = new HashSet<>();

        for (String typeID : huiTypeFactory.getAllTypeIds()) {

            allRelations.addAll(huiTypeFactory.getPossibleRelationsFrom(typeID));
            allRelations.addAll(huiTypeFactory.getPossibleRelationsTo(typeID));
        }
        allRelations = getRandomSubset(allRelations, getRandomInteger(1, MAX_NUM_TO_TEST));
        for (HuiRelation relation : allRelations) {
            assertEquals(relation, huiTypeFactory.getRelation(relation.getId()));
        }

    }

    /**
     * Test method for
     * {@link sernet.hui.common.connect.HUITypeFactory#getPropertyType(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetPropertyType() {

        HashSet<EntityType> allTypes = new HashSet<>(huiTypeFactory.getAllEntityTypes());
        HashSet<EntityType> typesSubSet = getRandomSubset(allTypes,
                getRandomInteger(1, MAX_NUM_TO_TEST));

        for (EntityType type : typesSubSet) {
            HashSet<PropertyType> propertyTypes = new HashSet<>(type.getAllPropertyTypes());
            propertyTypes = getRandomSubset(propertyTypes, getRandomInteger(1, MAX_NUM_TO_TEST));
            for (PropertyType propertyType : propertyTypes) {
                assertEquals(propertyType,
                        huiTypeFactory.getPropertyType(type.getId(), propertyType.getId()));
            }
        }
    }

    private int getRandomInteger(int min, int max) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Random int between " + min + " and " + max + ".");
        }
        return (int) (min + Math.random() * max);
    }

    private <T> HashSet<T> getRandomSubset(Set<T> set, int num) {
        if (set.size() <= num) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The whole subset will be returned.");
            }
            return new HashSet<>(set);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Subset with " + num + " entries.");
        }

        HashSet<T> subset = new HashSet<>();

        ArrayList<T> list = new ArrayList<>(set);
        Collections.shuffle(list);

        int i = 0;
        while (subset.size() < num) {
            i = (int) (Math.random() * (list.size() - 1));
            subset.add(list.get(i));
        }

        return subset;
    }
}
