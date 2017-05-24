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

import java.util.List;

import org.primefaces.model.menu.DefaultSubMenu;

import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.web.poseidon.services.MenuService;

public class IsmsCatalogGroupedByOrganizationSubMenu extends AbstractChartSubMenu {

    private static final long serialVersionUID = 1L;

    private MenuService menuService;

    public IsmsCatalogGroupedByOrganizationSubMenu(String title, MenuService menuService) {
        super(title);
        this.setIcon("fa fa-fw fa-line-chart");
        this.menuService = menuService;
    }

    protected void loadChildren() {
        List<Organization> visibleOrganisations = menuService.getVisibleOrganisations();
        for (Organization organization : visibleOrganisations) {
            DefaultSubMenu menuControlIsms = new ISOControlsSubMenu(organization, menuService);
            if (menuControlIsms.getElementsCount() > 0) {
                super.addElement(menuControlIsms);
            }
        }

    }
}