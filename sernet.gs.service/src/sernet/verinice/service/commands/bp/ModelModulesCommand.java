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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.exceptions.GroupNotFoundInScopeException;
import sernet.verinice.service.commands.CopyCommand;

/**
 * This command models modules (requirements groups) with objects of different types 
 * from an information network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelModulesCommand extends ChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(ModelModulesCommand.class);

    private Integer targetScopeId;
    private transient List<BpRequirementGroup> compendiumModules;
    private transient Set<CnATreeElement> compendiumRequirements;
    private transient List<BpRequirement> scopeRequirements;
    private transient Map<String, CnATreeElement> missingRequirements;
    private transient Map<String, BpRequirement> missingRequirementsWithParents;
    private transient Map<String, CnATreeElement> requirementParentsWithProperties;
    private Set<String> newModuleUuids = new HashSet<>();
    
    private String stationId;
    
    public ModelModulesCommand(List<BpRequirementGroup> modules, Integer targetScopeId) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        this.targetScopeId = targetScopeId;
        this.compendiumModules = modules;
        compendiumRequirements = new HashSet<>();
        missingRequirements = new HashMap<>();
        missingRequirementsWithParents = new HashMap<>();
        requirementParentsWithProperties = new HashMap<>();
    }
    
    @Override
    public void execute() {
        try {
            loadCompendiumRequirements();
            loadScopeRequirements();
            createListOfMissingRequirements();
            if(!missingRequirements.isEmpty()) {
                loadParents();
                insertMissingRequirements();
            }
        } catch (CommandException e) {
            getLog().error("Error while modeling safeguards.", e);
            throw new RuntimeCommandException("Error while modeling safeguards.", e);
        }
    }
    
    private void insertMissingRequirements() throws CommandException {
        CnATreeElement requirementGroup = getRequirementRootGroup();
        for (CnATreeElement requirement :  missingRequirementsWithParents.values()) {
            insertRequirement(requirementGroup, requirement);
        }
    }
    
    protected void insertRequirement(CnATreeElement requirementGroup, CnATreeElement requirement)
            throws CommandException {
        CnATreeElement group = requirement.getParent().getParent().getParent();
        CnATreeElement parent = getOrCreateGroup(requirementGroup,
                requirementParentsWithProperties.get(group.getUuid()));

        group = requirement.getParent().getParent();
        parent = getOrCreateGroup(parent, requirementParentsWithProperties.get(group.getUuid()));

        group = requirement.getParent();
        parent = getOrCreateGroup(parent, requirementParentsWithProperties.get(group.getUuid()));
        
        if (!isSafeguardInChildrenSet(parent.getChildren(),
                missingRequirements.get(requirement.getUuid()))) {
            CopyCommand copyCommand = new CopyCommand(parent.getUuid(),
                    Arrays.asList(requirement.getUuid()));
            getCommandService().executeCommand(copyCommand);
            newModuleUuids.add(parent.getUuid());
            if (getLog().isDebugEnabled()) {
                getLog().debug("Requirement: " + requirement.getTitle() + " created in group: "
                        + parent.getTitle());
            }
        } else if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguard: " + requirement.getTitle() + " already exists in group: "
                    + parent.getTitle());
        }
    }
    
    private boolean isSafeguardInChildrenSet(Set<CnATreeElement> targetChildren, CnATreeElement requirement) {
        for (CnATreeElement targetSafeguardElement : targetChildren) {
            CnATreeElement targetSafeguard = targetSafeguardElement;
            String targetIdentifier = getIdentifierOfRequirement(targetSafeguard);
            String identifier = getIdentifierOfRequirement(requirement);
            if (ModelCommand.nullSafeEquals(targetIdentifier, identifier)) {
                return true;
            }
        }
        return false;
    }

    private void loadCompendiumRequirements() {
        compendiumRequirements.clear();
        for (CnATreeElement module : compendiumModules) {
            RetrieveInfo ri = RetrieveInfo.getChildrenInstance();
            ri.setChildrenProperties(true);
            CnATreeElement moduleWithChildren = getDao().findByUuid(module.getUuid(), ri);
            compendiumRequirements.addAll(moduleWithChildren.getChildren());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadScopeRequirements() {
        scopeRequirements = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_SCOPE_ELEMENTS).setParameter("scopeId",
                        targetScopeId).setParameter("typeId", BpRequirement.TYPE_ID);
                query.setReadOnly(true);
                return query.list();
            }
        });
        if (getLog().isDebugEnabled()) {
            getLog().debug("Requirements in target scope: ");
            logElements(scopeRequirements);
        }
    }
    
    private void createListOfMissingRequirements() {
        missingRequirements.clear();
        for (CnATreeElement requirement : compendiumRequirements) {
            if (!isRequirementInScope(requirement)) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Requirement is not in scope yet: " + requirement);
                }
                missingRequirements.put(requirement.getUuid(), requirement);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadParents() {
        // Load the parents (predecessors) of all missing requirements 
        List<BpRequirement> requirements = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_LOAD_PARENT_IDS).setParameterList("uuids",
                        missingRequirements.keySet());
                query.setReadOnly(true);
                return query.list();
            }
        });
        final List<String> parentUuids = new LinkedList<>();
        for (BpRequirement requirement : requirements) {
            missingRequirementsWithParents.put(requirement.getUuid(), requirement);
            parentUuids.add(requirement.getParent().getUuid());
            parentUuids.add(requirement.getParent().getParent().getUuid());
            parentUuids.add(requirement.getParent().getParent().getParent().getUuid());
        }
        // Load the properties of the parents (predecessors)
        List<CnATreeElement> groupsWithProperties = getDao()
                .findByCallback(new HibernateCallback() {
                    @Override
                    public Object doInHibernate(Session session) throws SQLException {
                        Query query = session.createQuery(ModelCommand.HQL_ELEMENT_WITH_PROPERTIES)
                                .setParameterList("uuids", parentUuids);
                        query.setReadOnly(true);
                        return query.list();
                    }
                });
        for (CnATreeElement group : groupsWithProperties) {
            requirementParentsWithProperties.put(group.getUuid(), group);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Requirements parents: ");
            logElements(requirementParentsWithProperties.values());
        }
    }
    
    private boolean isRequirementInScope(CnATreeElement compendiumRequirement) {
        for (BpRequirement scopeRequirement : scopeRequirements) {
            if (ModelCommand.nullSafeEquals(scopeRequirement.getIdentifier(),
                    getIdentifierOfRequirement(compendiumRequirement))) {
                return true;
            }
        }
        return false;
    }
    
    private CnATreeElement getOrCreateGroup(CnATreeElement parent, CnATreeElement compendiumGroup)
            throws CommandException {
        CnATreeElement group = null;
        boolean groupFound = false;
        for (CnATreeElement child : parent.getChildren()) {
            if (child.getTitle().equals(compendiumGroup.getTitle())) {
                group = child;
                groupFound = true;
                break;
            }
        }
        if (!groupFound) {
            group = createGroup(parent, compendiumGroup);
        } else if (getLog().isDebugEnabled()) {
            getLog().debug("Requirement group: " + compendiumGroup.getTitle()
                    + " already exists in group: " + parent.getTitle());
        }
        return group;
    }

    protected CnATreeElement createGroup(CnATreeElement parent, CnATreeElement compendiumGroup)
            throws CommandException {
        CnATreeElement group;
        CopyCommand copyCommand = new CopyCommand(parent.getUuid(),
                Arrays.asList(compendiumGroup.getUuid()));
        copyCommand.setCopyChildren(false);
        copyCommand = getCommandService().executeCommand(copyCommand);
        String groupUuid = copyCommand.getNewElements().get(0);
        group = getDao().findByUuid(groupUuid,
                RetrieveInfo.getChildrenInstance().setChildrenProperties(true));
        parent.addChild(group);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Requirement group: " + compendiumGroup.getTitle() + " created in group: "
                    + parent.getTitle());
        }
        return group;
    }

    protected CnATreeElement getRequirementRootGroup() {
        CnATreeElement safeguardGroup = null;
        CnATreeElement scope = getDao().retrieve(targetScopeId, RetrieveInfo.getChildrenInstance());
        Set<CnATreeElement> children = scope.getChildren();
        for (CnATreeElement group : children) {
            if (group.getTypeId().equals(BpRequirementGroup.TYPE_ID)) {
                safeguardGroup = group;
            }
        }
        if(safeguardGroup==null) {
            throw new GroupNotFoundInScopeException(targetScopeId, BpRequirementGroup.TYPE_ID);
        }
        return getDao().retrieve(safeguardGroup.getDbId(),
                RetrieveInfo.getChildrenInstance().setChildrenProperties(true));
    }


    private String getIdentifierOfRequirement(CnATreeElement compendiumRequirement) {
        return compendiumRequirement.getEntity().getPropertyValue(BpRequirement.PROP_ID);
    }

    public Set<String> getNewModuleUuids() {
        return newModuleUuids;
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
    
    private void logElements(Collection<?> collection) {
        for (Object element : collection) {
            getLog().debug(element);
        }

    }
}
