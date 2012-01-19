package sernet.verinice.bpm;

import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;

import sernet.verinice.interfaces.bpm.IExecutionProcess;

public class UnreadHandler implements AssignmentHandler {

    @Override
    public void assign(Assignable arg0, OpenExecution execution) throws Exception {
        execution.setVariable(IExecutionProcess.VAR_TASK_READ_STATUS, IExecutionProcess.TASK_UNREAD);
    }

}
