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

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public interface IColumn {

    public String getColumnText();

    public String getImagePath();

    public String getTitle();

    public boolean isMultiselect();

    public boolean isSingleSelect();

    public boolean isNumericSelect();

    public boolean isBooleanSelect();

    public boolean isEnum();

    public boolean isLine();

    public boolean isReference();

    public boolean isCnaLinkReference();

    public boolean isText();

    public boolean isDate();

}