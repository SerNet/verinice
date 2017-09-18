/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.catalog;

import sernet.verinice.model.common.CnATreeElement;

/**
 * The root {@link CnATreeElement} for all catalogs displayed by the Compendium
 * View.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class CatalogModel extends CnATreeElement {
    private final static transient Logger log = Logger.getLogger(CatalogModel.class);

    private static final long serialVersionUID = 1L;

    public static final String TYPE_ID = "catalog_model";

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTitle() {
        return "Catalog Model";
    }


}
