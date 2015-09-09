/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter <mr[at]sernet[dot]de>.
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
 *     Moritz Reiter <mr[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import sernet.gs.ui.rcp.main.bsi.views.BrowserView;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.bsi.views.RelationView;

/**
 * Creates an RCP perspective for working with a Greenbone Security Manager connection in Verinice.
 * 
 * @author Moritz Reiter
 *
 */
public final class GreenbonePerspective implements IPerspectiveFactory {
    
    public static final String ID = "sernet.verinice.iso27k.rcp.GreenbonePerspective";
    
    @Override
    public void createInitialLayout(IPageLayout layout) {
        
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);   
        
        final float leftToRightRatio = 0.33f;
        IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, leftToRightRatio, editorArea);
        topLeft.addView(ISMView.ID);
        
        final float bottomToTopRatio = 0.66f;
        IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, bottomToTopRatio, ISMView.ID);
        bottomLeft.addView(RelationView.ID);
                
        IFolderLayout bottomRight = layout.createFolder("bottom", IPageLayout.BOTTOM, bottomToTopRatio, editorArea);
        bottomRight.addView(BrowserView.ID);
        bottomRight.addView(FileView.ID);
    }
}
