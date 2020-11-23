/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.consolidator;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;

import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.NonNullUtils;

/**
 * This holds the data for the table of {@link ModuleSelectionPage}.
 * <p>
 * This holds a {@link BpRequirementGroup}'s titles, the title of the parent and
 * scope, including abbreviations. It also has a reference to the original
 * BpRequirementGroup for later use.
 */
public class ConsolidatorTableContent {

    private @NonNull String title;
    private @NonNull String scope;
    private @NonNull String parent;
    private @NonNull BpRequirementGroup module;

    public @NonNull BpRequirementGroup getModule() {
        return module;
    }

    public @NonNull String getTitle() {
        return title;
    }

    public @NonNull String getScope() {
        return scope;
    }

    public @NonNull String getParent() {
        return parent;
    }

    private ConsolidatorTableContent(@NonNull String title, @NonNull String scope,
            @NonNull String parent, @NonNull BpRequirementGroup module) {
        this.title = title;
        this.scope = scope;
        this.parent = parent;
        this.module = module;
    }

    static List<@NonNull ConsolidatorTableContent> getContent(
            @NonNull Collection<Entry<BpRequirementGroup, ItNetwork>> requirementGroupsAndScopes) {
        return requirementGroupsAndScopes.stream().map(ConsolidatorTableContent::convert)
                .collect(Collectors.toList());
    }

    protected static @NonNull ConsolidatorTableContent convert(
            @NonNull Entry<BpRequirementGroup, ItNetwork> b) {
        BpRequirementGroup module = b.getKey();
        ItNetwork scope = b.getValue();
        CnATreeElement parent = module.getParent();
        return new ConsolidatorTableContent(NonNullUtils.toNonNull(module.getTitle()),
                toTitle(scope), toTitle(parent), module);
    }

    private static @NonNull String toTitle(CnATreeElement element) {
        if (element == null) {
            return "";
        }
        String prettyTitle = NonNullUtils.toNonNull(element.getTitle());
        if (element instanceof IAbbreviatedElement) {
            IAbbreviatedElement abb = (IAbbreviatedElement) element;
            if (!abb.getAbbreviation().isEmpty()) {
                prettyTitle = abb.getAbbreviation() + " " + prettyTitle;
            }
        }
        return prettyTitle;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((module == null) ? 0 : module.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
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
        ConsolidatorTableContent other = (ConsolidatorTableContent) obj;
        if (!parent.equals(other.parent))
            return false;
        if (!module.equals(other.module))
            return false;
        if (!scope.equals(other.scope))
            return false;
        return title.equals(other.title);
    }
}
