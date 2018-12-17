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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.service.commands.SaveElement;
import sernet.verinice.service.model.LoadModel;

/**
 * This service migrates the modeling of an IT network from version 1.16 to
 * 1.17. It is used by the service ModelingMigrationDatabaseService(Impl).
 */
public class ModelingMigrationServiceImpl implements ModelingMigrationService {

    private static final Logger log = Logger.getLogger(ModelingMigrationServiceImpl.class);

    public static final double DB_VERSION = 1.06D;

    private static final String ERROR_MESSAGE_IT_NETWORK = "Error migrating modeling in IT network with database ID: %d";
    private static final String ERROR_MESSAGE_LOAD_BSI_MODEL = "Error loading BSIModel";
    private static final String ERROR_MESSAGE_UPDATE_DB_VERSION = "Error updating database version";

    private ICommandService commandService;
    private ObjectFactory migrateItNetworkJobFactory;

    @Override
    public void migrateModelingOfItNetwork(Integer itNetworkDbId) {
        try {
            MigrateItNetworkJob itNetworkJob = (MigrateItNetworkJob) migrateItNetworkJobFactory
                    .getObject();
            itNetworkJob.setItNetworkDbId(itNetworkDbId);
            itNetworkJob.migrateModeling();
        } catch (MigrationException e) {
            log.error(String.format(ERROR_MESSAGE_IT_NETWORK, itNetworkDbId), e);
            throw e;
        } catch (Exception e) {
            String message = String.format(ERROR_MESSAGE_IT_NETWORK, itNetworkDbId);
            log.error(message, e);
            throw new MigrationException(message, e);
        }
    }

    @Override
    public synchronized boolean isMigrationRequired() {
        BSIModel bsiModel = loadBsiModel();
        if (bsiModel != null) {
            double dbVersion = bsiModel.getDbVersion();
            if (dbVersion < DB_VERSION) {
                updateDbVersion();
                return true;
            }
        }
        return false;
    }

    protected void updateDbVersion() {
        try {
            BSIModel bsiModel = loadBsiModel();
            bsiModel.setDbVersion(DB_VERSION);
            SaveElement<BSIModel> saveElementCommand = new SaveElement<>(bsiModel);
            getCommandService().executeCommand(saveElementCommand);
        } catch (MigrationException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_MESSAGE_UPDATE_DB_VERSION, e);
            throw new MigrationException(ERROR_MESSAGE_UPDATE_DB_VERSION, e);
        }
    }

    public BSIModel loadBsiModel() {
        try {
            LoadModel<BSIModel> command = new LoadModel<>(BSIModel.class);
            command = getCommandService().executeCommand(command);
            return command.getModel();
        } catch (Exception e) {
            log.error(ERROR_MESSAGE_LOAD_BSI_MODEL, e);
            throw new MigrationException(ERROR_MESSAGE_LOAD_BSI_MODEL, e);
        }
    }


    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    public void setMigrateItNetworkJobFactory(ObjectFactory migrateItNetworkJobFactory) {
        this.migrateItNetworkJobFactory = migrateItNetworkJobFactory;
    }

}

