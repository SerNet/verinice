/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.filter;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Filters by dynamic (SNCA) boolean property values.
 */
public class DynamicBooleanPropertyFilter extends AbstractDynamicPropertyFilter {
    private Boolean filterValue;

    public DynamicBooleanPropertyFilter(String dynamicPropertyType, Boolean value) {
        super(dynamicPropertyType);
        filterValue = value;
    }

    @Override
    protected boolean matches(CnATreeElement treeElement, String dynamicPropertyType) {
        return treeElement.getDynamicBooleanProperty(dynamicPropertyType).equals(filterValue);
    }
}
