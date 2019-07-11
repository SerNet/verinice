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

package sernet.verinice.model.bp.risk;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Impact implements Serializable, RiskPropertyValue {

    private static final long serialVersionUID = -1450873237653388576L;

    private static final String ID_PREFIX = "impact";

    private final String id;

    private final String label;

    private final String description;

    public Impact(String id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Impact [id=" + id + ", label=" + label + "]";
    }

    public boolean deepEquals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        Impact other = (Impact) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        return true;
    }

    public static boolean deepEquals(List<Impact> impacts1, List<Impact> impacts2) {
        if (impacts1 == null || impacts2 == null) {
            return false;
        }
        if (impacts1.size() != impacts2.size()) {
            return false;
        }
        for (int i = 0; i < impacts1.size(); i++) {
            if (!impacts1.get(i).deepEquals(impacts2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getPropertyKeyForIndex(int index) {
        return String.format("%s%02d", ID_PREFIX, index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Impact other = (Impact) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Impact withLabel(String newLabel) {
        return new Impact(id, newLabel, description);
    }

    public Impact withDescription(String newDescription) {
        return new Impact(id, label, newDescription);
    }

}