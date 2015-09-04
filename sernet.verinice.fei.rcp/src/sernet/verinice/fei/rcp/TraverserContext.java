/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.fei.rcp;

import java.util.Hashtable;
import java.util.Map;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TraverserContext {

    private Map<String, Object> propertyMap;


    public TraverserContext() {
        super();
        propertyMap = new Hashtable<String, Object>();
    }
    
    public void addProperty(String key, Object value) {
        getPropertyMap().put(key, value);
    }
    
    public Object getProperty(String key) {
        return getPropertyMap().get(key);
    }
    
    public Object removeProperty(String key) {
        return getPropertyMap().remove(key);
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected TraverserContext clone() {
        TraverserContext clone = new TraverserContext();
        for (String key : this.getPropertyMap().keySet()) {
            clone.addProperty(key, this.getPropertyMap().get(key)); 
        }
        return clone;
    }
}
