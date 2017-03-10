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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.web.poseidon.services.MenuService;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategyNormalized;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategySum;
import sernet.verinice.web.poseidon.view.menu.submenu.IsmsCatalogGroupedByOrganizationSubMenu;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "menu")
public class Menu {

    private static final Logger log = Logger.getLogger(Menu.class);

    private MenuModel model;

    @ManagedProperty("#{menuService}")
    private MenuService menuService;

    private DefaultSubMenu bausteinUmsSubMenu;

    private DefaultSubMenu massnahmenUmsetzungSubMenu;

    private DefaultSubMenu bausteinUmsNormSubMenu;

    private DefaultSubMenu massnahmenUmsetzungIsms;

    @PostConstruct()
    public void initMenu() {

        model = new DefaultMenuModel();

        DefaultMenuItem home = new DefaultMenuItem("Home");
        home.setUrl("/");
        home.setIcon("fa fa-fw fa-home");
        model.addElement(home);

        massnahmenUmsetzungSubMenu = new DefaultSubMenu("MassnahmenUms.");
        massnahmenUmsetzungSubMenu.setIcon("fa fa-fw fa-line-chart");

        DefaultMenuItem all = new DefaultMenuItem("Alle");
        all.setUrl("/dashboard/implementation-all.xhtml");
        all.setIcon("fa fa-fw fa-area-chart");
        massnahmenUmsetzungSubMenu.addElement(all);

        DefaultMenuItem total = new DefaultMenuItem("Gesamt");
        total.setUrl("/dashboard/implementation-total.xhtml");
        total.setIcon("fa fa-fw fa-area-chart");
        massnahmenUmsetzungSubMenu.addElement(total);

        try {
            addItNetworks(massnahmenUmsetzungSubMenu, "implementation-itnetwork.xhtml", null);
        } catch (UnsupportedEncodingException e) {
            log.error("could not create menu item", e);
        }

        bausteinUmsSubMenu = new DefaultSubMenu("BausteinUms. Kum.");
        bausteinUmsSubMenu.setIcon("fa fa-fw fa-line-chart");

        DefaultMenuItem bausteinUmsAll = new DefaultMenuItem("Alle");
        bausteinUmsAll.setUrl("/dashboard/implementation-bstums-all.xhtml");
        bausteinUmsAll.setIcon("fa fa-fw fa-area-chart");
        bausteinUmsSubMenu.addElement(bausteinUmsAll);

        DefaultMenuItem bausteinUmsTotal = new DefaultMenuItem("Gesamt");
        bausteinUmsTotal.setUrl("/dashboard/implementation-bstums-total.xhtml");
        bausteinUmsTotal.setIcon("fa fa-fw fa-area-chart");
        bausteinUmsSubMenu.addElement(bausteinUmsTotal);

        try {
            addItNetworks(bausteinUmsSubMenu, "implementation-bstums-itnetwork.xhtml", GroupByStrategySum.GET_PARAM_IDENTIFIER);
        } catch (UnsupportedEncodingException e) {
            log.error("could not create menu item", e);
        }

        bausteinUmsNormSubMenu = new DefaultSubMenu("BausteinUms. Norm.");
        bausteinUmsNormSubMenu.setIcon("fa fa-fw fa-line-chart");

        String normalizeParameter = "?crunchStrategy=" + GroupByStrategyNormalized.GET_PARAM_IDENTIFIER;

        DefaultMenuItem bausteinUmsNormAll = new DefaultMenuItem("Alle");
        bausteinUmsNormAll.setUrl("/dashboard/implementation-bstums-all.xhtml" + normalizeParameter);
        bausteinUmsNormAll.setIcon("fa fa-fw fa-area-chart");
        bausteinUmsNormSubMenu.addElement(bausteinUmsNormAll);

        DefaultMenuItem bausteinUmsNormTotal = new DefaultMenuItem("Gesamt");
        bausteinUmsNormTotal.setUrl("/dashboard/implementation-bstums-total.xhtml" + normalizeParameter);
        bausteinUmsNormTotal.setIcon("fa fa-fw fa-area-chart");
        bausteinUmsNormSubMenu.addElement(bausteinUmsNormTotal);

        try {
            addItNetworks(bausteinUmsNormSubMenu, "implementation-bstums-itnetwork.xhtml", GroupByStrategyNormalized.GET_PARAM_IDENTIFIER);
        } catch (UnsupportedEncodingException e) {
            log.error("could not create menu item", e);
        }

        massnahmenUmsetzungIsms = new IsmsCatalogGroupedByOrganizationSubMenu("MassnahmenUms. ISMS", menuService);

        model.addElement(massnahmenUmsetzungSubMenu);
        model.addElement(bausteinUmsSubMenu);
        model.addElement(bausteinUmsNormSubMenu);

        if (massnahmenUmsetzungIsms.getElementsCount() > 0) {
            model.addElement(massnahmenUmsetzungIsms);
        }

        addMiscItems();
    }

    private void addItNetworks(DefaultSubMenu menu, String templateFile, String param) throws UnsupportedEncodingException {
        for (ITVerbund itNetwork : getMenuService().getVisibleItNetworks()) {
            String title = itNetwork.getTitle();
            DefaultMenuItem item = new DefaultMenuItem(title);
            item.setIcon("fa fa-fw fa-area-chart");
            String scopeIdParam = "scopeId=" + itNetwork.getScopeId();
            String titleParam = "itNetwork=" + URLEncoder.encode(title, "UTF-8");
            String algorithm = (param == null) ? "" : "crunchStrategy=" + param;
            item.setUrl("/dashboard/" + templateFile + "?" + scopeIdParam + "&" + titleParam + "&" + algorithm);
            menu.addElement(item);
        }
    }

    private void addMiscItems() {

        DefaultMenuItem download = new DefaultMenuItem("Downloads");
        download.setUrl("/misc/download.xhtml");
        download.setIcon("fa fa-fw fa-download");
        model.addElement(download);

        DefaultMenuItem help = new DefaultMenuItem("Help");
        help.setUrl("/misc/help.xhtml");
        help.setIcon("fa fa-fw fa-info-circle");
        model.addElement(help);
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
