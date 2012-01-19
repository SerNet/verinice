/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service.commands;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27Scope;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@SuppressWarnings("restriction")
public class RetrieveCnATreeElement extends GenericCommand {

    public static final String NO_TAG = "NO_TAG"; //$NON-NLS-1$
    
    public static final String PARAM_TYPE_IDS = "type_ids";
    public static final String PARAM_TAGS = "tags";
    public static final String PARAM_FILTER_ORGS = "filter_orgs";
    
    
    public static String[] ALL_TYPES = new String[]{"ALL_TYPES","ALL_TYPES"};
    
	private CnATreeElement element;
	
	private RetrieveInfo retrieveInfo;

	private Integer dbId;

    private String typeId;
    
    private Map<String, Object> parameter;
	
    public RetrieveCnATreeElement(String typeId, Integer dbId) {
	    this.typeId = typeId;
		this.dbId = dbId;
	}
	

	public RetrieveCnATreeElement(String typeId, Integer dbId, RetrieveInfo retrieveInfo) {
	    this.typeId = typeId;
		this.dbId = dbId;
		this.retrieveInfo = retrieveInfo;
	}
	
	public RetrieveCnATreeElement(String typeId, Integer dbId, RetrieveInfo retrieveInfo, Map<String, Object> parameter) {
        this.typeId = typeId;
        this.dbId = dbId;
        this.retrieveInfo = retrieveInfo;
        this.parameter = parameter;
    }
	
	
	/**
	 * @param dbId2
	 * @return
	 */
	public static RetrieveCnATreeElement getISO27KModelISMViewInstance(Integer dbId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(ISO27KModel.TYPE_ID, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setPermissions(true).setChildren(true).setChildrenProperties(true).setChildrenPermissions(true).setGrandchildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	public static RetrieveCnATreeElement getOrganizationISMViewInstance(Integer dbId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(Organization.TYPE_ID, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setPermissions(true).setProperties(true).setChildren(true).setChildrenPermissions(true).setChildrenProperties(true).setGrandchildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	public static RetrieveCnATreeElement getGroupISMViewInstance(Integer dbId, String typeId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(typeId, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setProperties(true).setPermissions(true).setChildren(true).setChildrenPermissions(true).setChildrenProperties(true).setGrandchildren(true).setParent(true).setSiblings(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	public static RetrieveCnATreeElement getElementISMViewInstance(Integer dbId, String typeId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(typeId, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setPermissions(true).setProperties(true).setChildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
		element = dao.retrieve(dbId,getRetrieveInfo());
		applyParameter();
	}

    private void applyParameter() {
        if(parameter!=null && element!=null && !parameter.isEmpty()) {
            Set<IFilter> filterSet = new HashSet<IFilter>();
            Set<String[]> typeIdSet = (Set<String[]>) parameter.get(PARAM_TYPE_IDS);
            if(typeIdSet!=null && !typeIdSet.isEmpty()) {
                filterSet.add(new TypeFilter(typeIdSet));
            }
            String[] tagArray = (String[]) parameter.get(PARAM_TAGS);
            Object filterOrgsParam = parameter.get(PARAM_FILTER_ORGS);
            boolean filterOrgs = filterOrgsParam!=null && ((Boolean)filterOrgsParam).booleanValue();
            if(tagArray!=null && tagArray.length>0) {
                filterSet.add(new TagFilter(tagArray,filterOrgs));
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
    
    private boolean checkElement(CnATreeElement element, Set<IFilter> filterSet) {
        boolean result = true;
        for (IFilter filter : filterSet) {
            if(!filter.check(element)) {
                result = false;
                break;
            }
        }
        return result;
    }

    public void setElement(CnATreeElement element) {
		this.element = element;
	}

	public CnATreeElement getElement() {
		return element;
	}

	public void setRetrieveInfo(RetrieveInfo retrieveInfo) {
		this.retrieveInfo = retrieveInfo;
	}

	public RetrieveInfo getRetrieveInfo() {
		return retrieveInfo;
	}


    /**
     * @return the parameter
     */
    public Map<String, Object> getParameter() {
        return parameter;
    }


    /**
     * @param parameter the parameter to set
     */
    public void setParameter(Map<String, Object> parameter) {
        this.parameter = parameter;
    }
    
    class TypeFilter implements IFilter {

        Set<String[]> visibleTypeSet;

        /**
         * @param visibleTypeSet
         */
        public TypeFilter(Set<String[]> visibleTypeSet) {
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
                    result = strings[0].equals(RetrieveCnATreeElement.ALL_TYPES[0]) || strings[0].equals(typeId) || strings[1].equals(typeId);          
                }
            }
            return result;
        }
        
    }
    
    class TagFilter implements IFilter {

        String[] tagArray;
        boolean filterOrgs;

        /**
         * @param filterOrgs 
         * @param visibleTypeSet
         */
        public TagFilter(String[] tagArray, boolean filterOrgs) {
            super();
            this.tagArray = tagArray;
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
                        if (tag.equals(NO_TAG)) {
                            if (iso.getTags().size() < 1) {
                                result = true;
                            }
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
