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
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;

public class OpenViewAction extends Action {

    private final IWorkbenchWindow window;
    private final String viewId;

    public OpenViewAction(IWorkbenchWindow window, String label, String viewId, String imageDesc) {
        this.window = window;
        this.viewId = viewId;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar

        setId("ACTION_" + viewId); //$NON-NLS-1$
        // Associate the action with a pre-defined command, to allow key
        // bindings.
        // TODO add command ids for each view opened using this action
        // setActionDefinitionId(ICommandIds.CMD_OPEN);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(imageDesc));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (window != null) {
            try {
                window.getActivePage().showView(viewId);
            } catch (PartInitException e) {
                ExceptionUtil.log(e, Messages.OpenMultipleViewAction_2);
            }
        }
    }
}
