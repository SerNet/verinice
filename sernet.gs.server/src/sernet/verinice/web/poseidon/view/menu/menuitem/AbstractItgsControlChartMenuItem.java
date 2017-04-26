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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.DefaultMenuItem;

import sernet.verinice.model.bsi.ITVerbund;

/**
 * Provides menu item for IT baseline protection charts.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
abstract class AbstractItbpControlMenuItem extends DefaultMenuItem {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(AbstractItbpControlMenuItem.class);

    private ITVerbund itNetwork;

    abstract String getTemplateFile();

    abstract String getStrategy();

    AbstractItbpControlMenuItem(ITVerbund itVerbund) {
        super(itVerbund.getTitle());
        this.itNetwork = itVerbund;
        this.setUrl(createUrl());
    }

    private String createUrl() {
        try {
            String scopeIdParam = "scopeId=" + itNetwork.getScopeId();
            String titleParam = "itNetwork=" + URLEncoder.encode(itNetwork.getTitle(), "UTF-8");
            return "/dashboard/" + getTemplateFile() + "?" + scopeIdParam + "&" + titleParam + getStrategyParam();
        } catch (UnsupportedEncodingException e) {
            log.error("cannot create url", e);
        }

        return "";
    }

    private String getStrategyParam() {
        return getStrategy() != null ? "&strategy=" + getStrategy() : "";
    }

    @Override
    public String getIcon() {
        return "fa fa-fw fa-industry";
    }
}
