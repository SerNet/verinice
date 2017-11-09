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
 * This command models modules (requirements groups) from the ITBP compendium
 * with certain target object types of an IT network.
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
    private static final String HQL_REQUIREMENTS = "select distinct requirement from CnATreeElement requirement " +
            "join requirement.parent as module " +
            "join fetch requirement.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where requirement.objectType = '" + BpRequirement.TYPE_ID + "' " +
            "and module.uuid in (:uuids)"; //$NON-NLS-1$
    
    /**
     * HQL query to load the linked safeguards of a module
     */
    private static final String HQL_LINKED_SAFEGUARDS = "select distinct safeguard from CnATreeElement safeguard " +
            "join safeguard.linksUp as linksUp " +
            "join linksUp.dependant as requirement " +
            "join fetch safeguard.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where safeguard.objectType in (:typeIds) " +
            "and requirement.uuid = :uuid"; //$NON-NLS-1$
    
    private transient Set<String> moduleUuidsCompendium;
    private transient Set<String> moduleUuidsScope;
    private Integer scopeId;

    private transient List<BpRequirement> requirementsCompendium;
    private transient Map<String,BpRequirement> requirementsScope;
    private transient Map<String,Safeguard> safeguardsScope;
    private transient Map<String,BpThreat> threatsScope;
    
    public ModelLinksCommand(Set<String> moduleUuidsCompendium, Set<String> moduleUuidsScope,
            Integer scopeId) {
        super();
        this.moduleUuidsCompendium = moduleUuidsCompendium;
        this.moduleUuidsScope = moduleUuidsScope;
        this.scopeId = scopeId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            requirementsCompendium = loadRequirements(moduleUuidsCompendium);
            loadRequirementsOfScope();
            loadSafeguardsOfScope();
            loadThreatsOfScope();
            createLinks();
        } catch (CommandException e) {
            getLog().error("Error while creating links", e);
            throw new RuntimeCommandException("Error while creating links", e);
        }
    }
    
    private void createLinks() throws CommandException {
        List<Link> linkList = new LinkedList<>();
        for (BpRequirement requirementCompendium : requirementsCompendium) {
            linkList.addAll(createLinks(requirementCompendium));
        }
        CreateMultipleLinks createMultipleLinks = new CreateMultipleLinks(linkList);
        getCommandService().executeCommand(createMultipleLinks);
    }

    protected List<Link> createLinks(BpRequirement requirementCompendium) {
        List<Link> linkList = new LinkedList<>();
        List<CnATreeElement> linkedElements = loadLinkedElements(requirementCompendium.getUuid());
        Safeguard safeguardScope = null;
        BpThreat threatScope = null;
        for (CnATreeElement element : linkedElements) {
            if(element instanceof Safeguard) {
                Safeguard safeguardCompendium = (Safeguard) element;
                Link link = createLink(requirementCompendium,safeguardCompendium);
                if(link!=null) {
                    linkList.add(link);
                }
                safeguardScope = safeguardsScope.get(safeguardCompendium.getIdentifier());
            }
            if(element instanceof BpThreat) {
                BpThreat threatCompendium = (BpThreat) element;
                Link link = createLink(requirementCompendium,threatCompendium);
                if(link!=null) {
                    linkList.add(link);
                }
                threatScope = threatsScope.get(threatCompendium.getIdentifier());        
            }
            if(safeguardScope!=null && threatScope!=null) {
                linkList.add(new Link(safeguardScope, threatScope, Safeguard.REL_BP_SAFEGUARD_BP_THREAT));
            }
        }
        return linkList;
    }

    private Link createLink(BpRequirement requirementCompendium, Safeguard safeguardCompendium) {
        BpRequirement requirementScope = requirementsScope.get(requirementCompendium.getIdentifier());
        Safeguard safeguardScope = safeguardsScope.get(safeguardCompendium.getIdentifier());
        if(validate(requirementScope, safeguardScope))  {
            return new Link(requirementScope, safeguardScope, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        } else {
            return null;
        }
    }

    private Link createLink(BpRequirement requirementCompendium, BpThreat threatCompendium) {
        BpRequirement requirementScope = requirementsScope.get(requirementCompendium.getIdentifier());
        BpThreat threatScope = threatsScope.get(threatCompendium.getIdentifier());        
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
    
    @SuppressWarnings("unchecked")
    private List<BpRequirement> loadRequirements(final Set<String> moduleUuids) {
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

    @SuppressWarnings("unchecked")
    private List<CnATreeElement> loadLinkedElements(final String requirementUuid) {
         return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_LINKED_SAFEGUARDS).setParameter("uuid",
                        requirementUuid).setParameterList("typeIds", new String[]{Safeguard.TYPE_ID,BpThreat.TYPE_ID});
                query.setReadOnly(true);
                return query.list();
            }
        });
    }
    
    protected void loadRequirementsOfScope() {
        requirementsScope = new HashMap<>();
        List<BpRequirement> requirementsScopeList = loadRequirements(moduleUuidsScope);
        for (BpRequirement requirement : requirementsScopeList) {
            requirementsScope.put(requirement.getIdentifier(), requirement);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadSafeguardsOfScope() {
        List<Safeguard> safeguards = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_SCOPE_ELEMENTS).setParameter("scopeId",
                        scopeId).setParameter("typeId", Safeguard.TYPE_ID);
                query.setReadOnly(true);
                return query.list();
            }
        });
        safeguardsScope = new HashMap<>();
        for (Safeguard safeguard : safeguards) {
            safeguardsScope.put(safeguard.getIdentifier(), safeguard);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadThreatsOfScope() {
        List<BpThreat> threats = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_SCOPE_ELEMENTS).setParameter("scopeId",
                        scopeId).setParameter("typeId", BpThreat.TYPE_ID);
                query.setReadOnly(true);
                return query.list();
            }
        });
        threatsScope = new HashMap<>();
        for (BpThreat threat : threats) {
            threatsScope.put(threat.getIdentifier(), threat);
        }
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
