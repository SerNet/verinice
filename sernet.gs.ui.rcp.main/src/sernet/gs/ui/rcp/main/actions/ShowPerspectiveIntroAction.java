/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.gs.ui.rcp.main.actions;

import org.apache.log4j.Logger;

import java.util.Properties;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

import sernet.gs.ui.rcp.main.actions.ShowCheatSheetAction;

/**
 * Show perspecitve and open cheatsheet.
 * 
 * @author ak@sernet.de
 */
public abstract class ShowPerspectiveIntroAction implements IIntroAction {

    private static final Logger LOG = Logger.getLogger(ShowPerspectiveIntroAction.class);
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
     */
   
    @Override
    public void run(IIntroSite arg0, Properties arg1) {
        // Switch to perspective
        final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IPerspectiveDescriptor activePerspective = workbenchWindow.getActivePage().getPerspective();
        if(activePerspective==null || !activePerspective.getId().equals(getPerspectiveId())) {           
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    // switch perspective           
                    try {
                        workbenchWindow.getWorkbench().showPerspective(getPerspectiveId(),workbenchWindow);
                    } catch (WorkbenchException e) {
                        LOG.error("Can not switch to perspective: " + getPerspectiveId(), e);
                    }
                }
            });
        }
        
        // close intro/welcome page
        final IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro(); 
        PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
        
        // Show CheatSheet
        ShowCheatSheetAction action = new ShowCheatSheetAction("Show security assessment cheat sheet", getCheatSheetId());
        action.run();
    }

    /**
     * @return
     */
    public abstract String getCheatSheetId();
    public abstract String getPerspectiveId();

}
