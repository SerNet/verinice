package sernet.verinice.bpm.isam;

import org.apache.log4j.Logger;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;

public class ExecuteHandler implements AssignmentHandler {

    private static final Logger LOG = Logger.getLogger(ExecuteHandler.class);
    
    @Override
    public void assign(Assignable arg0, OpenExecution execution) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Execute task assigned...");
        }
    }

}
