package sernet.verinice.bpm.rcp;

import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.OpenViewAction;
import sernet.verinice.interfaces.ActionRightIDs;

public class OpenTaskViewAction extends OpenViewAction {

    public OpenTaskViewAction(IWorkbenchWindow window) {
        this(window, ActionRightIDs.TASKVIEW);
    }

    public OpenTaskViewAction(IWorkbenchWindow window, String rightID) {
        super(window, Messages.OpenTaskViewAction_0, TaskView.ID, ImageCache.VIEW_TASK, rightID);

        setEnabled(!Activator.getDefault().isStandalone());

    }
}
