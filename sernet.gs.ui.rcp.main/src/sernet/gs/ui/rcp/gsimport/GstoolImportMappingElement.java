/*******************************************************************************
 * Copyright (c) 2015 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.gsimport;

/**
 * Wrapper for elements used by {@link GsToolImportMappingView}. Constructed for support
 * of selecting edited and added elements in view.
 *
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class GstoolImportMappingElement implements Comparable<GstoolImportMappingElement> {

    public static final String UNKNOWN = "UNKNOWN";

    private String key;
    private String value;

    public GstoolImportMappingElement(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(GstoolImportMappingElement o) {

        int rc = 0;
        if (o == null) {
            rc = -1;
        } else {
            rc = this.key.compareToIgnoreCase(o.getKey());
        }
        return rc;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GstoolImportMappingElement other = (GstoolImportMappingElement) obj;

        if (key == null) {
            if (other.getKey() != null) {
                return false;
            }
        } else if (key.compareTo(other.getKey()) != 0) {
            return false;
        }

        if (value == null) {
            if (other.getValue() != null) {
                return false;
            }
        } else if (value.compareTo(other.getValue()) != 0) {
            return false;
        }
        return true;

    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

}
