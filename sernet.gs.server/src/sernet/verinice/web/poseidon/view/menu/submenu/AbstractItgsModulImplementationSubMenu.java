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

import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.web.poseidon.services.MenuService;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public abstract class AbstractItgsModulImplementationSubMenu extends AbstractMainSubMenu {

    private static final long serialVersionUID = 1L;

    private MenuService menuService;

    public AbstractItgsModulImplementationSubMenu(String title, MenuService menuService) {
        super(title);
        super.setIcon("fa fa-fw fa-line-chart");
        this.menuService = menuService;
    }

    abstract protected MenuItem getMenuItem(ITVerbund itNetwork);

    abstract protected String getStrategy();

    @Override
    protected void loadChildren() {

        DefaultMenuItem bausteinUmsAll = new DefaultMenuItem("Alle");
        bausteinUmsAll.setUrl("/dashboard/controls-all.xhtml?crunchStrategy=" + getStrategy());
        bausteinUmsAll.setIcon("fa fa-fw fa-area-chart");
        addElement(bausteinUmsAll);

        DefaultMenuItem bausteinUmsTotal = new DefaultMenuItem("Gesamt");
        bausteinUmsTotal.setUrl("/dashboard/controls-total.xhtml?crunchStrategy=" + getStrategy());
        bausteinUmsTotal.setIcon("fa fa-fw fa-area-chart");
        addElement(bausteinUmsTotal);

        for (ITVerbund itNetwork : menuService.getVisibleItNetworks()) {
            addElement(getMenuItem(itNetwork));
        }
    }

}