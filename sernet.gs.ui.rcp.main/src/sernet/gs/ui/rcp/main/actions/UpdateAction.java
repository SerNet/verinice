package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;

import sernet.gs.ui.rcp.main.ImageCache;

public class UpdateAction extends Action implements IAction {
	private IWorkbenchWindow window;
	
	public UpdateAction(IWorkbenchWindow window) {
		this.window = window;
		setId("sernet.gs.ui.rcp.main.actions.updateaction");
		setText("Suche nach neuen &Updates...");
		setToolTipText("Suche nach neuen Updates für Verinice");
		
	}
	
	@Override
	public void run() {
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				UpdateJob job = new UpdateJob("Suche Verinice-Updates", false, false);
				UpdateManagerUI.openInstaller(window.getShell(), job);
			}
		});
	}
}
