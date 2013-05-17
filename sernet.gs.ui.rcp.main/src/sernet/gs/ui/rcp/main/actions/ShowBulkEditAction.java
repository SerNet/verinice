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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.BulkEditDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.PersonBulkEditDialog;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.PasswordException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.taskcommands.BulkEditUpdate;
import sernet.gs.ui.rcp.main.service.taskcommands.ConfigurationBulkEditUpdate;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.CreateConfiguration;

/**
 * Erlaubt das gemeinsame Editieren der Eigenschaften von gleichen,
 * ausgewaehlten Objekten.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov
 *          2007) $ $LastChangedBy: koderman $
 * 
 */
public class ShowBulkEditAction extends RightsEnabledAction implements ISelectionListener {

    // FIXME server: bulk edit does not notify changes on self

    private static final transient Logger LOG = Logger.getLogger(ShowBulkEditAction.class);

    public static final String ID = "sernet.gs.ui.rcp.main.actions.showbulkeditaction"; //$NON-NLS-1$
    private final IWorkbenchWindow window;

    public ShowBulkEditAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.CASCADE));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText(Messages.ShowBulkEditAction_1);
        setRightID(ActionRightIDs.BULKEDIT);
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
    }

    @SuppressWarnings("restriction")
    @Override
    public void run() {
        Activator.inheritVeriniceContextState();

        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
        if (selection == null) {
            return;
        }

        // Realizes that the action to delete an element is greyed out,
        // when there is no right to do so.
        Iterator iterator = (selection).iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof CnATreeElement) {
                boolean writeallowed = CnAElementHome.getInstance().isWriteAllowed((CnATreeElement) next);
                if (!writeallowed) {
                    MessageDialog.openWarning(window.getShell(), 
                            Messages.ShowBulkEditAction_2, 
                            NLS.bind(Messages.ShowBulkEditAction_3, ((CnATreeElement) next).getTitle()));
                    setEnabled(false);
                    return;
                }
            }
        }

        final List<Integer> dbIDs = new ArrayList<Integer>(selection.size());
        final ArrayList<CnATreeElement> selectedElements = new ArrayList<CnATreeElement>();
        EntityType entType = null;
        final Class clazz;

        if (selection.getFirstElement() instanceof TodoViewItem) {
            // prepare list according to selected lightweight todo items:
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                TodoViewItem item = (TodoViewItem) iter.next();
                dbIDs.add(item.getDbId());
            }
            entType = HUITypeFactory.getInstance().getEntityType(MassnahmenUmsetzung.TYPE_ID);
            clazz = MassnahmenUmsetzung.class;
        } else if (selection.getFirstElement() instanceof Person || selection.getFirstElement() instanceof PersonIso){
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                CnATreeElement cElmt = (CnATreeElement)iter.next();
                LoadConfiguration command = new LoadConfiguration(cElmt);
                try {
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    if(command.getConfiguration() != null){
                        dbIDs.add(command.getConfiguration().getDbId());
                    } else { // no configuration existing for this user up to here, create new one
                        CreateConfiguration command2 = new CreateConfiguration(cElmt);
                        command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
                        dbIDs.add(command2.getConfiguration().getDbId());
                    }
                } catch (CommandException e) {
                    LOG.error("Error while retrieving configuration", e);
                    ExceptionUtil.log(e, Messages.ShowBulkEditAction_6); 
                }
            }
            if(selection.getFirstElement() instanceof Person || selection.getFirstElement() instanceof PersonIso){
                clazz = Configuration.class;
            } else {
                clazz = null;
            }
        } else {
            // prepare list according to selected tree items:
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                Object o = iter.next();
                CnATreeElement elmt = null;
                if (o instanceof CnATreeElement) {
                    elmt = (CnATreeElement) o;
                } else if (o instanceof DocumentReference) {
                    DocumentReference ref = (DocumentReference) o;
                    elmt = ref.getCnaTreeElement();
                }
                if (elmt == null) {
                    continue;
                }

                entType = HUITypeFactory.getInstance().getEntityType(elmt.getEntity().getEntityType());
                selectedElements.add(elmt);
                LOG.debug("Adding to bulk edit: " + elmt.getTitle()); //$NON-NLS-1$
            }
            clazz = null;
        }

        Dialog dialog = null;
        
        if(entType != null && !(entType.getId().equals(Person.TYPE_ID) || entType.getId().equals(PersonIso.TYPE_ID))){
            dialog = new BulkEditDialog(window.getShell(), entType);
        } else {
            dialog = new PersonBulkEditDialog(window.getShell(), Messages.ShowBulkEditAction_14);
        }
        if (dialog.open() != Window.OK) {
            return;
        }
        
        Entity tmpEntity = null;
        if(dialog instanceof BulkEditDialog){
            tmpEntity = ((BulkEditDialog)dialog).getEntity();
        }
        if(dialog instanceof PersonBulkEditDialog){
            tmpEntity = ((PersonBulkEditDialog)dialog).getEntity();
        }
        final Entity dialogEntity = tmpEntity;
        final Dialog chosenDialog = dialog;
        
        try {
            // close editors first:
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true);

            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @SuppressWarnings("restriction")
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();

                    // the selected items are of type CnaTreeelement and can be
                    // edited right here:
                    if (selectedElements.size() > 0){
                        if(!(selectedElements.get(0) instanceof Person || selectedElements.get(0) instanceof PersonIso)) {
                            editLocally(selectedElements, dialogEntity, monitor);
                        }
                    }  else {
                        // the selected elements are of type TodoView or other
                        // light weight items,
                        // editing has to be deferred to server (lookup of real
                        // items needed)
                        try {
                            String pw1 = null;
                            String pw2 = null;
                            if(chosenDialog instanceof PersonBulkEditDialog){
                                pw1 = ((PersonBulkEditDialog)chosenDialog).getPassword();
                                pw2 = ((PersonBulkEditDialog)chosenDialog).getPassword2();
                            }
                            editOnServer(clazz, dbIDs, dialogEntity, monitor, pw1, pw2);
                        } catch (CommandException e) {
                            throw new InterruptedException(e.getLocalizedMessage());
                        }
                    }

                    monitor.done();

                    boolean isIsoElement = false;
                    for (CnATreeElement cnATreeElement : selectedElements) {
                        isIsoElement = (cnATreeElement instanceof IISO27kElement);
                        if (isIsoElement) {
                            break;
                        }
                    }
                    // update once when finished:
                    if (CnAElementFactory.getLoadedModel() != null) {
                        CnAElementFactory.getLoadedModel().refreshAllListeners(IBSIModelListener.SOURCE_BULK_EDIT);
                    }
                    if (isIsoElement) {
                        CnAElementFactory.getInstance().getISO27kModel().refreshAllListeners(IBSIModelListener.SOURCE_BULK_EDIT);
                    }
                }
            });
        } catch (InterruptedException e) {
            ExceptionUtil.log(e, Messages.ShowBulkEditAction_5);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.ShowBulkEditAction_6);
        }
    }

    private void editOnServer(Class<? extends CnATreeElement> clazz, List<Integer> dbIDs, Entity dialogEntity, IProgressMonitor monitor, String newPassword, String newPassword2) throws CommandException {
        monitor.setTaskName(Messages.ShowBulkEditAction_7);
        monitor.beginTask(Messages.ShowBulkEditAction_8, IProgressMonitor.UNKNOWN);
        GenericCommand command = null;
        if(!dialogEntity.getEntityType().trim().equalsIgnoreCase(Configuration.TYPE_ID)){
            command = new BulkEditUpdate(clazz, dbIDs, dialogEntity);
        } else {
            boolean changePassword = false;
            if(newPassword!=null && !newPassword.isEmpty()) {
                if(!newPassword.equals(newPassword2)) {
                    throw new PasswordException(Messages.ConfigurationAction_10);
                } else {
                    changePassword = true;
                }
            }
            command = new ConfigurationBulkEditUpdate(dbIDs, dialogEntity, changePassword, newPassword);
        }
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        if(((ConfigurationBulkEditUpdate)command).getFailedUpdates().size() > 0){
            StringBuilder sb = new StringBuilder();
            sb.append(Messages.ShowBulkEditAction_15 +":\n");
            for(String username : ((ConfigurationBulkEditUpdate)command).getFailedUpdates()){
                sb.append(username + "\n");
            }
            ExceptionUtil.log(new ConfigurationException(Messages.ShowBulkEditAction_16), Messages.ShowBulkEditAction_16 + "\n" + sb.toString() );
        }
       
    }
    

    
    /**
     * Action is enabled when only items of the same type are selected.
     */
    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (input instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) input;

            // check for listitems:
            if (selection.size() > 0 && selection.getFirstElement() instanceof TodoViewItem) {
                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    if (!(iter.next() instanceof TodoViewItem)) {
                        setEnabled(false);
                        return;
                    }
                }
                if(checkRights()){
                    setEnabled(true);
                }
                return;
            }

            // check for document references:
            CnATreeElement elmt = null;
            if (selection.size() > 0 && selection.getFirstElement() instanceof DocumentReference) {
                elmt = ((DocumentReference) selection.getFirstElement()).getCnaTreeElement();
            }

            // check for other objects:
            else if (selection.size() > 0 && selection.getFirstElement() instanceof CnATreeElement && ((CnATreeElement) selection.getFirstElement()).getEntity() != null) {
                elmt = (CnATreeElement) selection.getFirstElement();
            }

            if (elmt != null) {
                String type = elmt.getEntity().getEntityType();

                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    Object o = iter.next();
                    if (o instanceof CnATreeElement) {
                        elmt = (CnATreeElement) o;

                    } else if (o instanceof DocumentReference) {
                        DocumentReference ref = (DocumentReference) o;
                        elmt = ref.getCnaTreeElement();
                    }
                }

                if (elmt == null || elmt.getEntity() == null || !elmt.getEntity().getEntityType().equals(type)) {
                    setEnabled(false);
                    return;
                }

                if(checkRights()){
                    setEnabled(true);
                }
                return;
            }
        }
        setEnabled(false);
    }


    private void editLocally(List<CnATreeElement> selectedElements, Entity dialogEntity, IProgressMonitor monitor) {
        monitor.setTaskName(Messages.ShowBulkEditAction_9);
        monitor.beginTask(Messages.ShowBulkEditAction_10, selectedElements.size() + 1);

        // for every target:
        for (CnATreeElement elmt : selectedElements) {
            // set values:
            Entity editEntity = elmt.getEntity();
            editEntity.copyEntity(dialogEntity);
            monitor.worked(1);
        }
        try {
            monitor.setTaskName(Messages.ShowBulkEditAction_11);
            monitor.beginTask(Messages.ShowBulkEditAction_12, IProgressMonitor.UNKNOWN);
            CnAElementHome.getInstance().update(selectedElements);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.ShowBulkEditAction_13);
        }

    }
}
