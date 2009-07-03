/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ViewIntroAdapterPart;

import sernet.gs.ui.rcp.main.actions.ShowCheatSheetAction;
import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.bsi.views.OpenCataloguesJob;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * Workbench Window advisor.
 * 
 * @author koderman@sernet.de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov 2007) $ 
 * $LastChangedBy: koderman $
 *
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1000, 700));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
	}

	@Override
	public void postWindowOpen() {
		if (Activator.getDefault().getPluginPreferences()
				.getBoolean(PreferenceConstants.FIRSTSTART)) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Hinweis zum Datenschutz", 
			"Langfassung:\nVerinice sucht bei jedem Start automatisch nach Updates und baut dafür eine einfache " +
			"HTTP-Verbindung zum " +
			"Webserver 'updates.verinice.org' auf. Falls dort Updates vorhanden sind, " +
			"werden Sie gefragt, ob Sie diese installieren möchten.\n" +
			"Die automatischen Updates können Sie in den Einstellungen deaktivieren, wir empfehlen jedoch, " +
			"diese aktiviert zu lassen, damit Sie von Funktions- und Sicherheitsupdates " +
			"profitieren.\n" +
			"Verinice übermittelt keinerlei Daten über Ihren Rechner oder Ihre Konfiguration an den Update-Server " +
			"und installiert nichts ohne Ihre ausdrückliche Zustimmung!\n\n" +
			"Kurzfassung: \nVerinice telefoniert nicht nach Hause und installiert keinen Bundestrojaner. ;-)");
		}
		
		loadBsiCatalogues();
		showFirstSteps();
		preloadDBMapper();
	}

	private void preloadDBMapper() {
		WorkspaceJob job = new WorkspaceJob("Preloading Hibernate OR-Mapper...") {
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				CnAElementHome.getInstance().preload(CnAWorkspace.getInstance().getConfDir());
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}

	
	private void showFirstSteps() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.addPartListener(new IPartListener() {

					public void partActivated(IWorkbenchPart part) {
					}

					public void partBroughtToTop(IWorkbenchPart part) {
					}

					public void partClosed(IWorkbenchPart part) {
						if (part instanceof ViewIntroAdapterPart)
							if (Activator.getDefault().getPluginPreferences()
									.getBoolean(PreferenceConstants.FIRSTSTART)) {
								ShowCheatSheetAction action = new ShowCheatSheetAction(true, "Erste Schritte");
								action.run();
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
			WorkspaceJob job = new OpenCataloguesJob(
					Messages.BSIMassnahmenView_0);
			job.setUser(false);
			job.schedule();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					Messages.BSIMassnahmenView_2, e);
		}
	}

}
