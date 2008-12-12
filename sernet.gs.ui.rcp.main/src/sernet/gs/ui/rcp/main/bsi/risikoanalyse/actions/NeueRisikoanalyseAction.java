package sernet.gs.ui.rcp.main.bsi.risikoanalyse.actions;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Starts the wizard for a risk analysis according to
 * BSI-Standard 100-3.
 * 
 * @author ahanekop@sernet.de
 */
public class NeueRisikoanalyseAction implements IObjectActionDelegate {
	
	private IWorkbenchPart targetPart;
	
	/**
	 * Sets the active part for the delegate.
	 * This method will be called every time the action appears in a popup menu.
	 * 
	 * @param action the action proxy that handles presentation portion of the
	 *        action; must not be null.
	 * @param newTargetPart the new part target; must not be null.
	 */
	public void setActivePart(IAction newAction, IWorkbenchPart newTargetPart) {
		targetPart = newTargetPart;
	}

	/**
	 * This method is called by the proxy action when the action has been
     * triggered. It opens the risk analysis wizard.
	 */
	public void run(IAction action) {
		
		final Object selection = ((IStructuredSelection) targetPart.getSite()
				.getSelectionProvider().getSelection()).getFirstElement();
		
		if (!(selection instanceof IBSIStrukturElement)) {
			return;
		}
		
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							monitor.beginTask("Öffne wizard...", 1);
							
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									CnATreeElement element = (CnATreeElement) selection;
									Shell shell = new Shell();
									RiskAnalysisWizard wizard = new RiskAnalysisWizard(element);
									wizard.init(PlatformUI.getWorkbench(), null);
									WizardDialog wizardDialog = new org.eclipse.jface.wizard.WizardDialog(
											shell, wizard);
									wizardDialog.setPageSize(800, 600);
									wizardDialog.open();
								}
							});
						}
					});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Fehler beim Öffnen der Risikoanalyse.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Fehler beim Öffnen der Risikoanalyse.");
		}
	}

	/**
	 * Not used.
	 * Must be implemented due to IActionDelegate.
	 * 
     * @param action the action proxy that handles presentation portion of 
     * 		the action
     * @param selection the current selection, or <code>null</code> if there
     * 		is no selection.
     */
	public void selectionChanged(IAction action, ISelection selection) {}
}
