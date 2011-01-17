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
package sernet.gs.ui.rcp.main;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ViewIntroAdapterPart;

import sernet.gs.ui.rcp.main.actions.ShowCheatSheetAction;
import sernet.gs.ui.rcp.main.bsi.views.OpenCataloguesJob;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.iso27k.rcp.Iso27kPerspective;

/**
 * Workbench Window advisor.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov
 *          2007) $ $LastChangedBy: koderman $
 * 
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1100, 768));
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
        IPreferenceStore apiStore = PlatformUI.getPreferenceStore();
        apiStore.setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
        configurer.setShowPerspectiveBar(true);
        // Set the preference toolbar to the left place
        // If other menus exists then this will be on the left of them
        apiStore.setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, "TOP_LEFT");
        apiStore.setValue(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_EXTRAS, Iso27kPerspective.ID + "," + Perspective.ID + ",sernet.verinice.samt.rcp.SamtPerspective" );
        apiStore.setValue(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_SIZE, 360);
    }

    @Override
    public void postWindowOpen() {
        if (Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.FIRSTSTART)) {
            Activator.getDefault().getPluginPreferences().setValue(PreferenceConstants.FIRSTSTART, false);

            MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.ApplicationWorkbenchWindowAdvisor_0, Messages.ApplicationWorkbenchWindowAdvisor_1);
        }

        showFirstSteps();
        preloadDBMapper();
    }

    private void preloadDBMapper() {
        WorkspaceJob job = new WorkspaceJob(Messages.ApplicationWorkbenchWindowAdvisor_2) {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();
                CnAElementHome.getInstance().preload(CnAWorkspace.getInstance().getConfDir());
                return Status.OK_STATUS;
            }
        };
        job.schedule();

    }

    private void showFirstSteps() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(new IPartListener() {

            public void partActivated(IWorkbenchPart part) {
            }

            public void partBroughtToTop(IWorkbenchPart part) {
            }

            public void partClosed(IWorkbenchPart part) {
                if (part instanceof ViewIntroAdapterPart) {
                    if (Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.FIRSTSTART)) {
                        Preferences prefs = Activator.getDefault().getPluginPreferences();
                        prefs.setValue(PreferenceConstants.FIRSTSTART, false);

                        ShowCheatSheetAction action = new ShowCheatSheetAction(Messages.ApplicationWorkbenchWindowAdvisor_3);
                        action.run();
                    }
                }
            }

            public void partDeactivated(IWorkbenchPart part) {
            }

            public void partOpened(IWorkbenchPart part) {
            }
        });

    }

    public void loadBsiCatalogues() {
        try {
            WorkspaceJob job = new OpenCataloguesJob(Messages.ApplicationWorkbenchWindowAdvisor_20);
            job.setUser(false);
            job.schedule();
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error(Messages.ApplicationWorkbenchWindowAdvisor_22, e);
        }
    }

}
