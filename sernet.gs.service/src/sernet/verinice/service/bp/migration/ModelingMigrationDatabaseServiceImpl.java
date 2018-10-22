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
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This service migrates the modeling in ITBP from version 1.16 to 1.17.
 * 
 * The migration of all IT networks will only be performed if the version of the
 * database is less than 1.06. If this is the case, then the version of the
 * database is updated to 1.06 before the migration is performed. Even if the
 * migration fails due to an error, the version of the database remains 1.06.
 * This causes the migration to be performed exactly once.
 * 
 * Migration can be deactivated with a system property. If this is required, the
 * property must be added to the verinice.ini file in standalone mode or to the
 * tomcat6.conf file on the verinice.PRO server:
 * 
 * -Dveriniceserver.itbp.migration.1-16to1-17=false
 */
public class ModelingMigrationDatabaseServiceImpl implements ModelingMigrationDatabaseService {

    private static final Logger log = Logger.getLogger(ModelingMigrationDatabaseServiceImpl.class);

    private static final String VERINICESERVER_ITBP_MIGRATION_1_16TO1_17 = "veriniceserver.itbp.migration.1-16to1-17";
    public static final double DB_VERSION = 1.06D;

    private static final String ERROR_MESSAGE = "Error migrating modeling";

    private ModelingMigrationService modelingMigrationService;
    private IBaseDao<CnATreeElement, Serializable> elementDao;

    @Override
    public void migrateModeling() { // throws MigrationException
        try {
            if (!getModelingMigrationService().isMigrationRequired()) {
                if (log.isInfoEnabled()) {
                    log.info(
                            "The migration to new ITBP modeling has already been completed or is in progress");
                }
                return;
            }
            if (!isMigrationActivated()) {
                log.warn("System property: " + VERINICESERVER_ITBP_MIGRATION_1_16TO1_17
                        + " is false. Skipping migration to new ITBP modeling.  ");
                return;
            }
            doMigration();
        } catch (MigrationException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            throw new MigrationException(ERROR_MESSAGE, e);
        }
    }

    private boolean isMigrationActivated() {
        return !"false".equals(System.getProperty(VERINICESERVER_ITBP_MIGRATION_1_16TO1_17));
    }

    private void doMigration() {
        List<Integer> itNetworkIds = loadItNetworkIds();
        if (log.isInfoEnabled()) {
            log.info("Migrating modeling of " + itNetworkIds.size() + " IT networks...");
        }
        for (Integer itNetworkDbId : itNetworkIds) {
            getModelingMigrationService().migrateModelingOfItNetwork(itNetworkDbId);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Integer> loadItNetworkIds() {
        DetachedCriteria itNetworkIDsCriteria = DetachedCriteria.forClass(CnATreeElement.class)
                .createAlias("parent", "p").add(Restrictions.eq("objectType", "bp_itnetwork"))
                .add(Restrictions.in("p.objectType",
                        new String[] { "bp_model", "bp_import_group" }))
                .setProjection(Projections.property("dbId"));
        return elementDao.findByCriteria(itNetworkIDsCriteria);
    }


    public ModelingMigrationService getModelingMigrationService() {
        return modelingMigrationService;
    }

    public void setModelingMigrationService(ModelingMigrationService modelingMigrationService) {
        this.modelingMigrationService = modelingMigrationService;
    }

    public IBaseDao<CnATreeElement, Serializable> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Serializable> elementDao) {
        this.elementDao = elementDao;
    }


}

