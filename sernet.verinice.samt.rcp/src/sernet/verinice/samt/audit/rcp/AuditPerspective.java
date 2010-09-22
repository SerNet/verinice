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
package sernet.verinice.samt.audit.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import sernet.gs.ui.rcp.main.bsi.views.BrowserView;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.bsi.views.RelationView;
import sernet.verinice.samt.audit.rcp.AssetView;
import sernet.verinice.samt.audit.rcp.ControlView;
import sernet.verinice.samt.audit.rcp.ElementView;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class AuditPerspective implements IPerspectiveFactory {

    public static final String ID = "sernet.verinice.samt.audit.rcp.AuditPerspective";
    
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
        layout.addView(SimpleAuditView.ID, IPageLayout.LEFT, 0.25f, editorArea);
        layout.addView(RelationView.ID, IPageLayout.BOTTOM, 0.7f, SimpleAuditView.ID);
        layout.addView(FileView.ID, IPageLayout.BOTTOM, 0.7f, editorArea);
        
        //layout.addView(OrganizationView.ID, IPageLayout.LEFT, 0.25f, editorArea);
        //layout.addView(AuditView.ID, IPageLayout.BOTTOM, 0.25f, OrganizationView.ID);
        //layout.addView(AssetView.ID, IPageLayout.BOTTOM, 0.33f, AuditView.ID);
        //layout.addView(ControlView.ID, IPageLayout.BOTTOM, 0.5f, AssetView.ID);
    }

}
