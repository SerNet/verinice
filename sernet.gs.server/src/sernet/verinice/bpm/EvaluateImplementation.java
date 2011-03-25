package sernet.verinice.bpm;

import org.jbpm.api.jpdl.DecisionHandler;
import org.jbpm.api.model.OpenExecution;

import sernet.verinice.interfaces.bpm.IExecutionProcess;
import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;
import sernet.verinice.model.samt.SamtTopic;

@SuppressWarnings("serial")
public class EvaluateImplementation implements DecisionHandler {

    @Override
    public String decide(OpenExecution execution) {
        String transition;
        String implementation = (String) execution.getVariable(IIsaExecutionProcess.VAR_IMPLEMENTATION);
        boolean isImplemented = implementation!=null 
           && !implementation.equals(String.valueOf(SamtTopic.IMPLEMENTED_NOTEDITED_NUMERIC));
        if(isImplemented) {
            transition = IIsaExecutionProcess.TRANSITION_IMPLEMENTED;
        } else {
            transition = IIsaExecutionProcess.TRANSITION_NOT_IMPLEMENTED;           
        }
        execution.setVariable(IExecutionProcess.VAR_TASK_READ_STATUS, IExecutionProcess.TASK_UNREAD);      
        return transition;
    }

}
