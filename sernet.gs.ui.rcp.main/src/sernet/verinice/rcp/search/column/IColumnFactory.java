/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search.column;

import sernet.hui.common.connect.PropertyType;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class IColumnFactory {

    public static IColumn getTitleColumn(ColumnStore columnStore) {
        return new TitleColumn(columnStore);
    }

    public static IColumn getIconColumn(ColumnStore columnStore) {
        return new IconColumn(columnStore);
    }

    public static IColumn getScopeColumn(ColumnStore columnStore) {
        return new ScopeColumn(columnStore);
    }

    public static IColumn getPropertyTypeColumn(PropertyType propertyType, IColumnStore columnStore) {
        return new PropertyTypeColumn(columnStore, propertyType);
    }

    public static IColumn getPropertyTypeColumn(PropertyType propertyType, IColumnStore columnStore, int order) {
        return new PropertyTypeColumn(columnStore, propertyType, order);
    }


    public static IColumn getOccurenceColumn(IColumnStore columnStore) {
        return new OccurenceColumn(columnStore);
    }
}
