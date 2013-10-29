package sernet.verinice.bpm;

import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;

import sernet.verinice.interfaces.bpm.IGenericProcess;

public class UnreadHandler implements AssignmentHandler {

    @Override
    public void assign(Assignable arg0, OpenExecution execution) throws Exception {
        execution.setVariable(IGenericProcess.VAR_TASK_READ_STATUS, IGenericProcess.TASK_UNREAD);
    }

}
