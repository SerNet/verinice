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
package sernet.gs.ui.rcp.main.bsi.editors;

import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.validation.CnAValidation;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LinkRemover implements IBSIModelListener, IISO27KModelListener {

    private LinkMaker linkMaker;

    /**
     * @param linkMaker
     */
    public LinkRemover(LinkMaker linkMaker) {
        this.linkMaker = linkMaker;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#childAdded(sernet.verinice.model.common.CnATreeElement, sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void childAdded(CnATreeElement category, CnATreeElement child) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#childChanged(sernet.verinice.model.common.CnATreeElement, sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void childChanged(CnATreeElement child) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#childRemoved(sernet.verinice.model.common.CnATreeElement, sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void childRemoved(CnATreeElement category, CnATreeElement child) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#databaseChildAdded(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void databaseChildAdded(CnATreeElement child) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#databaseChildChanged(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void databaseChildChanged(CnATreeElement child) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#databaseChildRemoved(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void databaseChildRemoved(CnATreeElement child) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#databaseChildRemoved(sernet.verinice.model.common.ChangeLogEntry)
     */
    @Override
    public void databaseChildRemoved(ChangeLogEntry entry) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#linkAdded(sernet.verinice.model.common.CnALink)
     */
    @Override
    public void linkAdded(CnALink link) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#linkChanged(sernet.verinice.model.common.CnALink, sernet.verinice.model.common.CnALink, java.lang.Object)
     */
    @Override
    public void linkChanged(CnALink oldLink, CnALink newLink, Object source) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#linkRemoved(sernet.verinice.model.common.CnALink)
     */
    @Override
    public void linkRemoved(CnALink link) {
        // try to remove links in both directions:
        linkMaker.getInputElmt().removeLinkDown(link);
        linkMaker.getInputElmt().removeLinkUp(link);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#modelRefresh()
     */
    @Override
    public void modelRefresh() {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#modelRefresh(java.lang.Object)
     */
    @Override
    public void modelRefresh(Object source) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#modelReload(sernet.verinice.model.bsi.BSIModel)
     */
    @Override
    public void modelReload(BSIModel newModel) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISO27KModelListener#modelReload(sernet.verinice.model.iso27k.ISO27KModel)
     */
    @Override
    public void modelReload(ISO27KModel newModel) {
        
    }
    
    @Override
    public void validationAdded(Integer scopeId){};
    
    @Override
    public void validationRemoved(Integer scopeId){};
    
    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation){};

}


