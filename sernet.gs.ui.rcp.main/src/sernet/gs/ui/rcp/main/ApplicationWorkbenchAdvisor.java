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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * This workbench advisor creates the window advisor, and specifies
 * the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	
	private static final String PERSPECTIVE_ID = "sernet.gs.ui.rcp.main.perspective";

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	} 
	
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		configurer.setSaveAndRestore(true);
		activateProxyService();
	}
	
	/**
	 * Activate the proxy service by obtaining it.
	 * Copied from IDEWorkbenchAdvisor.
	 */
	private void activateProxyService() {
		Bundle bundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$
		Object proxyService = null;
		if (bundle != null) {
			ServiceReference ref = bundle.getBundleContext().getServiceReference(IProxyService.class.getName());
			if (ref != null){
				proxyService = bundle.getBundleContext().getService(ref);
			}
		}
		if (proxyService == null) {
			IDEWorkbenchPlugin.log("Proxy service could not be found."); //$NON-NLS-1$
		}
	}
	
	public void postStartup(){
	    removeUnneededPrefPages();
	}
	
	/**
	 * removes prefPages that were loaded from plugins but not needed
	 * currently thats:
	 *     -   org.eclipse.datatools.connectivity.ui.preferences.dataNode
	 *     -   org.eclipse.birt.report.designer.ui.preferences
	 *     -   org.eclipse.birt.chart.ui.swt.fieldassist.preferences.FieldAssistPreferencePage
	 */
	private void removeUnneededPrefPages(){
	    PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
	    // add id of prefPage to remove here
	    String[] prefPageIDsToRemove = new String[]{
	            "org.eclipse.datatools.connectivity.ui.preferences.dataNode",
	            "org.eclipse.birt.report.designer.ui.preferences",
	            "org.eclipse.birt.chart.ui.swt.fieldassist.preferences.FieldAssistPreferencePage"
	    };
	    Set<String> idSet = new HashSet<String>();
	    for(String s : prefPageIDsToRemove){
	        idSet.add(s);
	    }
	    for (IPreferenceNode node : pm.getRootSubNodes()){
	        if(idSet.contains(node.getId())){
	            // removing prefPages
	            pm.remove(node);
	        }
	    }
	}
}
