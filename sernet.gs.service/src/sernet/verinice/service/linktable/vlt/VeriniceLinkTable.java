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
 * same data as {@link LinkTableConfiguration} but is made for (de-)serialization
 * only.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class VeriniceLinkTable {

    public static final String VLT = ".vlt"; //$NON-NLS-1$
    
    private boolean useAllScopes;
    private List<Integer> scopeIds;
    private List<String> columnPaths;
    private List<String> relationIds;

    private VeriniceLinkTable() {
        super();
    }

    private VeriniceLinkTable(Builder builder) {
        super();
        this.setAllScopes(builder.useAllScopes());
        this.scopeIds = builder.getScopeIds();
        this.columnPaths = builder.getColumnPaths();
        this.relationIds = builder.getRelationIds();
    }

    public List<Integer> getScopeIds() {
        return scopeIds;
    }

    public void setScopeIds(List<Integer> scopeIds) {
        this.scopeIds = scopeIds;
    }

    public void addScopeId(Integer scopeId) {
        if (scopeIds == null) {
            scopeIds = new ArrayList<>();
        }
        this.scopeIds.add(scopeId);
    }
    
    public void clearScopeIds() {
        if (scopeIds == null) {
            scopeIds = new ArrayList<>();
        } else {
            this.scopeIds.clear();
        }
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


    @Override
    public String toString() {
        return "VeriniceLinkTable [useAllScopes=" + useAllScopes
                + ", scopeIds=" + scopeIds + ", columnPaths=" + columnPaths + ", relationIds="
                + relationIds + "]";
    }

    public static class Builder {
        private boolean allScopes;
        private List<Integer> scopeIds = new LinkedList<>();
        private List<String> columnPaths = new LinkedList<>();
        private List<String> relationIds = new LinkedList<>();

        public Builder() {
        }

        public VeriniceLinkTable build() {
            return new VeriniceLinkTable(this);
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
