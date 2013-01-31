package sernet.verinice.bpm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbpm.pvm.internal.model.ExecutionImpl;

import sernet.verinice.interfaces.bpm.IProcessServiceIsa;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.bpm.ProcessInformation;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Empty dummy implementation of {@link IProcessServiceIsa}
 * for verinice.PRO with no process features.
 * 
 * See {@link ProcessServiceIsa} for the real implementation.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessServiceDummy implements IProcessServiceIsa {

    @Override
    public void deleteProcess(String id) {
    }

    @Override
    public List<ExecutionImpl> findControlExecution(String uuidControl) {
        return Collections.emptyList();
    }

    @Override
    public List<ExecutionImpl> findIsaExecution(String uuidSamtTopic) {
        return Collections.emptyList();
    }

    @Override
    public String findProcessDefinitionId(String processDefinitionKey) {
        return null;
    }

    @Override
    public void handleControl(Control control) {
    }

    @Override
    public void handleSamtTopic(SamtTopic control) {
    }

    @Override
    public void startProcess(String processDefinitionKey, Map<String, ?> variables) {
    }

    @Override
    public IProcessStartInformation startProcessForIsa(String uuidAudit) {
        return new ProcessInformation(0);
    }

    /**
     * False, because this is not a real implementation.
     * 
     * @see sernet.verinice.interfaces.bpm.IProcessServiceIsa#isActive()
     */
    @Override
    public boolean isActive() {
        return false;
    }

}
