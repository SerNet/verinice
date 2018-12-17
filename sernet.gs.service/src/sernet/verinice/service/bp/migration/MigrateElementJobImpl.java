/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
 ******************************************************************************/
package sernet.verinice.service.bp.migration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.engine.SessionFactoryImplementor;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This class migrates the requirements, safeguards and threats of an element.
 * See ModelingMigrationServiceImpl for more details
 */
public class MigrateElementJobImpl implements MigrateElementJob {

    private static final Logger log = Logger.getLogger(MigrateElementJobImpl.class);

    private CnATreeElement element;
    private Set<CnATreeElement> elementsToDelete;

    private VeriniceGraph veriniceGraph;
    private String hibernateDialect;

    private ICommandService commandService;
    private IBaseDao<CnALink, Serializable> linkDao;

    public MigrateElementJobImpl() {
        super();
        this.elementsToDelete = new HashSet<>();
    }

    /*
     * @see sernet.verinice.service.bp.migration.MigrateElementJob#migrateModeling()
     */
    @Override
    public void migrateModeling(CnATreeElement element, VeriniceGraph veriniceGraph) {
        setElement(element);
        setVeriniceGraph(veriniceGraph);
        if (isMigrated()) {
            if (log.isInfoEnabled()) {
                log.info("The element has already been migrated: " + element);
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Migrating modeling of element: " + element + "...");
        }
        try {
            Set<CnATreeElement> requirements = veriniceGraph.getLinkTargetsByElementType(element,
                    BpRequirement.TYPE_ID);
            migrateRequirements(requirements);
            Set<CnATreeElement> threats = veriniceGraph.getLinkTargetsByElementType(element,
                    BpThreat.TYPE_ID);
            migrateThreats(threats);
            migrateSafeguards(requirements);
        } catch (CommandException e) {
            String message = "Error while migrating: " + element;
            log.error(message, e);
            throw new MigrationException(message, e);
        }
    }

    private void migrateRequirements(Set<CnATreeElement> requirements) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Migrating requirements: " + requirements.size());
        }
        for (CnATreeElement requirement : requirements) {
            migrateRequirement(requirement);
        }
    }

    private void migrateRequirement(CnATreeElement requirement) throws CommandException {
        MigrateRequirementJob requirementJob = new MigrateRequirementJob(element, requirement);
        requirementJob.setVeriniceGraph(veriniceGraph);
        requirementJob.setCommandService(commandService);
        requirementJob.setLinkDao(linkDao);
        requirementJob.setHibernateDialect(getHibernateDialect());
        requirementJob.migrateModeling();
        elementsToDelete.add(requirement);
    }

    private void migrateThreats(Set<CnATreeElement> threats) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Migrating  threats: " + threats.size());
        }
        for (CnATreeElement threat : threats) {
            migrateThreat(threat);
        }
    }

    private void migrateThreat(CnATreeElement threat) throws CommandException {
        MigrateThreatJob threatJob = new MigrateThreatJob(element, threat);
        threatJob.setVeriniceGraph(veriniceGraph);
        threatJob.setCommandService(commandService);
        threatJob.setLinkDao(linkDao);
        threatJob.setHibernateDialect(getHibernateDialect());
        threatJob.migrateModeling();
        elementsToDelete.add(threat);
    }

    private void migrateSafeguards(Set<CnATreeElement> requirements) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Migrating  safeguards...");
        }
        for (CnATreeElement requirement : requirements) {
            Set<CnATreeElement> safeguards = veriniceGraph.getLinkTargetsByElementType(requirement,
                    Safeguard.TYPE_ID);
            for (CnATreeElement safeguard : safeguards) {
                migrateSafeguard(safeguard);
            }

        }
    }

    private void migrateSafeguard(CnATreeElement safeguard) throws CommandException {
        MigrateSafeguardJob safeguardJob = new MigrateSafeguardJob(element, safeguard);
        safeguardJob.setVeriniceGraph(veriniceGraph);
        safeguardJob.setCommandService(commandService);
        safeguardJob.setLinkDao(linkDao);
        safeguardJob.setHibernateDialect(getHibernateDialect());
        safeguardJob.migrateModeling();
        elementsToDelete.add(safeguard);
    }

    /**
     * Check if the element is already migrated. If the item has children, it
     * has already been migrated. Since ItNetworks always have children, return
     * false for ItNetworks.
     */
    private boolean isMigrated() {
        if (ItNetwork.TYPE_ID.equals(element.getTypeId())) {
            return false;
        }
        return !veriniceGraph.getChildren(element).isEmpty();
    }

    public Set<CnATreeElement> getElementsToDelete() {
        return elementsToDelete;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public void setVeriniceGraph(VeriniceGraph veriniceGraph) {
        this.veriniceGraph = veriniceGraph;
    }

    private String getHibernateDialect() {
        if (hibernateDialect == null) {
            hibernateDialect = loadHibernateDialectFromSession();
        }
        return hibernateDialect;
    }

    private String loadHibernateDialectFromSession() {
        return (String) linkDao.executeCallback(
                session -> ((SessionFactoryImplementor) session.getSessionFactory()).getDialect()
                        .toString());
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    public void setLinkDao(IBaseDao<CnALink, Serializable> linkDao) {
        this.linkDao = linkDao;
    }

}
