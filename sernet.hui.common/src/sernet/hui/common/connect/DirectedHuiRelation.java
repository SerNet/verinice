/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter.
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
 *     Moritz Reiter - initial API and implementation
 ******************************************************************************/

package sernet.hui.common.connect;

/**
 * This is a wrapper class for a HuiRelation that adds a direction to the
 * relation. It can be 'forward', meaning from 'source' to 'target', or
 * 'backward', meaning from 'target' to 'source'.
 * 
 * It is important to understand, that this information is relative to the
 * context. A HuiRelation is always a property of a HUI entity X. This implies,
 * that then X is the source of the relation, and Y is the target. Looking at
 * the relation from the perspective of X, means that the relation is forward,
 * looking at it from the perspective of Y means that it is backward.
 * 
 * @author Moritz Reiter
 */
public final class DirectedHuiRelation {

    private final HuiRelation huiRelation;
    private final boolean isForward;

    private DirectedHuiRelation(HuiRelation huiRelation, boolean isForward) {

        this.huiRelation = huiRelation;
        this.isForward = isForward;
    }

    /**
     * A static factory method that returns an instance of a directed
     * HuiRelation.
     *
     * @param huiRelation
     *            the undirected HuiRelation
     * @param isForward
     *            true, if the HuiRelation is "forward" according to the context
     *            of invocation
     * @return the HuiRelation from the parameter, but with an added direction
     */
    public static DirectedHuiRelation getDirectedHuiRelation(HuiRelation huiRelation,
            boolean isForward) {

        return new DirectedHuiRelation(huiRelation, isForward);
    }

    /**
     * @return the label, depending on the direction of the relation
     */
    public String getLabel() {

        if (this.isForward()) {
            return this.getHuiRelation().getName();
        } else {
            return this.getHuiRelation().getReversename();
        }
    }

    public HuiRelation getHuiRelation() {

        return this.huiRelation;
    }

    public boolean isForward() {

        return this.isForward;
    }

    @Override
    public String toString() {
        return "DirectedHuiRelation [huiRelation=" + huiRelation + ", isForward=" + isForward + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((huiRelation == null) ? 0 : huiRelation.hashCode());
        result = prime * result + (isForward ? 1231 : 1237);
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
        DirectedHuiRelation other = (DirectedHuiRelation) obj;
        if (huiRelation == null) {
            if (other.huiRelation != null)
                return false;
        } else if (!huiRelation.equals(other.huiRelation))
            return false;
        if (isForward != other.isForward)
            return false;
        return true;
    }

}
