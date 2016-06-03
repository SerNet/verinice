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

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class VqlEdge implements PathElement {

    private final EdgeType edgeType;
    private final String path;
    private VqlNode source;
    private VqlNode target;

    enum EdgeType {
        LINK, PARENT, CHILD, PROP
    };

    VqlEdge(EdgeType edgeType, String path, VqlNode source, VqlNode target) {
        this.edgeType = edgeType;
        this.path = path;
        this.source = source;
        this.target = target;
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VqlEdge [edgeType=" + edgeType + ", path=" + path + ", source=" + source + ", target=" + target + "]";
    }


    public String getPath() {
        return path;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.linktable.mergevql.PathElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return "edge";
    }
}
