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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelSafeguardsCommand extends ChangeLoggingCommand {
    
    private transient Logger log = Logger.getLogger(ModelSafeguardsCommand.class);
    
    /**
     * HQL query to load the linked safeguards of a module
     */
    private static final String HQL_LINKED_SAFEGUARDS = "select distinct safeguard from CnATreeElement safeguard " +
            "join safeguard.linksUp as linksUp " +
            "join linksUp.dependant as requirement " +
            "join requirement.parent as module " +
            "join fetch safeguard.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where safeguard.objectType = '" + Safeguard.TYPE_ID + "' " +
            "and module.uuid in (:uuids)"; //$NON-NLS-1$
    

    /**
     * HQL query to load the linked safeguards of a module
     */
    private static final String HQL_SCOPE_SAFEGUARDS = "select distinct safeguard from CnATreeElement safeguard " +
            "join fetch safeguard.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where safeguard.objectType = '" + Safeguard.TYPE_ID + "' " +
            "and safeguard.scopeId = :scopeId"; //$NON-NLS-1$
    
    /**
     * HQL query to load the linked safeguards of a module
     */
    private static final String HQL_LOAD_PARENT_IDS = "select distinct safeguard from CnATreeElement safeguard " +
            "join fetch safeguard.parent as p1 " +
            "join fetch p1.parent as p2 " +
            "join fetch p2.parent as p3 " +
            "and safeguard.uuid in (:uuids)"; //$NON-NLS-1$
    
    private List<String> moduleUuids;
    private Integer targetScopeId;
    private transient List<Safeguard> compendiumSafeguards;
    private transient List<Safeguard> scopeSafeguards;
    private transient List<Safeguard> missingSafeguards;
    private transient List<String> missingSafeguardUuids;
    private transient Set<String> parentUuids;
    
    private String stationId;
        
    public ModelSafeguardsCommand(List<String> moduleUuids, Integer targetScopeId) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        this.moduleUuids = moduleUuids;
        this.targetScopeId = targetScopeId;
        missingSafeguards = new LinkedList<>();
        missingSafeguardUuids = new LinkedList<>();
        parentUuids = new HashSet<>();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        loadCompendiumSafeguards();
        loadScopeSafeguards();
        createListOfMssingSafeguards();
    }
    
    @SuppressWarnings("unchecked")
    private void loadScopeSafeguards() {
        scopeSafeguards = getDao().findByCallback(new HibernateCallback() {
            @Override       
            public Object doInHibernate( Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(HQL_SCOPE_SAFEGUARDS)
                        .setParameter("scopeId", targetScopeId);
                query.setReadOnly(true);
                return query.list();
            }
        });
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards in target scope: ");
            logElements(scopeSafeguards);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCompendiumSafeguards() {
        compendiumSafeguards = getDao().findByCallback(new HibernateCallback() {
            @Override       
            public Object doInHibernate( Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(HQL_LINKED_SAFEGUARDS)
                        .setParameterList("uuids", moduleUuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards linked to modules: ");
            logElements(compendiumSafeguards);
        }        
    }
    
    @SuppressWarnings("unchecked")
    private void loadParentIds() {
        compendiumSafeguards = getDao().findByCallback(new HibernateCallback() {
            @Override       
            public Object doInHibernate( Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(HQL_LOAD_PARENT_IDS)
                        .setParameterList("uuids", missingSafeguardUuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards linked to modules: ");
            logElements(compendiumSafeguards);
        }        
    }
    
    
    
    private void createListOfMssingSafeguards() {
        missingSafeguards.clear();
        missingSafeguardUuids.clear();
        for (Safeguard safeguard : compendiumSafeguards) {
            if(!isSafeguardInScope(safeguard)) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Safeguard is not in scope yet: " + safeguard);
                }
                missingSafeguards.add(safeguard);
                missingSafeguardUuids.add(safeguard.getUuid());
            }
        }
    }


    private boolean isSafeguardInScope(Safeguard compendiumSafeguard) {
        for (Safeguard scopeSafeguard : scopeSafeguards) {
            if(ModelCommand.nullSafeEquals(scopeSafeguard.getIdentifier(),compendiumSafeguard.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    
    /**
     * @param compendiumSafeguards2
     */
    private void logElements(List<? extends CnATreeElement> compendiumSafeguards2) {
        for (CnATreeElement element : compendiumSafeguards2) {
            getLog().debug(element);
        }
        
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ModelSafeguardsCommand.class);
        }
        return log;
    }

}
