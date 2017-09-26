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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.bp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.gs.ui.rcp.main.bsi.views.BrowserView;
import sernet.verinice.bp.rcp.BaseProtectionView;
import sernet.verinice.rcp.catalog.CatalogView;

/**
 * Modernized BSI GS-Perspektive
 * 
 * @author Urs Zeidler uz[at]sernet.de
 */
public class BaseProtectionPerspective implements IPerspectiveFactory {
	public static final String ID = "sernet.verinice.rcp.bp.BaseProtectionPerspective";
		
	private static final float RATIO_CONTROL_FOLDER = 0.3f;
	private static final float RATIO_MODEL_FOLDER = 0.4f;
	private static final float RATIO_DETAILS_FOLDER = 0.5f;

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		IFolderLayout controlFolder = layout.createFolder("control", IPageLayout.LEFT, RATIO_CONTROL_FOLDER, editorArea);
		controlFolder.addView(CatalogView.ID);
		controlFolder.addPlaceholder(CatalogView.ID + ":*");
		layout.getViewLayout(CatalogView.ID).setCloseable(true);
		
		IFolderLayout modelFolder = layout.createFolder("model", IPageLayout.LEFT, RATIO_MODEL_FOLDER, editorArea);
		modelFolder.addView(BaseProtectionView.ID);
		modelFolder.addPlaceholder(BaseProtectionView.ID + ":*");
		layout.getViewLayout(BSIMassnahmenView.ID).setCloseable(true);
		
		IFolderLayout folder = layout.createFolder("datails",IPageLayout.BOTTOM, RATIO_DETAILS_FOLDER, editorArea);
		folder.addView(BrowserView.ID);
		layout.getViewLayout(BSIMassnahmenView.ID).setCloseable(true);	
	}
}
