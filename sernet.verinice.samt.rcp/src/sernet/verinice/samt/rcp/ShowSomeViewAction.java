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
package sernet.verinice.samt.rcp;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import sernet.gs.ui.rcp.main.ExceptionUtil;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public abstract class ShowSomeViewAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    protected abstract String getViewId();

    /**
     * 
     */
    public ShowSomeViewAction() {
        super();
    }

    @Override
    public void dispose() {
        this.window = null;
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    
    }

    @Override
    public void run(IAction action) {
        if (window != null) {
            try {
                window.getActivePage().showView(getViewId());
            } catch (PartInitException e) {
                ExceptionUtil.log(e, "Could not open view.");
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

}
