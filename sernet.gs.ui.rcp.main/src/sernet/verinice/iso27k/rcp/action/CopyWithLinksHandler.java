/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.CnPItems;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.ValidateLinksInSubtrees;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CopyWithLinksHandler extends CopyHandler {

    private static final int ERROR_MESSAGE_LINK_CUTOFF = 5;

    /*
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selectedElements = HandlerUtil.getCurrentStructuredSelection(event);
        if (!selectedElements.isEmpty()) {
            Set<String> selectedElementsUUIDs = new HashSet<>(selectedElements.size());
            for (Object selectedElement : selectedElements.toList()) {
                if (selectedElement instanceof CnATreeElement) {
                    selectedElementsUUIDs.add(((CnATreeElement) selectedElement).getUuid());
                }
            }

            ValidateLinksInSubtrees validateLinksInSubtrees = new ValidateLinksInSubtrees(
                    selectedElementsUUIDs);
            try {
                validateLinksInSubtrees = ServiceFactory.lookupCommandService()
                        .executeCommand(validateLinksInSubtrees);

                Set<CnALink> invalidLinks = validateLinksInSubtrees.getInvalidLinks();
                if (!invalidLinks.isEmpty()) {
                    showInvalidLinksMessage(invalidLinks);
                    throw new ExecutionException(
                            "Found " + invalidLinks.size() + " invalid link(s): " + invalidLinks);

                }
            } catch (CommandException e) {
                throw new ExecutionException("Could not check links", e);
            }

        }

        Object result = super.execute(event);
        CnPItems.setCopyLinks(true);
        return result;
    }

    private static void showInvalidLinksMessage(Set<CnALink> invalidLinks) {
        StringBuilder dialogMessageBuilder = new StringBuilder(
                Messages.getString("InvalidLinksDialogMessage")).append("\n");
        invalidLinks.stream().limit(ERROR_MESSAGE_LINK_CUTOFF)
                .forEach(link -> dialogMessageBuilder.append("\n - ")
                        .append(link.getDependant().getTitle()).append(" → ")
                        .append(link.getDependency().getTitle()).append(" (")
                        .append(link.getRelationId()).append(")"));
        if (invalidLinks.size() > ERROR_MESSAGE_LINK_CUTOFF) {
            dialogMessageBuilder.append("\n...");
        }
        MessageDialog.openError(null, Messages.getString("InvalidLinksDialogTitle"),
                dialogMessageBuilder.toString());
    }

}
