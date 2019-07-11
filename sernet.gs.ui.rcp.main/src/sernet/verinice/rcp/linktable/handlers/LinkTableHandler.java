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
package sernet.verinice.rcp.linktable.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.ui.rcp.main.StatusLine;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.rcp.linktable.EditorCloseException;
import sernet.verinice.rcp.linktable.LinkTableEditorInput;
import sernet.verinice.rcp.linktable.LinkTableUtil;
import sernet.verinice.rcp.linktable.Messages;

/**
 * Base handler class for opening new and existing report queries.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class LinkTableHandler extends RightsEnabledHandler {

    private static final Logger LOG = Logger.getLogger(LinkTableHandler.class);

    public LinkTableHandler() {
        super(false);
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.EXPORT_LINK_TABLE;
    }

    /*
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (checkRights()) {
            final LinkTableEditorInput veriniceLinkTable = createLinkTable();
            StatusLine.setMessage("Opening report query " + veriniceLinkTable.getName() + "...");
            if (veriniceLinkTable != null) {
                Display.getDefault().asyncExec(() -> {
                    validateInputAndOpenEditor(veriniceLinkTable);
                    StatusLine.setMessage("");
                });
            }
        } else {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error", //$NON-NLS-1$
                    Messages.ExportLinkTableHandler_1);
        }
        return null;
    }

    private void validateInputAndOpenEditor(final LinkTableEditorInput veriniceLinkTable) {
        try {
            if (!LinkTableUtil.isValidVeriniceLinkTable(veriniceLinkTable.getInput()).isValid()) {
                MessageDialog confirmInvalidInput = new MessageDialog(
                        Display.getCurrent().getActiveShell(), Messages.LinkTableHandler_1, null,
                        Messages.LinkTableHandler_2 + Messages.LinkTableHandler_3,
                        MessageDialog.QUESTION,
                        new String[] { Messages.LinkTableHandler_4, Messages.LinkTableHandler_5 },
                        0);
                int open = confirmInvalidInput.open();
                if (open != 0) {
                    return;
                }
            }
            EditorFactory.getInstance().updateAndOpenObject(veriniceLinkTable);
        } catch (Exception e) {
            if (e.getCause() instanceof EditorCloseException) {
                EditorCloseException ex = (EditorCloseException) e.getCause();
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .closeEditor(ex.getEditor(), true);
            } else {
                throw e;
            }
        }
    }

    protected abstract LinkTableEditorInput createLinkTable();

}
