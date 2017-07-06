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
import org.primefaces.model.menu.MenuElement;

/**
 * Makes sure that every sub menu loads its children.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
abstract class AbstractChartSubMenu extends DefaultSubMenu {

    private static final long serialVersionUID = 1L;

    private boolean isLoaded = false;

    public AbstractChartSubMenu(String title) {
        super(title);
    }

    /**
     * Adds MenuItem or {@link DefaultSubMenu} to an implementation of this
     * class.
     */
    protected abstract void loadChildren();

    @Override
    public List<MenuElement> getElements() {
        loadData();
        return super.getElements();
    }

    @Override
    public int getElementsCount() {
        loadData();
        return super.getElementsCount();
    }

    private void loadData() {
        if (!isLoaded) {
            loadChildren();
            isLoaded = true;
        }
    }
}
