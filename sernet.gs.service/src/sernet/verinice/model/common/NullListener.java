/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.common;

import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.validation.CnAValidation;

/**
 * Default listener used inside model. Does not do anything.
 *
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class NullListener implements IBSIModelListener {

    @Override
    public void childAdded(CnATreeElement category, CnATreeElement child) {
        // do nothing
    }

    @Override
    public void childChanged(CnATreeElement child) {
        // do nothing

    }

    @Override
    public void childRemoved(CnATreeElement category, CnATreeElement child) {
        // do nothing

    }

    @Override
    public void linkChanged(CnALink old, CnALink link, Object source) {
        // do nothing

    }

    /**
     * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet
     *             werden
     */
    @Override
    public void modelRefresh() {
        modelRefresh(null);
    }

    @Override
    public void modelRefresh(Object source) {
        // do nothing
    }

    @Override
    public void linkRemoved(CnALink link) {
        // do nothing

    }

    @Override
    public void linkAdded(CnALink link) {
        // do nothing
    }

    @Override
    public void databaseChildAdded(CnATreeElement child) {
        // do nothing
    }

    @Override
    public void databaseChildChanged(CnATreeElement child) {
        // do nothing
    }

    @Override
    public void databaseChildRemoved(CnATreeElement child) {
        // do nothing

    }

    @Override
    public void modelReload(BSIModel newModel) {
        // do nothing

    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildRemoved(
     * sernet.gs.ui.rcp.main.common.model.ChangeLogEntry)
     */
    @Override
    public void databaseChildRemoved(ChangeLogEntry entry) {
        // do nothing
    }

    @Override
    public void validationAdded(Integer scopeId) {
        // do nothing
    }

    @Override
    public void validationRemoved(Integer scopeId) {
        // do nothing
    }

    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation) {
        // do nothing
    }
}
