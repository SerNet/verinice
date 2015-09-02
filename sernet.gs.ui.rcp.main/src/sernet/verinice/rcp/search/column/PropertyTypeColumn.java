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
 * Adapter for {@link PropertyType}. We need some aditional information for
 * doing proper sorting of columns, since the icon and scope columns are
 * default, but not a configured in SNCA.xml.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class PropertyTypeColumn extends AbstractColumn {

    private PropertyType propertyType;

    private int rank;

    public PropertyTypeColumn(IColumnStore columnstore, PropertyType propertyType) {
        super(columnstore);
        this.propertyType = propertyType;
    }

    public PropertyTypeColumn(IColumnStore columnstore, PropertyType propertyType, int order) {
        this(columnstore, propertyType);
        this.rank = order;
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumn#isMultiselect()
     */
    @Override
    public boolean isMultiselect() {
        return (propertyType == null) ? false : propertyType.isMultiselect();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumn#isSingleSelect()
     */
    @Override
    public boolean isSingleSelect() {
        return (propertyType == null) ? false : propertyType.isSingleSelect();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumn#isNumericSelect()
     */
    @Override
    public boolean isNumericSelect() {
        return (propertyType == null) ? false : propertyType.isNumericSelect();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.IColumn#isBooleanSelect()
     */
    @Override
    public boolean isBooleanSelect() {
        return (propertyType == null) ? false : propertyType.isBooleanSelect();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.IColumn#isEnum()
     */
    @Override
    public boolean isEnum() {
        return (propertyType == null) ? false : propertyType.isEnum();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.IColumn#isLine()
     */
    @Override
    public boolean isLine() {
        return (propertyType == null) ? false : propertyType.isLine();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.IColumn#isReference()
     */
    @Override
    public boolean isReference() {
        return (propertyType == null) ? false : propertyType.isReference();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.IColumn#isCnaLinkReference()
     */
    @Override
    public boolean isCnaLinkReference() {
        return (propertyType == null) ? false : propertyType.isCnaLinkReference();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.IColumn#isText()
     */
    @Override
    public boolean isText() {
        return (propertyType == null) ? false : propertyType.isText();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.IColumn#isDate()
     */
    @Override
    public boolean isDate() {
        return (propertyType == null) ? false : propertyType.isDate();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.column.IColumn#getTitle()
     */
    @Override
    public String getTitle() {
        return propertyType.getName();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.AbstractColumn#getId()
     */
    @Override
    public String getId() {
        return propertyType.getId();
    }

    public String getPropertyTags() {
        return propertyType.getTags();
    }

    @Override
    public int getRank(){
        return rank;
    }
}
