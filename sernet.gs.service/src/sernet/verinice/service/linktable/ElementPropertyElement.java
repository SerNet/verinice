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

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Path element in a column path definition which loads a property of an element.
 * Delimiter for this path element is: IPathElement.DELIMITER_PROPERTY (.)
 * See LinkTableDataModel for a description of column path definitions.
 *
 * @see LinkTableDataModel
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementPropertyElement extends PropertyElement<CnATreeElement>  {

    private static final Logger LOG = Logger.getLogger(ElementPropertyElement.class);

    public ElementPropertyElement() {
        super();
        result = new HashMap<>();
    }

    public ElementPropertyElement(String propertyTypeId) {
        this.propertyTypeId = propertyTypeId;
        result = new HashMap<>();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.IPathElement#load(sernet.verinice.model.common.CnATreeElement, sernet.verinice.interfaces.graph.VeriniceGraph)
     */
    @Override
    public void load(CnATreeElement element, VeriniceGraph graph) {
        String propertyValue = getPropertyValue(element);
        Map<String, Object> resultMap = new HashMap<>();
        
        String parentId = String.valueOf(element.getDbId());
        
        resultMap.put(parentId, propertyValue);
        if (LOG.isDebugEnabled()) {
            LOG.debug(element.getTitle() + "(" + parentId + ")." + propertyTypeId + " = " + propertyValue + " loaded");
        }
        getResult().put(parentId, resultMap);
    }
    
    protected String getPropertyValue(CnATreeElement element) {
        IPropertyAdapter adapter = PropertyAdapterFactory.getAdapter(element);
        return adapter.getPropertyValue(propertyTypeId);
    }

}
