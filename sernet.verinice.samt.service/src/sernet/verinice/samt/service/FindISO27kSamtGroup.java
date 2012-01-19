/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
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
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
@SuppressWarnings("serial")
public class FindISO27kSamtGroup extends GenericCommand implements IAuthAwareCommand {

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
    
    private int dbId;

    private static final String CONTROLGROUP_IS27KGROUP_PROPERTY = "controlgroup_is_NoIso_group";

    public FindISO27kSamtGroup(){
    	// BIRT JavaScript Constructor for use with class.newInstance()
    }
    
    public FindISO27kSamtGroup(boolean hydrate, int rootId) {
        hydrateParent = hydrate;
        dbId = rootId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        IBaseDao<ControlGroup, Serializable> dao = getDaoFactory().getDAO(ControlGroup.class);

        // find all ControlGroups
        StringBuilder sbHql = new StringBuilder();
        sbHql.append("select distinct controlGroup from ControlGroup as controlGroup");

        final String hql = sbHql.toString();
        if (getLog().isDebugEnabled()) {
            getLog().debug("hql: " + hql);
        }

        List<ControlGroup> controlGroupList;

        controlGroupList = dao.findByQuery(hql, null);

        if (controlGroupList == null) {
            controlGroupList = Collections.emptyList();
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("number of controlGroups " + FindISO27kSamtGroup.nullSaveSize(controlGroupList));
        }

        // check if parent is Audit and children are SamtTopics
        List<ControlGroup> resultList = new ArrayList<ControlGroup>();
        int count = 0;
        for (ControlGroup controlGroup : controlGroupList) {
            if (isISO27kControlGroup(controlGroup) && isSamtTopicCollection(controlGroup.getChildren())) {
                resultList.add(controlGroup);
            }
            count++;
        }

        if (resultList != null && !resultList.isEmpty()) {
            CnATreeElement parent = resultList.get(0).getParent();
            for (ControlGroup g : resultList) {
                if (isParent(g, dbId)) {
                    selfAssessmentGroup = g;
                    if(g.getParent().getUuid().equals(parent.getUuid())){
                        parent = g.getParent();
                    } else  {
                        break;
                    }
                }
            }
            if(parent instanceof ControlGroup)
                selfAssessmentGroup = (ControlGroup)parent;
            if(selfAssessmentGroup != null)
                hydrate(selfAssessmentGroup);
        }

    }

    private static int nullSaveSize(List<ControlGroup> controlGroupList) {
        int size = 0;
        if (controlGroupList != null) {
            size = controlGroupList.size();
        }
        return size;
    }

    /**
     * @param parent
     * @return
     */
    private boolean isAudit(CnATreeElement parent) {
        while (parent != null) {
            if (parent != null && Audit.TYPE_ID.equals(parent.getTypeId())) {
                return true;
            } else {
                parent = parent.getParent();
            }
        }
        return false;
    }

    private boolean isSamtTopicCollection(Collection<CnATreeElement> collection) {
        boolean isSamtTopicSet = true;
        for (CnATreeElement element : collection) {
            if (element != null) {
                if (ControlGroup.TYPE_ID.equals(element.getTypeId())) {
                    isSamtTopicSet = isSamtTopicCollection(element.getChildren());
                } else {
                    isSamtTopicSet = SamtTopic.TYPE_ID.equals(element.getTypeId());
                }
                if (!isSamtTopicSet) {
                    break;
                }
            }
        }
        return isSamtTopicSet;
    }

    private void hydrate(ControlGroup selfAssessmentGroup) {
        selfAssessmentGroup.getTitle();

        if (hydrateParent)
            selfAssessmentGroup.getParent().getTitle();
    }

    /**
     * @param controlGroup
     * @param dbId2
     * @return
     */
    private boolean isParent(CnATreeElement child, Integer parentId) {
        if (child.getParent() == null)
            return false;
        if (child.getParent().getDbId().equals(parentId))
            return true;
        return isParent(child.getParent(), parentId);
    }

    private boolean isISO27kControlGroup(ControlGroup group) {
        String is27kGroup = group.getEntity().getValue(CONTROLGROUP_IS27KGROUP_PROPERTY);
        if (is27kGroup != null && is27kGroup.equals("0")) {
            is27kGroup = "true";
        }
        boolean ret = Boolean.parseBoolean(is27kGroup);
        return ret;
    }

    public ControlGroup getSelfAssessmentGroup() {
        return selfAssessmentGroup;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#getAuthService()
     */
    public IAuthService getAuthService() {
        return authService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#setAuthService
     * (sernet.gs.ui.rcp.main.service.IAuthService)
     */
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }
    
    public void setDBIDandHydrate(int dbId, boolean hydrate){
    	this.dbId = dbId;
    	this.hydrateParent = hydrate;
    }

}
