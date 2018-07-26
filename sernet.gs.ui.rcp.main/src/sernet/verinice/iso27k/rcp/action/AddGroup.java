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

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.NotSufficientRightsException;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CnATreeElementBuildException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.rcp.AddGroupMessageHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class AddGroup extends Action implements IObjectActionDelegate, RightEnabledUserInteraction {

    private static final String MESSAGE_KEY_NEW_ELEMENT_GROUP = "AddGroup.19";

    private static final Logger logger = Logger.getLogger(AddGroup.class);

    private IWorkbenchPart targetPart;

    private CnATreeElement parent;

    private String typeId;

    public AddGroup() {
        super();
    }

    public AddGroup(CnATreeElement element, String typeId, String childTypeId) {
        super();
        this.parent = element;
        this.typeId = typeId;
        this.setImageDescriptor(ImageDescriptor
                .createFromImage(ImageCache.getInstance().getImageForTypeId(childTypeId)));
        this.setText(Optional.ofNullable(AddGroupMessageHelper.getMessageForAddGroup(typeId))
                .orElse(Messages.getString(MESSAGE_KEY_NEW_ELEMENT_GROUP)));
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    /*
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (!checkRights()) {
            Exception e = new NotSufficientRightsException("Action not allowed for user");
            logger.error("Could not add element", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.getString("AddElement.21")); //$NON-NLS-1$
        } else {
            try {
                if (targetPart != null) {
                    Object sel = ((IStructuredSelection) targetPart.getSite().getSelectionProvider()
                            .getSelection()).getFirstElement();
                    if (sel instanceof Group<?>) {
                        parent = (Group<?>) sel;
                    }
                }
                if (parent != null) {
                    createNewGroup(parent, typeId);
                }

            } catch (Exception e) {
                logger.error("Could not add element group", e); //$NON-NLS-1$
                ExceptionUtil.log(e, Messages.getString("AddGroup.18")); //$NON-NLS-1$
            }
        }
    }

    private static void createNewGroup(CnATreeElement parent, String typeId)
            throws CommandException, CnATreeElementBuildException {
        String currentType = typeId;
        if (currentType == null) {
            // child groups have the same type as parents
            currentType = parent.getTypeId();
            if (parent instanceof Asset) {
                currentType = ControlGroup.TYPE_ID;
            }
        }
        boolean inheritIcon = Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.INHERIT_SPECIAL_GROUP_ICON);
        CnATreeElement newElement = CnAElementFactory.getInstance().saveNew((CnATreeElement) parent,
                currentType, null, inheritIcon);
        Optional.ofNullable(newElement).ifPresent(EditorFactory.getInstance()::openEditor);
    }

    /*
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        run();
    }

    /*
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.
     * IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(checkRights());
        if (selection instanceof IStructuredSelection) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            boolean allowed = false;
            boolean enabled = false;
            if (sel instanceof CnATreeElement) {
                allowed = CnAElementHome.getInstance().isNewChildAllowed((CnATreeElement) sel);
            }
            if (sel instanceof Audit) {
                action.setText(Messages.getString(MESSAGE_KEY_NEW_ELEMENT_GROUP));
            } else if (sel instanceof IISO27kGroup) {
                enabled = true;
                IISO27kGroup group = (IISO27kGroup) sel;
                String typeId0 = group.getChildTypes()[0];
                if (group instanceof Asset) {
                    typeId0 = Control.TYPE_ID;
                }
                action.setImageDescriptor(ImageDescriptor
                        .createFromImage(ImageCache.getInstance().getImageForTypeId(typeId0)));
                action.setText(Optional
                        .ofNullable(AddGroupMessageHelper.getMessageForAddGroup(group.getTypeId()))
                        .orElse(Messages.getString(MESSAGE_KEY_NEW_ELEMENT_GROUP))); // $NON-NLS-1$
            }
            // Only change state when it is enabled, since we do not want to
            // trash the enablement settings of plugin.xml
            if (action.isEnabled()) {
                action.setEnabled(allowed && enabled);
            }
        }
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient) VeriniceContext
                .get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.ADDISMGROUP;
    }

}
