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

import sernet.verinice.service.linktable.IPathElement.Direction;

/**
 * Abstract base class for property elements in a column path.
 * A column path is a description of a report column in GenericDataModel.
 * See GenericDataModel for a description of column path definitions.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class PropertyElement {

    protected Map<String,Map<String, Object>> result;
    private String alias;
    private Direction direction;

    public PropertyElement() {
        super();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#createValueMap(java.util.Map, java.lang.String)
     */
    public Map<String, String> createResultMap(Map<String, String> map, String key) {
        Set<String> childKeySet = getResult().keySet();
        for (String childKey : childKeySet) {
            if(key.endsWith(childKey)) {
                Map<String,Object> resultMap = getResult().get(childKey);
                Set<String> resultKeySet = getResult().keySet();
                for (String resultKey : resultKeySet) {
                    if(key.endsWith(resultKey)) {
                        String value = (String) resultMap.get(resultKey);
                        map.put(key, value);
                    }
                }
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