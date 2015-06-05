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
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportElements;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.samt.service.FindSamtGroup;

/**
 *
 */
@SuppressWarnings("serial")
public class FindISO27kSamtGroup extends GenericCommand implements IAuthAwareCommand, ICachedCommand {

    private transient Logger log = Logger.getLogger(FindSamtGroup.class);
    
    private boolean resultInjectedFromCache = false;

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
    
    public FindISO27kSamtGroup(boolean hydrate, String rootId){
        this(hydrate, Integer.parseInt(rootId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, dbId);
            try {
                command = getCommandService().executeCommand(command);
            } catch (CommandException e) {
                log.error("Error while executing command");
            }


            List<CnATreeElement> controlGroupList = null;

            if(command.getElements() != null && command.getElements().size() > 0){
                controlGroupList = command.getElements();
            }

            if (controlGroupList == null) {
                controlGroupList = Collections.emptyList();
            }

            if (getLog().isDebugEnabled()) {
                getLog().debug("number of controlGroups " + FindISO27kSamtGroup.nullSaveSize(controlGroupList));
            }

            // check if parent is Audit and children are SamtTopics
            List<ControlGroup> resultList = new ArrayList<ControlGroup>();
            for (CnATreeElement elmt : controlGroupList) {
                if(elmt instanceof ControlGroup){
                    ControlGroup controlGroup = (ControlGroup)elmt;

                    if (isISO27kControlGroup(controlGroup) && isSamtTopicCollection(controlGroup.getChildren())) {
                        resultList.add(controlGroup);
                    }}
            }

            if (resultList != null && !resultList.isEmpty()) {
                selfAssessmentGroup = determineRootControlgroup(resultList);
                if(selfAssessmentGroup != null){
                    hydrate(selfAssessmentGroup);
                }
            }
        }
    }
    
    private ControlGroup determineRootControlgroup(List<ControlGroup> list){
        ArrayList<ControlGroup> cList = new ArrayList<ControlGroup>(0);
        for(ControlGroup g : list){
            if(hasSamtTopicChildrenOnly(g) && g.getParent() instanceof ControlGroup){
                cList.add(g);
            }
        }
        ControlGroup parent = null;
        boolean errorOccured = false;
        for(ControlGroup g : cList){
            if(parent == null){
                parent = (ControlGroup)g.getParent();
            } else {
                if(parent.getUuid().equals(g.getParent().getUuid())){
                    continue;
                } else {
                    errorOccured = true;
                    break;
                }
            }
        }
        if(errorOccured){
            return null;
        } else {
            return parent;
        }
    }
    
    private boolean hasSamtTopicChildrenOnly(ControlGroup group){
        boolean retVal = true;
        for(CnATreeElement elmt : group.getChildren()){
            if(!(elmt instanceof SamtTopic)){
                retVal = false;
                break;
            }
        }
        return retVal;
    }
    private static int nullSaveSize(List<CnATreeElement> controlGroupList) {
        int size = 0;
        if (controlGroupList != null) {
            size = controlGroupList.size();
        }
        return size;
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

        if (hydrateParent){
            selfAssessmentGroup.getParent().getTitle();
        }
    }

    private boolean isISO27kControlGroup(ControlGroup group) {
        String is27kGroup = group.getEntity().getValue(CONTROLGROUP_IS27KGROUP_PROPERTY);
        if (is27kGroup != null && is27kGroup.equals("0")) {
            is27kGroup = "true";
        }
        return Boolean.parseBoolean(is27kGroup);
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

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(dbId));
        cacheID.append(String.valueOf(hydrateParent));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.selfAssessmentGroup = (ControlGroup)result;
        resultInjectedFromCache = true;
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return selfAssessmentGroup;
    }


}
