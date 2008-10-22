package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;

import sernet.gs.ui.rcp.main.ImageCache;

public class ManageUpdatesAction extends Action implements IAction {
	private IWorkbenchWindow window;
	
	public ManageUpdatesAction(IWorkbenchWindow window) {
		this.window = window;
		setId("sernet.gs.ui.rcp.main.actions.manageupdatesaction");
		setText("Installierte Updates anzeigen...");
		setToolTipText("Anzeigen / Bearbeiten installierter Programmbestandteile");
		
	}
	
	@Override
	public void run() {
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				UpdateManagerUI.openConfigurationManager(window.getShell());
			}
		});
	}
}
