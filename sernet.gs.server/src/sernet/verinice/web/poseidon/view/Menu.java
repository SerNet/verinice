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
package sernet.verinice.web.poseidon.view;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.web.poseidon.services.MenuService;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "menu")
public class Menu {

    private MenuModel model;

    private DefaultSubMenu massnahmenUmsetzungSubMenu;

    @ManagedProperty("#{menuService}")
    private MenuService menuService;

    @PostConstruct()
    public void initMenu(){

        model = new DefaultMenuModel();

        massnahmenUmsetzungSubMenu = new DefaultSubMenu("MassnahmenUms.");
        massnahmenUmsetzungSubMenu.setIcon("fa fa-fw fa-line-chart");

        DefaultMenuItem total = new DefaultMenuItem("Gesamt");
        total.setUrl("/dashboard/implementation-total.xhtml");
        total.setIcon("fa fa-fw fa-area-chart");
        massnahmenUmsetzungSubMenu.addElement(total);

        addItNetworks();

        model.addElement(massnahmenUmsetzungSubMenu);
    }

    private void addItNetworks() {
        for(ITVerbund itNetwork : getMenuService().getVisibleItNetworks()){
            DefaultMenuItem item = new DefaultMenuItem(itNetwork.getTitle());
            item.setIcon("fa fa-fw fa-area-chart");
            massnahmenUmsetzungSubMenu.addElement(item);
        }
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
