package sernet.gs.ui.rcp.main.bsi.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Open-Action for BSI objects
 * 
 * @author koderman@sernet.de
 *
 */
public class OpenAction implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		
		Object sel = ((IStructuredSelection)targetPart.getSite()
				.getSelectionProvider().getSelection()).getFirstElement();
		
		if (sel instanceof CnATreeElement) {
			EditorFactory.getInstance().openEditor(sel);
		}
	
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

}
