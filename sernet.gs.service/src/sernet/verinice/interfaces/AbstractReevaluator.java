/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * Convenient class to implement a {@link IReevaluator} to spare the override of
 * methods.
 *
 * @author Urs Zeidler uz[at]sernet.de
 *
 */
public abstract class AbstractReevaluator implements IReevaluator {
    private static final Logger LOG = Logger.getLogger(AbstractReevaluator.class);

    /**
     * Collects all downwards linked {@link CnATreeElement} which don't have any
     * further links in the given bottomNodes Set.
     */
    protected void findBottomNodes(CnATreeElement downwardElement, Set<CnATreeElement> bottomNodes, CascadingTransaction downwardsTA) {
        if (downwardsTA.hasBeenVisited(downwardElement)) {
            return;
        }

        try {
            downwardsTA.enter(downwardElement);
        } catch (TransactionAbortedException e) {
            LOG.error("Aborted while determining bottom node for object: " + downwardElement.getTitle(), e); //$NON-NLS-1$
            return;
        }

        int countLinks = 0;
        for (CnALink link : downwardElement.getLinksDown()) {
            if (link.getDependency().isProtectionRequirementsProvider()) {
                countLinks++;
                findBottomNodes(link.getDependency(), bottomNodes, downwardsTA);
            }
        }

        // could not go further down, so add this node:
        if (countLinks == 0){
            bottomNodes.add(downwardElement);
        }
    }

     @Override
    public void updateValue(CascadingTransaction ta) {
    }

     @Override
    public void setValue(CascadingTransaction ta, String properyName, Object value) {
    }

    @Override
    public int getConfidentiality() {
        return 0;
    }

    @Override
    public int getAvailability() {
        return 0;
    }

    @Override
    public int getIntegrity() {
        return 0;
    }

    @Override
    public void setConfidentiality(int i) {
    }

    @Override
    public void setIntegrity(int i) {
    }

    @Override
    public void setAvailability(int i) {
    }

    @Override
    public String getConfidentialityDescription() {
        return null;
    }

    @Override
    public String getIntegrityDescription() {
        return null;
    }

    @Override
    public String getAvailabilityDescription() {
        return null;
    }

    @Override
    public boolean isCalculatedConfidentiality() {
        return false;
    }

    @Override
    public boolean isCalculatedIntegrity() {
        return false;
    }

    @Override
    public boolean isCalculatedAvailability() {
        return false;
    }

    @Override
    public void setConfidentialityDescription(String text) {
    }

    @Override
    public void setIntegrityDescription(String text) {
    }

    @Override
    public void setAvailabilityDescription(String text) {
    }

    @Override
    public void updateConfidentiality(CascadingTransaction ta) {
    }

    @Override
    public void updateIntegrity(CascadingTransaction ta) {
    }

    @Override
    public void updateAvailability(CascadingTransaction ta) {
    }

}
