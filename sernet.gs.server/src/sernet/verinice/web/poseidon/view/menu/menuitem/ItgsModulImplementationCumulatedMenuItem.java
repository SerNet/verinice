<<<<<<< HEAD
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
package sernet.verinice.web.poseidon.view.menu.menuitem;

import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategySum;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ItgsModulImplementationCumulatedMenuItem extends AbstractItgsControlChartMenuItem {

    public ItgsModulImplementationCumulatedMenuItem(ITVerbund itVerbund) {
        super(itVerbund);
    }

    private static final long serialVersionUID = 1L;

    @Override
    String getTemplateFile() {
        return "implementation-bstums-itnetwork.xhtml";
    }

    @Override
    String getStrategy() {
        return GroupByStrategySum.GET_PARAM_IDENTIFIER;
    }
}
||||||| merged common ancestors
=======
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

import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategySum;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ItgsModulImplementationCumulatedMenuItem extends AbstractItgsControlMenuItem {

    ItgsModulImplementationCumulatedMenuItem(ITVerbund itVerbund) {
        super(itVerbund);
    }

    private static final long serialVersionUID = 1L;

    @Override
    String getTemplateFile() {
        return "implementation-bstums-itnetwork.xhtml";
    }

    @Override
    String getStrategy() {
        return GroupByStrategySum.GET_PARAM_IDENTIFIER;
    }
}
>>>>>>> Refactor Menu.
