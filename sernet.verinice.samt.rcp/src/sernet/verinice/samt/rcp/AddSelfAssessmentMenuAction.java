package sernet.verinice.samt.rcp;
import java.io.IOException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.samt.service.CreateSelfAssessment;


public class AddSelfAssessmentMenuAction implements IWorkbenchWindowActionDelegate {

    public static final String ID = "sernet.verinice.samt.rcp.AddSelfAssessmentMenuAction"; //$NON-NLS-1$
    
    @Override
    public void dispose() {
        // do nothing
    }

    @Override
    public void init(IWorkbenchWindow window) {
        // do nothing
    }

    @Override
    public void run(IAction action) {
        CreateNewSelfAssessmentService createSamtService = new CreateNewSelfAssessmentService();
        try {
            createSamtService.createSelfAssessment();
        } catch (CommandException e) {
            ExceptionUtil.log(e, Messages.AddSelfAssessmentMenuAction_1);
        } catch (IOException e) {
            ExceptionUtil.log(e, Messages.AddSelfAssessmentMenuAction_2);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

}


