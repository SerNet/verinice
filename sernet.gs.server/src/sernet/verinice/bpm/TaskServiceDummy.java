package sernet.verinice.bpm;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;

/**
 * Empty dummy implementation of {@link ITaskService}
 * for verinice.PRO with no process features.
 * 
 * See {@link TaskService} for the real implementation.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskServiceDummy implements ITaskService {

    @Override
    public void completeTask(String taskId) {}

    @Override
    public void completeTask(String taskId, String outcomeId) {}
    
    @Override
    public void completeTask(String taskId, Map<String, Object> parameter) {}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#completeTask(java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public void completeTask(String taskId, String outcomeId, Map<String, Object> parameter) {}

    @Override
    public List<String> getElementList() {
        return Collections.emptyList();
    }

    @Override
    public List<ITask> getTaskList() {
        return Collections.emptyList();
    }

    @Override
    public List<ITask> getTaskList(ITaskParameter parameter) {
        return Collections.emptyList();
    }

    @Override
    public void markAsRead(String taskId) {
    }

    /**
     * False, because this is not a real implementation.
     * 
     * @see sernet.verinice.interfaces.bpm.ITaskService#isActive()
     */
    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void cancelTask(String taskId) {}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#setAssignee(java.lang.String, java.lang.String)
     */
    @Override
    public void setAssignee(Set<String> taskIdset, String username) {}
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#setAssignee(java.lang.String, java.lang.String)
     */
    @Override
    public void setAssigneeVar(Set<String> taskIdset, String username) {}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#setVariables(java.lang.String, java.util.Map)
     */
    @Override
    public void setVariables(String taskId, Map<String, Object> param) {}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#getVariables(java.lang.String)
     */
    @Override
    public Map<String, Object> getVariables(String taskId) {      
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#loadTaskDescription(java.lang.String, java.util.Map)
     */
    @Override
    public String loadTaskDescription(String taskId, Map<String, Object> varMap) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#loadTaskTitle(java.lang.String, java.util.Map)
     */
    @Override
    public String loadTaskTitle(String taskId, Map<String, Object> varMap) {
        return null;
    }

}
