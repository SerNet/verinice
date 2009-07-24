/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.BulkEditDialog;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.BulkEditUpdate;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;

/**
 * Erlaubt das gemeinsame Editieren der Eigenschaften von gleichen, ausgewählten
 * Objekten.
 * 
 * @author koderman@sernet.de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov
 *          2007) $ $LastChangedBy: koderman $
 * 
 */
public class ShowBulkEditAction extends Action implements ISelectionListener {

	// FIXME server: bulk edit does not notify changes on self
	
	public static final String ID = "sernet.gs.ui.rcp.main.actions.showbulkeditaction";
	private final IWorkbenchWindow window;

	public ShowBulkEditAction(IWorkbenchWindow window, String label) {
		this.window = window;
		setText(label);
		setId(ID);
		setActionDefinitionId(ID);
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(
				ImageCache.CASCADE));
		window.getSelectionService().addSelectionListener(this);
		setToolTipText("Gleichartige Elemente gemeinsam editieren.");
	}

	public void run() {
		IStructuredSelection selection = (IStructuredSelection) window
				.getSelectionService().getSelection();
		if (selection == null)
			return;
		
		final List<Integer> dbIDs = new ArrayList<Integer>(selection.size());
		final ArrayList<CnATreeElement> selectedElements = new ArrayList<CnATreeElement>();
		EntityType entType = null;
		final Class clazz;

		if (selection.getFirstElement() instanceof TodoViewItem) {
			// prepare list according to selected lightweight todo items:
				for (Iterator iter = selection.iterator(); iter.hasNext();) {
					TodoViewItem item = (TodoViewItem) iter.next();
					dbIDs.add(item.getdbId());
				}
				entType = HUITypeFactory.getInstance().getEntityType(MassnahmenUmsetzung.TYPE_ID);
				clazz = MassnahmenUmsetzung.class;
		}
		else {
			// prepare list according to selected tree items:
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object o = iter.next();
				CnATreeElement elmt = null;
				if (o instanceof CnATreeElement)
					elmt = (CnATreeElement) o;
				else if (o instanceof DocumentReference) {
					DocumentReference ref = (DocumentReference) o;
					elmt = ref.getCnaTreeElement();
				}
				if (elmt == null)
					continue;
				
				entType = HUITypeFactory.getInstance().getEntityType(
						elmt.getEntity().getEntityType());
				selectedElements.add(elmt);
				Logger.getLogger(this.getClass()).debug(
						"Adding to bulk edit: " + elmt.getTitel());
			}
			clazz=null;
		}
		

		final BulkEditDialog dialog = new BulkEditDialog(window.getShell(),
				entType);
		if (dialog.open() != InputDialog.OK)
			return;

		try {
			// close editors first:
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().closeAllEditors(true /* ask save */);

			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							
							// the selected items are of type CnaTreeelement and can be edited right here:
							if (selectedElements.size() >0)
								editLocally(selectedElements, dialog.getEntity(), monitor);
							else {
								// the selected elements are of type TodoView or other light weight items,
								// editing has to be deferred to server (lookup of real items needed)
								try {
									editOnServer(clazz, dbIDs, dialog.getEntity(), monitor);
								} catch (CommandException e) {
									throw new InterruptedException(e.getLocalizedMessage());
								}
							}
							
							monitor.done();
							// update once when finished:
							CnAElementFactory.getLoadedModel()
									.refreshAllListeners(IBSIModelListener.SOURCE_BULK_EDIT);
						}

						
					});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Error executing bulk edit.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Aborted.");
		}
	}


	private void editOnServer(Class<? extends CnATreeElement> clazz, List<Integer> dbIDs,
			Entity dialogEntity, IProgressMonitor monitor) throws CommandException {
		monitor.setTaskName("Setze veränderte Werte...");
		monitor.beginTask("Bulk Edit", IProgressMonitor.UNKNOWN);
		
		BulkEditUpdate command = new BulkEditUpdate(clazz, dbIDs, dialogEntity);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
	}

	/**
	 * Action is enabled when only items of the same type are selected.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection input) {
		if (input instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) input;

			// check for listitems:
			TodoViewItem item;
			if (selection.size() > 0
					&& selection.getFirstElement() instanceof TodoViewItem) {
				item = ((TodoViewItem) selection.getFirstElement());
				for (Iterator iter = selection.iterator(); iter.hasNext();) {
					if (!(iter.next() instanceof TodoViewItem) ) {
						setEnabled(false);
						return;
					}
				}
				setEnabled(true);
				return;
			}
			
			// check for document references:
			CnATreeElement elmt = null;
			if (selection.size() > 0
					&& selection.getFirstElement() instanceof DocumentReference) {
				elmt = ((DocumentReference) selection.getFirstElement())
						.getCnaTreeElement();
			}

			// check for other objects:
			else if (selection.size() > 0
					&& selection.getFirstElement() instanceof CnATreeElement
					&& ((CnATreeElement) selection.getFirstElement())
							.getEntity() != null) {
				elmt = (CnATreeElement) selection.getFirstElement();
			}

			if (elmt != null) {
				String type = elmt.getEntity().getEntityType();
				EntityType entType = HUITypeFactory.getInstance()
						.getEntityType(type);

				for (Iterator iter = selection.iterator(); iter.hasNext();) {
					Object o = iter.next();
					if (o instanceof CnATreeElement) {
						elmt = (CnATreeElement) o;

					} else if (o instanceof DocumentReference) {
						DocumentReference ref = (DocumentReference) o;
						elmt = ref.getCnaTreeElement();
					}
				}

				if (elmt == null) {
					setEnabled(false);
					return;
				}

				if (elmt.getEntity() == null
						|| !elmt.getEntity().getEntityType().equals(type)) {
					setEnabled(false);
					return;
				}
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}

	private void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	private void editLocally(
			 ArrayList<CnATreeElement> selectedElements,
			 Entity dialogEntity,
			IProgressMonitor monitor) {
		monitor.setTaskName("Setze veränderte Werte...");
		monitor.beginTask("Bulk Edit", selectedElements
				.size() + 1);
		
		// for every target:
		for (CnATreeElement elmt : selectedElements) {
			// set values:
			Entity editEntity = elmt.getEntity();
			editEntity.copyEntity(dialogEntity);
			monitor.worked(1);
		}
		try {
			monitor
					.setTaskName("Speichere veränderte Werte...");
			monitor.beginTask(
					"Speichere veränderte Werte...",
					IProgressMonitor.UNKNOWN);
			CnAElementHome.getInstance().update(
					selectedElements);
		} catch (Exception e) {
			ExceptionUtil
					.log(e,
							"Elemente konnten nicht gespeichert werden.");
		}
		
	}
}
