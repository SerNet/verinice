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
package sernet.verinice.service.linktable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration bean for {@link LinkTableService}.
 *
 * Use LinkTableConfiguration.Builder to create instances of this class:
 *
 * LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
 * builder.addScopeId(1)
 * .addColumnPath("asset<assetgroup.assetgroup_name")
 * .addColumnPath("asset.asset_name");
 * LinkTableConfiguration config = builder.build();
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LinkTableConfiguration implements ILinkTableConfiguration {


    private static final long serialVersionUID = 5296374920424255723L;
    
    private Set<String> columnPathes;
    private Set<String> linkTypeIds;
    private Set<Integer> scopeIds;

    /**
     * Use LinkTableConfiguration.Builder to create instances of this class.
     *
     * @param builder The builder for this class
     */
    private LinkTableConfiguration(Builder builder) {
        setColumnPathes(builder.getColumnPathes());
        setLinkTypeIds(builder.getLinkTypeIds());
        setScopeIds(builder.getScopeIds());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.ILinkTableConfiguration#getScopeIdArray()
     */
    @Override
    public Integer[] getScopeIdArray() {
        return getScopeIds().toArray(new Integer[getScopeIds().size()]);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.ILinkTableConfiguration#getColumnPathes()
     */
    @Override
    public Set<String> getColumnPaths() {
        if(columnPathes==null) {
            columnPathes = new HashSet<>();
        }
        return columnPathes;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.ILinkTableConfiguration#getObjectTypeIds()
     */
    @Override
    public Set<String> getObjectTypeIds() {
        Set<String> objectTypeIds = new HashSet<>();
        for (String columnPath : getColumnPathArray()) {
            objectTypeIds.addAll(ColumnPathParser.getObjectTypeIds(columnPath));
        }
        return objectTypeIds;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.linktable.ILinkTableConfiguration#getPropertyTypeIds()
     */
    @Override
    public Set<String> getPropertyTypeIds() {
        Set<String> propertyTypeIds = new HashSet<>();
        for(String columnPath : getColumnPathArray()){
            List<String> pathElements = ColumnPathParser.getColumnPathAsList(columnPath);
            pathElements = ColumnPathParser.removeAlias(pathElements);
            if(!pathElements.contains(":")) {
                propertyTypeIds.add(pathElements.get(pathElements.size()-1));
            }    
        }
        return propertyTypeIds;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.ILinkTableConfiguration#getLinkTypeIds()
     */
    @Override
    public Set<String> getLinkTypeIds() {
        if(linkTypeIds==null) {
            linkTypeIds = new HashSet<>();
        }
        return linkTypeIds;
    }

    public void setScopeIds(Set<Integer> scopeIds) {
        this.scopeIds = scopeIds;
    }

    @Override
    public void addScopeId(Integer scopeId) {
        getScopeIds().add(scopeId);
    }

    public Set<Integer> getScopeIds() {
        if(scopeIds==null) {
            scopeIds = new HashSet<>();
        }
        return scopeIds;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.linktable.ILinkTableConfiguration#removeAllScopeIds()
     */
    @Override
    public void removeAllScopeIds() {
        getScopeIds().clear();
    }

    public String[] getColumnPathArray() {
        return columnPathes.toArray(new String[columnPathes.size()]);
    }

    public void setColumnPathes(Set<String> columnPathes) {
        this.columnPathes = columnPathes;
    }

    public void addColumnPath(String columnPath) {
        getColumnPaths().add(columnPath);
    }

    public String[] getObjectTypeIdArray() {
        Set<String> objectIds = getObjectTypeIds();
        return objectIds.toArray(new String[objectIds.size()]);
    }

    public void setLinkTypeIds(Set<String> linkTypeIds) {
        this.linkTypeIds = linkTypeIds;
    }

    public void addLinkTypeId(String linkTypeId) {
        getLinkTypeIds().add(linkTypeId);
    }

    public static class Builder {
        private Set<String> columnPathes = new LinkedHashSet<>();
        private Set<String> linkTypeIds = new HashSet<>();
        private Set<Integer> scopeIds = new HashSet<>();

        public Builder() {
            super();
        }

        public Builder(Integer scopeId, Set<String> columnPathes) {
            super();
            getScopeIds().add(scopeId);
            this.columnPathes = columnPathes;
        }

        public Builder(Set<Integer> scopeIds, Set<String> columnPathes) {
            super();
            this.scopeIds = scopeIds;
            this.columnPathes = columnPathes;
        }

        public LinkTableConfiguration build() {
            return new LinkTableConfiguration(this);
        }

        public Builder setColumnPathes(Set<String> columnPathes) {
            this.columnPathes = columnPathes;
            return this;
        }

        public Builder addColumnPath(String columnPath) {
            getColumnPathes().add(columnPath);
            return this;
        }

        public Builder setLinkTypeIds(Set<String> linkTypeIds) {
            this.linkTypeIds = linkTypeIds;
            return this;
        }

        public Builder addLinkTypeId(String linkTypeId) {
            getLinkTypeIds().add(linkTypeId);
            return this;
        }

        public Builder setScopeIds(Set<Integer> scopeIds) {
            this.scopeIds = scopeIds;
            return this;
        }

        public Builder addScopeId(Integer scopeId) {
            getScopeIds().add(scopeId);
            return this;
        }

        public Set<String> getColumnPathes() {
            return columnPathes;
        }

        public Set<String> getLinkTypeIds() {
            return linkTypeIds;
        }

        public Set<Integer> getScopeIds() {
            return scopeIds;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columnPathes == null) ? 0 : columnPathes.hashCode());
        result = prime * result + ((linkTypeIds == null) ? 0 : linkTypeIds.hashCode());
        result = prime * result + ((scopeIds == null) ? 0 : scopeIds.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkTableConfiguration other = (LinkTableConfiguration) obj;
        if (columnPathes == null) {
            if (other.columnPathes != null)
                return false;
        } else if (!columnPathes.equals(other.columnPathes))
            return false;
        if (linkTypeIds == null) {
            if (other.linkTypeIds != null)
                return false;
        } else if (!linkTypeIds.equals(other.linkTypeIds))
            return false;
        if (scopeIds == null) {
            if (other.scopeIds != null)
                return false;
        } else if (!scopeIds.equals(other.scopeIds))
            return false;
        return true;
    }

}
