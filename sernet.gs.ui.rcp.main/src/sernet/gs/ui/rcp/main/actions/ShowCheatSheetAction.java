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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;

import sernet.gs.ui.rcp.main.ExceptionUtil;

public class ShowCheatSheetAction extends Action {

    private boolean tutorial;
    private static final String ID1 = "sernet.gs.ui.rcp.main.showcheatsheetaction"; //$NON-NLS-1$
    private static final String ID2 = "sernet.gs.ui.rcp.main.showcheatsheetlistaction"; //$NON-NLS-1$

    public ShowCheatSheetAction(boolean tutorial, String title) {
        setText(title);
        if (tutorial) {
            setId(ID1);
        } else {
            setId(ID2);
        }
        this.tutorial = tutorial;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("org.eclipse.ui.cheatsheets.views.CheatSheetView"); //$NON-NLS-1$
                IViewPart part = window.getActivePage().findView("org.eclipse.ui.cheatsheets.views.CheatSheetView"); //$NON-NLS-1$
                if (part != null && tutorial) {
                    CheatSheetView view = (CheatSheetView) part;
                    view.setInput("sernet.gs.ui.rcp.main.cheatsheet1"); //$NON-NLS-1$
                }
            } catch (PartInitException e) {
                ExceptionUtil.log(e, Messages.ShowCheatSheetAction_5);
            }
        }
    }
}
