package sernet.verinice.bpm;

import java.util.Collections;
import java.util.List;

import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.iso27k.Audit;

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
    public void completeTask(String taskId) {
    }

    @Override
    public void completeTask(String taskId, String outcomeId) {
    }

    @Override
    public List<Audit> getAuditList() {
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

}
