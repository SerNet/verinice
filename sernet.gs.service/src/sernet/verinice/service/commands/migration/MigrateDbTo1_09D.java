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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

            Set<String> existingIndexes = new HashSet<>();
            if (isOracle()) {
                existingIndexes.addAll((List<String>) session
                        .createSQLQuery("select index_name from USER_INDEXES").list());
            }

            Stream.of("dependant_id_idx ON cnalink (dependant_id)",
                    "dependency_id_idx ON cnalink (dependency_id)",
                    "entity_id_idx ON cnatreeelement (entity_id)",
                    "parent_idx ON cnatreeelement (parent)",
                    "typedlist_id_idx ON propertylist (typedlist_id)",
                    "properties_id_idx ON properties (properties_id)",
                    "cte_id_idx ON permission (cte_id)",
                    "cnatreeelement_id_idx ON note (cnatreeelement_id)",
                    "cnalink_type_idx ON cnalink (type_id)",
                    "jbpm4_task_asignee_idx ON jbpm4_task (assignee_)",
                    "jbpm4_execution_parent_idx ON jbpm4_execution (parent_)",
                    "jbpm4_variable_execution_idx ON jbpm4_variable (execution_)").forEach(spec -> {
                        String statement = null;
                        if (isPostgres()) {
                            statement = "CREATE INDEX IF NOT EXISTS " + spec;
                        } else if (isOracle()) {
                            String indexName = spec.substring(0, spec.indexOf(' '));
                            if (existingIndexes.contains(indexName)) {
                                return;
                            }
                            statement = "CREATE INDEX " + spec;
                        } else if (isDerby()) {
                            // derby seems to just ignore duplicate indexes, so
                            // no special precautions here
                            statement = "CREATE INDEX " + spec;
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
