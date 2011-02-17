package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.bsi.dialogs.XMLImportDialog;
import sernet.gs.ui.rcp.main.bsi.wizards.ImportCSVWizard;

public class ImportCSVAction extends Action{
	public static final String ID = "sernet.gs.ui.rcp.main.importcsvaction";
	public ImportCSVAction(IWorkbenchWindow window, String label) {
        setText(label);
		setId(ID);
		setEnabled(true);
	}

	public void run() {
	    //Display.getCurrent().getActiveShell()
		final WizardDialog wizard = new WizardDialog(Display.getCurrent().getActiveShell(), new ImportCSVWizard());
		wizard.open();
	}
}
