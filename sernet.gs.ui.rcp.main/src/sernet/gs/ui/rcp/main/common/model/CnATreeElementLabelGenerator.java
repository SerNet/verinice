/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.hui.common.connect.IIdentifiableElement;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Generate user-presentable labels for elements
 */
public final class CnATreeElementLabelGenerator {

    public static String getElementTitle(CnATreeElement element) {
        if (element instanceof Safeguard) {
            Safeguard safeguard = (Safeguard) element;
            return createTitleForElementWithSecurityLevel(safeguard.getIdentifier(),
                    safeguard.getSecurityLevel(), safeguard.getTitle());
        } else if (element instanceof BpRequirement) {
            BpRequirement requirement = (BpRequirement) element;
            return createTitleForElementWithSecurityLevel(requirement.getIdentifier(),
                    requirement.getSecurityLevel(), requirement.getTitle());
        } else if (element instanceof IIdentifiableElement) {
            return ((IIdentifiableElement) element).getFullTitle();
        }
        StringBuilder sb = new StringBuilder();
        if (element instanceof IAbbreviatedElement) {
            String abbreviation = ((IAbbreviatedElement) element).getAbbreviation();
            if (!StringUtils.isEmpty(abbreviation)) {
                sb.append(abbreviation);
                sb.append(" ");
            }
        }
        String title = element.getTitle();
        if (!StringUtils.isEmpty(title)) {
            sb.append(title);
        }
        return sb.toString();
    }

    private static String createTitleForElementWithSecurityLevel(String identifier,
            SecurityLevel level, String title) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(identifier)) {
            sb.append(identifier);
            sb.append(' ');
        }
        sb.append('[');
        if (level != null) {
            sb.append(level.getLabel().toUpperCase(Locale.getDefault()));
        }
        sb.append(']');
        if (!StringUtils.isEmpty(title)) {
            sb.append(' ');
            sb.append(title);
        }
        return sb.toString();
    }

    private CnATreeElementLabelGenerator() {

    }

}
