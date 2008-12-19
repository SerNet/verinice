package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.WorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ICommandIds;
import sernet.gs.ui.rcp.main.ImageCache;


public class OpenMultipleViewAction extends Action {
	
	private final IWorkbenchWindow window;
	private final String viewId;
	private int instance = 0;
	
	public OpenMultipleViewAction(IWorkbenchWindow window, String label, String viewId, String imageDesc) {
		this.window = window;
		this.viewId = viewId;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
        
		setId("ACTION_" + viewId);
        // Associate the action with a pre-defined command, to allow key bindings.
		// FIXME add command ids for each view opened using this action
		//setActionDefinitionId(ICommandIds.CMD_OPEN);
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(imageDesc));
	}
	
	public void run() {
		if(window != null) {	
			try {
				window.getActivePage().showView(viewId, "" + instance++, IWorkbenchPage.VIEW_CREATE);
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening view: " 
						+ e.getMessage());
			}
		}
	}
}
