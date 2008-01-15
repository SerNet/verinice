package sernet.gs.ui.rcp.main.bsi.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class AddSonstItActionDelegate implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		
		try {
			Object sel = ((IStructuredSelection)targetPart.getSite()
					.getSelectionProvider().getSelection()).getFirstElement();
			CnATreeElement newElement=null;
			if (sel instanceof SonstigeITKategorie) {
				CnATreeElement cont = (CnATreeElement) sel;
				newElement = CnAElementFactory.getInstance()
					.saveNew(cont, SonstIT.TYPE_ID, null);
			}
			if (newElement != null)
				EditorFactory.getInstance().openEditor(newElement);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Konnte Objekt nicht hinzufügen.");
		}
	
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
