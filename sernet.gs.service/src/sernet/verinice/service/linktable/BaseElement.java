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
package sernet.verinice.service.linktable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for for all path elements except property elements.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class BaseElement<E,C> implements IPathElement<E,C> {

    private String elementTypeId;
    private IPathElement<C,?> child;
    private Map<String,Map<String, Object>> result;
    private String alias;
    private Direction direction;
    
    public BaseElement() {
        super();
        result = new HashMap<String,Map<String, Object>>();
    }

    public BaseElement(String elementTypeId) {
        this();
        this.elementTypeId = elementTypeId;
    }

    /**
     * Add a single result to the map with all results:
     * - Iterate over all results
     * - Find the result which fits
     * - Iterate over the results of the results
     * - Expand the key and delegate processing to the child element
     * 
     * @param map A map with all results
     * @param key The key of the result
     * @return A map with all results including the single result
     */
    @Override
    public Map<String, String> addResultToMap(Map<String, String> map, String key) {
        // Iterate over all results
        Set<String> childKeySet = getResult().keySet();
        for (String childKey : childKeySet) {
            // Find the result which fits
            if(key==null || key.endsWith(childKey)) {
                // Iterate over the results of the results
                Set<String> resultKeySet = getResult().get(childKey).keySet();
                for (String resultKey : resultKeySet) {
                    // Expand the key and delegate processing to the child element
                    String newKey = (key==null) ? resultKey : key + RESULT_KEY_SEPERATOR + resultKey;
                    child.addResultToMap(map, newKey);
                }
            }
        }
        return map;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#setTypeId(java.lang.String)
     */
    @Override
    public void setTypeId(String typeId) {
        setElementTypeId(typeId);
    }

    @Override
    public IPathElement<C,?> getChild() {
        return child;
    }

    @Override
    public void setChild(IPathElement<C,?> child) {
        this.child = child;
    }

    @Override
    public String getTypeId() {
        return getElementTypeId();
    }

    public String getElementTypeId() {
        return elementTypeId;
    }

    public void setElementTypeId(String elementTypeId) {
        this.elementTypeId = elementTypeId;
    }


    @Override
    public Map<String,Map<String, Object>> getResult() {
        return result;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
        if(getChild()!=null) {
            getChild().setAlias(alias);
        }
    }
    
    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

}
