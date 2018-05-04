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
package sernet.hui.common.connect;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper class to work with relations between elements
 */
public final class HuiRelationUtil {

    private static final HUITypeFactory huiTypeFactory = HitroUtil.getInstance().getTypeFactory();

    /**
     * Returns a set of all possible relations between two element types. The
     * set will be sorted by the relations' labels.
     */
    public static Set<DirectedHuiRelation> getAllRelationsBothDirections(String sourceEntityTypeId,
            String targetEntityTypeId) {

        Set<HuiRelation> forwardRelations = huiTypeFactory.getPossibleRelations(sourceEntityTypeId,
                targetEntityTypeId);
        Set<HuiRelation> backwardRelations;
        if (!sourceEntityTypeId.equals(targetEntityTypeId)) {
            backwardRelations = huiTypeFactory.getPossibleRelations(targetEntityTypeId,
                    sourceEntityTypeId);
        } else {
            backwardRelations = Collections.emptySet();
        }
        return collateRelations(forwardRelations, backwardRelations);
    }

    private static Set<DirectedHuiRelation> collateRelations(Set<HuiRelation> forwardRelations,
            Set<HuiRelation> backwardRelations) {

        Set<DirectedHuiRelation> collatedRelations = new TreeSet<>(
                getDirectedHuiRelationComparator());

        for (HuiRelation forwardRelation : forwardRelations) {
            collatedRelations
                    .add(DirectedHuiRelation.getDirectedHuiRelation(forwardRelation, true));
        }
        for (HuiRelation backwardRelation : backwardRelations) {
            collatedRelations
                    .add(DirectedHuiRelation.getDirectedHuiRelation(backwardRelation, false));
        }

        return collatedRelations;
    }

    private static Comparator<DirectedHuiRelation> getDirectedHuiRelationComparator() {
        return new SortByLabel();
    }

    private static final class SortByLabel implements Comparator<DirectedHuiRelation> {
        Collator collator = Collator.getInstance(Locale.getDefault());

        @Override
        public int compare(DirectedHuiRelation relation1, DirectedHuiRelation relation2) {
            return collator.compare(relation1.getLabel(), relation2.getLabel());
        }
    }

    private HuiRelationUtil() {

    }
}
