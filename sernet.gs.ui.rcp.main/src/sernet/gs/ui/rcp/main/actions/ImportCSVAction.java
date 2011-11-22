package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.wizards.ImportCSVWizard;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;

public class ImportCSVAction extends RightsEnabledAction {
	public static final String ID = "sernet.gs.ui.rcp.main.importcsvaction";
	public ImportCSVAction(IWorkbenchWindow window, String label) {
        setText(label);
		setId(ID);
		setRightID(ActionRightIDs.IMPORTCSV);
		if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
		    IInternalServerStartListener listener = new IInternalServerStartListener(){
		        @Override
		        public void statusChanged(InternalServerEvent e) {
		            if(e.isStarted()){
		                setEnabled(checkRights());
		            }
		        }

		    };
		    Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }
	}

	public void run() {
	    //Display.getCurrent().getActiveShell()
		final WizardDialog wizard = new WizardDialog(Display.getCurrent().getActiveShell(), new ImportCSVWizard());
		wizard.open();
	}
}
