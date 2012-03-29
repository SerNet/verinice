/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.rcp;

import java.util.HashMap;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import sernet.gs.ui.rcp.main.bsi.views.BrowserView;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.bsi.views.NoteView;
import sernet.gs.ui.rcp.main.bsi.views.RelationView;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.CatalogView;
import sernet.verinice.iso27k.rcp.ISMView;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class SamtPerspective implements IPerspectiveFactory {

    public static final String ID = "sernet.verinice.samt.rcp.SamtPerspective";
    
    public static HashMap<String, String> viewsRightIDs;
    
    static{
        viewsRightIDs = new HashMap<String, String>();
        viewsRightIDs.put(SamtView.ID, ActionRightIDs.SAMTVIEW);
        viewsRightIDs.put(BrowserView.ID, ActionRightIDs.BSIBROWSER);
        viewsRightIDs.put(SpiderChartView.ID, ActionRightIDs.SHOWCHARTVIEW);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui
     * .IPageLayout)
     */
    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);
        layout.addView(SamtView.ID, IPageLayout.LEFT, 0.3f, editorArea);
        layout.addView(BrowserView.ID, IPageLayout.TOP, 0.6f, editorArea);
        layout.addView(SpiderChartView.ID, IPageLayout.BOTTOM, 0.6f, SamtView.ID);
    }

}
