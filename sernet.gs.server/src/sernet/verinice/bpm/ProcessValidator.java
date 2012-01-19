package sernet.verinice.bpm;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jbpm.pvm.internal.model.ExecutionImpl;

import sernet.verinice.hibernate.HibernateDao;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.bpm.IProcessService;
import sernet.verinice.model.common.CnATreeElement;


public class ProcessValidator implements IProcessCreater {

    private final Logger log = Logger.getLogger(ProcessValidator.class);
    
    private IProcessService processService;
    
    private HibernateDao<ExecutionImpl, Integer> jbpmExecutionDao;
    
    private IBaseDao<CnATreeElement,Integer> elementDao;
    
    @SuppressWarnings("unchecked")
    @Override
    public void create() {
        /*
         * Auslesen der execution dbids ohne verinice element
         * SELECT jbpm4_variable.execution_,cnatreeelement.dbid FROM jbpm4_variable
         * LEFT OUTER JOIN cnatreeelement on jbpm4_variable.string_value_=cnatreeelement.uuid
         * WHERE jbpm4_variable.key_='UUID'
         * AND dbid IS NULL
         */
        String hqlVar = "SELECT var.execution.id,var.execution.dbid,var.string FROM org.jbpm.pvm.internal.type.Variable AS var " +
        		"WHERE var.key = 'UUID'";
        List result = getJbpmExecutionDao().findByQuery(hqlVar, new Object[]{});
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            String id = (String) row[0];
            Long processId = (Long) row[1];
            String uuid = (String) row[2];
            String hqlElement = "SELECT element.dbId FROM sernet.verinice.model.common.CnATreeElement as element " +
            		"WHERE element.uuid = ?";
            List resultElement = getJbpmExecutionDao().findByQuery(hqlElement, new Object[]{uuid});
            if(resultElement==null || resultElement.isEmpty()) {              
                if (log.isDebugEnabled()) {
                    log.debug("Uuid not found: " + uuid + ". Deleting process with id: " + processId);
                }
                deleteProcess(id);
            }
        }
        
    }

    private void deleteProcess(String processId) {
        getProcessService().deleteProcess(processId);      
    }

    public IProcessService getProcessService() {
        return processService;
    }

    public void setProcessService(IProcessService processService) {
        this.processService = processService;
    }

    public HibernateDao<ExecutionImpl, Integer> getJbpmExecutionDao() {
        return jbpmExecutionDao;
    }

    public void setJbpmExecutionDao(HibernateDao<ExecutionImpl, Integer> jbpmExecutionDao) {
        this.jbpmExecutionDao = jbpmExecutionDao;
    }

    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }

}
