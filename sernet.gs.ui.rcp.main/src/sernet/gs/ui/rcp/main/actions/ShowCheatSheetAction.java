package sernet.gs.ui.rcp.main.actions;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.Application;
import sernet.gs.ui.rcp.main.ICommandIds;
import sernet.gs.ui.rcp.main.ImageCache;


public class ShowCheatSheetAction extends Action {
	
	private boolean tutorial;
	private static final String ID1 = "sernet.gs.ui.rcp.main.showcheatsheetaction";
	private static final String ID2 = "sernet.gs.ui.rcp.main.showcheatsheetlistaction";
	
	public ShowCheatSheetAction(boolean tutorial, String title) {
        setText(title);
        if (tutorial)
        	setId(ID1);
        else
        	setId(ID2);
		this.tutorial = tutorial;
	}
	
	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window != null) {	
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()[0]
				           .showView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
				IViewPart part = window.getActivePage()
					.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
				if (part != null && tutorial) {
					CheatSheetView view = (CheatSheetView) part;
					view.setInput("sernet.gs.ui.rcp.main.cheatsheet1");
				}
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", 
						"Error opening view: " 
						+ e.getMessage());
			}
		}
	}
}
