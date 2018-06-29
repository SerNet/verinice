/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * Contributors:
 * - Jochen Kemnade
 * - Alexander Ben Nasrallah
 *
 ******************************************************************************/
package sernet.verinice.model.bp;

/**
 * The security level for basic protection (BP) requirements, safeguards etc.
 */
public enum SecurityLevel {

    BASIC, STANDARD, HIGH;

    private String label;

    private SecurityLevel() {
        label = Messages.getString(getClass().getSimpleName() + "." + this.name());
    }

    public String getLabel() {
        return label;
    }

    /**
     * Compare security levels null safe. null is defined to be the smallest
     * value.
     */
    public static int compare(SecurityLevel sl1, SecurityLevel sl2) {
        if (sl1 == sl2) {
            return 0;
        } else if (sl1 == null) {
            return -1;
        } else if (sl2 == null) {
            return 1;
        } else {
            return sl1.compareTo(sl2);
        }
    }
}
