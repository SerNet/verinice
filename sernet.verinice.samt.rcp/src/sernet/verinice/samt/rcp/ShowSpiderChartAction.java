package sernet.verinice.samt.rcp;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.RightEnabledUserInteraction;

public class ShowSpiderChartAction extends ShowSomeViewAction
        implements IViewActionDelegate, RightEnabledUserInteraction {

    @Override
    protected String getViewId() {
        return SpiderChartView.ID;
    }

    @Override
    public void init(final IAction action) {
        if (Activator.getDefault().isStandalone()
                && !Activator.getDefault().getInternalServer().isRunning()) {
            IInternalServerStartListener listener = e -> {
                if (e.isStarted()) {
                    action.setEnabled(checkRights());
                }
            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            action.setEnabled(checkRights());
        }
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient) VeriniceContext
                .get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.SHOWCHARTVIEW;
    }

    /*
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart arg0) {
        // no-op

    }
}
