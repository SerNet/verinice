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
package sernet.verinice.rcp;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.NotSufficientRightsException;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.CnATreeElementBuildException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * THis handler creates new groups for ISO2700 and base protection elements.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class AddGroupHandler extends RightsEnabledHandler {

    private static final Logger LOG = Logger.getLogger(AddGroupHandler.class);

    /*
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            if (checkRights()) {
                CnATreeElement parent = getSelectedElement(event);
                createGroup(parent);
            } else {
                throw new NotSufficientRightsException("Action not allowed for user"); //$NON-NLS-1$
            }
        } catch (NotSufficientRightsException e) {
            LOG.error("Could not add element", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddGroupHandler_permission_error);
        } catch (Exception e) {
            LOG.error("Could not add element group", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddGroupHandler_error);
        }
        return null;
    }

    protected void createGroup(CnATreeElement parent)
            throws CommandException, CnATreeElementBuildException {
        CnATreeElement newGroup = null;
        if (parent != null) {
            String groupTypeId = getTypeIdForNewGroup(parent);

            boolean inheritIcon = Activator.getDefault().getPreferenceStore()
                    .getBoolean(PreferenceConstants.INHERIT_SPECIAL_GROUP_ICON);
            newGroup = CnAElementFactory.getInstance().saveNew(parent, groupTypeId, null,
                    inheritIcon);
        }
        if (newGroup != null) {
            EditorFactory.getInstance().openEditor(newGroup);
        }
    }

    protected String getTypeIdForNewGroup(CnATreeElement parent) {
        if (parent instanceof Asset) {
            return ControlGroup.TYPE_ID;
        }
        // child groups have the same type as parents
        return parent.getTypeId();
    }

    protected CnATreeElement getSelectedElement(ExecutionEvent event) {
        CnATreeElement element = null;
        final IStructuredSelection selection = (IStructuredSelection) HandlerUtil
                .getCurrentSelection(event);
        Object sel = selection.getFirstElement();
        if (sel instanceof IISO27kGroup || sel instanceof IBpGroup) {
            element = (CnATreeElement) sel;
        }
        return element;
    }

}
