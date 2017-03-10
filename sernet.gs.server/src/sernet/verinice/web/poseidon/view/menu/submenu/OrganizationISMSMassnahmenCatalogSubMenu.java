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

import java.util.Set;

import org.primefaces.model.menu.DefaultMenuItem;

import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.web.poseidon.services.MenuService;

class OrganizationISMSMassnahmenCatalogSubMenu extends AbstractMainSubMenu {

    private static final long serialVersionUID = 1L;

    private Organization organization;

    private MenuService menuService;

    public OrganizationISMSMassnahmenCatalogSubMenu(Organization organization, MenuService menuService) {
        super(organization.getTitle());
        this.organization = organization;
        this.menuService = menuService;
    }

    protected void loadChildren() {
        Set<ControlGroup> catalogs = menuService.getCatalogs();
        for (ControlGroup catalog : catalogs) {
            if (catalog.getScopeId().equals(organization.getDbId())) {
                DefaultMenuItem catalogItem = new DefaultMenuItem(catalog.getTitle());
                super.addElement(catalogItem);
            }
        }
    }
}