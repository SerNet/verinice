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

import sernet.verinice.model.bp.DefaultBpModelListener;
import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.iso27k.IISO27KModelListener;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class LinkRemover extends DefaultBpModelListener
        implements IBSIModelListener, IISO27KModelListener, IBpModelListener {

    private LinkMaker linkMaker;

    /**
     * @param linkMaker
     */
    public LinkRemover(LinkMaker linkMaker) {
        this.linkMaker = linkMaker;
    }

    /*
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#linkRemoved(sernet.verinice.
     * model.common.CnALink)
     */
    @Override
    public void linkRemoved(CnALink link) {
        // try to remove links in both directions:
        linkMaker.getInputElmt().removeLinkDown(link);
        linkMaker.getInputElmt().removeLinkUp(link);
    }

    /*
     * @see sernet.verinice.model.bsi.IBSIModelListener#modelRefresh()
     */
    @Override
    public void modelRefresh() {

    }

    /*
     * @see
     * sernet.verinice.model.bsi.IBSIModelListener#modelReload(sernet.verinice.
     * model.bsi.BSIModel)
     */
    @Override
    public void modelReload(BSIModel newModel) {

    }
}
