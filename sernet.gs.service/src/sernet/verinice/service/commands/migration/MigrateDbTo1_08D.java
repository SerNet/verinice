/*******************************************************************************
 * Copyright (c) 2021 Jochen Kemnade.
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

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.Permission;

/**
 * Add a unique indexes to the changelogentry table (VN-3103)
 */
public class MigrateDbTo1_08D extends DbMigration {

    private static final long serialVersionUID = 7708250228070887772L;
    private static final Logger logger = Logger.getLogger(MigrateDbTo1_08D.class);

    @Override
    public void execute() {
        IBaseDao<@NonNull Permission, Serializable> dao = getDaoFactory().getDAO(Permission.class);
        dao.executeCallback(session -> {
            session.createSQLQuery(
                    "CREATE INDEX changelogentry_changetime ON ChangeLogEntry(changetime)")
                    .executeUpdate();
            session.createSQLQuery(
                    "CREATE INDEX changelogentry_elementChange ON ChangeLogEntry(elementChange)")
                    .executeUpdate();
            session.createSQLQuery(
                    "CREATE INDEX changelogentry_stationid ON ChangeLogEntry(stationid)")
                    .executeUpdate();
            logger.info("Index sucessfully created");
            return null;
        });

        updateVersion();
    }

    @Override
    public double getVersion() {
        return 1.08D;
    }

}
