/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
package sernet.verinice.service.linktable.generator.mergepath;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a node of column path within a {@link VqlAst}.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class VqlNode {

    private final String text;

    private final String path;

    private final Map<String, String> propertyType2Alias = new HashMap<>();

    public VqlNode(String text, String path) {
        this.text = text;
        this.path = path;
    }

    public VqlNode(String path) {
        this("matching node", path);
    }

    public void addPropertyType(String propertyType) {
        propertyType2Alias.put(propertyType, StringUtils.EMPTY);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VqlNode other = (VqlNode) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return true;
    }

    public String getPath() {
        return new String(path);
    }

    @Override
    public String toString() {
        return "VqlNode [text=" + text + ", path=" + path + ", properties=" + propertyType2Alias + "]";
    }

    public String getText() {
        return new String(text);
    }

    public boolean isMatch() {
        return !this.propertyType2Alias.keySet().isEmpty();
    }

    public String getPathForProperty(String propertyType) {

        if (!isMatch()) {
            throw new IllegalStateException("VqlNode contains no properties: " + this);
        }

        if (!propertyType2Alias.containsKey(propertyType)) {
            throw new IllegalStateException("VqlNode does not contain this property type: " + propertyType);
        }

        return path + "." + propertyType;
    }

    public Set<String> getPropertyTypes() {
        return propertyType2Alias.keySet();
    }

    /**
     * Set an alias for the column path. Can be used as column header.
     */
    public void setAlias(String propertyType, String alias) {
        propertyType2Alias.put(propertyType, alias);

    }

    /**
     * Returns an alias for the column path. If no alias is set the column path
     * is returned.
     *
     */
    public String getAlias(String propertyType) {
        return propertyType2Alias.get(propertyType);
    }
}
