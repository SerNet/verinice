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
package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;

public class ShowPreferencesAction extends Action {
	
	
	private String prefPage = null;
	private static final String ID = "sernet.gs.ui.rcp.showprefsaction"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public ShowPreferencesAction(String showPage) {
		super();
		setId(ID);
		setText(Messages.getString("ShowPreferencesAction.1")); //$NON-NLS-1$
		this.prefPage = showPage;
		//setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.TOOL));
	}
	
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
			if (nodes[i].getId().equals("org.eclipse.ui.preferencePages.Workbench")) {
				IPreferenceNode[] subNodes = nodes[i].getSubNodes();
				for (IPreferenceNode subNode : subNodes) {
					if (!subNode.getId().equals("org.eclipse.ui.net.NetPreferences"))
						nodes[i].remove(subNode.getId());
					else
						manager.addToRoot(subNode);
					manager.remove(nodes[i]);
				}
			}
			
			if (
					 nodes[i].getId().equals("org.eclipse.help.ui.browsersPreferencePage") //$NON-NLS-1$
					|| nodes[i].getId().equals("org.eclipse.help.ui.appserverPreferencePage") //$NON-NLS-1$
					) {
				manager.remove(nodes[i]);
			}
		}
		//|| nodes[i].getId().equals("org.eclipse.update.internal.ui.preferences.MainPreferencePage") //$NON-NLS-1$
		
			

		
		final PreferenceDialog dialog = new PreferenceDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), manager);
		
		if (prefPage != null)
			dialog.setSelectedNode(prefPage);

		BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), new Runnable() {
			public void run() {
				
				dialog.create();				
				dialog.open();
			}
		});	
		
	}
}
