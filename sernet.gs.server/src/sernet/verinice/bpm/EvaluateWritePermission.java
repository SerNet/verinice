package sernet.verinice.bpm;

import org.jbpm.api.jpdl.DecisionHandler;
import org.jbpm.api.model.OpenExecution;

import sernet.verinice.interfaces.bpm.IExecutionProcess;
import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;

@SuppressWarnings("serial")
public class EvaluateWritePermission implements DecisionHandler {

    @Override
    public String decide(OpenExecution execution) {
        String transition;
        String writePermission = (String) execution.getVariable(IIsaExecutionProcess.VAR_WRITE_PERMISSION);
        boolean isWritePermission = Boolean.TRUE.toString().equals(writePermission);
        if(isWritePermission) {
            transition = IIsaExecutionProcess.TRANSITION_IS_WRITE_PERMISSION;
        } else {
            transition = IIsaExecutionProcess.TRANSITION_NO_WRITE_PERMISSION;
            execution.setVariable(IExecutionProcess.VAR_TASK_READ_STATUS, IExecutionProcess.TASK_UNREAD);
        }
        return transition;
    }

}
