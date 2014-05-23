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
package sernet.verinice.model.iso27k;

import java.io.Serializable;

import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MaximumAssetValueListener implements ILinkChangeListener, Serializable {

    private static final InheritLogger LOG_INHERIT = InheritLogger.getLogger(MaximumAssetValueListener.class);
    
    private CnATreeElement sbTarget;
    
    private static final String STRING_CONNECTOR_FOR = " for ";
     
    public MaximumAssetValueListener(CnATreeElement item) {
        this.sbTarget = item;
    }

    public void determineIntegritaet(CascadingTransaction ta) throws TransactionAbortedException {
        if (hasBeenVisited(ta)) {
            return;
        }
        
        ta.enter(sbTarget); 
        
        if (LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("Determining integrity for " + sbTarget.getTitle()); //$NON-NLS-1$
        }
        
        // get protection level from upward links:
        int highestValue = 0;
        for (CnALink link : sbTarget.getLinksUp()) {
            CnATreeElement upwardElmt = link.getDependant();
            if (upwardElmt.isSchutzbedarfProvider()) {
                // upwardElement might depend on maximum level itself, so
                // recurse up:
                upwardElmt.getLinkChangeListener().determineIntegritaet(ta);

                int value = upwardElmt.getSchutzbedarfProvider().getIntegritaet();
                if (value > highestValue) {
                    highestValue = value;
                }
            }
        }
        
        // if we dont use the maximum principle, keep current level:
        if (!sbTarget.getSchutzbedarfProvider().isCalculatedIntegrity()) {
            if (LOG_INHERIT.isInfo()) {
                LOG_INHERIT.info("Integrity is set manually: " + sbTarget.getSchutzbedarfProvider().getIntegritaet() + STRING_CONNECTOR_FOR + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return;
        }
        
        if (LOG_INHERIT.isInfo()) {
            LOG_INHERIT.info("Setting maximum integrity " + highestValue + STRING_CONNECTOR_FOR + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sbTarget.getSchutzbedarfProvider().setIntegritaet(highestValue);
    }

    public void determineVerfuegbarkeit(CascadingTransaction ta) throws TransactionAbortedException {
        if (hasBeenVisited(ta)) {
            return;
        }
        
        ta.enter(sbTarget);
        
        if (LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("Determining availability for " + sbTarget.getTitle()); //$NON-NLS-1$
        }

        // otherwise get protection level from upward links:
        int highestValue = 0;
        for (CnALink link : sbTarget.getLinksUp()) {
            CnATreeElement upwardElmt = link.getDependant();
            if (upwardElmt.isSchutzbedarfProvider()) {

                // upwardElement might depend on maximum level itself, so
                // recurse up:
                upwardElmt.getLinkChangeListener().determineVerfuegbarkeit(ta);

                int value = upwardElmt.getSchutzbedarfProvider().getVerfuegbarkeit();
                if (value > highestValue) {
                    highestValue = value;
                }
            }
        }

        // if we dont use the maximum principle, keep current level:
        if (!sbTarget.getSchutzbedarfProvider().isCalculatedAvailability()) {
            if (LOG_INHERIT.isInfo()) {
                LOG_INHERIT.info("Availability is set manually: " + sbTarget.getSchutzbedarfProvider().getIntegritaet() + STRING_CONNECTOR_FOR + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return;
        }
        
        if (LOG_INHERIT.isInfo()) {
            LOG_INHERIT.info("Setting maximum availability " + highestValue + STRING_CONNECTOR_FOR + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sbTarget.getSchutzbedarfProvider().setVerfuegbarkeit(highestValue);
    }

    public void determineVertraulichkeit(CascadingTransaction ta)
            throws TransactionAbortedException {

        if (hasBeenVisited(ta)){
            return;
        }
        ta.enter(sbTarget);
        
        if (LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("Determining confidentiality for " + sbTarget.getTitle()); //$NON-NLS-1$
        }

        // otherwise get protection level from upward links:
        int highestValue = 0;
        for (CnALink link : sbTarget.getLinksUp()) {
            CnATreeElement upwardElmt = link.getDependant();
            if (upwardElmt.isSchutzbedarfProvider()) {

                // upwardElement might depend on maximum level itself, so
                // recurse up:
                upwardElmt.getLinkChangeListener().determineVertraulichkeit(ta);

                int value = upwardElmt.getSchutzbedarfProvider().getVertraulichkeit();
                if (value > highestValue) {
                    highestValue = value;
                }
            }
        }
        // if we dont use the maximum principle, keep current level:
        if (!sbTarget.getSchutzbedarfProvider().isCalculatedConfidentiality()) {   
            if (LOG_INHERIT.isInfo()) {
                LOG_INHERIT.info("Confidentiality is set manually: " + sbTarget.getSchutzbedarfProvider().getIntegritaet() + STRING_CONNECTOR_FOR + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return;
        }
        
        if (LOG_INHERIT.isInfo()) {
            LOG_INHERIT.info("Setting maximum confidentiality " + highestValue + STRING_CONNECTOR_FOR + sbTarget.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sbTarget.getSchutzbedarfProvider().setVertraulichkeit(highestValue);

    }
    
    /**
     * @param ta
     * @return
     */
    private boolean hasBeenVisited(CascadingTransaction ta) {
        if (ta.hasBeenVisited(sbTarget)) {
            if (LOG_INHERIT.isDebug()) {
                LOG_INHERIT.debug(sbTarget.getTitle() + " has benn visited"); //$NON-NLS-1$
            }
            return true; // we have already been down this path
        }
        return false;
    }

}
