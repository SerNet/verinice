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
package sernet.verinice.web.poseidon.view.menu.submenu;

import org.primefaces.model.menu.MenuItem;

import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.web.poseidon.services.MenuService;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategyNormalized;
import sernet.verinice.web.poseidon.view.menu.menuitem.ModuleNormalizedMenuItem;

/**
 * Provides sub menu item for cumulated, grouped by module chart menu items.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ModuleImplementationNormalizedSubMenu extends AbstractModuleImplementationSubMenu {

    private static final long serialVersionUID = 1L;

    public ModuleImplementationNormalizedSubMenu(String title, MenuService menuService) {
        super(title, menuService);
    }

    @Override
    protected String getStrategy() {
        return GroupByStrategyNormalized.GET_PARAM_IDENTIFIER;
    }

    @Override
    protected MenuItem getMenuItem(ITVerbund itNetwork) {
        return new ModuleNormalizedMenuItem(itNetwork);
    }

}