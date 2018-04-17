/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.model.common;

/**
 * Represents a modeling domain, e.g. Base Protection or ISO
 */
public enum Domain {
    ISM(Messages.getString("Domain.ISM")), BASE_PROTECTION_OLD(
            Messages.getString("Domain.BASE_PROTECTION_OLD")), BASE_PROTECTION(
                    Messages.getString("Domain.BASE_PROTECTION")), @Deprecated
    DATA_PROTECTION(Messages.getString("Domain.DATA_PROTECTION"));

    private final String label;

    private static final String LABEL_OBSOLETE = Messages.getString("Domain.obsolete");

    Domain(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Get the label for obsolete domains, e.g. data protection
     */
    public static String getLabelObsolete() {
        return LABEL_OBSOLETE;
    }
}
