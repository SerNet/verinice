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
package sernet.verinice.samt.rcp;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

import sernet.gs.ui.rcp.main.actions.ShowCheatSheetAction;
import sernet.verinice.samt.audit.rcp.AuditPerspective;

/**
 * This action is called by a link an the intro/welcome page of verinice.
 * The action switches to the {@link AuditPerspective} (Samt=Self Assessment)
 * and closes the welcome page.
 * 
 * At the moment there is a static link to this action on the standard welcome 
 * page. This is an "illegal" reference from the verinice core to the Samt bundle.
 * 
 * You can fix this by implementing a extension of the welcome page. An expandable
 * welcome page defines an anchor for extensions in the welcome page of the core. External
 * bundle can add content at the place of this anchor.
 * This does not work for simple html welcome pages. Verinice core welcome page is a html page.
 * 
 * See: http://www.developer.com/java/ent/article.php/10933_3698021_1/Eclipse-Tip-Making-a-Good-First-Impression.htm
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class SamtIntroAction implements IIntroAction {

    private static final Logger LOG = Logger.getLogger(SamtIntroAction.class);
    
    private static final String CHEAT_SHEET_ID = "sernet.verinice.samt.rcp.cheatSheets.GettingStarted";
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
     */
   
    @Override
    public void run(IIntroSite arg0, Properties arg1) {
        // Switch to AuditPerspective
        final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IPerspectiveDescriptor activePerspective = workbenchWindow.getActivePage().getPerspective();
        if(activePerspective==null || !activePerspective.getId().equals(SamtPerspective.ID)) {           
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    // switch perspective           
                    try {
                        workbenchWindow.getWorkbench().showPerspective(SamtPerspective.ID,workbenchWindow);
                    } catch (WorkbenchException e) {
                        LOG.error("Can not switch to perspective: " + SamtPerspective.ID, e);
                    }
                }
            });
        }
        
        // close intro/welcome page
        final IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro(); 
        PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
        
        // Show CheatSheet
        ShowCheatSheetAction action = new ShowCheatSheetAction("Show self assessment cheat sheet",CHEAT_SHEET_ID);
        action.run();
    }

}
