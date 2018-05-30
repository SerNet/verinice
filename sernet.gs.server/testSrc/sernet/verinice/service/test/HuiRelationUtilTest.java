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
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import sernet.hui.common.connect.DirectedHuiRelation;
import sernet.hui.common.connect.HuiRelationUtil;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.Process;

public class HuiRelationUtilTest extends ContextConfiguration {

    @Test
    public void getRelationsBetweenIsoPersonAndProcess() {
        Set<DirectedHuiRelation> allRelations = HuiRelationUtil
                .getAllRelationsBothDirections(PersonIso.TYPE_ID, Process.TYPE_ID);

        for (DirectedHuiRelation directedHuiRelation : allRelations) {
            if (directedHuiRelation.isForward()) {
                assertEquals(PersonIso.TYPE_ID, directedHuiRelation.getHuiRelation().getFrom());
                assertEquals(Process.TYPE_ID, directedHuiRelation.getHuiRelation().getTo());
            } else {
                assertEquals(Process.TYPE_ID, directedHuiRelation.getHuiRelation().getFrom());
                assertEquals(PersonIso.TYPE_ID, directedHuiRelation.getHuiRelation().getTo());
            }
        }

        assertThat(allRelations, JUnitMatchers
                .hasItem(new DirectedRelationMatcher("rel_person_process_responsible", true)));
        assertThat(allRelations, JUnitMatchers.hasItem(
                new DirectedRelationMatcher("rel_process_person_zugriffsberechtigt", false)));

    }

    @Test
    public void getRelationsFromAndToIsoPerson() {
        Set<DirectedHuiRelation> allRelations = HuiRelationUtil
                .getAllRelationsBothDirections(PersonIso.TYPE_ID);

        for (DirectedHuiRelation directedHuiRelation : allRelations) {
            if (directedHuiRelation.isForward()) {
                assertEquals(PersonIso.TYPE_ID, directedHuiRelation.getHuiRelation().getFrom());
            } else {
                assertEquals(PersonIso.TYPE_ID, directedHuiRelation.getHuiRelation().getTo());
            }
        }
        assertThat(allRelations, JUnitMatchers
                .hasItem(new DirectedRelationMatcher("rel_person_process_responsible", true)));
        assertThat(allRelations, JUnitMatchers.hasItem(
                new DirectedRelationMatcher("rel_process_person_zugriffsberechtigt", false)));
        assertThat(allRelations, JUnitMatchers
                .hasItem(new DirectedRelationMatcher("rel_person_vulnerability_rep", true)));
        assertThat(allRelations, JUnitMatchers
                .hasItem(new DirectedRelationMatcher("rel_org_personiso_vv_bsig", false)));
    }

    private static final class DirectedRelationMatcher
            extends TypeSafeMatcher<DirectedHuiRelation> {

        private String relationId;
        private boolean isForward;

        public DirectedRelationMatcher(String relationId, boolean isForward) {
            this.relationId = relationId;
            this.isForward = isForward;
        }

        @Override
        public boolean matchesSafely(DirectedHuiRelation directedRelation) {
            return directedRelation.isForward() == isForward
                    && directedRelation.getHuiRelation().getId().equals(relationId);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a " + (isForward ? "forward" : "backward")
                    + " relation with id " + relationId);
        }
    }
}
