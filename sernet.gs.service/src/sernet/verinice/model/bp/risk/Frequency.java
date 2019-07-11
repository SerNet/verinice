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

public class Frequency implements Serializable, RiskPropertyValue {

    private static final long serialVersionUID = 6441596162125577738L;

    private static final String ID_PREFIX = "frequency";

    private final String id;

    private final String label;

    private final String description;

    public Frequency(String id, String label, String description) {
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
        return "Frequency [id=" + id + ", label=" + label + "]";
    }

    public boolean deepEquals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        Frequency other = (Frequency) obj;
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

    public static boolean deepEquals(List<Frequency> frequencies1, List<Frequency> frequencies2) {
        if (frequencies1 == null || frequencies2 == null) {
            return false;
        }
        if (frequencies1.size() != frequencies2.size()) {
            return false;
        }
        for (int i = 0; i < frequencies1.size(); i++) {
            if (!frequencies1.get(i).deepEquals(frequencies2.get(i))) {
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
        Frequency other = (Frequency) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Frequency withLabel(String newLabel) {
        return new Frequency(id, newLabel, description);
    }

    public Frequency withDescription(String newDescription) {
        return new Frequency(id, label, newDescription);
    }

}