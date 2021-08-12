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
import org.hibernate.SQLQuery;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.Permission;

/**
 * Add a unique index to the permission table (VN-2589)
 */
public class MigrateDbTo1_07D extends DbMigration {

    private static final long serialVersionUID = -4364557665875723661L;
    private static final Logger logger = Logger.getLogger(MigrateDbTo1_07D.class);

    @Override
    public void execute() {
        IBaseDao<@NonNull Permission, Serializable> dao = getDaoFactory().getDAO(Permission.class);
        dao.executeCallback(session -> {
            SQLQuery query = session.createSQLQuery(
                    "CREATE UNIQUE INDEX permission_cte_id_role ON Permission(cte_id, role)");
            query.executeUpdate();
            logger.info("Index sucessfully created");
            return null;
        });

        updateVersion();
    }

    @Override
    public double getVersion() {
        return 1.07D;
    }

}
