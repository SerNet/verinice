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

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;

import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.views.BrowserView;

public class Perspective implements IPerspectiveFactory {
	public static final String ID = "sernet.gs.ui.rcp.main.perspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		//layout.addStandaloneView(NavigationView.ID,  false, IPageLayout.LEFT, 0.25f, editorArea);
		layout.addView(BSIMassnahmenView.ID,  IPageLayout.LEFT, 0.3f, editorArea);
		
		layout.addView(BsiModelView.ID,  IPageLayout.LEFT, 0.4f, editorArea);
		
		IFolderLayout folder = layout.createFolder("messages", 
				IPageLayout.BOTTOM, 0.5f, editorArea);
		folder.addPlaceholder(BrowserView.ID + ":*");
		folder.addView(BrowserView.ID);
		
		layout.getViewLayout(BSIMassnahmenView.ID).setCloseable(true);
		layout.getViewLayout(BrowserView.ID).setCloseable(true);
	}
	
	
}
