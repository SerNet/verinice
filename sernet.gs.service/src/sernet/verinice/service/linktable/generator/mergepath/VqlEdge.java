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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an edge between {@link VqlNode}.
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public final class VqlEdge {

    private static final String EDGE_TYPE_DELIMTER = ":";
    private static final String PATH_DELIMITER = "/";
    private static final String PROPERTY_DELIMITER = ".";
    private final EdgeType edgeType;
    private final String path;
    private Set<String> propertyTypes;
    private VqlNode source;
    private VqlNode target;

    /**
     * There is no LT-Type in the memory representation. From a technical point
     * of view the LINK-Type is the same. The only difference between them, LT
     * has a property.
     *
     * Every LT-Type is mapped to a an edge of type LINK with at least one
     * property.
     *
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     *
     */
    public enum EdgeType {
        LINK, PARENT, CHILD, PROP
    }

    VqlEdge(EdgeType edgeType, String path, VqlNode source, VqlNode target) {
        this.edgeType = edgeType;
        this.path = path;
        this.source = source;
        this.target = target;

        if (edgeType == EdgeType.LINK) {
            propertyTypes = new HashSet<>();
        }
    }

    /**
     * Add a property type to graph
     *
     * @param propertyType
     */
    public void addPropertyType(String propertyType) {
        propertyTypes.add(propertyType);
    }

    public Set<String> getPropertyTypes() {

        if (this.edgeType != EdgeType.LINK) {
            return Collections.emptySet();
        }

        return propertyTypes;
    }

    public String getPath() {
        return path;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public String getPathforProperty(String propertyType) {
        if (!isMatch()) {
            throw new IllegalStateException("VqlEdge contains no properties: " + this);
        }

        if (!propertyTypes.contains(propertyType)) {
            throw new IllegalStateException("VqlEdge does not contain this property type: " + propertyType);
        }

        // map the path back. In vql the pathes to an edge property always contains ":"
        int lastIndexOf  = path.lastIndexOf(PATH_DELIMITER);
        String originalPath = path.substring(0, lastIndexOf) + EDGE_TYPE_DELIMTER + path.substring(lastIndexOf + 1);
        return originalPath + PROPERTY_DELIMITER + propertyType;
    }

    /**
     * Returns true if this is a link and property types are found.
     */
    public boolean isMatch() {
        return EdgeType.LINK == edgeType && !propertyTypes.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((edgeType == null) ? 0 : edgeType.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        VqlEdge other = (VqlEdge) obj;
        if (edgeType != other.edgeType)
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "VqlEdge [edgeType=" + edgeType + ", path=" + path + ", propertyTypes="
                + propertyTypes + "]";
    }
}
