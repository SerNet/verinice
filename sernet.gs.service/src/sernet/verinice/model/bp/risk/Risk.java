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

public class Risk implements Serializable, RiskPropertyValue {

    private static final long serialVersionUID = -6696978614506867808L;

    private static final String ID_PREFIX = "risk";

    private final String id;

    private final String label;

    private final String description;

    private final Color color;

    public Risk(String id, String label, String description, Color color) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.color = color;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Risk [id=" + id + ", label=" + label + "]";
    }

    public boolean deepEquals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        Risk other = (Risk) obj;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.deepEquals(other.color))
            return false;
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

    public static boolean deepEquals(List<Risk> risks1, List<Risk> risks2) {
        if (risks1 == null || risks2 == null) {
            return false;
        }
        if (risks1.size() != risks2.size()) {
            return false;
        }
        for (int i = 0; i < risks1.size(); i++) {
            if (!risks1.get(i).deepEquals(risks2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static final class Color implements Serializable {

        private static final long serialVersionUID = 4913363396710458223L;

        public final int red;
        public final int green;
        public final int blue;

        public Color(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public boolean deepEquals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Color other = (Color) obj;
            if (blue != other.blue)
                return false;
            if (green != other.green)
                return false;
            if (red != other.red)
                return false;
            return true;
        }
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
        Risk other = (Risk) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Risk withLabel(String newLabel) {
        return new Risk(id, newLabel, description, color);
    }

    public Risk withDescription(String newDescription) {
        return new Risk(id, label, newDescription, color);
    }

    public Risk withColor(Color newColor) {
        return new Risk(id, label, description, newColor);
    }

}