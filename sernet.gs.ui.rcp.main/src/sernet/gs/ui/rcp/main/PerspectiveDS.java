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

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import sernet.gs.ui.rcp.main.bsi.views.DSModelView;

public class PerspectiveDS implements IPerspectiveFactory {
	public static final String ID = "sernet.gs.ui.rcp.main.dsperspective";
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		//layout.addStandaloneView(NavigationView.ID,  false, IPageLayout.LEFT, 0.25f, editorArea);
		layout.addView(DSModelView.ID,  IPageLayout.LEFT, 0.25f, editorArea);
		
		layout.getViewLayout(DSModelView.ID).setCloseable(true);
		layout.addPerspectiveShortcut(ID);

//		PlatformUI.getWorkbench().showPerspective(YOUR_PERSPECTIVE_ID, 
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow());


	}
	
	
}
