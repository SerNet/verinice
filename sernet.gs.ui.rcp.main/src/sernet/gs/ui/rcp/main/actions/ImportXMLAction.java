//Neu hinzugefügt vom Projektteam: XML import

package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.bsi.dialogs.XMLImportDialog;

public class ImportXMLAction extends Action implements IViewActionDelegate {
	
	public static final String ID = "sernet.gs.ui.rcp.main.importxmlaction";
	
	public ImportXMLAction() {
        setText("Import...");
        setId(ID);
        setEnabled(true);
    }
	
	public ImportXMLAction(IWorkbenchWindow window, String label) {
        setText(label);
		setId(ID);
		setEnabled(true);
	}

	public void run() {
		final XMLImportDialog dialog = new XMLImportDialog(Display.getCurrent().getActiveShell());	
        if (dialog.open() != Window.OK) {
            return;
        }
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        run();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {      
    }

    

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) { 
    }
	
}

