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

import java.util.Set;

/**
 * Helper class to generate SQL code
 */
public final class SqlHelper {

    private static final String SQL_INSERT_LINK = "INSERT INTO cnalink "
            + "(dependant_id,dependency_id,type_id,linktype,\"comment\") "
            + "VALUES (:dependantId,:dependencyId,:linkType,1,:comment)";

    private static final String SQL_INSERT_LINK_WITHOUT_VALUES = "INSERT INTO cnalink "
            + "(dependant_id,dependency_id,type_id,linktype,\"comment\") ";

    private SqlHelper() {
        super();
    }

    public static String generateDefaultSqlToCreateLinks(Set<CreateLinkData> createLinkDataSet) {
        StringBuilder sb = new StringBuilder(SQL_INSERT_LINK_WITHOUT_VALUES);
        sb.append("VALUES ");
        return generateDefaultSqlToCreateLinks(createLinkDataSet, sb);
    }

    public static String generateDerbySqlToCreateLinks(Set<CreateLinkData> createLinkDataSet) {
        StringBuilder sb = new StringBuilder(
                SQL_INSERT_LINK_WITHOUT_VALUES.replace("\"comment\"", "comment"));
        sb.append("VALUES ");
        return generateDefaultSqlToCreateLinks(createLinkDataSet, sb);
    }

    private static String generateDefaultSqlToCreateLinks(Set<CreateLinkData> createLinkDataSet,
            StringBuilder sb) {
        boolean first = true;
        for (CreateLinkData data : createLinkDataSet) {
            if (!first) {
                sb.append(",");
            }
            append(sb, data);
            first = false;
        }
        return sb.toString();
    }

    public static String generateOracleSqlToCreateLinks(Set<CreateLinkData> createLinkDataSet) {
        StringBuilder sb = new StringBuilder(SQL_INSERT_LINK_WITHOUT_VALUES);
        boolean first = true;
        for (CreateLinkData data : createLinkDataSet) {
            if (!first) {
                sb.append("UNION ");
            }
            appendForOracle(sb, data);
            first = false;
        }
        return sb.toString();
    }

    private static void append(StringBuilder sb, CreateLinkData data) {
        sb.append("(");
        sb.append(data.getDependantId()).append(",");
        sb.append(data.getDependencyId()).append(",");
        sb.append("'").append(data.getRelationId())
                .append("',1,'Created during modeling update to 1.17')");
    }

    private static void appendForOracle(StringBuilder sb, CreateLinkData data) {
        sb.append("SELECT ");
        sb.append(data.getDependantId()).append(",");
        sb.append(data.getDependencyId()).append(",");
        sb.append("'").append(data.getRelationId())
                .append("',1,'Created during modeling update to 1.17' FROM DUAL ");
    }

    public static String generateDerbySqlToCreateLink() {
        return SQL_INSERT_LINK.replace("\"comment\"", "comment");
    }

    public static String generateDefaultSqlToCreateLink() {
        return SQL_INSERT_LINK;
    }


}
