package sernet.verinice.samt.rcp;

import org.eclipse.jface.action.IAction;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.ActionRightIDs;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ShowSamtViewAction extends ShowSomeViewAction implements IViewActionDelegate, RightEnabledUserInteraction {

    @Override
    public void init(final IAction action){
        if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
                        action.setEnabled(checkRights());
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            action.setEnabled(checkRights());
        }
    }
    
    /**
     * @return
     */
    @Override
    protected String getViewId() {
        return SamtView.ID;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.SAMTVIEW;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart arg0) {
        // TODO Auto-generated method stub
        
    }

}


