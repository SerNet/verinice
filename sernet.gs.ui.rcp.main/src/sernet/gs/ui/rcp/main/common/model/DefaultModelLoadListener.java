/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uzeidler[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.iso27k.ISO27KModel;

/**
 * A convenient implementation for the {@link IModelLoadListener}. 
 * 
 * @author Urs Zeidler uz[at]sernet.de
 *
 */
public class DefaultModelLoadListener implements IModelLoadListener {

    @Override
    public void loaded(BSIModel model) {
    }

    @Override
    public void loaded(ISO27KModel model) {
    }

    @Override
    public void loaded(BpModel model) {
    }

    @Override
    public void loaded(CatalogModel model) {
    }

    @Override
    public void closed(BSIModel model) {
    }

}
