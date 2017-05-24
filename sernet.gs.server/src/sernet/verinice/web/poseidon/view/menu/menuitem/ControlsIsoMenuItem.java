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

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.menu.DefaultMenuItem;

import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ControlsIsoMenuItem extends DefaultMenuItem {

    private static final long serialVersionUID = 1L;
    private Organization organization;
    private ControlGroup controlGroup;
    private String templateFile = "/dashboard/controls-iso.xhtml";

    public ControlsIsoMenuItem(Organization organization, ControlGroup controlGroup) {
        super(controlGroup.getTitle());
        this.organization = organization;
        this.controlGroup = controlGroup;
        setUrl(templateFile);
        setUrl(createUrl());
        setIcon("fa fa-fw fa-area-chart");
    }

    private String createUrl(){
        String param = StringUtils.join(new String[]{getScopeId(), getCatalogId(), getCatalogName(), getOrganizationName()}, "&");
        return templateFile + "?" + param;
    }

    private String getOrganizationName() {
        return "organizationName=" + organization.getTitle();
    }

    private String getCatalogName() {
        return "catalogName=" + controlGroup.getTitle();
    }

    private String getCatalogId() {
        return "catalogId=" + String.valueOf(controlGroup.getDbId());
    }

    private String getScopeId() {
        return "scopeId=" + String.valueOf(organization.getScopeId());
    }
}
