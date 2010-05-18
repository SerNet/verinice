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

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.samt.model.SamtTopic;

/**
 * Executes a search to find one {@link ControlGroup}
 * which contains {@link SamtTopic}s. 
 * 
 * German (use: http://translate.google.com/ to translate):
 * 
 * Gesucht wird nur nach den Gruppen, deren parent eine {@link Organization} ist.
 * Die gefunden Gruppe kann SamtTopics entweder direkt enthalten oder indirekt
 * in einer Untergruppe.
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
        
        List<ControlGroup> controlGroupList = dao.findByQuery(hql,null);
        if(controlGroupList==null) {
            controlGroupList = Collections.emptyList();
        }
        
        if (getLog().isDebugEnabled()) {
            getLog().debug("number of controlGroups " + FindSamtGroup.nullSaveSize(controlGroupList));
        }
        
        // check if parent if Organization and children are SamtTopics
        List<ControlGroup> resultList = new ArrayList<ControlGroup>();
        for (ControlGroup controlGroup : controlGroupList) {
            if(isOrganization(controlGroup.getParent()) && isSamtTopicSet(controlGroup.getChildren())) {
                resultList.add(controlGroup);
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
     * @param parent
     * @return
     */
    private boolean isOrganization(CnATreeElement parent) {
        return (parent!=null && Organization.TYPE_ID.equals(parent.getTypeId()));
    }
    
    /**
     * @param children
     * @return
     */
    private boolean isSamtTopicSet(Collection<CnATreeElement> children) {
        boolean isSamtTopicSet = true;
        for (CnATreeElement child : children) {
            if(child!=null) {
                if(ControlGroup.TYPE_ID.equals(child.getTypeId())) {
                    isSamtTopicSet=isSamtTopicSet(child.getChildren());
                } else {
                    isSamtTopicSet = SamtTopic.TYPE_ID.equals(child.getTypeId());
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
