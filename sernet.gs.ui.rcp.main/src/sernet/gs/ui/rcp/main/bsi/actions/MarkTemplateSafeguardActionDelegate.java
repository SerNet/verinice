/******************************************************************************* 
 * Copyright (c) 2016 Viktor Schmidt. 
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation 
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

public class MarkTemplateSafeguardActionDelegate extends MarkTemplateActionDelegate {
    private IWorkbenchPart targetPart;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public void run(IAction action) {
        try {
            markSelectionsAsTemplateSafequardsAndUpdate();
            markParentAsTemplateModuleAndUpdate();
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.MarkTemplateSafeguardActionDelegate_0);
        }
    }

    private void markSelectionsAsTemplateSafequardsAndUpdate() throws CommandException {
        Object[] selections = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection()).toArray();
        for (Object selection : selections) {
            if (selection instanceof MassnahmenUmsetzung) {
                MassnahmenUmsetzung safeguard = (MassnahmenUmsetzung) selection;
                safeguard.setTemplateTypeValue(CnATreeElement.TemplateType.TEMPLATE.name());
                CnAElementHome.getInstance().update(safeguard);
            }
        }
    }
    
    private void markParentAsTemplateModuleAndUpdate() throws CommandException {
        Object firstElement = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection()).getFirstElement();
        if (firstElement instanceof MassnahmenUmsetzung) {
            BausteinUmsetzung module = (BausteinUmsetzung) ((MassnahmenUmsetzung) firstElement).getParent();
            module.setTemplateTypeValue(CnATreeElement.TemplateType.TEMPLATE.name());
            CnAElementHome.getInstance().update(module);

            // notify all listeners:
            CnAElementFactory.getModel(module.getParent()).childChanged(module);
            CnAElementFactory.getModel(module.getParent()).databaseChildChanged(module);
        }
    }
}