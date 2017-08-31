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
package sernet.verinice.moditbp.rcp.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.moditbp.categories.BusinessProcessGroup;
import sernet.verinice.model.moditbp.elements.BusinessProcess;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class AddBusinessProcessActionDelegate extends AbstractAddBpElementActionDelegate {

    private IWorkbenchPart targetPart;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public void run(IAction action) {

        try {
            Object sel = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection()).getFirstElement();
            CnATreeElement newElement = null;
            if (sel instanceof BusinessProcessGroup) {
                CnATreeElement cont = (CnATreeElement) sel;
                boolean inheritIcon = Activator.getDefault().getPreferenceStore()
                        .getBoolean(PreferenceConstants.INHERIT_SPECIAL_GROUP_ICON);
                newElement = CnAElementFactory.getInstance().saveNew(cont, BusinessProcess.TYPE_ID, null, inheritIcon);
            }
            if (newElement != null) {
                EditorFactory.getInstance().openEditor(newElement);
            }
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.AddBusinessprocessDelegate_0);
        }

    }

}
