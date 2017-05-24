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

import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import sernet.gs.web.Util;
import sernet.verinice.web.poseidon.services.MenuService;
import sernet.verinice.web.poseidon.view.menu.menuitem.HomeMenuItem;
import sernet.verinice.web.poseidon.view.menu.menuitem.IsoEditorMenuItem;
import sernet.verinice.web.poseidon.view.menu.menuitem.ItbpSafeguardsMenuItem;
import sernet.verinice.web.poseidon.view.menu.menuitem.TasksMenuItem;
import sernet.verinice.web.poseidon.view.menu.submenu.IsmsCatalogGroupedByOrganizationSubMenu;
import sernet.verinice.web.poseidon.view.menu.submenu.ItbpControlsSubMenu;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "menu")
public class MenuView {

    private MenuModel model;

    private static final String MESSAGES = "sernet.verinice.web.WebMessages";

    @ManagedProperty("#{menuService}")
    private MenuService menuService;

    @PostConstruct()
    public void initMenu() {
        model = new DefaultMenuModel();
        model.addElement(new HomeMenuItem());
        model.addElement(new IsoEditorMenuItem());
        model.addElement(new ItbpSafeguardsMenuItem());
        model.addElement(new TasksMenuItem());
        model.addElement(new ItbpControlsSubMenu(menuService));
        model.addElement(new IsmsCatalogGroupedByOrganizationSubMenu(msg("menu.control-impl-isms"), menuService));
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

    private String msg(String key) {
        return Util.getMessage(MESSAGES, key);
    }
}
