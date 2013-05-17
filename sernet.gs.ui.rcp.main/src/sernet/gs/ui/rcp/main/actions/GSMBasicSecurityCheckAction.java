/*******************************************************************************
 * Copyright (c) 2012 Julia Haas.
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
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.GSMKonsolidatorCommand;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
public class GSMBasicSecurityCheckAction extends RightsEnabledAction implements ISelectionListener {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.gsmbasicsecuritycheckaction"; //$NON-NLS-1$

    private static final Logger LOG = Logger.getLogger(GSMBasicSecurityCheckAction.class);
    private final IWorkbenchWindow window;
    private String gsmresult = "GSM Result";
    private boolean serverIsRunning = true;
    
    public GSMBasicSecurityCheckAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.KONSOLIDATOR));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText(Messages.GSMBasicSecurityCheckAction_1);
        setRightID(ActionRightIDs.KONSOLIDATOR);
        if (Activator.getDefault().isStandalone() && !Activator.getDefault().getInternalServer().isRunning()) {
            serverIsRunning = false;
            IInternalServerStartListener listener = new IInternalServerStartListener() {
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if (e.isStarted()) {
                        serverIsRunning = true;
                        setEnabled(checkRights());
                    }
                }
            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }
    }

    public void run () {

        try{
            dorun();
            CnAElementFactory.getInstance().reloadModelFromDatabase();
        }
        catch(Exception e){
            LOG.error("Error while security check.", e);
            ExceptionUtil.log(e, Messages.GSMBasicSecurityCheckAction_6);

        }
    }
  
    public void dorun() {
        Activator.inheritVeriniceContextState();
        final IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    for (Iterator serverIter = selection.iterator(); serverIter.hasNext();) {
                        Object o = serverIter.next();
                        if (o instanceof Server){
                            Server serverelement = (Server) o;
                            monitor.beginTask(Messages.GSMBasicSecurityCheckAction_2, IProgressMonitor.UNKNOWN);
                            konsolidiereModule(serverelement);
                        }
                    }
                    monitor.done();
                }
            });
        }
        catch (InterruptedException e) {
            ExceptionUtil.log(e, Messages.GSMBasicSecurityCheckAction_5);
        }
        catch(Exception e){
            LOG.error("Error while security check", e);
            ExceptionUtil.log(e, Messages.GSMBasicSecurityCheckAction_6);
        }
    } 


    private void konsolidiereModule(Server serverelement){
        final List<Server> selectedServers = new ArrayList<Server>();
        final List<BausteinUmsetzung> bausteine = new ArrayList<BausteinUmsetzung>();
        BausteinUmsetzung source = null;
        BausteinUmsetzung  baustein = null;
        RetrieveInfo ris = RetrieveInfo.getChildrenInstance().setChildrenProperties(true).setParent(true);
        serverelement = (Server) Retriever.retrieveElement(serverelement,ris);
        selectedServers.add(serverelement);
        try{
            for (CnATreeElement bausteineLst : serverelement.getChildren()){
                baustein = (BausteinUmsetzung) bausteineLst;
                bausteine.add(baustein);
                String gsmname = baustein.getTitle().trim();
                if(gsmname.equals(gsmresult)){
                    source = baustein;
                }
            }
            if(source!=null){
                konsolidiereMassnahmen(bausteine, source);
            }   
        }catch(Exception e){
            LOG.error("Error while security check", e);
            ExceptionUtil.log(e, Messages.GSMBasicSecurityCheckAction_6);
        }
    }
           
    
    /**
    * @param bausteine
    * @param source
    */
    private void konsolidiereMassnahmen(final List<BausteinUmsetzung> bausteine, BausteinUmsetzung source) {
        try {
            GSMKonsolidatorCommand command = new GSMKonsolidatorCommand(bausteine, source);
            if(source==null){
                showInfoMessage();
            }
            ServiceFactory.lookupCommandService().executeCommand(command);
         } catch (CommandException e) {
            ExceptionUtil.log(e, Messages.GSMBasicSecurityCheckAction_4);
        }
    }
    
    private void showInfoMessage(){
       Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                // code der in der GUI laufen soll 
                MessageDialog.openInformation(window.getShell(), "Info", Messages.GSMBasicSecurityCheckAction_7);
            }
        });
        }    
    
      /**
     * Action is enabled when Server selected.
     * 
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (serverIsRunning) {
            setEnabled(checkRights());  
        if (input instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) input;   
            for (Iterator servers = selection.iterator(); servers.hasNext();) {
                Object serverelement = servers.next(); 
                if (!(serverelement instanceof Server)) {
                    setEnabled(false);
                    return;
                }
            }
            if (checkRights()) {
                setEnabled(true);
            }
            return;
        }
        // no structured selection:
        setEnabled(false);
    }
    }
}