package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.PreferencesPageContainer;

import sernet.gs.ui.rcp.main.Application;
import sernet.gs.ui.rcp.main.ImageCache;

public class ShowPreferencesAction extends Action {
	
	
	private static final String ID = "sernet.gs.ui.rcp.showprefsaction"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public ShowPreferencesAction() {
		
		super();
		setId(ID);
		setText(Messages.getString("ShowPreferencesAction.1")); //$NON-NLS-1$
		//setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.TOOL));
	}

	public void run() {

		PreferenceManager manager = 
			PlatformUI.getWorkbench().getPreferenceManager();
		
		IPreferenceNode[] nodes = manager.getRootSubNodes();
		for (int i=0; i < nodes.length; ++i) {
			if (nodes[i].getId().equals("org.eclipse.ui.preferencePages.Workbench") //$NON-NLS-1$
					|| nodes[i].getId().equals("org.eclipse.update.internal.ui.preferences.MainPreferencePage") //$NON-NLS-1$
					|| nodes[i].getId().equals("org.eclipse.help.ui.browsersPreferencePage") //$NON-NLS-1$
					|| nodes[i].getId().equals("org.eclipse.help.ui.appserverPreferencePage") //$NON-NLS-1$
					) {
				manager.remove(nodes[i]);
			}
		}
		
			

		
		final PreferenceDialog dialog = new PreferenceDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), manager);

		BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), new Runnable() {
			public void run() {
				
				dialog.create();				
				dialog.open();
			}
		});	
		
	}
}
