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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command models modules (requirements groups) from the ITBP compendium
 * with certain target object types of an IT network. Supported types are: IT
 * networks, business processes, other/IoT systems, ICS systems, IT systems,
 * networks and rooms.
 * 
 * Modeling process:
 * 
 * Requirements
 * 
 * The module is created as a child object below the target object. All
 * requirements in the module are copied as well. Modules and requirements are
 * only created once per target object but can occur multiple times in different
 * target objects.
 * 
 * Safeguards
 * 
 * If an implementation hint (safeguard group) is available for the module in
 * the ITBP Compendium all safeguards and all applicable safeguard groups are
 * created in the IT network. Safeguards and groups are only created once in the
 * IT network.
 * 
 * Elemental threats
 * 
 * Elemental threat groups and the elemental threats are created as objects in
 * the IT network. They are only created once in the IT network.
 *
 * Specific threats
 * 
 * Specific threats groups and specific threats are created as objects in the IT
 * network. They are only created once in the IT network.
 * 
 * Links
 * 
 * Links from newly created requirements to elemental threats are created
 * according to the cross reference tables in the ITBP compendium modules If
 * safeguards (in an implementation hint) for an module in the ITBP compendium
 * exist two links are created: Requirement to safeguard and safeguard to
 * elemental threat. According to the definition in compendium a link from a
 * module to the specific threat group is created.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelCommand extends ChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(ModelCommand.class);

    /**
     * HQL query to load the elements. The entity and the properties
     * are loaded by a single statement with joins.
     */
    public static final String HQL_ELEMENT_WITH_PROPERTIES = "select distinct element from CnATreeElement element " +
            "join fetch element.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where element.uuid in (:uuids)"; //$NON-NLS-1$
    
    /**
     * HQL query to load all safeguards of a scope
     */
    public static final String HQL_SCOPE_ELEMENTS = "select distinct safeguard from CnATreeElement safeguard " +
            "join fetch safeguard.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where safeguard.objectType = :typeId " +
            "and safeguard.scopeId = :scopeId"; //$NON-NLS-1$
    
    private List<String> compendiumUuids;
    private List<String> targetUuids;
    private transient List<BpRequirementGroup> requirementGroups;
    private transient List<CnATreeElement> targetElements;
    private transient List<String> newModuleUuidsScope = Collections.emptyList();
    
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
            if(!newModuleUuidsScope.isEmpty()) {
                handleSafeguards();
                handleThreats();
                createLinks();
            }
        } catch (CommandException e) {
            getLog().error("Error while modeling.", e);
            throw new RuntimeCommandException("Error while modeling.", e);
        }
    }

    private void handleModules() throws CommandException {
        ModelModulesCommand modelModulesCommand = new ModelModulesCommand(requirementGroups, targetUuids);
        modelModulesCommand = getCommandService().executeCommand(modelModulesCommand);
        newModuleUuidsScope = modelModulesCommand.getNewModuleUuids();
    }
    
    private void handleSafeguards() throws CommandException {
        ModelSafeguardsCommand modelSafeguardsCommand = new ModelSafeguardsCommand(compendiumUuids,getTargetScopeId());
        modelSafeguardsCommand = getCommandService().executeCommand(modelSafeguardsCommand);
        
    }

    private void handleThreats() throws CommandException {
        ModelThreatsCommand modelThreatsCommand = new ModelThreatsCommand(compendiumUuids,getTargetScopeId());
        modelThreatsCommand = getCommandService().executeCommand(modelThreatsCommand);
    }
    

    private void createLinks() throws CommandException {
       ModelLinksCommand modelLinksCommand = new ModelLinksCommand(compendiumUuids, newModuleUuidsScope, getTargetScopeId());
       modelLinksCommand = getCommandService().executeCommand(modelLinksCommand);
    }


    
    private Integer getTargetScopeId() {
        return targetElements.get(0).getScopeId();
    }
    
    @SuppressWarnings("unchecked")
    private void loadElements() {
        final List<String> allUuids = getAllUuids();
        List<CnATreeElement> elements = getDao().findByCallback(new HibernateCallback() {
            @Override       
            public Object doInHibernate( Session session) throws SQLException {
                Query query = session.createQuery(HQL_ELEMENT_WITH_PROPERTIES)
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
