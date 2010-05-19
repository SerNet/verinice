/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.model;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.gs.ui.rcp.main.common.model.TransactionAbortedException;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MaximumAssetValueListener implements ILinkChangeListener,
        Serializable {

    private CnATreeElement sbTarget;
    
    private static final Logger LOG = Logger.getLogger(MaximumAssetValueListener.class);

    public MaximumAssetValueListener(CnATreeElement item) {
        this.sbTarget = item;
    }

    public void determineIntegritaet(CascadingTransaction ta)
            throws TransactionAbortedException {
        if (hasBeenVisited(ta))
            return;
        ta.enter(sbTarget);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Determining integrity for " + sbTarget); //$NON-NLS-1$
        }
        

        // get protection level from upward links:
        int highestValue = 0;
        allLinks: for (CnALink link : sbTarget.getLinksUp()) {
            CnATreeElement upwardElmt = link.getDependant();
            if (upwardElmt.isSchutzbedarfProvider()) {
                // upwardElement might depend on maximum level itself, so
                // recurse up:
                upwardElmt.getLinkChangeListener().determineIntegritaet(ta);

                int value = upwardElmt.getSchutzbedarfProvider()
                        .getIntegritaet();
                if (value > highestValue)
                    highestValue = value;
            }
        }
        
        // if we dont use the maximum principle, keep current level:
        if (!sbTarget.getSchutzbedarfProvider().isCalculatedIntegrity()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Keeping current integrity " + sbTarget.getSchutzbedarfProvider().getIntegritaet() + " for " + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using maximum integrity " + highestValue + " for " + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sbTarget.getSchutzbedarfProvider().setIntegritaet(highestValue);
    }

    /**
     * @param ta
     * @return
     */
    private boolean hasBeenVisited(CascadingTransaction ta) {
        if (ta.hasBeenVisited(sbTarget)) {
            return true; // we have already been down this path
        }
        return false;
    }

    public void determineVerfuegbarkeit(CascadingTransaction ta)
            throws TransactionAbortedException {
        if (hasBeenVisited(ta))
            return;
        ta.enter(sbTarget);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Determining availability for " + sbTarget); //$NON-NLS-1$
        }

        // otherwise get protection level from upward links:
        int highestValue = 0;
        allLinks: for (CnALink link : sbTarget.getLinksUp()) {
            CnATreeElement upwardElmt = link.getDependant();
            if (upwardElmt.isSchutzbedarfProvider()) {

                // upwardElement might depend on maximum level itself, so
                // recurse up:
                upwardElmt.getLinkChangeListener().determineVerfuegbarkeit(ta);

                int value = upwardElmt.getSchutzbedarfProvider()
                        .getVerfuegbarkeit();
                if (value > highestValue)
                    highestValue = value;
            }
        }

        // if we dont use the maximum principle, keep current level:
        if (!sbTarget.getSchutzbedarfProvider().isCalculatedAvailability()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Keeping current availability " + sbTarget.getSchutzbedarfProvider().getVerfuegbarkeit() + " for " + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using maximum availability " + highestValue + " for " + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sbTarget.getSchutzbedarfProvider().setVerfuegbarkeit(highestValue);
    }

    public void determineVertraulichkeit(CascadingTransaction ta)
            throws TransactionAbortedException {

        if (hasBeenVisited(ta))
            return;
        ta.enter(sbTarget);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Determining confidentiality for " + sbTarget); //$NON-NLS-1$
        }

        // otherwise get protection level from upward links:
        int highestValue = 0;
        allLinks: for (CnALink link : sbTarget.getLinksUp()) {
            CnATreeElement upwardElmt = link.getDependant();
            if (upwardElmt.isSchutzbedarfProvider()) {

                // upwardElement might depend on maximum level itself, so
                // recurse up:
                upwardElmt.getLinkChangeListener().determineVertraulichkeit(ta);

                int value = upwardElmt.getSchutzbedarfProvider()
                        .getVertraulichkeit();
                if (value > highestValue)
                    highestValue = value;
            }
        }
        // if we dont use the maximum principle, keep current level:
        if (!sbTarget.getSchutzbedarfProvider().isCalculatedConfidentiality()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Keeping current confidentiality " + sbTarget.getSchutzbedarfProvider().getVertraulichkeit() + " for " + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
            }            
            return;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using maximum confidentiality " + highestValue + " for " + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sbTarget.getSchutzbedarfProvider().setVertraulichkeit(highestValue);

    }

}
