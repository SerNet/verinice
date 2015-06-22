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
package sernet.verinice.report.service.impl.dynamictable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for path element Link-, Child and ParentElement
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class BaseElement implements IPathElement {

    private static final String RESULT_KEY_SEPERATOR = ".";
    
    private String elementTypeId;
    private IPathElement child;
    private Map<String,Map<String, Object>> result;
    
    public BaseElement() {
        super();
        result = new HashMap<String,Map<String, Object>>();
    }

    public BaseElement(String elementTypeId) {
        this();
        this.elementTypeId = elementTypeId;
    }

    public Map<String, String> createResultMap(Map<String, String> map, String key) {
        Set<String> childKeySet = getResult().keySet();
        for (String childKey : childKeySet) {
            if(key==null || key.endsWith(childKey)) {
                Set<String> resultKeySet = getResult().get(childKey).keySet();
                for (String resultKey : resultKeySet) {
                    String newKey = (key==null) ? resultKey : key + RESULT_KEY_SEPERATOR + resultKey;
                    child.createResultMap(map,  newKey);
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
    
    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IParentPathElement#getChild()
     */
    public IPathElement getChild() {
        return child;
    }

    public void setChild(IPathElement child) {
        this.child = child;
    }

    public String getElementTypeId() {
        return elementTypeId;
    }

    public void setElementTypeId(String elementTypeId) {
        this.elementTypeId = elementTypeId;
    }
    
   
    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#getResult()
     */
    @Override
    public Map<String,Map<String, Object>> getResult() {
        return result;
    }
    
}
