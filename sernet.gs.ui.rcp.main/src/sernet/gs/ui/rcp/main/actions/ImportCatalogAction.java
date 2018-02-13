/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 * Sebastian Hagedorn - initial API and implementation
 * Daniel Murygin - Implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.bsi.dialogs.XMLImportDialog;
import sernet.verinice.interfaces.ActionRightIDs;

/**
 * This action opens the import dialog for VNA files as catalogs.
 * This action is executed when the import button is pressed in the tool bar of
 * the Catalog View. The action is configured in the plugin.xml file.
 * 
 * @author Sebastian Hagedorn
 * @author Daniel Murygin
 */
public class ImportCatalogAction extends ImportXMLAction {
    
    public static final String ID = "sernet.gs.ui.rcp.main.importcatalogaction";

    public void run() {
        final XMLImportDialog dialog = new XMLImportDialog(Display.getCurrent().getActiveShell(), true);  
        if (dialog.open() != Window.OK) {
            return;
        }
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.CATALOGIMPORT;
    }

}
