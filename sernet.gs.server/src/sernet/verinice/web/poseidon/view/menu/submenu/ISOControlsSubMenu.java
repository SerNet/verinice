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

import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.menu.DefaultMenuItem;

import sernet.gs.web.Util;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.web.poseidon.services.MenuService;
import sernet.verinice.web.poseidon.view.menu.menuitem.ControlsIsoMenuItem;

/**
 * Provides a sub menu item for ISO control catalogs.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
class ISOControlsSubMenu extends AbstractChartSubMenu {

    private static final long serialVersionUID = 1L;

    private static final String MESSAGES = "sernet.verinice.web.WebMessages";

    private Organization organization;

    private MenuService menuService;

    private List<ControlGroup> catalogs;

    public ISOControlsSubMenu(Organization organization, MenuService menuService) {
        super(organization.getTitle());
        this.setIcon("fa fa-fw fa-building");
        this.organization = organization;
        this.menuService = menuService;
    }

    protected void loadChildren() {

        getCatalogs();

        if (!catalogs.isEmpty()) {
            addStaticMenuItems();
            addCatalogs();
        }
    }

    private void addCatalogs() {
        for (ControlGroup catalog : catalogs) {
            addElement(new ControlsIsoMenuItem(organization, catalog));
        }
    }

    private void getCatalogs() {
        catalogs = new ArrayList<>();
        for (ControlGroup catalog : menuService.getCatalogs()) {
            if (catalog.getScopeId().equals(organization.getDbId())) {
                catalogs.add(catalog);
            }
        }
    }

    private void addStaticMenuItems() {
        DefaultMenuItem allIsmsChartsMenuItem = new DefaultMenuItem(Util.getMessage(MESSAGES, "menu.all"));
        allIsmsChartsMenuItem.setUrl("/dashboard/controls-iso-all.xhtml?scopeId=" + organization.getDbId());
        allIsmsChartsMenuItem.setIcon("fa fa-fw fa-area-chart");
        addElement(allIsmsChartsMenuItem);

        DefaultMenuItem totalIsmsChartsMenuItem = new DefaultMenuItem(Util.getMessage(MESSAGES, "menu.total"));
        totalIsmsChartsMenuItem.setUrl("/dashboard/controls-iso-total.xhtml?scopeId=" + organization.getDbId());
        totalIsmsChartsMenuItem.setIcon("fa fa-fw fa-area-chart");
        addElement(totalIsmsChartsMenuItem);
    }

}