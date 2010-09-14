/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class NaturalizeAction extends Action implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(NaturalizeAction.class);
    
    public static final String ID = "sernet.gs.ui.rcp.main.actions.NaturalizeAction"; //$NON-NLS-1$

    List<CnATreeElement> selectedElementList = new LinkedList<CnATreeElement>();

    ICommandService commandService;

    public NaturalizeAction(IWorkbenchWindow window) {
        setText(Messages.NaturalizeAction_0);
        setId(ID);
        setActionDefinitionId(ID);
        // TODO: create icon
        //setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.SECURITY));
        setToolTipText(Messages.NaturalizeAction_1);
        window.getSelectionService().addSelectionListener(this);
    }

    public void run() {
        try {
            for (CnATreeElement element : selectedElementList) {
                element.setSourceId(null);
                element.setExtId(null);
                SaveElement<CnATreeElement> command = new SaveElement<CnATreeElement>(element);
                command = getCommandService().executeCommand(command);
                element = command.getElement();
                CnAElementFactory.getModel(element).childChanged(element.getParent(), element);
            }
        } catch (Exception e) {
            LOG.error("Error while naturalizing element", e);
            ExceptionUtil.log(e, Messages.NaturalizeAction_2);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
     * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        boolean enabled = false;
        selectedElementList.clear();
        for (Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if (o instanceof CnATreeElement && ((CnATreeElement) o).getSourceId()!=null) {
                selectedElementList.add((CnATreeElement) o);
                enabled = true;
            } else {
                selectedElementList.clear();
                enabled = false;
                break;
            }
        }
        this.setEnabled(enabled);
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

}
