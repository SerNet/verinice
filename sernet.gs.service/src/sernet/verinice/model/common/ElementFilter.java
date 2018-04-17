/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.interfaces.IFilter;
import sernet.verinice.interfaces.IParameter;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27Scope;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * Do not instantiate this class use public final methods.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class ElementFilter {

    public static final String NO_TAG = "NO_TAG"; //$NON-NLS-1$
    public static final String PARAM_TYPE_IDS = "type_ids"; //$NON-NLS-1$
    public static final String PARAM_TAGS = "tags"; //$NON-NLS-1$
    public static final String PARAM_FILTER_ORGS = "filter_orgs"; //$NON-NLS-1$
    public static final String[] ALL_TYPES = new String[] { "ALL_TYPES", "ALL_TYPES" }; //$NON-NLS-1$

    private ElementFilter() {
        super();
    }

    /**
     * Filters the element's children with the filter parameters.
     * 
     * @param element
     * @param filterParameters
     */
    public static void filterChildrenOfElement(CnATreeElement element,
            Map<String, Object> filterParameters) {
        if (filterParameters != null && element != null && !filterParameters.isEmpty()) {
            Set<IFilter> filterSet = new HashSet<>();
            Set<String[]> typeIdSet = (Set<String[]>) filterParameters.get(PARAM_TYPE_IDS);
            if (typeIdSet != null && !typeIdSet.isEmpty()) {
                filterSet.add(TypeFilter.createFilter(typeIdSet));
            }
            String[] tagArray = (String[]) filterParameters.get(PARAM_TAGS);
            Object filterOrgsParam = filterParameters.get(PARAM_FILTER_ORGS);
            boolean filterOrgs = filterOrgsParam != null
                    && ((Boolean) filterOrgsParam).booleanValue();
            if (tagArray != null && tagArray.length > 0) {
                filterSet.add(TagFilter.createFilter(tagArray, filterOrgs));
            }
            Set<CnATreeElement> children = element.getChildren();
            Set<CnATreeElement> childrenFiltered = new HashSet<>();
            for (CnATreeElement child : children) {
                if (checkElement(child, filterSet)) {
                    childrenFiltered.add(child);
                }
            }
            element.setChildren(childrenFiltered);
        }
    }

    public static Map<String, Object> convertToMap(List<IParameter> paramerterList) {
        if (paramerterList == null || paramerterList.isEmpty()) {
            return null;
        }
        Map<String, Object> parameterMap = null;
        parameterMap = new Hashtable<>();
        for (IParameter param : paramerterList) {
            if (param instanceof TypeParameter) {
                addTypeParameter(parameterMap, param);
            }
            if (param instanceof TagParameter) {
                addTagParameter(parameterMap, param);
            }
        }
        return parameterMap;
    }

    private static void addTagParameter(Map<String, Object> parameterMap, IParameter param) {
        TagParameter tagParameter = (TagParameter) param;
        String[] tagArray = tagParameter.getPattern();
        if (tagArray != null && tagArray.length > 0) {
            parameterMap.put(ElementFilter.PARAM_TAGS, tagParameter.getPattern());
            parameterMap.put(ElementFilter.PARAM_FILTER_ORGS, tagParameter.isFilterOrg());
        }
    }

    private static void addTypeParameter(Map<String, Object> parameterMap, IParameter param) {
        Set<String[]> typeIdSet = (Set<String[]>) param.getParameter();
        if (typeIdSet != null && !typeIdSet.isEmpty()) {
            String[] typeIdArray = typeIdSet.iterator().next();
            if (typeIdSet.size() > 1
                    || !Arrays.equals(typeIdArray, ElementFilter.ALL_TYPES)) {
                parameterMap.put(ElementFilter.PARAM_TYPE_IDS, typeIdSet);
            }
        }
    }

    private static boolean checkElement(CnATreeElement element, Set<IFilter> filterSet) {
        boolean result = true;
        for (IFilter filter : filterSet) {
            if (!filter.check(element)) {
                result = false;
                break;
            }
        }
        return result;
    }

    static class TypeFilter implements IFilter {

        private Set<String[]> visibleTypeSet;

        public static TypeFilter createFilter(Set<String[]> visibleTypeSet) {
            return new TypeFilter(visibleTypeSet);
        }

        protected TypeFilter(Set<String[]> visibleTypeSet) {
            super();
            this.visibleTypeSet = visibleTypeSet;
        }

        @Override
        public boolean check(CnATreeElement element) {
            return element.isScope() || contains(visibleTypeSet, element.getTypeId());
        }

        private boolean contains(Set<String[]> visibleTypePairs, String typeId) {
            for (String[] visibleTypePair : visibleTypePairs) {
                String visibleElementType = visibleTypePair[0];
                String visibleGroupType = visibleTypePair[1];
                if (visibleElementType.equals(ALL_TYPES[0]) || visibleElementType.equals(typeId)
                        || visibleGroupType.equals(typeId)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class TagFilter implements IFilter {
        private String[] tagArray;
        private boolean filterOrgs;

        public static TagFilter createFilter(String[] tagArray, boolean filterOrgs) {
            return new TagFilter(tagArray, filterOrgs);
        }

        protected TagFilter(String[] tagArray, boolean filterOrgs) {
            super();
            this.tagArray = (tagArray != null) ? tagArray.clone() : null;
            this.filterOrgs = filterOrgs;
        }

        /*
         * @see
         * sernet.verinice.iso27k.service.commands.IFilter#check(sernet.verinice
         * .model.common.CnATreeElement)
         */
        @Override
        public boolean check(CnATreeElement element) {
            if (ArrayUtils.isEmpty(tagArray)) {
                return true;
            }
            if (Organization.TYPE_ID.equals(element.getTypeId())) {
                if (filterOrgs) {
                    return checkTags(tagArray, (Organization) element);
                } else {
                    return true;
                }
            } else if (element instanceof IISO27kElement && !(element instanceof Group)
                    && !(element instanceof IISO27Scope)) {
                return checkTags(tagArray, (IISO27kElement) element);
            } else {
                return true;
            }
        }

        private static boolean checkTags(String[] tagsfFromFilter, ITaggableElement element) {
            Collection<String> tagsFromElement = element.getTags();
            for (String tag : tagsfFromFilter) {
                if (tag.equals(NO_TAG) && tagsFromElement.isEmpty()) {
                    return true;
                }
                if (tagsFromElement.contains(tag)) {
                    return true;
                }
            }
            return false;
        }
    }
}
