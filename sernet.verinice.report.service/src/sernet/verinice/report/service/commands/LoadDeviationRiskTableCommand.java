/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.ComprehensiveSamtReportMatrix;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportElements;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Retrieves the values needed for the 'deviation/risk' tables that are part of
 * each worst finding.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>,
 * @author Sebastian Hagedorn <sh@sernet.de>
 * 
 */
@SuppressWarnings("serial")
public class LoadDeviationRiskTableCommand extends GenericCommand {

    public static final String CONTROL_DEVIATION_PROPERTY = "control_isa_audit_devi";
    public static final String CONTROL_RISK_PROPERTY = "control_isa_audit_ra";
    public static final String SAMT_DEVIATION_PROPERTY = "samt_topic_audit_devi";
    public static final String SAMT_RISK_PROPERTY = "samt_topic_audit_ra";
    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";

    private List<List<String>> result;

    private Integer chapterId;
    private String chapterName;
    private int rootObjectId;
    
    private boolean isNonIsoGroup = false;

    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;

    private transient Logger log;

    public LoadDeviationRiskTableCommand(int chapterId, String chapterName) {
        log = Logger.getLogger(LoadDeviationRiskTableCommand.class);
        int chapterId0 = -1;
        if(String.valueOf(chapterId).startsWith(String.valueOf(LoadChapterListCommand.PLACEHOLDER_CONTROLGROUP_ID))){
            String chapterIdString = String.valueOf(chapterId);
            chapterIdString = chapterIdString.substring(String.valueOf(LoadChapterListCommand.PLACEHOLDER_CONTROLGROUP_ID).length());
            chapterId0 = Integer.parseInt(chapterIdString);
            isNonIsoGroup = true;
        }
        this.chapterId = (chapterId0 > -1) ? chapterId0 : chapterId;
        this.chapterName = chapterName;
    }

    public LoadDeviationRiskTableCommand(int chapterId, String chapterName, int rootObject) {
        this(chapterId, chapterName);
        this.rootObjectId = rootObject;
    }

    public List<List<String>> getResult() {
        return result;
    }
    
    private boolean isOverviewElement(ControlGroup group){
        if(group.getEntity().getValue(OVERVIEW_PROPERTY) != null && !group.getEntity().getValue(OVERVIEW_PROPERTY).equals("1")){
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        final int matrixLines = 3;
        final int matrixColumns = 4;
        Audit rootElement = null;
        ComprehensiveSamtReportMatrix chapterValues = new ComprehensiveSamtReportMatrix(matrixLines, matrixColumns);
        ArrayList<String> l = new ArrayList<String>();
        try {
            if (getCache().get(rootObjectId) != null) {
                rootElement = (Audit) getCache().get(rootObjectId).getValue();
            } else {
                LoadCnAElementById c1 = new LoadCnAElementById(Audit.TYPE_ID, rootObjectId);
                c1 = ServiceFactory.lookupCommandService().executeCommand(c1);
                if (c1.getFound() instanceof Audit) {
                    rootElement = (Audit) c1.getFound();
                    getCache().put(new Element(rootObjectId, rootElement));
                }
            }
            String deviationProperty = "";
            String riskProperty = "";
            List<CnATreeElement> cList = null;
            ControlGroup rootChapterElement = getRootChapter(chapterId);
            LoadReportElements command = null;
            if(chapterId.intValue() == -1){
                cList = new ArrayList<CnATreeElement>(0);
                for(CnATreeElement elmt : getControlGroupChildren(rootChapterElement)){
                    if(elmt instanceof ControlGroup){
                        ControlGroup group = (ControlGroup)elmt;
                        if(isOverviewElement(group)){
                            LoadReportElements c1 = new LoadReportElements(SamtTopic.TYPE_ID, group.getDbId());
                            c1 = ServiceFactory.lookupCommandService().executeCommand(c1);
                            if(c1.getElements() != null && c1.getElements().size() > 0){
                                for(CnATreeElement e : c1.getElements()){
                                    cList.add((SamtTopic)e);
                                }
                            }
                        }
                    }
                }
            }else if(isNonIsoGroup){
                command = new LoadReportElements(SamtTopic.TYPE_ID, chapterId);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                cList = command.getElements();                
            } else {
                if (rootElement != null && rootChapterElement != null) {
                    if (getCache().get(rootChapterElement.getDbId()) != null) {
                        cList = (List<CnATreeElement>) getCache().get(rootChapterElement.getDbId());
                    } else {
                        if (getCache().get(rootChapterElement.getUuid()) != null) {
                            cList = (List<CnATreeElement>) getCache().get(rootChapterElement.getUuid()).getValue();
                        } else {
                            command = new LoadReportElements(SamtTopic.TYPE_ID, rootChapterElement.getDbId());
                            command = ServiceFactory.lookupCommandService().executeCommand(command);
                            cList = command.getElements();
                            getCache().put(new Element(rootChapterElement.getUuid(), cList));
                        }
                    }
                }
            }
            
            deviationProperty = SAMT_DEVIATION_PROPERTY;
            riskProperty = SAMT_RISK_PROPERTY;
            if(cList!=null) {
                for (CnATreeElement elmt : cList) {
                    int deviVal = elmt.getNumericProperty(deviationProperty);
                    int riskVal = elmt.getNumericProperty(riskProperty);
                    int deviField = -2;
                    int riskField = -2;
                    if (deviVal >= -1) {
                        deviField = deviVal + 1;
                    }
                    if (riskVal >= -1) {
                        riskField = riskVal + 1;
                    }
                    if (deviField > -1 && riskField > -1) {
                        chapterValues.increaseCount(deviField, riskField);
                    }
                }
            }

            if (rootChapterElement != null) {
                l.add(chapterName);
                for (int line = 0; line < matrixLines; line++) {
                    for (int column = 0; column < matrixColumns; column++) {
                        l.add(String.valueOf(chapterValues.getValue(line, column)));
                    }
                }
            }

        } catch (CommandException e) {
            log.error("Error while executing command", e);
        }

        result = new ArrayList<List<String>>();
        result.add(l);
    }

    /**
     * 
     * @param id
     * @return
     */
    private ControlGroup getRootChapter(int id) {
        if (id != -1 && id != -5) {
            LoadCnAElementById command = new LoadCnAElementById(ControlGroup.TYPE_ID, id);
            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                return (ControlGroup) command.getFound();
            } catch (CommandException e) {
                log.error("Error while executing command", e);
            }
        } else {
            ControlGroup rootControlgroup = null;
            switch (id) {
            case -1:
                LoadCnAElementById command = new LoadCnAElementById(Audit.TYPE_ID, rootObjectId);
                try {
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    for (CnATreeElement e : command.getFound().getChildren()) {
                        if (e instanceof ControlGroup) {
                            rootControlgroup = (ControlGroup) e;
                        }
                    }
                } catch (CommandException e) {
                    log.error("Error while executing command", e);
                }
                break;
            default:
                break;
            }
            return rootControlgroup;
        }
        return null;
    }

    private Cache getCache() {
        if (manager == null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || cache == null || !Status.STATUS_ALIVE.equals(cache.getStatus())) {
            cache = createCache();
        } else {
            cache = manager.getCache(cacheId);
        }
        return cache;
    }

    private Cache createCache() {
        final int maxElementsInMemory = 20000;
        final int timeToLiveSeconds = 600;
        final int timeToIdleSeconds = 500;
        cacheId = UUID.randomUUID().toString();
        manager = CacheManager.create();
        cache = new Cache(cacheId, maxElementsInMemory, false, false, timeToLiveSeconds, timeToIdleSeconds);
        manager.addCache(cache);
        return cache;
    }
    
    private CnATreeElement loadChildren(CnATreeElement el) {
        if (el.isChildrenLoaded()) {
            return el;
        } else if (getCache().get("childrenlist" + el.getUuid()) != null) {
            return (CnATreeElement) getCache().get("childrenlist" + el.getUuid()).getValue();
        }

        LoadChildrenForExpansion command;
        command = new LoadChildrenForExpansion(el);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            CnATreeElement newElement = command.getElementWithChildren();
            newElement.setChildrenLoaded(true);
            getCache().put(new Element("childrenlist" + el.getUuid(), newElement));
            return newElement;
        } catch (CommandException e) {
            log.error("error while loading children of CnaTreeElment", e);
        }
        return null;
    }
    
    private Set<ControlGroup> getControlGroupChildren(ControlGroup group){
        Set<ControlGroup> set = new HashSet<ControlGroup>(0);
        if(!group.isChildrenLoaded()){
            set.addAll(getControlGroupChildren((ControlGroup)loadChildren(group)));
        } else {
            for(CnATreeElement child : group.getChildren()){
                if(child instanceof ControlGroup){
                    set.add((ControlGroup)child);
                    set.addAll(getControlGroupChildren((ControlGroup)child));
                }
            }
        }
        return set;
    }
}
