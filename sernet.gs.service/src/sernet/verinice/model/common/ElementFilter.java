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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.verinice.interfaces.IFilter;
import sernet.verinice.interfaces.IParameter;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27Scope;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public abstract class ElementFilter {

    public static final String NO_TAG = "NO_TAG"; //$NON-NLS-1$
    public static final String PARAM_TYPE_IDS = "type_ids"; //$NON-NLS-1$
    public static final String PARAM_TAGS = "tags"; //$NON-NLS-1$
    public static final String PARAM_FILTER_ORGS = "filter_orgs"; //$NON-NLS-1$
    public static final String[] ALL_TYPES = new String[]{"ALL_TYPES","ALL_TYPES"}; //$NON-NLS-1$ //$NON-NLS-1$
    
    public static void applyParameter(CnATreeElement element, Map<String, Object> parameter) {
        if(parameter!=null && element!=null && !parameter.isEmpty()) {
            Set<IFilter> filterSet = new HashSet<IFilter>();
            Set<String[]> typeIdSet = (Set<String[]>) parameter.get(PARAM_TYPE_IDS);
            if(typeIdSet!=null && !typeIdSet.isEmpty()) {
                filterSet.add(TypeFilter.createFilter(typeIdSet));
            }
            String[] tagArray = (String[]) parameter.get(PARAM_TAGS);
            Object filterOrgsParam = parameter.get(PARAM_FILTER_ORGS);
            boolean filterOrgs = filterOrgsParam!=null && ((Boolean)filterOrgsParam).booleanValue();
            if(tagArray!=null && tagArray.length>0) {
                filterSet.add(TagFilter.createFilter(tagArray,filterOrgs));
            }
            Set<CnATreeElement> children = element.getChildren();
            Set<CnATreeElement> childrenFiltered = new HashSet<CnATreeElement>();
            for (CnATreeElement child : children) {
                if(checkElement(child, filterSet)) {
                    childrenFiltered.add(child);
                }
            }
            element.setChildren(childrenFiltered);        
        }       
    }
    
    /**
     * @return
     */
    public static Map<String, Object> getConvertToMap(List<IParameter> paramerterList) {
        Map<String, Object> result = null;
        if(paramerterList!=null && !paramerterList.isEmpty()) {
            result = new Hashtable<String, Object>();
            for (IParameter param : paramerterList) {
               if(param instanceof TypeParameter) {              
                   Set<String[]> typeIdSet = (Set<String[]>) param.getParameter();
                   if(typeIdSet!=null && !typeIdSet.isEmpty()) {
                       String[] typeIdArray = typeIdSet.iterator().next();
                       if(typeIdSet.size()>1 || !Arrays.equals(typeIdArray,ElementFilter.ALL_TYPES)) {
                           result.put(ElementFilter.PARAM_TYPE_IDS, typeIdSet);
                       } 
                   }
               }
               if(param instanceof TagParameter ) {
                   TagParameter tagParameter = (TagParameter) param;
                   String[] tagArray = tagParameter.getPattern();
                   if(tagArray!=null && tagArray.length>0) {
                       result.put(ElementFilter.PARAM_TAGS, tagParameter.getPattern());
                       result.put(ElementFilter.PARAM_FILTER_ORGS, tagParameter.isFilterOrg());
                   }
               }
            }
        }
        return result;
    }
    
    private static boolean checkElement(CnATreeElement element, Set<IFilter> filterSet) {
        boolean result = true;
        for (IFilter filter : filterSet) {
            if(!filter.check(element)) {
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
        
        /**
         * @param visibleTypeSet
         */
        protected TypeFilter(Set<String[]> visibleTypeSet) {
            super();
            this.visibleTypeSet = visibleTypeSet;
        }

        /* (non-Javadoc)
         * @see sernet.verinice.iso27k.service.commands.IFilter#check(sernet.verinice.model.common.CnATreeElement)
         */
        @Override
        public boolean check(CnATreeElement element) {
            return contains(visibleTypeSet,element.getTypeId());
        }
        
        private boolean contains(Set<String[]> visibleTypeSet, String typeId) {
            boolean result = Organization.TYPE_ID.equals(typeId) || ISO27KModel.TYPE_ID.equals(typeId);
            if(!result) {
                for (Iterator<String[]> iterator = visibleTypeSet.iterator(); iterator.hasNext() && !result;) {
                    String[] strings = iterator.next();
                    result = strings[0].equals(ALL_TYPES[0]) || strings[0].equals(typeId) || strings[1].equals(typeId);          
                }
            }
            return result;
        }
        
    }
    
    static class TagFilter implements IFilter {

        private String[] tagArray;
        private boolean filterOrgs;

        public static TagFilter createFilter(String[] tagArray, boolean filterOrgs) {
            return new TagFilter(tagArray,filterOrgs);
        }
        
        /**
         * @param filterOrgs 
         * @param visibleTypeSet
         */
        protected TagFilter(String[] tagArray, boolean filterOrgs) {
            super();
            this.tagArray = (tagArray != null) ? tagArray.clone() : null;
            this.filterOrgs = filterOrgs;
        }

        /* (non-Javadoc)
         * @see sernet.verinice.iso27k.service.commands.IFilter#check(sernet.verinice.model.common.CnATreeElement)
         */
        @Override
        public boolean check(CnATreeElement element) {
            boolean result = true;
            if(tagArray!=null && tagArray.length>0) {
                if(filterOrgs 
                   && Organization.TYPE_ID.equals(element.getTypeId()) ) {
                    result = false;
                    Collection<String> tagList = TagHelper.getTags(element.getEntity().getSimpleValue(Organization.PROP_TAG));
                    for (String tag : tagArray) {
                        if (tag.equals(NO_TAG)) {
                            if (tagList.size() < 1) {
                                result = true;
                            }
                        }
                        for (String zielTag : tagList) {
                            if (zielTag.equals(tag)) {
                                result = true;
                            }
                        }
                    }
                } else if (!filterOrgs
                    &&   element instanceof IISO27kElement 
                    && !(element instanceof Group)
                    && !(element instanceof IISO27Scope)) {
                    result = false;
                    IISO27kElement iso = (IISO27kElement) element;
                    for (String tag : tagArray) {
                        if (tag.equals(NO_TAG) && iso.getTags().size() < 1) {
                            result = true;
                        }
                        for (String zielTag : iso.getTags()) {
                            if (zielTag.equals(tag)) {
                                result = true;
                            }
                        }
                    }
                }
            }
            return result;
        }

    }
}
