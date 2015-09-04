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
package sernet.hui.common.connect;

import java.util.Comparator;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class PropertyTypeNameComparator implements Comparator<PropertyType> {

    private static PropertyTypeNameComparator instance = new PropertyTypeNameComparator();

    private PropertyTypeNameComparator() {
        super();
    }
    
    public static PropertyTypeNameComparator getInstance() {
        return instance;
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(PropertyType o1, PropertyType o2) {
        final int less = -1;
        final int equal = 0;
        final int greater = 1;
        int result = less;
        if(o2!=null && o1!=null) {
            if(o1.getName()==null) {
                result = (o2.getName()==null) ? equal : greater;
            } else if(o2.getName()!=null) {
                result = o1.getName().compareTo(o2.getName());
            }
        } else {
            if(o2==null) {
                result = (o1==null) ? equal : less;
            } else {
                result = greater;
            }
        }
        return result;
    } 

}
