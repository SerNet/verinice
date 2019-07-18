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
package sernet.verinice.rcp.templates;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;

import sernet.gs.ui.rcp.main.ApplicationActionBarAdvisor;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.CnATreeElement.TemplateType;
import sernet.verinice.rcp.Preferences;
import sernet.verinice.service.commands.templates.LoadModelingTemplateSettings;

/**
 * <p>
 * Modeling templates in the BSI IT baseline security allow to create and
 * maintain the modules, safeguards and objects at freely definable, ideally at
 * central location, and to use them in other points of application.
 * </p>
 * <p>
 * Each {@link CnATreeElement}, representing a Object or Module, can be marked
 * as modeling template or each Safeguard as central
 * ({@link TemplateType#TEMPLATE}).
 * </p>
 * <p>
 * Example:
 * </p>
 * <p>
 * A Modeling template in the BSI IT baseline:
 * </p>
 * <p>
 * <b>IT Network 1</b> <ui>
 * <li>Object (marked as modeling template, {@link TemplateType#TEMPLATE})</li>
 * <ui>
 * <li>Module 1 (marked as modeling template,
 * {@link TemplateType#TEMPLATE})</li> <ui>
 * <li>Safeguard 1 (marked as central, {@link TemplateType#TEMPLATE})</li>
 * <li>Safeguard 2</li> </ui>
 * <li>Module 2</li> <ui>
 * <li>Safeguard 1</li>
 * <li>Safeguard 2 (marked as central, {@link TemplateType#TEMPLATE})</li> </ui>
 * <li>Module 3...</li> </ui> </ui>
 * </p>
 * 
 * @see CnATreeElement#implementedTemplateUuids
 * @see TemplateType
 * @see sernet.gs.server.DeleteOrphanTemplateRelationsJob
 * 
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public abstract class MarkTemplateActionDelegate implements IObjectActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(ApplicationActionBarAdvisor.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.
     * IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public final void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(checkRights());
        if (!isModelingTemplateActive()) {
            return;
        }
        // Realizes that the action to mark a element as template is greyed out,
        // if it is already template or implementation.
        Object sel = ((IStructuredSelection) selection).getFirstElement();
        if (sel instanceof ITVerbund) {
            action.setEnabled(false);
        } else if (sel instanceof CnATreeElement) {

            CnATreeElement element = (CnATreeElement) sel;
            if (element.isTemplateOrImplementation()) {
                action.setEnabled(false);
            }
        }
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.MARKTEMPLATE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return isModelingTemplateActive() && service.isEnabled(getRightID());
    }

    private static ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }

    private boolean isModelingTemplateActive() {
        if (Preferences.isStandalone()) {
            return false;
        }
        LoadModelingTemplateSettings modelingTemplateSettings = new LoadModelingTemplateSettings();
        try {
            modelingTemplateSettings = getCommandService().executeCommand(modelingTemplateSettings);
        } catch (CommandException e) {
            LOG.error("Error while loading modeling template settings", e); //$NON-NLS-1$
        }
        return modelingTemplateSettings.isModelingTemplateActive();
    }
}
