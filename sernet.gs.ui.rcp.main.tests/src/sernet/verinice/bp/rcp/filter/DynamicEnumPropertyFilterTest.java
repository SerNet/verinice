/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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
package sernet.verinice.bp.rcp.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Tests {@link DynamicEnumPropertyFilter}.
 */
public class DynamicEnumPropertyFilterTest {
    @Test
    public void elementPassesIfValueInList() {
        DynamicEnumPropertyFilter<TestEnum> sut = new DynamicEnumPropertyFilter<>("test_prop",
                TestEnum.class,
                Stream.of(TestEnum.SECOND, TestEnum.THIRD).collect(Collectors.toSet()));

        CnATreeElement treeElementMock = mock(CnATreeElement.class);
        when(treeElementMock.hasDynamicProperty("test_prop")).thenReturn(true);
        when(treeElementMock.getDynamicEnumProperty("test_prop", TestEnum.class))
                .thenReturn(TestEnum.THIRD);

        Boolean passed = sut.select(null, null, treeElementMock);

        assertTrue(passed);
    }

    @Test
    public void elementDoesntPassIfValueNotInList() {
        DynamicEnumPropertyFilter<TestEnum> sut = new DynamicEnumPropertyFilter<>("test_prop",
                TestEnum.class,
                Stream.of(TestEnum.SECOND, TestEnum.THIRD).collect(Collectors.toSet()));

        CnATreeElement treeElementMock = mock(CnATreeElement.class);
        when(treeElementMock.hasDynamicProperty("test_prop")).thenReturn(true);
        when(treeElementMock.getDynamicEnumProperty("test_prop", TestEnum.class))
                .thenReturn(TestEnum.FIRST);

        Boolean passed = sut.select(null, null, treeElementMock);

        assertFalse(passed);
    }
}

enum TestEnum {
    FIRST, SECOND, THIRD
}
