/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.junit.Test;

import sernet.verinice.service.linktable.ChildElement;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.IPathElement;
import sernet.verinice.service.linktable.LinkElement;
import sernet.verinice.service.linktable.LinkTypeElement;
import sernet.verinice.service.linktable.ParentElement;
import sernet.verinice.service.linktable.PropertyElement;
import sernet.verinice.service.linktable.RootElement;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LtrColumnPathParserTest {


    private static String ALIAS1 = "auditgroup-name";
    private static String ALIAS2 = "person_relation";
    private static String ALIAS3 = "controlPerson";

    private static String[] ALIASES = {ALIAS1,null,ALIAS2,null,null,ALIAS3};

    private static final String[] COLUMN_PATHES_ARRAY  = {
            "auditgroup>audit.audit_name AS " + ALIAS1,
            "incident_scenario/threat.threat_name",
            "asset:person-iso.name AS " + ALIAS2,
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

    private static final Set<String> COLUMN_PATHES;

    static {
        COLUMN_PATHES = new LinkedHashSet<>(Arrays.asList(COLUMN_PATHES_ARRAY));
    }

    @Test
    public void testAlias() {
        IPathElement[] pathElementArray = ColumnPathParser.getPathElements(COLUMN_PATHES);
        int i=0;
        for (IPathElement pathElement : pathElementArray) {
            assertEquals(ALIASES[i], pathElement.getAlias());
            i++;
        }
    }

    @Test
    public void testGetPathElements() {
        IPathElement[] pathElementArray = ColumnPathParser.getPathElements(COLUMN_PATHES);
        int i=0;
        for (IPathElement pathElement : pathElementArray) {
            checkPath(pathElement, COLUMN_PATHES_ARRAY[i]);
            i++;
        }
    }

    @Test
    public void testGetObjectTypeIdsWithArray() {
        IPathElement[] pathElementArray = ColumnPathParser.getPathElements(COLUMN_PATHES);
        List<String> objectTypeIds = new LinkedList<>(ColumnPathParser.getObjectTypeIds(pathElementArray));
        checkObjectTypes(objectTypeIds);
    }

    @Test
    public void testGetObjectTypeIds() {
        List<String> objectTypeIds = new LinkedList<>(ColumnPathParser.getObjectTypeIds(COLUMN_PATHES));
        checkObjectTypes(objectTypeIds);
    }


    private void checkPath(IPathElement pathElement, String columnPath) {
        StringTokenizer st = new StringTokenizer(
                columnPath,
                new String(new char[]{IPathElement.DELIMITER_LINK,IPathElement.DELIMITER_LINK_TYPE,IPathElement.DELIMITER_CHILD,IPathElement.DELIMITER_PARENT,IPathElement.DELIMITER_PROPERTY}),
                true);
        Class<? extends IPathElement> expectedClass = RootElement.class;
        if(st.hasMoreTokens()) {
            assertEquals(expectedClass, pathElement.getClass());
            assertEquals(st.nextToken(), pathElement.getTypeId());
            pathElement = pathElement.getChild();
            expectedClass = getClassForDelimiter(st.nextToken());
        }

    }

    private void checkObjectTypes(List<String> objectTypeIds) {
        Collections.sort(objectTypeIds);
        assertArrayEquals(EXPECTED_OBJECT_TYPES, objectTypeIds.toArray());
    }

    private Class<? extends IPathElement> getClassForDelimiter(String delimiter) {
        switch (delimiter.toCharArray()[0]) {
        case IPathElement.DELIMITER_LINK:
            return LinkElement.class;
        case IPathElement.DELIMITER_LINK_TYPE:
            return LinkTypeElement.class;
        case IPathElement.DELIMITER_CHILD:
            return ChildElement.class;
        case IPathElement.DELIMITER_PARENT:
            return ParentElement.class;
        case IPathElement.DELIMITER_PROPERTY:
            return PropertyElement.class;
        default:
            return null;
        }
    }

}
