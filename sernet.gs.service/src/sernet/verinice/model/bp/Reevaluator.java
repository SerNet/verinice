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
package sernet.verinice.model.bp;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.AbstractReevaluator;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * Request linked bottom nodes to redetermine values. @see
 * ILinkChangeListener#determineValue(CascadingTransaction)
 *
 * @author Urs Zeidler uz[at]sernet.de
 *
 */
public class Reevaluator extends AbstractReevaluator implements Serializable {

    private static final long serialVersionUID = 1396555841881922883L;

    private static final Logger LOG = Logger.getLogger(Reevaluator.class);

    private CnATreeElement cnaTreeElement;

    public Reevaluator(CnATreeElement cnaTreeElement) {
        super();
        this.cnaTreeElement = cnaTreeElement;
    }

    @Override
    public void updateValue(CascadingTransaction ta) {
        try {
            // 1st step: traverse down:
            // find bottom nodes from which to start:
            CascadingTransaction downwardsTA = new CascadingTransaction();
            Set<CnATreeElement> bottomNodes = new HashSet<>();
            findBottomNodes(cnaTreeElement, bottomNodes, downwardsTA);

            // 2nd step: traverse up:
            for (CnATreeElement bottomNode : bottomNodes) {
                bottomNode.getLinkChangeListener().determineValue(ta);
            }
        } catch (TransactionAbortedException tae) {
            LOG.debug("Value evaluation aborted."); //$NON-NLS-1$
            throw new RuntimeException(tae);
        } catch (RuntimeException e) {
            ta.abort();
            throw e;
        } catch (java.lang.Exception e) {
            ta.abort();
            throw new RuntimeException(e);
        }
    }
}
