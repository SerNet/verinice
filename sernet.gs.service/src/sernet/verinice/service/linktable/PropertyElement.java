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
package sernet.verinice.service.linktable;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Abstract base class for property elements in a column path.
 * A column path is a description of a report column in LinkTableDataModel.
 * See LinkTableDataModel for a description of column path definitions.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class PropertyElement<E> implements IPathElement<E,EndOfPathElement> {

    private static final Logger LOG = Logger.getLogger(PropertyElement.class);
    protected Map<String,Map<String, Object>> result;
    private String alias;
    private Direction direction;

    public PropertyElement() {
        super();
    }  
    
    /**
     * Iterate over all result, find the result which fits
     * and put it in the map.
     * 
     * @param map The map with all results
     * @param key The key of the result
     * @return The map with all results
     */
    @Override
    public Map<String, String> addResultToMap(Map<String, String> map, String key) {
        // Iterate over all result
        Set<String> childKeySet = getResult().keySet();
        for (String childKey : childKeySet) {
            // Find the result which fits
            if(key.endsWith(childKey)) {
                // Put the result in the map
                Map<String,Object> resultMap = getResult().get(childKey);
                Object object = resultMap.get(childKey);
                String value;
                if (object instanceof LinkTableResult) {
                    LinkTableResult ltResult = (LinkTableResult) object;
                    value = String.valueOf(ltResult.getResult());
                } else {
                    value = (String) object;
                }
                map.put(key, value);             
            }
        }
        return map;
    }

    public String getPropertyTypeId() {
        return propertyTypeId;
    }

    public void setPropertyTypeId(String propertyTypeId) {
        this.propertyTypeId = propertyTypeId;
    }
    
    public String getTypeId() {
        return propertyTypeId;
    }

    public void setTypeId(String typeId) {
        propertyTypeId = typeId;
    
    }

    public Map<String,Map<String, Object>> getResult() {
        return result;
    }

    public IPathElement<EndOfPathElement,?> getChild() {
        // A property element never has childs
        return null;
    }

 
    public void setChild(IPathElement<EndOfPathElement,?> child) {
     // A property element never has childs
    }

   
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    protected String propertyTypeId;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

}