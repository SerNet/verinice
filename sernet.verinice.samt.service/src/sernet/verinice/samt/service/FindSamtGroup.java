/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.samt.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Returns a self-assessment {@link ControlGroup}.
 * 
 * German (use: http://translate.google.com/ to translate):
 * 
 * Eine self-assessment group ist eine {@link ControlGroup}, 
 * deren parent eine {@link Organization} ist.
 * Die Gruppe darf nur SamtTopics oder ControlGroups enthalten. 
 * Wenn ControlGroups enthalten sind duerfen diese wieder nur nur SamtTopics oder ControlGroups
 * enthalten.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class FindSamtGroup extends GenericCommand implements IAuthAwareCommand {

    private transient Logger log = Logger.getLogger(FindSamtGroup.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(FindSamtGroup.class);
        }
        return log;
    }
    
    private transient IAuthService authService;
    
    private ControlGroup selfAssessmentGroup = null;
    
    private boolean hydrateParent;

    private Integer dbId = null;
    
    public FindSamtGroup()
    {
    	this(false);
    }
    
    public FindSamtGroup(boolean hydrateParent)
    {
    	this.hydrateParent = hydrateParent;
    }

    public FindSamtGroup(boolean hydrateParent, Integer orgDbId) {
        this.hydrateParent = hydrateParent;
        this.dbId = orgDbId;
    }
    
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        IBaseDao<ControlGroup, Serializable> dao = getDaoFactory().getDAO(ControlGroup.class);
        
        // find all ControlGroups
        StringBuilder sbHql =  new StringBuilder();
        sbHql.append("select distinct controlGroup from ControlGroup as controlGroup");
        
       
        
        final String hql = sbHql.toString();
        if (getLog().isDebugEnabled()) {
            getLog().debug("hql: " + hql);
        }
        
        List<ControlGroup> controlGroupList;
       
        controlGroupList = dao.findByQuery(hql,null);
        
        
        
        if(controlGroupList==null) {
            controlGroupList = Collections.emptyList();
        }
        
        if (getLog().isDebugEnabled()) {
            getLog().debug("number of controlGroups " + FindSamtGroup.nullSaveSize(controlGroupList));
        }
        
        // check if parent is Audit and children are SamtTopics
        List<ControlGroup> resultList = new ArrayList<ControlGroup>();
        for (ControlGroup controlGroup : controlGroupList) {
            if(isAudit(controlGroup.getParent()) && isSamtTopicCollection(controlGroup.getChildren())) {
                resultList.add(controlGroup);
            }
        }
        
        if(resultList!=null && !resultList.isEmpty() && resultList.size()>1 && dbId != null) {
            // find group in specified org or audit:
            for (ControlGroup controlGroup : resultList) {
                if (isParent(controlGroup, dbId)) {
                    selfAssessmentGroup =  controlGroup;
                    hydrate(selfAssessmentGroup);
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("result: " + selfAssessmentGroup);
                    }
                    return;
                }
            }
        }
        
        // result is first element in resultList 
        if(resultList!=null && !resultList.isEmpty()) {
            getLog().debug("number of self assessment groups " + FindSamtGroup.nullSaveSize(resultList));
            selfAssessmentGroup =  resultList.get(0);
            hydrate(selfAssessmentGroup);
            if (getLog().isDebugEnabled()) {
                getLog().debug("result: " + selfAssessmentGroup);
            }
        } 
    }

    /**
     * @param controlGroup
     * @param dbId2
     * @return
     */
    private boolean isParent(CnATreeElement child, Integer parentId) {
        if (child.getParent()==null)
            return false;
        if (child.getParent().getDbId().equals(parentId))
            return true;
        return isParent(child.getParent(), parentId);
    }

    /**
     * @param parent
     * @return
     */
    private boolean isAudit(CnATreeElement parent) {
        return (parent!=null && Audit.TYPE_ID.equals(parent.getTypeId()));
    }
    
    /**
     * Returns true if collection is a self-assessment collection.
     * 
     * A collection is a self-assessment collection if
     * all elements of a collection are 
     * a: {@link SamtTopic} or
     * b: a {@link ControlGroup}
     * 
     * If a ControlGroup is found isSamtTopicSet
     * is called recursively on all children of the group.
     * 
     * @param collection a collection of {@link CnATreeElement}
     * @return true if collection is a self-assessment collection
     */
    private boolean isSamtTopicCollection(Collection<CnATreeElement> collection) {
        boolean isSamtTopicSet = true;
        for (CnATreeElement element : collection) {
            if(element!=null) {
                if(ControlGroup.TYPE_ID.equals(element.getTypeId())) {
                    isSamtTopicSet = isSamtTopicCollection(element.getChildren());
                } else {
                    isSamtTopicSet = SamtTopic.TYPE_ID.equals(element.getTypeId());
                }
                if(!isSamtTopicSet) {
                    break;
                }
            }
        }
        return isSamtTopicSet;
    }
    

    /**
     * @param selfAssessmentGroup2
     */
    private void hydrate(ControlGroup selfAssessmentGroup) {
        selfAssessmentGroup.getTitle();
        
        if (hydrateParent)
        	selfAssessmentGroup.getParent().getTitle();
    }



    /**
     * @param controlGroupList
     * @return
     */
    private static int nullSaveSize(List<ControlGroup> controlGroupList) {
        int size = 0;
        if(controlGroupList!=null) {
            size = controlGroupList.size();
        }
        return size;
    }

    public ControlGroup getSelfAssessmentGroup() {
        return selfAssessmentGroup;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#getAuthService()
     */
    public IAuthService getAuthService() {
        return authService;
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#setAuthService(sernet.gs.ui.rcp.main.service.IAuthService)
     */
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }


}
