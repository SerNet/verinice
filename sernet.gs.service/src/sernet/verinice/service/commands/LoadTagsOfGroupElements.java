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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command loads all tags of all children of a group.
 * The set of tags is always sorted after loading.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LoadTagsOfGroupElements extends GenericCommand {

    private static final String GSM_PREFIX = "gsm_";
    
    private static final String GSM_ISM_PREFIX = "gsm_ism_";
    
    private static final String TAG_SUFFIX = "_tag";
    
    private String groupUuid;
    
    private Set<String> tagSet = new TreeSet<String>();
    
    private transient IBaseDao<CnATreeElement, Serializable> elementDao;

    public LoadTagsOfGroupElements(String uuid) {
        this.groupUuid = uuid;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.commands.LoadElementByUuid#execute()
     */
    @Override
    public void execute() {
        tagSet.clear();
        if(this.groupUuid==null) {
            return;
        }
        RetrieveInfo ri = new RetrieveInfo();
        ri.setChildren(true);
        ri.setChildrenProperties(true);
        CnATreeElement group = getElementDao().findByUuid(this.groupUuid, ri);
        if(group!=null) {
            for (CnATreeElement child : group.getChildren()) {
                tagSet.addAll(LoadTagsOfGroupElements.getTagList(child));            
                tagSet.addAll(LoadTagsOfGroupElements.getGsmTagList(child));                         
                tagSet.addAll(LoadTagsOfGroupElements.getGsmIsmTagList(child));
            }
        }
    }

    public static Collection<String> getTagList(CnATreeElement child) {
        return TagHelper.getTags(child.getEntity().getSimpleValue(LoadTagsOfGroupElements.generateTagPropertyName(child.getTypeId())));
    }
    private static String generateTagPropertyName(String typeId) {
        return new StringBuilder(typeId).append(TAG_SUFFIX).toString();
    }

    public static Collection<String> getGsmTagList(CnATreeElement child) {
        return TagHelper.getTags(child.getEntity().getSimpleValue(LoadTagsOfGroupElements.generateGsmTagPropertyName(child.getTypeId())));
    }
    private static String generateGsmTagPropertyName(String typeId) {
        return new StringBuilder(GSM_PREFIX).append(typeId).append(TAG_SUFFIX).toString();
    }
    
    public static Collection<String> getGsmIsmTagList(CnATreeElement child) {
        return TagHelper.getTags(child.getEntity().getSimpleValue(LoadTagsOfGroupElements.generateGsmTagIsmPropertyName(child.getTypeId())));
    }
    private static String generateGsmTagIsmPropertyName(String typeId) {
        return new StringBuilder(GSM_ISM_PREFIX).append(typeId).append(TAG_SUFFIX).toString();
    }

    /**
     * @return Sorted set with all tags of the children of the group
     */
    public Set<String> getTagSet() {
        return tagSet;
    }

    public IBaseDao<CnATreeElement, Serializable> getElementDao() {
        if(elementDao==null) {
            elementDao = getDaoFactory().getDAO(CnATreeElement.class);      
        }
        return elementDao;
    }

}
