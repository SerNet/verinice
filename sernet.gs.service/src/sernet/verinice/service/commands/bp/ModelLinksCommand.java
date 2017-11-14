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

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Link;
import sernet.verinice.service.commands.CreateMultipleLinks;

/**
 * This command creates all necessary links between the objects when 
 * modelling modules from the compendium and target objects from an 
 * information network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelLinksCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(ModelLinksCommand.class);    

    /**
     * HQL query to load the linked safeguards of a module
     */
    private static final String HQL_REQUIREMENTS = "select requirement from CnATreeElement requirement " +
            "join requirement.parent as module " +
            "join fetch requirement.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where requirement.objectType = '" + BpRequirement.TYPE_ID + "' " +
            "and module.uuid in (:uuids)"; //$NON-NLS-1$
    
    /**
     * HQL query to load the linked safeguards of a module
     */
    private static final String HQL_LINKED_ELEMENTS = "select element from CnATreeElement element " +
            "join element.linksUp as linksUp " +
            "join linksUp.dependant as requirement " +
            "join fetch element.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where element.objectType in (:typeIds) " +
            "and requirement.uuid = :uuid"; //$NON-NLS-1$
    
    private transient Set<String> moduleUuidsFromCompendium;
    private transient Set<String> newModuleUuidsFromScope;
    private Integer scopeId;

    private transient Set<CnATreeElement> targetElements;
    private transient Set<BpRequirement> requirementsFromCompendium;
    private transient Map<String,BpRequirement> newRequirementsFromScope;
    private transient Map<String,BpRequirement> allRequirementsFromScope;
    private transient Map<String,Safeguard> allSafeguardsFromScope;
    private transient Map<String,BpThreat> allThreatsFromScope;
    
    public ModelLinksCommand(Set<String> moduleUuidsCompendium, Set<String> newModulesInScopeUuids,
            Integer scopeId, Set<CnATreeElement> targetElements) {
        super();
        this.moduleUuidsFromCompendium = moduleUuidsCompendium;
        this.newModuleUuidsFromScope = newModulesInScopeUuids;
        this.scopeId = scopeId;
        this.targetElements = targetElements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            requirementsFromCompendium = loadRequirements(moduleUuidsFromCompendium);
            loadAllRequirementsFromScope();
            if(isNewModuleInScope()) {
                loadNewRequirementsFromScope();
                loadAllSafeguardsFromScope();
                loadAllThreatsFromScope();
            }      
            createLinks();
        } catch (CommandException e) {
            getLog().error("Error while creating links", e);
            throw new RuntimeCommandException("Error while creating links", e);
        }
    }

    private void createLinks() throws CommandException {
        List<Link> linkList = new LinkedList<>();
        for (BpRequirement requirementCompendium : requirementsFromCompendium) {
            linkList.addAll(createLinks(requirementCompendium));
        }
        CreateMultipleLinks createMultipleLinks = new CreateMultipleLinks(linkList);
        getCommandService().executeCommand(createMultipleLinks);
    }

    protected List<Link> createLinks(BpRequirement requirementCompendium) {
        List<Link> linkList = new LinkedList<>();
        linkList.addAll(linkRequirementWithTargetElements(requirementCompendium));
        if(isNewModuleInScope()) {
            Set<CnATreeElement> linkedElements = loadLinkedElements(requirementCompendium.getUuid());
            linkList.addAll(createLinksToSafeguardAndThreat(requirementCompendium, linkedElements));
        }
        return linkList;
    }

    private List<Link> linkRequirementWithTargetElements(BpRequirement requirementCompendium) {
        List<Link> linkList = new LinkedList<>();
        for (CnATreeElement targetScope : targetElements) {
            linkList.add(createLinksToTarget(requirementCompendium,targetScope));
        }
        return linkList;
    }

    private Link createLinksToTarget(BpRequirement requirementCompendium,
            CnATreeElement targetScope) {
        BpRequirement requirementScope = allRequirementsFromScope.get(requirementCompendium.getIdentifier());
        if(validate(requirementScope, targetScope))  {
            return new Link(requirementScope, targetScope);
        } else {
            return null;
        }
        
    }

    private List<Link> createLinksToSafeguardAndThreat(BpRequirement requirementCompendium,
            Set<CnATreeElement> linkedElements) {
        List<Link> linkList = new LinkedList<>();
        for (CnATreeElement element : linkedElements) {
            if(element instanceof Safeguard) {
                Safeguard safeguardCompendium = (Safeguard) element;
                Link link = createLink(requirementCompendium,safeguardCompendium);
                if(link!=null) {
                    linkList.add(link);
                }
            }
            if(element instanceof BpThreat) {
                BpThreat threatCompendium = (BpThreat) element;
                Link link = createLink(requirementCompendium,threatCompendium);
                if(link!=null) {
                    linkList.add(link);
                }        
            }
        }
        return linkList;
    }

    private Link createLink(BpRequirement requirementCompendium, Safeguard safeguardCompendium) {
        BpRequirement requirementScope = newRequirementsFromScope.get(requirementCompendium.getIdentifier());
        Safeguard safeguardScope = allSafeguardsFromScope.get(safeguardCompendium.getIdentifier());
        if(validate(requirementScope, safeguardScope))  {
            return new Link(requirementScope, safeguardScope, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        } else {
            return null;
        }
    }

    private Link createLink(BpRequirement requirementCompendium, BpThreat threatCompendium) {
        BpRequirement requirementScope = newRequirementsFromScope.get(requirementCompendium.getIdentifier());
        BpThreat threatScope = allThreatsFromScope.get(threatCompendium.getIdentifier());        
        if(validate(requirementScope, threatScope))  {
            return new Link(requirementScope, threatScope, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        } else {
            return null;
        }
    }
    
    private boolean validate(CnATreeElement elementA, CnATreeElement elementB) {
        if(elementA==null || elementB==null) {
            getLog().warn("Element is null. Can not create link.");
        }
        return elementA!=null && elementB!=null;
    }
    
    private Set<BpRequirement> loadRequirements(final Set<String> moduleUuids) {
        return new HashSet<>(findRequirementsByModuleUuid(moduleUuids));
    }
    
    @SuppressWarnings("unchecked")
    private List<BpRequirement> findRequirementsByModuleUuid(final Set<String> moduleUuids) {
         return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_REQUIREMENTS).setParameterList("uuids",
                        moduleUuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
    }

    private Set<CnATreeElement> loadLinkedElements(final String requirementUuid) {
         return new HashSet<>(findLinkedElements(requirementUuid));
    }
    
    @SuppressWarnings("unchecked")
    private List<CnATreeElement> findLinkedElements(final String requirementUuid) {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_LINKED_ELEMENTS).setParameter("uuid",
                        requirementUuid).setParameterList("typeIds", new String[]{Safeguard.TYPE_ID,BpThreat.TYPE_ID});
                query.setReadOnly(true);
                return query.list();
            }
        });
    }
    
    protected void loadNewRequirementsFromScope() {
        newRequirementsFromScope = new HashMap<>();
        List<BpRequirement> requirementList = findRequirementsByModuleUuid(newModuleUuidsFromScope);
        for (BpRequirement requirement : requirementList) {
            newRequirementsFromScope.put(requirement.getIdentifier(), requirement);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadAllRequirementsFromScope() {
        List<BpRequirement> requirements = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_SCOPE_ELEMENTS).setParameter("scopeId",
                        scopeId).setParameter("typeId", BpRequirement.TYPE_ID);
                query.setReadOnly(true);
                return query.list();
            }
        });
        allRequirementsFromScope = new HashMap<>();
        for (BpRequirement requirement : requirements) {
            allRequirementsFromScope.put(requirement.getIdentifier(), requirement);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadAllSafeguardsFromScope() {
        List<Safeguard> safeguards = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_SCOPE_ELEMENTS).setParameter("scopeId",
                        scopeId).setParameter("typeId", Safeguard.TYPE_ID);
                query.setReadOnly(true);
                return query.list();
            }
        });
        allSafeguardsFromScope = new HashMap<>();
        for (Safeguard safeguard : safeguards) {
            allSafeguardsFromScope.put(safeguard.getIdentifier(), safeguard);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadAllThreatsFromScope() {
        List<BpThreat> threats = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_SCOPE_ELEMENTS).setParameter("scopeId",
                        scopeId).setParameter("typeId", BpThreat.TYPE_ID);
                query.setReadOnly(true);
                return query.list();
            }
        });
        allThreatsFromScope = new HashMap<>();
        for (BpThreat threat : threats) {
            allThreatsFromScope.put(threat.getIdentifier(), threat);
        }
    }
    
    private boolean isNewModuleInScope() {
        return newModuleUuidsFromScope!=null && !newModuleUuidsFromScope.isEmpty();
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ModelLinksCommand.class);
        }
        return log;
    }

}
