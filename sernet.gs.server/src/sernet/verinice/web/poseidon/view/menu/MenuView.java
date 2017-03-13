/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
package sernet.verinice.web.poseidon.view.menu;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import sernet.verinice.web.poseidon.services.MenuService;
import sernet.verinice.web.poseidon.view.menu.submenu.IsmsCatalogGroupedByOrganizationSubMenu;
import sernet.verinice.web.poseidon.view.menu.submenu.ItgsControlsSubMenu;
import sernet.verinice.web.poseidon.view.menu.submenu.ItgsModulImplementationCumulatedSubMenu;
import sernet.verinice.web.poseidon.view.menu.submenu.ItgsModulImplementationNormalizedSubMenu;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "menu")
public class MenuView {

    private MenuModel model;

    @ManagedProperty("#{menuService}")
    private MenuService menuService;

    @PostConstruct()
    public void initMenu() {

        model = new DefaultMenuModel();

        DefaultMenuItem home = new DefaultMenuItem("Home");
        home.setUrl("/");
        home.setIcon("fa fa-fw fa-home");
        model.addElement(home);

        DefaultSubMenu itgsControls = new ItgsControlsSubMenu(menuService);
        model.addElement(itgsControls);

        ItgsModulImplementationCumulatedSubMenu element = new ItgsModulImplementationCumulatedSubMenu("BausteinUms. Kum.", menuService);
        model.addElement(element);

        DefaultSubMenu itgsModuldImplementationNormalizedSubMenu = new ItgsModulImplementationNormalizedSubMenu("BausteinUms. Norm", menuService);
        model.addElement(itgsModuldImplementationNormalizedSubMenu);

        DefaultSubMenu massnahmenUmsetzungIsms = new IsmsCatalogGroupedByOrganizationSubMenu("MassnahmenUms. ISMS", menuService);
        model.addElement(massnahmenUmsetzungIsms);
    }


    public MenuModel getModel() {
        return model;
    }

    public void setModel(MenuModel menuModel) {
        this.model = menuModel;
    }


    public MenuService getMenuService() {
        return menuService;
    }


    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }
}
