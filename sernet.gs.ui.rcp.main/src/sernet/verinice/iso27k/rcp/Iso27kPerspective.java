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
package sernet.verinice.iso27k.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.bsi.views.NoteView;
import sernet.gs.ui.rcp.main.bsi.views.RelationView;

public class Iso27kPerspective implements IPerspectiveFactory {
	public static final String ID = "sernet.verinice.iso27k.rcp.Iso27kPerspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		layout.addView(CatalogView.ID,  IPageLayout.LEFT, 0.20f, editorArea);
		
		layout.addView(ISMView.ID,  IPageLayout.LEFT, 0.35f, editorArea);
		layout.addView(RelationView.ID,  IPageLayout.BOTTOM, 0.7f, ISMView.ID);
		
		IFolderLayout folder = layout.createFolder("information", IPageLayout.BOTTOM, 0.75f, editorArea);
		folder.addView(FileView.ID);
		folder.addPlaceholder(NoteView.ID + ":*");
		
		layout.getViewLayout(CatalogView.ID).setCloseable(true);
		layout.getViewLayout(ISMView.ID).setCloseable(true);
	}
	
	
}
