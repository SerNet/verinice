/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.qm;

import org.apache.log4j.Logger;

import sernet.gs.service.ServerInitializer;
import sernet.verinice.bpm.ProzessExecution;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaQm extends ProzessExecution {

    private static final Logger LOG = Logger.getLogger(IsaQm.class);
    
    public String loadFeedback(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        String comment = null;
        try {
            CnATreeElement element = loadElementByUuid(uuid);  
            if(element instanceof Control) {
                comment = ((Control)element).getFeedbackNote();
                if(comment!=null && comment.trim().isEmpty()) {
                    comment = null;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Feedback note of control: " + element.getUuid() + ": " + comment); //$NON-NLS-1$
                }
            }
        } catch(Exception t) {
            LOG.error("Error while loading comment.", t); //$NON-NLS-1$
        }
        return comment;
    }
      
}
