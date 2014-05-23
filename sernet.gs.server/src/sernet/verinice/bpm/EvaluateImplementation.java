package sernet.verinice.bpm;

import org.jbpm.api.jpdl.DecisionHandler;
import org.jbpm.api.model.OpenExecution;

import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.model.samt.SamtTopic;

@SuppressWarnings("serial")
public class EvaluateImplementation implements DecisionHandler {

    @Override
    public String decide(OpenExecution execution) {
        String transition;
        Object value = execution.getVariable(IGenericProcess.VAR_IMPLEMENTATION);
        Integer implementation = null;
        if(value instanceof Integer) {
            implementation = (Integer) value;
        }
        if(value instanceof String) {
            implementation = Integer.valueOf((String) value);
        }
        boolean isImplemented = implementation!=null 
           && !(implementation.intValue()==SamtTopic.IMPLEMENTED_NOTEDITED_NUMERIC);
        if(isImplemented) {
            transition = IGenericProcess.TRANSITION_IMPLEMENTED;
        } else {
            transition = IGenericProcess.TRANSITION_NOT_IMPLEMENTED;           
        }
        execution.setVariable(IGenericProcess.VAR_TASK_READ_STATUS, IGenericProcess.TASK_UNREAD);      
        return transition;
    }

}
