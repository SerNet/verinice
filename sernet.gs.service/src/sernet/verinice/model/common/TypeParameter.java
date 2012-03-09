/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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

import java.util.HashSet;
import java.util.Set;


import sernet.verinice.interfaces.IParameter;
import sernet.verinice.model.common.ElementFilter;

/**
 * @author Daniel Murygin <dm@sernet.de>
 * 
 */
public class TypeParameter implements IParameter {
    
    private Set<String[]> visibleTypeSet = new HashSet<String[]>(1);
   
    
    public void addType(String[] type) {
        if(visibleTypeSet.contains(ElementFilter.ALL_TYPES)) {
            visibleTypeSet.remove(ElementFilter.ALL_TYPES);
        }
        visibleTypeSet.add(type);
    }
    
    
    public Set<String[]> getVisibleTypeSet() {
        return visibleTypeSet;
    }

    public void setVisibleTypeSet(Set<String[]> visibleTypeSet) {
        this.visibleTypeSet = visibleTypeSet;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.IParameter#getParameter()
     */
    @Override
    public Object getParameter() {
        return getVisibleTypeSet();
    }

}
