/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.RunRiskAnalysisCommand;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

public class RunRiskAnalysisAction extends RightsEnabledAction implements ISelectionListener  {

    public static final String ID = "sernet.gs.ui.rcp.main.runriskanalysisaction"; //$NON-NLS-1$
    
    private List<Integer> scopeIds = new LinkedList<>();

    public RunRiskAnalysisAction(IWorkbenchWindow window) {
        setText(Messages.RunRiskAnalysisAction_0);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ISO27K_RISK));
        setRightID(ActionRightIDs.RISKANALYSIS);
        if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
                        setEnabled(checkRights());
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }
        CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {
            @Override
            public void closed(BSIModel model) {
                setEnabled(false);
            }
            @Override
            public void loaded(BSIModel model) {
                if(checkRights()){
                    setEnabled(true);
                }
            }
            @Override
            public void loaded(ISO27KModel model) {
                if(checkRights()){
                    setEnabled(true);
                }
            }
        });
        window.getSelectionService().addSelectionListener(this);
    }


    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        Activator.inheritVeriniceContextState();
        try {
            // close editors:
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /* ask save */);
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    Integer[] scopeIdArray = scopeIds.toArray(new Integer[scopeIds.size()]);
                    RunRiskAnalysisCommand command = new RunRiskAnalysisCommand(scopeIdArray);
                    try {
                        ServiceFactory.lookupCommandService().executeCommand(command);
                    } catch (CommandException e) {
                        ExceptionUtil.log(e, Messages.RunRiskAnalysisAction_1);
                    }
                }
            });
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.RunRiskAnalysisAction_2);
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart arg0, ISelection input) {
        if (input instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) input;
            scopeIds.clear();
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                Object selectedObject = iter.next();
                if (isOrganization(selectedObject)) {
                    CnATreeElement element = (CnATreeElement) selectedObject;
                    scopeIds.add(element.getDbId());
                }
            }
        }      
    }


    /**
     * @param element
     * @return
     */
    private boolean isOrganization(Object element) {
        return element instanceof Organization;
    }

}
