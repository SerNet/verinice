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

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.MenuItem;

import sernet.gs.web.Util;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.web.poseidon.services.MenuService;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategy;

/**
 *
 * Provides menu item for chart submenu of it networks.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public abstract class AbstractModuleImplementationSubMenu extends AbstractChartSubMenu {

    private static final long serialVersionUID = 1L;

    private static final String MESSAGES = "sernet.verinice.web.WebMessages";

    private MenuService menuService;

    public AbstractModuleImplementationSubMenu(String title, MenuService menuService) {
        super(title);
        super.setIcon("fa fa-fw fa-line-chart");
        this.menuService = menuService;
    }

    abstract protected MenuItem getMenuItem(ITVerbund itNetwork);

    /**
     * Returns a strategy for calculating chart data.
     *
     * @see GroupByStrategy
     *
     * @return Identifier for the strategy.
     */
    abstract protected String getStrategy();

    @Override
    protected void loadChildren() {
        if(menuService.isAllMenuVisible(MassnahmenUmsetzung.HIBERNATE_TYPE_ID)) {
            addAllAndTotalMenu();
        }
        for (ITVerbund itNetwork : menuService.getVisibleItNetworks()) {
            addElement(getMenuItem(itNetwork));
        }
    }

    protected void addAllAndTotalMenu() {
        DefaultMenuItem bausteinUmsAll = new DefaultMenuItem(Util.getMessage(MESSAGES, "menu.all"));
        bausteinUmsAll.setUrl("/dashboard/controls-module-all.xhtml?crunchStrategy=" + getStrategy());
        bausteinUmsAll.setIcon("fa fa-fw fa-area-chart");
        addElement(bausteinUmsAll);

        DefaultMenuItem bausteinUmsTotal = new DefaultMenuItem(Util.getMessage(MESSAGES, "menu.total"));
        bausteinUmsTotal.setUrl("/dashboard/controls-module-total.xhtml?crunchStrategy=" + getStrategy());
        bausteinUmsTotal.setIcon("fa fa-fw fa-area-chart");
        addElement(bausteinUmsTotal);
    }
}