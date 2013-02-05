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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.bsi.views.RelationView;
import sernet.verinice.interfaces.ActionRightIDs;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class AuditPerspective implements IPerspectiveFactory {

    public static final String ID = "sernet.verinice.samt.audit.rcp.AuditPerspective";
    
    private static final Map<String, String> viewRightIDs;
    
    static{
        viewRightIDs = new HashMap<String, String>();
        viewRightIDs.put(SimpleAuditView.ID, ActionRightIDs.SIMPLEAUDITVIEW);
        viewRightIDs.put(RelationView.ID, ActionRightIDs.RELATIONS);
        viewRightIDs.put(FileView.ID, ActionRightIDs.FILES);
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
        final float ratio70Percent = 0.7f;
        final float ratio25Percent = 0.25f;
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);
        layout.addView(SimpleAuditView.ID, IPageLayout.LEFT, ratio25Percent, editorArea);
        layout.addView(RelationView.ID, IPageLayout.BOTTOM, ratio70Percent, SimpleAuditView.ID);
        layout.addView(FileView.ID, IPageLayout.BOTTOM, ratio70Percent, editorArea);
    }

}
