package sernet.gs.ui.rcp.main.bsi.risikoanalyse.actions;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Starts the wizard for Risikoanalyse.
 * 
 * @author ahanekop@sernet.de
 *
 */
public class NeueRisikoanalyseAction implements IObjectActionDelegate{
	private IWorkbenchPart targetPart;
	
	public NeueRisikoanalyseAction()  {
		// TODO Auto-generated method stub
	}
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		Object sel = ((IStructuredSelection)targetPart.getSite()
				.getSelectionProvider().getSelection()).getFirstElement();
		if (!(sel instanceof IBSIStrukturElement)) {
			return;
		}
		CnATreeElement struktElement = (CnATreeElement) sel;
		
		Shell shell = new Shell();
		RiskAnalysisWizard wizard =  new RiskAnalysisWizard(struktElement);
    	wizard.init(PlatformUI.getWorkbench(), null);
    	WizardDialog wizDialog =  new org.eclipse.jface.wizard.WizardDialog(shell, wizard);
    	wizDialog.setPageSize(800, 600);
    	wizDialog.open();
    	
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

}
