package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.hibernate.StaleObjectStateException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.Application;
import sernet.gs.ui.rcp.main.ApplicationActionBarAdvisor;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ICommandIds;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.BulkEditDialog;
import sernet.gs.ui.rcp.main.bsi.editors.EditorRegistry;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.wizards.ExportWizard;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;

/**
 * Erlaubt das gemeinsame Editieren der Eigenschaften von gleichen,
 * ausgewähltne Objekten.
 * 
 * @author koderman@sernet.de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov 2007) $ 
 * $LastChangedBy: koderman $
 *
 */
public class ShowBulkEditAction extends Action implements ISelectionListener {
	
	public static final String ID = "sernet.gs.ui.rcp.main.actions.showbulkeditaction";
	private final IWorkbenchWindow window;
	
	public ShowBulkEditAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
		setId(ID);
		setActionDefinitionId(ID);
		setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.CASCADE));
		window.getSelectionService().addSelectionListener(this);
		setToolTipText("Gleichartige Elemente gemeinsam editieren.");
	}
	
	
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) 
			window.getSelectionService().getSelection();
		if (selection == null)
			return;
		final ArrayList<CnATreeElement> selectedElements = new ArrayList<CnATreeElement>();
		EntityType entType=null;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (! (o instanceof CnATreeElement))
				continue;
			CnATreeElement elmt = (CnATreeElement) o;
			entType = HUITypeFactory.getInstance().getEntityType(elmt.getEntity()
					.getEntityType());
			selectedElements.add( elmt);
			Logger.getLogger(this.getClass()).debug("Adding to bulk edit: "
					+ elmt.getTitle());
		}

		final BulkEditDialog dialog = new BulkEditDialog(window.getShell(), entType);
		if (dialog.open() != InputDialog.OK)
			return;
		
		try {
			// close editors first:
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().closeAllEditors(true /*ask save*/);
			
			PlatformUI.getWorkbench().getProgressService().
				busyCursorWhile(new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.setTaskName("Setze veränderte Werte...");
						monitor.beginTask("Bulk Edit", selectedElements.size()+1);
						// for every target:
						for (CnATreeElement elmt : selectedElements) {
							// set values:
							Entity editEntity = elmt.getEntity();
							editEntity.copyEntity(dialog.getEntity());
							monitor.worked(1);
						}
						try {
							monitor.setTaskName("Speichere veränderte Werte...");
							monitor.beginTask("Speichere veränderte Werte...", IProgressMonitor.UNKNOWN);
							CnAElementHome.getInstance().update(selectedElements);
						} 
						catch (Exception e) {
							ExceptionUtil.log(e, "Elemente konnten nicht gespeichert werden.");
						}
						monitor.done();
						// update once when finished:
						CnAElementFactory.getCurrentModel().refreshAllListeners();
						updateSchutzbedarf(selectedElements);
					}
			});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Error executing bulk edit.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Aborted.");
		}
	}

	


	protected void updateSchutzbedarf(ArrayList<CnATreeElement> selectedElements) {
		// description may have been set to "Maximumprinzip"
		for (CnATreeElement element : selectedElements) {
			element.getLinkChangeListener().integritaetChanged();
			element.getLinkChangeListener().verfuegbarkeitChanged();
			element.getLinkChangeListener().vertraulichkeitChanged();
		}
		
		// schutzbedarf may have been changed manually, update dependencies:
		for (CnATreeElement element : selectedElements) {
			if (element.isSchutzbedarfProvider())
				element.getSchutzbedarfProvider().updateAll();
		}
	}


	/**
	 * Action is enabled when only items of the same type are selected.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection input) {
		if (input instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) input;
			if (selection.size() > 0 
					&& selection.getFirstElement() instanceof CnATreeElement
					&& ((CnATreeElement) selection.getFirstElement()).getEntity() != null) {
				CnATreeElement elmt = (CnATreeElement) selection.getFirstElement();
				String type = elmt.getEntity().getEntityType();
				EntityType entType = HUITypeFactory.getInstance().getEntityType(type);
				
				for (Iterator iter = selection.iterator(); iter.hasNext();) {
					Object o = iter.next();
					if (! (o instanceof CnATreeElement)) {
						setEnabled(false);
						return;
					}
					elmt = (CnATreeElement) o;
					if (elmt.getEntity() == null
							|| !elmt.getEntity().getEntityType().equals(type)) {
						setEnabled(false);
						return;
					}
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
}
