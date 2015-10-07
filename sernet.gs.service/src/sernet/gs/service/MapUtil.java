/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.gs.service;

import java.util.Map;

/**
 * MapUtil provides util / helper methods for map handling.
 * Do not instantiate this class. Use static methods.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class MapUtil {

 
    /**
     * Do not instantiate this class
     */
    private MapUtil() {
    }
    
    /**
     * Compares maps for value-equality:
     * 
     * 1. Check that the maps are the same size
     * 2. Get the set of keys from one map
     * 3. For each key from that set you retrieved, 
     *    check that the value retrieved from each map for that key is the same 
     *    (if the key is absent from one map, that's a total failure of equality).
     * 
     * @param mapA
     * @param mapB
     * @return True if all values from mapA are equal to values from mapB 
     * @see http://stackoverflow.com/questions/2674021/how-to-compare-two-maps-by-their-values
     */
    public static <K,V> boolean compare(Map<K, V> mapA, Map<K, V> mapB) {
        if (mapA == mapB) return true;
        if (mapA == null || mapB == null || mapA.size() != mapB.size()) return false;
        for (K key : mapA.keySet()) {
            V value1 = mapA.get(key);
            V value2 = mapB.get(key);
            if (value1 == null && value2 == null) {
                continue;
            } else if (value1 == null || value2 == null) {
                return false;
            }
            if (!value1.equals(value2)) {
                return false; 
            }
        }
        return true;
    }

}
