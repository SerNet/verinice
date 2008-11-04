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
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
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

		final IStructuredSelection selection = ((IStructuredSelection) targetPart
				.getSite().getSelectionProvider().getSelection());

		if (!MessageDialog
				.openQuestion(
						(Shell) targetPart.getAdapter(Shell.class),
						"Wirklich löschen?",
						"Alle "
								+ selection.size()
								+ " markierten Elemente werden entfernt. Vorsicht: diese Operation "
								+ "kann nicht rückgängig gemacht werden!\n\n"
								+ "Wirklich löschen?")) {
			return;
		}

		// ask twice if IT verbund
		boolean goahead = true;
		boolean skipQuestion = false;
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			if (iterator.next() instanceof ITVerbund) {
				if (!goahead)
					return;
				
				if (!MessageDialog
						.openQuestion(
								(Shell) targetPart.getAdapter(Shell.class),
								"IT-Verbund wirklich löschen?",
								"Sie haben einen IT-Verbund zum Löschen markiert. "
										+ "Das wird alle darin enthaltenen Objekte entfernen "
										+ "(Server, Clients, Personen...)\n\n"
										+ "Wirklich wirklich wirklich löschen?")) {
					skipQuestion = true;
					goahead = false;
					return;
				}
				else {
					skipQuestion = true;
				}
			}
		}

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							monitor.beginTask("Lösche Objekte", selection
									.size());
							for (Iterator iter = selection.iterator(); iter
									.hasNext();) {
								Object sel = (Object) iter.next();

								if (sel instanceof IBSIStrukturElement
										|| sel instanceof BausteinUmsetzung
										|| sel instanceof FinishedRiskAnalysis
										|| sel instanceof GefaehrdungsUmsetzung
										|| sel instanceof ITVerbund) {

									// do not delete last ITVerbund:
									if (sel instanceof ITVerbund
											&& CnAElementFactory
													.getCurrentModel()
													.getItverbuende().size() < 2) {
										return;
									}

									CnATreeElement el = (CnATreeElement) sel;

									try {
										monitor.setTaskName("Lösche: "
												+ el.getTitel());
										monitor.worked(1);
										el.remove();
										CnAElementHome.getInstance().remove(el);

									} catch (Exception e) {
										ExceptionUtil
												.log(e,
														"Fehler beim Löschen von Element.");
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
