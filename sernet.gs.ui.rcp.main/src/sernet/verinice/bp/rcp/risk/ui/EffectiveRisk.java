/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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
package sernet.verinice.bp.rcp.risk.ui;

import java.util.Objects;

/**
 * The effective risk for a threat, threat group or target object
 */
public final class EffectiveRisk {

    public static final EffectiveRisk UNKNOWN = new EffectiveRisk("_unknown"); //$NON-NLS-1$
    public static final EffectiveRisk TREATED = new EffectiveRisk("_treated"); //$NON-NLS-1$

    private final String riskId;

    private EffectiveRisk(String riskId) {
        this.riskId = Objects.requireNonNull(riskId);
    }

    public static EffectiveRisk of(String riskId) {
        if (riskId == null) {
            return null;
        }
        return new EffectiveRisk(riskId);
    }

    public String getRiskId() {
        return riskId;
    }

    @Override
    public String toString() {
        return "EffectiveRisk [riskId=" + riskId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((riskId == null) ? 0 : riskId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EffectiveRisk other = (EffectiveRisk) obj;
        if (riskId == null) {
            if (other.riskId != null)
                return false;
        } else if (!riskId.equals(other.riskId))
            return false;
        return true;
    }

}
