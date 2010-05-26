package sernet.verinice.samt.rcp;

import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import sernet.gs.ui.rcp.main.actions.Messages;

public class ShowSamtViewAction extends ShowSomeViewAction  {

    /**
     * @return
     */
    @Override
    protected String getViewId() {
        return SamtView.ID;
    }

}


