//Neu hinzugefügt vom Projektteam: XML import

package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import sernet.gs.ui.rcp.main.bsi.dialogs.XMLImportDialog;

public class ImportXMLAction extends Action {
	
	public static final String ID = "sernet.gs.ui.rcp.main.importxmlaction";
	
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
}

