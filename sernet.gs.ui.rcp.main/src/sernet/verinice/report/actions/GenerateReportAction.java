package sernet.verinice.report.actions;

import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.report.rcp.GenerateReportDialog;

public class GenerateReportAction extends ReportAction implements IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {

    @Override
    protected void openDialog() {
        // reportgeneration called from applicationactionbar
        if(!isContextMenuCall() || rootObjects == null || rootObjects.size() == 0){
            dialog = new GenerateReportDialog(shell);
        }
        // reportgeneration called from contextMenu
        else if(rootObjects.size() == 1 && isContextMenuCall()){
            dialog = new GenerateReportDialog(shell, rootObjects.get(0));
        // special case, more than one root-object / scope for reportgeneration selected
        } else if(rootObjects != null && rootObjects.size() > 1 && isContextMenuCall()){
            dialog = new GenerateReportDialog(shell, rootObjects, IReportType.USE_CASE_ID_GENERAL_REPORT);
        }
        dialog.setContextMenuCall(isContextMenuCall());
    }

}
