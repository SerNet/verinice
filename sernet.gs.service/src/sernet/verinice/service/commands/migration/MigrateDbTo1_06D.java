/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.service.commands.migration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.RemoveElement;

/**
 * In Verinice 1.15, the modernized base protection was introduced. In that
 * release, when modeling networks, requirements and safeguards (along with
 * their groups) were copied to the respective network only once and linked to
 * every target object. This class performs the migration to the new modeling
 * structure (separate copies of requirements and safeguards below the target
 * objects).
 * 
 * Migration can be deactivated with a system property. If this is required, the
 * property must be added to the verinice.ini file in standalone mode or to the
 * tomcat6.conf file on the verinice.PRO server:
 * 
 * -Dveriniceserver.itbp.migration.1-16to1-17=false
 */
public class MigrateDbTo1_06D extends DbMigration {

    private static final long serialVersionUID = -616760581832706615L;

    private static final Logger logger = Logger.getLogger(MigrateDbTo1_06D.class);

    public static final double VERSION = 1.06D;

    private static final String VERINICESERVER_ITBP_MIGRATION_1_16TO1_17 = "veriniceserver.itbp.migration.1-16to1-17";

    private static final String SCOPE_ID = "scopeId";
    private static final String OBJECT_TYPE = "objectType";

    private List<Integer> iTNetworkIDs;

    private Map<Integer, Map<String, CnATreeElement>> copiedElementsPerTargetById = new HashMap<>();

    private transient IBaseDao<CnATreeElement, Serializable> elementDao;



    /**
     * Create a migration command that migrates data from all available IT
     * networks
     */
    public MigrateDbTo1_06D() {
        this(null);
    }

    /**
     * Create a migration command that only migrates data from IT networks with
     * the given IDs
     */
    public MigrateDbTo1_06D(List<Integer> iTNetworkIDs) {
        this.iTNetworkIDs = iTNetworkIDs;
    }

    @Override
    public void execute() {
        if ("false".equals(System.getProperty(VERINICESERVER_ITBP_MIGRATION_1_16TO1_17))) {
            logger.warn("System property: " + VERINICESERVER_ITBP_MIGRATION_1_16TO1_17
                    + " is false. Skipping migration to new ITBP modeling.  ");
        } else {
            initializeDaos();
            migrateModeling();
        }
        logger.info("Updating database to version " + VERSION);
        updateVersion();
    }

    @SuppressWarnings("unchecked")
    private void migrateModeling() {
        if (iTNetworkIDs == null) {
            DetachedCriteria itNetworkIDsCriteria = DetachedCriteria
                    .forClass(CnATreeElement.class).createAlias("parent", "p")
                    .add(Restrictions.eq(OBJECT_TYPE, "bp_itnetwork"))
                    .add(Restrictions.in("p.objectType",
                            new String[] { "bp_model", "bp_import_group" }))
                    .setProjection(Projections.property("dbId"));
            iTNetworkIDs = elementDao.findByCriteria(itNetworkIDsCriteria);
        }
        if (!iTNetworkIDs.isEmpty()) {
            migrateModelingInItNetworks();
        }
    }

    private void migrateModelingInItNetworks() {
        logger.info("Migrating IT networks with ids: " + iTNetworkIDs);
        initializeElements();
        try {
            List<CnATreeElement> requirements = loadRequirements();
            int numberOfRequirements = requirements.size();
            logger.info("Migrating " + numberOfRequirements + " requirements");
            int n = 0;
            for (CnATreeElement element : requirements) {
                processRequirement(element);
                n++;
                if (logger.isInfoEnabled()) {
                    logger.info(n + " of " + numberOfRequirements + " requirements processed");
                }
            }

            List<CnATreeElement> threats = loadThreats();
            int numberOfThreats = threats.size();
            logger.info("Migrating " + numberOfThreats + " threats");
            n = 0;
            for (CnATreeElement element : threats) {
                processThreat(element);
                n++;
                if (logger.isInfoEnabled()) {
                    logger.info(n + " of " + numberOfThreats + " threats processed");
                }
            }

            removeEmptyGroups(iTNetworkIDs);
        } catch (CommandException e) {
            throw new RuntimeCommandException("Failed to migrate database", e);
        }
    }

    /**
     * Loads elements to initialize them. The elements are not used. Calling
     * this method prevents LazyInitializationException during migration.
     */
    private void initializeElements() {
        List<CnATreeElement> safeguards = loadSafeguards();
        logger.info(safeguards.size() + " safeguards loaded");
        List<CnATreeElement> targetObjects = loadTargetObjects();
        logger.info(targetObjects.size() + " target objects loaded");
    }

    private void processRequirement(CnATreeElement requirement) throws CommandException {
        MigrateRequirementTo1_06D migrateRequirementCommand = new MigrateRequirementTo1_06D(
                requirement, copiedElementsPerTargetById);
        getCommandService().executeCommand(migrateRequirementCommand);
    }

    private void processThreat(CnATreeElement threat) throws CommandException {
        MigrateThreatTo1_06D migrateThreatCommand = new MigrateThreatTo1_06D(threat,
                copiedElementsPerTargetById);
        getCommandService().executeCommand(migrateThreatCommand);
    }

    private void removeEmptyGroups(List<Integer> allITNetworkIDs) throws CommandException {
        Set<CnATreeElement> elementsToRemove = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<CnATreeElement> result = elementDao.findByCriteria(DetachedCriteria
                .forClass(CnATreeElement.class).setFetchMode("children", FetchMode.JOIN)
                .add(Restrictions.in("dbId", allITNetworkIDs)));

        for (CnATreeElement itNetwork : result) {
            for (CnATreeElement child : itNetwork.getChildren()) {
                if (!(isBpRequirementGroup(child) || isSafeguardGroup(child)
                        || isBpThreatGroup(child))) {
                    continue;
                }
                addEmptyChildGroups(elementsToRemove, child);
            }
        }

        RemoveElement<CnATreeElement> removeElements = new RemoveElement<>(elementsToRemove);
        getCommandService().executeCommand(removeElements);
    }

    private boolean addEmptyChildGroups(Set<CnATreeElement> set, CnATreeElement element) {
        boolean foundElementPreventingDeletion = false;
        for (CnATreeElement child : element.getChildren()) {
            if (isBpRequirementGroup(child) || isSafeguardGroup(child) || isBpThreatGroup(child)) {
                boolean childIsEmpty = addEmptyChildGroups(set, child);
                if (!childIsEmpty) {
                    foundElementPreventingDeletion = true;
                }
            } else {
                foundElementPreventingDeletion = true;
            }

        }
        if (!foundElementPreventingDeletion) {
            set.add(element);
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private List<CnATreeElement> loadSafeguards() {
        DetachedCriteria safeguardsCriteria = DetachedCriteria.forClass(CnATreeElement.class)
                .add(Restrictions.eq(OBJECT_TYPE, "bp_safeguard"))
                .add(Restrictions.in(SCOPE_ID, iTNetworkIDs));
        new RetrieveInfo().setProperties(true).setLinksDown(true).setLinksUp(true).setParent(true)
                .configureCriteria(safeguardsCriteria);
        return elementDao.findByCriteria(safeguardsCriteria);
    }

    @SuppressWarnings({ "unchecked" })
    private List<CnATreeElement> loadThreats() {
        DetachedCriteria threatsCriteria = DetachedCriteria.forClass(CnATreeElement.class)
                .add(Restrictions.eq(OBJECT_TYPE, "bp_threat"))
                .add(Restrictions.in(SCOPE_ID, iTNetworkIDs));
        new RetrieveInfo().setLinksDown(true).setLinksUp(true).configureCriteria(threatsCriteria);
        return elementDao.findByCriteria(threatsCriteria);
    }

    @SuppressWarnings("unchecked")
    private List<CnATreeElement> loadTargetObjects() {
        DetachedCriteria targetObjectsCriteria = DetachedCriteria.forClass(CnATreeElement.class)
                .add(Restrictions.in(OBJECT_TYPE,
                        new String[] { "bp_application", "bp_businessprocess", "bp_device",
                                "bp_icssystem", "bp_itnetwork", "bp_itsystem", "bp_network",
                                "bp_room" }))
                .add(Restrictions.in(SCOPE_ID, iTNetworkIDs));
        new RetrieveInfo().setProperties(true).setLinksDown(true).setLinksUp(true)
                .configureCriteria(targetObjectsCriteria);
        return elementDao.findByCriteria(targetObjectsCriteria);
    }

    @SuppressWarnings("unchecked")
    private List<CnATreeElement> loadRequirements() {
        DetachedCriteria requirementsCriteria = DetachedCriteria.forClass(CnATreeElement.class)
                .add(Restrictions.eq(OBJECT_TYPE, "bp_requirement"))
                .add(Restrictions.in(SCOPE_ID, iTNetworkIDs));
        RetrieveInfo.getPropertyInstance().setLinksDown(true).setLinksUp(true).setParent(true)
                .configureCriteria(requirementsCriteria);
        requirementsCriteria.setFetchMode("parent.entity", FetchMode.JOIN);
        return elementDao.findByCriteria(requirementsCriteria);
    }

    private static boolean isSafeguardGroup(CnATreeElement element) {
        return "bp_safeguard_group".equals(element.getTypeId());
    }

    private static boolean isBpRequirementGroup(CnATreeElement element) {
        return "bp_requirement_group".equals(element.getTypeId());
    }

    private static boolean isBpThreatGroup(CnATreeElement element) {
        return "bp_threat_group".equals(element.getTypeId());
    }

    private void initializeDaos() {
        elementDao = getDaoFactory().getDAO(CnATreeElement.class);
    }

    enum OperationMode {
        COPY, MOVE
    }

    @Override
    public double getVersion() {
        return VERSION;
    }
}