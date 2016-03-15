package sernet.verinice.report.actions;

import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.report.rcp.GenerateReportDialog;

public class AuditReportAction extends ReportAction implements IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {
    
    @Override
    protected void openDialog() {
        if(rootObjects.size() == 1){
            dialog = new GenerateReportDialog(shell, rootObjects.get(0));
        } else {
            dialog = new GenerateReportDialog(shell, rootObjects, IReportType.USE_CASE_ID_AUDIT_REPORT);
        }
        dialog.setContextMenuCall(true);
    }
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.GENERATEAUDITREPORT;
    }

}


