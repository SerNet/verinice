/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.Permission;

/**
 * Add default db indexes (VN-3105)
 */
public class MigrateDbTo1_09D extends DbMigration {

    private static final long serialVersionUID = 6926269654676235659L;

    @Override
    public void execute() {
        IBaseDao<@NonNull Permission, Serializable> dao = getDaoFactory().getDAO(Permission.class);

        dao.executeCallback(session -> {

            Stream.of("CREATE INDEX dependant_id_idx ON cnalink (dependant_id)",
                    "CREATE INDEX dependency_id_idx ON cnalink (dependency_id)",
                    "CREATE INDEX entity_id_idx ON cnatreeelement (entity_id)",
                    "CREATE INDEX parent_idx ON cnatreeelement (parent)",
                    "CREATE INDEX typedlist_id_idx ON propertylist (typedlist_id)",
                    "CREATE INDEX properties_id_idx ON properties (properties_id)",
                    "CREATE INDEX cte_id_idx ON permission (cte_id)",
                    "CREATE INDEX cnatreeelement_id_idx ON note (cnatreeelement_id)",
                    "CREATE INDEX cnalink_type_idx ON cnalink (type_id)",
                    "CREATE INDEX jbpm4_task_asignee_idx ON jbpm4_task (assignee_)",
                    "CREATE INDEX jbpm4_execution_parent_idx ON jbpm4_execution (parent_)",
                    "CREATE INDEX jbpm4_variable_execution_idx ON jbpm4_variable (execution_)")
                    .forEach(statement -> {
                        if (isPostgres() || isOracle()) {
                            statement = statement.replace("CREATE INDEX ",
                                    "CREATE INDEX IF NOT EXISTS ");
                        }
                        session.createSQLQuery(statement).executeUpdate();
                    });
            return null;
        });
        updateVersion();
    }

    @Override
    public double getVersion() {
        return 1.09D;
    }

}
