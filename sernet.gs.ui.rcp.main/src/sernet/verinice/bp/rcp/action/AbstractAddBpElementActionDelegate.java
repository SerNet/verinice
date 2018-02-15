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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.actions.AbstractAddCnATreeElementActionDelegate;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;

/**
 * A base class for adding new BP elements to an element group.
 * 
 * The class needs to be extended on a per-element-type-basis. Derived classes
 * can be used as action classes for an objectContribution in
 * <code>plugin.xml</code>
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public abstract class AbstractAddBpElementActionDelegate<T extends CnATreeElement>
        extends AbstractAddCnATreeElementActionDelegate {

    protected IWorkbenchPart targetPart;
    private final Class<? extends Group<T>> elementGroupClass;
    private final String typeId;
    private final String failureMessage;

    public AbstractAddBpElementActionDelegate(Class<? extends Group<T>> elementGroupClass,
            String typeId, String failureMessage) {
        this.elementGroupClass = elementGroupClass;
        this.typeId = typeId;
        this.failureMessage = failureMessage;
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.ADDBPELEMENT;
    }

    public final void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public final void run(IAction action) {
        try {
            Object sel = ((IStructuredSelection) targetPart.getSite().getSelectionProvider()
                    .getSelection()).getFirstElement();
            if (elementGroupClass.isAssignableFrom(sel.getClass())) {
                CnATreeElement cont = (CnATreeElement) sel;
                boolean inheritIcon = Activator.getDefault().getPreferenceStore()
                        .getBoolean(PreferenceConstants.INHERIT_SPECIAL_GROUP_ICON);
                CnATreeElement newElement = CnAElementFactory.getInstance().saveNew(cont, typeId,
                        null, inheritIcon);
                if (newElement != null) {
                    EditorFactory.getInstance().openEditor(newElement);
                }
            }
        } catch (Exception e) {
            ExceptionUtil.log(e, failureMessage);
        }

    }
}