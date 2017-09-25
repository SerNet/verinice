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
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

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
public class ModelCommand extends ChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(ModelCommand.class);

    /**
     * HQL query to load the elements. The entity and the properties
     * are loaded by a single statement with joins.
     */
    private static final String HQL_QUERY = "select distinct element from CnATreeElement element " +
            "join fetch element.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where element.uuid in (:uuids)"; //$NON-NLS-1$
    
    
    private List<String> compendiumUuids;
    private List<String> targetUuids;
    private transient List<BpRequirementGroup> requirementGroups;
    private transient List<CnATreeElement> targetElements;
    
    private String stationId;
    
    public ModelCommand(List<String> compendiumUuids, List<String> targetUuids) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        validateParameter(compendiumUuids, targetUuids);
        this.compendiumUuids = compendiumUuids;
        this.targetUuids = targetUuids;
    }
    

    @Override
    public void execute() {
        try {
            loadElements();
            handleModules();         
            handleSafeguards();
            handleThreats();
        } catch (CommandException e) {
            getLog().error("Error while modeling.", e);
            throw new RuntimeCommandException("Error while modeling.", e);
        }
    }

    private void handleModules() throws CommandException {
        ModelModulesCommand modelModulesCommand = new ModelModulesCommand(requirementGroups, targetUuids);
        modelModulesCommand = getCommandService().executeCommand(modelModulesCommand);
    }
    
    private void handleSafeguards() throws CommandException {
        ModelSafeguardsCommand modelSafeguardsCommand = new ModelSafeguardsCommand(compendiumUuids,getTargetScopeId());
        modelSafeguardsCommand = getCommandService().executeCommand(modelSafeguardsCommand);
        
    }
    
    private Integer getTargetScopeId() {
        return targetElements.get(0).getScopeId();
    }


    private void handleThreats() {
        // TODO Auto-generated method stub
        
    }
    
    @SuppressWarnings("unchecked")
    private void loadElements() {
        final List<String> allUuids = getAllUuids();
        List<CnATreeElement> elements = getDao().findByCallback(new HibernateCallback() {
            @Override       
            public Object doInHibernate( Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(HQL_QUERY)
                        .setParameterList("uuids", allUuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
        distributeElements(elements);
    }

    protected void distributeElements(List<CnATreeElement> elements) {
        requirementGroups = new LinkedList<>();
        targetElements = new LinkedList<>();
        for (CnATreeElement element : elements) {
            if (compendiumUuids.contains(element.getUuid())
                    && element instanceof BpRequirementGroup) {
                requirementGroups.add((BpRequirementGroup) element);
            }
            if (targetUuids.contains(element.getUuid())) {
                targetElements.add(element);
            }
        }
    }
    
    private List<String> getAllUuids() {
        List<String> allUuids = new LinkedList<>();
        allUuids.addAll(compendiumUuids);
        allUuids.addAll(targetUuids);
        return allUuids;
    }


    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    private void validateParameter(List<String> compendiumUuids, List<String> targetUuids) {
        if(compendiumUuids==null) {
            throw new IllegalArgumentException("Compedium ids must not be null.");
        }
        if(targetUuids==null) {
            throw new IllegalArgumentException("Target element ids must not be null.");
        }
        if(compendiumUuids.isEmpty()) {
            throw new IllegalArgumentException("Compedium uuid list is empty.");
        }
        if(targetUuids.isEmpty()) {
            throw new IllegalArgumentException("Target element uuid list is empty.");
        }
    }
    
    public static boolean nullSafeEquals(String targetModuleId, String moduleId) {
        if(targetModuleId==null || moduleId==null) {
            return false;
        }
        return targetModuleId.equals(moduleId);
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
            log = Logger.getLogger(ModelCommand.class);
        }
        return log;
    }


}
