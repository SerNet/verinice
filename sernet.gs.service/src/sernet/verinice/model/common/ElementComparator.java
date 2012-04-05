/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.model.common;

import java.util.Comparator;

import sernet.gs.service.NumericStringComparator;

/**
 * Compares CnATreeElements by it's title using {@link NumericStringComparator}.
 * 
 * @see NumericStringComparator
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementComparator<T> implements Comparator<T> {
    
    NumericStringComparator numericStringComparator = new NumericStringComparator();

    ITitleAdaptor<T> titleAdaptor;
 
    /**
     * @param titleAdaptor
     */
    public ElementComparator(ITitleAdaptor<T> titleAdaptor) {
        super();
        this.titleAdaptor = titleAdaptor;
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second
     * 
     * CnATreeElement are compared by it's title using {@link NumericStringComparator}.
     * 
     * @see NumericStringComparator#compare(Object, Object)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(T o1, T o2) {
        int FIRST_IS_LESS = -1;
        int EQUAL = 0;
        int FIRST_IS_GREATER = 1;
        int result = FIRST_IS_LESS;
        if (o1 != null && titleAdaptor.getTitle(o1) != null) {
            if (o2 != null && titleAdaptor.getTitle(o2) != null) {
                result = numericStringComparator.compare(titleAdaptor.getTitle(o1).toLowerCase(), titleAdaptor.getTitle(o2).toLowerCase());
            } else {
                result = FIRST_IS_GREATER;
            }
        } else if (o2 == null) {
            result = EQUAL;
        }
        return result;
    }

}