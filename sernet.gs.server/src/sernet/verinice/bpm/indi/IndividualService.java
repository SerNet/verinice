/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.indi;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sernet.verinice.bpm.ProcessServiceVerinice;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.IIndividualService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.model.bpm.ProcessInformation;

/**
 * Process service to create and handle individual tasks
 * defined in jBPM definition: individual-task.jpdl.xml
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndividualService extends ProcessServiceVerinice implements IIndividualService {

    private IAuthService authService;
    
    public IndividualService() {
        super();
        // this is not the main process service:
        setWasInitCalled(true);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIndividualService#startProcess(sernet.verinice.interfaces.bpm.IndividualServiceParameter)
     */
    @Override
    public IProcessStartInformation startProcess(IndividualServiceParameter parameter) {
        Map<String, ?> parameterMap = createParameterMap(parameter);
        startProcess(IIndividualProcess.KEY, parameterMap);
        return new ProcessInformation(1);
    }

    /**
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> createParameterMap(IndividualServiceParameter parameter) {
        final int maxDescriptionLength = 254;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(IGenericProcess.VAR_UUID, parameter.getUuid());
        map.put(IGenericProcess.VAR_AUDIT_UUID, loadScopeUuid(parameter.getUuid()));
        map.put(IGenericProcess.VAR_ASSIGNEE_NAME, parameter.getAssignee());
        map.put(IIndividualProcess.VAR_RELATION_ID, parameter.getAssigneeRelationId());
        map.put(IGenericProcess.VAR_DUEDATE, parameter.getDueDate()); 
        map.put(IIndividualProcess.VAR_REMINDER_DAYS, parameter.getReminderPeriodDays());
        map.put(IIndividualProcess.VAR_REMINDER_DATE, getReminderDate(parameter));
        map.put(IGenericProcess.VAR_OWNER_NAME, getAuthService().getUsername());
        String description = parameter.getDescription();
        if(description!=null) {
            if(description.length()>maxDescriptionLength) {
                map.put(IIndividualProcess.VAR_DESCRIPTION, description.toCharArray());
            } else {
                map.put(IIndividualProcess.VAR_DESCRIPTION, description);
            }
        }
        map.put(IIndividualProcess.VAR_TITLE, parameter.getTitle());
        map.put(IGenericProcess.VAR_TYPE_ID, parameter.getTypeId());
        map.put(IIndividualProcess.VAR_PROPERTY_TYPES, parameter.getProperties());
        map.put(IIndividualProcess.VAR_IS_WITH_RELEASE_PROCESS, parameter.isWithAReleaseProcess());
        return map;
    }


    private Object loadScopeUuid(String elementUuid) {
        String scopeUuid = null;
        Integer scopeDbId = loadScopeDbId(elementUuid);
        if(scopeDbId!=null) {
            scopeUuid = loadUuid(scopeDbId);
        }
        return scopeUuid;
    }
    
    private Integer loadScopeDbId(String uuid) {
        if(uuid==null) {
            return null;
        }
        Integer scopeDbId = null;
        List<Integer> scopeIdList = getElementDao().findByQuery("select e.scopeId from CnATreeElement e where e.uuid = ?", new Object[]{uuid});
        if(!scopeIdList.isEmpty()) {
            scopeDbId = scopeIdList.get(0);
        }
        return scopeDbId;
    }

    private String loadUuid(Integer dbId) {
        if(dbId==null) {
            return null;
        }
        String scopeUuid = null;
        List<String> uuidList = getElementDao().findByQuery("select e.uuid from CnATreeElement e where e.dbId = ?", new Object[]{dbId});
        if(!uuidList.isEmpty()) {
            scopeUuid = uuidList.get(0);
        }
        return scopeUuid;
    }

    /**
     * @param parameter
     */
    private Date getReminderDate(IndividualServiceParameter parameter) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(parameter.getDueDate());
        cal.add(Calendar.DAY_OF_MONTH, -1*parameter.getReminderPeriodDays());  
        return cal.getTime();
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

}
