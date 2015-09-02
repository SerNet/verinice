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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Command takes a org or an audit as input, and computes the root controlgroup
 * that contains all isa questions (within sub controlgroups)
 * (the so called "SamtGroup")
 */
@SuppressWarnings("serial")
public class FindSGCommand extends GenericCommand implements ICachedCommand{

    private transient Logger log = Logger.getLogger(FindSGCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(FindSGCommand.class);
        }
        return log;
    }

    private transient IAuthService authService;

    private ControlGroup selfAssessmentGroup = null;

    private boolean hydrateParent;
    
    private int dbId;

    private static final String CONTROLGROUP_IS27KGROUP_PROPERTY = "controlgroup_is_NoIso_group";
    
    private Set<ControlGroup> outSortedCG;
    private Set<String> alreadySeenCG;

    private boolean resultInjectedFromCache = false;

    public FindSGCommand(){
    	// BIRT JavaScript Constructor for use with class.newInstance()
    }
    
    public FindSGCommand(boolean hydrate, int rootId) {
        hydrateParent = hydrate;
        dbId = rootId;
        outSortedCG = new HashSet<ControlGroup>(0);
        alreadySeenCG = new HashSet<String>(0);
    }
    
    public FindSGCommand(boolean hydrate, String rootId){
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
            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, dbId, true);
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
                getLog().debug("number of controlGroups " + FindSGCommand.nullSaveSize(controlGroupList));
            }

            // check if parent is Audit and children are SamtTopics
            List<ControlGroup> resultList = new ArrayList<ControlGroup>();
            for (CnATreeElement elmt : controlGroupList) {
                if(elmt instanceof ControlGroup){
                    ControlGroup controlGroup = (ControlGroup)elmt;

                    if (!(alreadySeenCG.contains(controlGroup.getUuid())) && 
                            isISO27kControlGroup(controlGroup) && containsSamtTopicsOnly(controlGroup)
                            ) {
                        resultList.add(controlGroup);
                    }
                    alreadySeenCG.add(controlGroup.getUuid());
                }
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
    
    private boolean containsSamtTopicsOnly(ControlGroup cg){
        if(!cg.isChildrenLoaded()){
            cg = (ControlGroup)loadChildren(cg);
        }
        if(!outSortedCG.contains(cg)){
            if(cg.getChildren().size() == 0){
                return false;
            }
            for(CnATreeElement child : cg.getChildren()){
                if(!(child instanceof SamtTopic)){
                    outSortedCG.add((ControlGroup)cg);
                    return false;
                }
            }
        } else {
            return false; // cg did already sorted fail the check
        }
        return true;
    }
    
    private CnATreeElement loadChildren(CnATreeElement elmt){
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(elmt.getTypeId());
        CnATreeElement hydratedElement = dao.findById(elmt.getDbId());
        HydratorUtil.hydrateElement(dao, hydratedElement, true);
        return hydratedElement;
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
        cacheID.append(hydrateParent);
        cacheID.append(String.valueOf(dbId));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.selfAssessmentGroup = (ControlGroup)result;
        this.resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return selfAssessmentGroup;
    }

}
