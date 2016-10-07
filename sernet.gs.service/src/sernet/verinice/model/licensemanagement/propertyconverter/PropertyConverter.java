/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.licensemanagement.propertyconverter;

import java.util.Date;

import org.apache.commons.beanutils.ConvertUtils;

import sernet.verinice.interfaces.licensemanagement.IPropertyConverter;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class PropertyConverter implements IPropertyConverter {

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.licensemanagement.IPropertyConverter#convertToString(java.lang.Object)
     */
    @Override
    public String convertToString(Object property) {
        return (String)ConvertUtils.convert(property, String.class);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.licensemanagement.IPropertyConverter#convertToIntger(java.lang.Object)
     */
    @Override
    public Integer convertToInteger(Object property){
        return (Integer) ConvertUtils.lookup(Integer.class).convert(Integer.class, property);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.licensemanagement.IPropertyConverter#convertToDate(java.lang.Object)
     */
    @Override
    public Date convertToDate(Object property){
        return (Date)ConvertUtils.lookup(Date.class).convert(Date.class, property);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.licensemanagement.IPropertyConverter#convertToLong(java.lang.Object)
     */
    @Override
    public Long convertToLong(Object property) {
        return (Long) ConvertUtils.lookup(Long.class).convert(Long.class, property);
    }

}
