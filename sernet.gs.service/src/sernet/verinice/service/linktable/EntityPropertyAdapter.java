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

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.connect.URLUtil;
import sernet.verinice.model.common.CnATreeElement;

/**
 * An EntityPropertyAdapter reads properties from an CnATreeElement.
 * This class is used in the context of link tables.
 * 
 * See ILinkTableService for an introduction to link tables.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class EntityPropertyAdapter implements IPropertyAdapter {


    private final CnATreeElement element;

    public EntityPropertyAdapter(CnATreeElement elment) {
        this.element = elment;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.linktable.IPropertyAdapter#getPropertyValue(java.lang.Object, java.lang.String)
     */
    @Override
    public String getPropertyValue(String propertyId) {
        if(element==null) {
            return null;
        }
        Entity entity = element.getEntity();
        if(entity==null) {
            return null;
        }
        
        return getPropertyValue(entity, propertyId);
    }

    public String getPropertyValue(Entity entity, String propertyId) {
        String value;      
        if(CnATreeElement.isStaticProperty(propertyId)) {
            value = CnATreeElement.getStaticProperty(element, propertyId);
        } else {             
            value = entity.getPropertyValue(propertyId);
            PropertyType propertyType = getPropertyType(element.getTypeId(), propertyId);
            if(isUrlAndNotEmpty(propertyType, value)) {
                value = URLUtil.createLinkForSpreadsheet(value);
            }
            if(isDate(propertyType)) {
                value = entity.getDateInISO8601(propertyId);
            }
        }
        return value;
    }

    protected boolean isUrlAndNotEmpty(PropertyType propertyType, String value) {
        return propertyType!=null && propertyType.isURL() && isNotEmpty(value);
    }

    private boolean isDate(PropertyType propertyType) {
        return propertyType!=null && propertyType.isDate();
    }
    
    private boolean isNotEmpty(String value) {
        return value!=null && !value.isEmpty();
    }
    
    private PropertyType getPropertyType(String elementId, String propertyId) {
        return getEntityType(elementId).getPropertyType(propertyId);
    }
    
    private EntityType getEntityType(String elementId) {
        return HUITypeFactory.getInstance().getEntityType(elementId);
    }
    
}
