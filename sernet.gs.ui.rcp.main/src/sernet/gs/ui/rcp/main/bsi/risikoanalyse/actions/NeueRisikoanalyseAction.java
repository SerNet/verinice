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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.actions;

import java.lang.reflect.InvocationTargetException;

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

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * Starts the wizard for a risk analysis according to
 * BSI-Standard 100-3.
 * 
 * @author ahanekop[at]sernet[dot]de
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
		Activator.inheritVeriniceContextState();

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
							Activator.inheritVeriniceContextState();

							monitor.beginTask("Öffne wizard...", 1);
							
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									Activator.inheritVeriniceContextState();

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
	public void selectionChanged(IAction action, ISelection selection) {
		// Realizes that the action to create a new risk analysis is greyed out,
		// when there is no right to do so.
		Object sel = ((IStructuredSelection) selection).getFirstElement();
		
		
		if (sel instanceof IBSIStrukturKategorie) {
			// Risk analysis should not work on category instances.
			action.setEnabled(false);
		} else if (sel instanceof IISO27kElement ) {
			action.setEnabled(false);
		} else if (sel instanceof CnATreeElement ) {
			// To make a risk analysis one needs write permission for the object in question.
			boolean b = CnAElementHome
				.getInstance()
				.isWriteAllowed((CnATreeElement) sel);
			
			// Only change state when it is enabled, since we do not want to
			// trash the enablement settings of plugin.xml
			if (action.isEnabled())
				action.setEnabled(b);
		}
	}
}
