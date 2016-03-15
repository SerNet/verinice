/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class OpenLinkTableHandler extends RightsEnabledHandler {

    private static final Logger LOG = Logger.getLogger(OpenLinkTableHandler.class);
    private static final String VLT = ".vlt"; //$NON-NLS-1$

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.EXPORT_LINK_TABLE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final String filePath = createFilePath();
        VeriniceLinkTable veriniceLinkTable = VeriniceLinkTableIO.read(filePath);
        veriniceLinkTable.setId(filePath);
        EditorFactory.getInstance().updateAndOpenObject(veriniceLinkTable);
        return null;
    }

    private String createFilePath() {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        dialog.setText("Load verinice link table (.vlt) file");
        try {
            dialog.setFilterPath(getDirectory());
        } catch (Exception e1) {
            LOG.debug("Error with file path: " + getDirectory(), e1);
            dialog.setFileName(""); //$NON-NLS-1$
        }
        dialog.setFilterExtensions(new String[] {"*" + VLT}); //$NON-NLS-1$
        dialog.setFilterNames(new String[] {"verinice link table (.vlt)"});
        dialog.setFilterIndex(0);
        String filePath = dialog.open();
        return filePath;
    }

    private String getDirectory() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String dir = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_CSV_EXPORT);
        if(dir==null || dir.isEmpty()) {
            dir = System.getProperty("user.home");
        }
        if (!dir.endsWith(System.getProperty("file.separator"))) {
            dir = dir + System.getProperty("file.separator");
        }
        return dir;
    }

}
