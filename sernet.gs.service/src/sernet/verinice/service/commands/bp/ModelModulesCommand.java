/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyCommand;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelModulesCommand extends ChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(ModelModulesCommand.class);

    private List<String> targetUuids;
    private transient List<BpRequirementGroup> modules;
    
    private String stationId;
    
    public ModelModulesCommand(List<BpRequirementGroup> modules, List<String> targetUuids) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        this.modules = modules;
        this.targetUuids = targetUuids;
    }
    
    @Override
    public void execute() {
        try {
            handleModules();         
        } catch (CommandException e) {
            getLog().error("Error while modeling.", e);
            throw new RuntimeCommandException("Error while modeling.", e);
        }
    }

    private void handleModules() throws CommandException {
        for (String targetUuid : targetUuids) {
            CnATreeElement target = loadElementWithChildren(targetUuid);
            List<String> missingUuids = createListOfMssingUuids(target);
            if(!missingUuids.isEmpty()) {
                CopyCommand copyCommand = new CopyCommand(targetUuid, missingUuids);
                copyCommand = getCommandService().executeCommand(copyCommand);
            }
        }       
    }
    
 
    private List<String> createListOfMssingUuids(CnATreeElement targetWithChildren) {
        List<String> uuids = new LinkedList<>();
        Set<CnATreeElement> targetChildren = targetWithChildren.getChildren();
        for (BpRequirementGroup module : modules) {
            if(!isModuleInChildrenSet(targetChildren,module)) {
                uuids.add(module.getUuid());
            }
        }
        return uuids;
    }


    private boolean isModuleInChildrenSet(Set<CnATreeElement> targetChildren,
            BpRequirementGroup module) {
        for (CnATreeElement targetModuleElement : targetChildren) {
            BpRequirementGroup targetModule = (BpRequirementGroup) targetModuleElement; 
            if(nullSafeEquals(targetModule.getIdentifier(),module.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    private boolean nullSafeEquals(String targetModuleId, String moduleId) {
        if(targetModuleId==null || moduleId==null) {
            return false;
        }
        return targetModuleId.equals(moduleId);
    }
    private CnATreeElement loadElementWithChildren(String uuid) {
        RetrieveInfo ri = RetrieveInfo.getChildrenInstance().setChildrenProperties(true);
        return getDao().findByUuid(uuid,ri); 
    }


    public List<String> getTargetUuids() {
        return targetUuids;
    }

    public void setTargetUuids(List<String> targetUuids) {
        this.targetUuids = targetUuids;
    }

    public List<BpRequirementGroup> getModules() {
        return modules;
    }

    public void setModules(List<BpRequirementGroup> requirementGroups) {
        this.modules = requirementGroups;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ModelModulesCommand.class);
        }
        return log;
    }

}
