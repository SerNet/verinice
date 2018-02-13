/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
package sernet.gs.service;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Collator instance of the NumericStringComparator. Sorting is done by
 * NumericStringComparator. All other calls are delegated to a
 * Collator instance for the default locale.
 * 
 * A Collator which deals with alphabet characters 'naturally', but 
 * deals with numerics numerically. See NumericStringComparator
 * for more details.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class NumericStringCollator extends Collator {

    static final Comparator<Object> nsc = new NumericStringComparator();
    final Collator collator = Collator.getInstance(Locale.getDefault());
    
    @Override
    public int compare(String source, String target) {
        return nsc.compare(source, target);
    }

    @Override
    public CollationKey getCollationKey(String source) {
        return collator.getCollationKey(source);
    }
    
    @Override
    public int hashCode() {
        return collator.hashCode();
    }
    
    @Override
    public boolean equals(Object that) {
        return collator.equals(that);
    }
    
}