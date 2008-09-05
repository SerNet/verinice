package sernet.gs.ui.rcp.main.bsi.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Delete items on user request.
 * 
 * @author akoderman@sernet.de
 * 
 */
public class DeleteActionDelegate implements IObjectActionDelegate {

	private IWorkbenchPart targetPart;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {

		if (!MessageDialog.openQuestion((Shell) targetPart
				.getAdapter(Shell.class), "Wirklich löschen?",
				"Alle markierten Elemente werden entfernt. Vorsicht: diese Operation "
						+ "kann nicht rückgängig gemacht werden!\n\n"
						+ "Wirklich löschen?")) {
			return;
		}

		final IStructuredSelection selection = ((IStructuredSelection) targetPart
				.getSite().getSelectionProvider().getSelection());

		try {
			PlatformUI.getWorkbench().getProgressService().
			busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Lösche Objekte", selection.size());
					for (Iterator iter = selection.iterator(); iter.hasNext();) {
						Object sel = (Object) iter.next();
						
						if (!(sel instanceof ITVerbund)
								&& sel instanceof IBSIStrukturElement
								|| sel instanceof BausteinUmsetzung
								|| sel instanceof FinishedRiskAnalysis
								|| sel instanceof GefaehrdungsUmsetzung) {
							CnATreeElement el = (CnATreeElement) sel;
							
							try {
								monitor.setTaskName("Lösche: " + el.getTitel());
								monitor.worked(1);
								el.remove();
								CnAElementHome.getInstance().remove(el);
								
							} catch (Exception e) {
								ExceptionUtil.log(e, "Fehler beim Löschen von Element.");
							}
							
						}
					}
				}
			});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Error while deleting object.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Interrupted: Delete objects.");
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

}
