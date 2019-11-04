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
package sernet.gs.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Test class for sernet.gs.service.CollectionUtil
 */
public class CollectionUtilTest {

    @Test
    public void testPartition() {
        List<Integer> list = createList(100000, 10000);
        checkPartioning(list, 1000);
        checkPartioning(list, 1);
        checkPartioning(createList(100000, 300), 1000);
        checkPartioning(Collections.emptyList(), 1000);
    }

    public void checkPartioning(List<Integer> list, int partitonSize) {
        Collection<List<Integer>> partitions = CollectionUtil.partition(list, partitonSize);
        int size = 0;
        for (List<Integer> partition : partitions) {
            size += partition.size();
            assertTrue(partition.size() <= partitonSize);
        }
        assertEquals(list.size(), size);
    }

    public List<Integer> createList(int maxInt, int numberOfElements) {
        int[] randomIntsArray = IntStream.generate(() -> new Random().nextInt(maxInt))
                .limit(numberOfElements).toArray();
        List<Integer> list = Arrays.stream(randomIntsArray).boxed().collect(Collectors.toList());
        return list;
    }

}
