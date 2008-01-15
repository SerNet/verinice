package sernet.gs.ui.rcp.main;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Point;
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
