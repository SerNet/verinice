/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
package sernet.verinice.service.linktable.vlt;

import java.util.*;

import sernet.verinice.service.linktable.LinkTableConfiguration;

/**
 * Bean to (de-)serialize Link Table configuration to a VLT file. A VLT
 * (verinice link table) file is a JSON file with suffix '.vlt'. See JSON schema
 * VltSchema.json in this package.
 *
 * See {@link VeriniceLinkTableIO} for (de-)serialization. This class holds the
 * same dat as {@link LinkTableConfiguration} but is made for (de-)serialization
 * only.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class VeriniceLinkTable {

    public static final String VLT = ".vlt"; //$NON-NLS-1$
    
    private String id;
    private String name;
    private boolean useAllScopes;
    private List<Integer> scopeIds;
    private List<String> columnPaths;
    private List<String> relationIds;

    private VeriniceLinkTable() {
        super();
        this.id = UUID.randomUUID().toString();
    }

    private VeriniceLinkTable(Builder builder) {
        super();
        this.id = UUID.randomUUID().toString();
        this.name = builder.getName();
        this.setAllScopes(builder.useAllScopes());
        this.scopeIds = builder.getScopeIds();
        this.columnPaths = builder.getColumnPaths();
        this.relationIds = builder.getRelationIds();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getScopeIds() {
        return scopeIds;
    }

    public void setScopeIds(List<Integer> scopeIds) {
        this.scopeIds = scopeIds;
    }

    public void addScopeId(Integer scopeId) {
        this.scopeIds.add(scopeId);
    }

    public List<String> getColumnPaths() {
        return columnPaths;
    }

    public void setColumnPaths(List<String> columnPaths) {
        this.columnPaths = columnPaths;
    }

    public void addColumnPath(String columnPath) {
        this.columnPaths.add(columnPath);
    }

    public List<String> getRelationIds() {
        return relationIds;
    }

    public void setRelationIds(List<String> relationIds) {
        this.relationIds = relationIds;
    }

    public void addRelationId(String relationId) {
        this.relationIds.add(relationId);
    }

    public boolean useAllScopes() {
        return useAllScopes;
    }

    public void setAllScopes(boolean allScopes) {
        this.useAllScopes = allScopes;
    }


    public static class Builder {
        private String name;
        private boolean allScopes;
        private List<Integer> scopeIds = new LinkedList<>();
        private List<String> columnPaths = new LinkedList<>();
        private List<String> relationIds = new LinkedList<>();

        public Builder(String name) {
            this.name = name;
        }

        public VeriniceLinkTable build() {
            return new VeriniceLinkTable(this);
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public boolean useAllScopes() {
            return allScopes;
        }

        public List<Integer> getScopeIds() {
            return scopeIds;
        }

        public Builder setScopeIds(List<Integer> scopeIds) {
            this.scopeIds = scopeIds;
            return this;
        }

        public Builder setAllScopes(boolean allScopes) {
            this.allScopes = allScopes;
            return this;
        }

        public Builder addScopeId(Integer scopeId) {
            this.scopeIds.add(scopeId);
            return this;
        }

        public List<String> getColumnPaths() {
            return columnPaths;
        }

        public Builder setColumnPaths(List<String> columnPaths) {
            this.columnPaths = columnPaths;
            return this;
        }

        public Builder addColumnPath(String columnPath) {
            this.columnPaths.add(columnPath);
            return this;
        }

        public List<String> getRelationIds() {
            return relationIds;
        }

        public Builder setRelationIds(List<String> relationPaths) {
            this.relationIds = relationPaths;
            return this;
        }

        public Builder addRelationId(String relationId) {
            this.relationIds.add(relationId);
            return this;
        }

    }

}
