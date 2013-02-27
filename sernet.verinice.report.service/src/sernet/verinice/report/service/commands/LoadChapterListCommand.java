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
 *     Sebastian Hagedorn <sh@sernet.de> - providing content to the skeleton
 ******************************************************************************/
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Loads the ids and names of various elements of {@link CnATreeElement} (?)
 * that are eligible for a security assesment. They are referenced as chapters
 * since each element turns into a chapter in the 'comprehensive security
 * assessment report'.
 * 
 * TODO samt: Check the comments for explanations of where the ids come from and
 * what they are supposed to mean.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * @author Sebastian Hagedorn <sh@sernet.de>
 */
@SuppressWarnings("serial")
public class LoadChapterListCommand extends GenericCommand {

    private Object[][] result;

    private Integer chapterId;

    private CnATreeElement rootObject;

    private transient Logger log;
    
    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;

    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";
    public static final Integer PLACEHOLDER_CONTROLGROUP_ID = -7;
    private static final int OVERVIEW_PROPERTY_TARGET = 0;
    
    public LoadChapterListCommand(Integer chapterId) {
        log = Logger.getLogger(LoadChapterListCommand.class);
        this.chapterId = chapterId;
    }

    public LoadChapterListCommand(Integer chapterId, int rootId) {
        this(chapterId);
        setRootObject(rootId);
    }

    private void setRootObject(int id) {
        if (getCache().get(id) != null) {
            rootObject = (Audit) getCache().get(id).getValue();
            return;
        } else {
            LoadCnAElementById command = new LoadCnAElementById(Audit.TYPE_ID, id);
            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                CnATreeElement e = command.getFound();
                if (e instanceof Audit) {
                    rootObject = loadChildren((Audit) e);
                }
            } catch (Exception e) {
                log.error("Error while executing command", e);
            }
        }
    }

    public Object[][] getResult() {
    	return (result != null) ? result.clone() : null;
    }

    @Override
    public void execute() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.addAll(computeChapters(chapterId));
        result = sortResults(list.toArray(new Object[list.size()][]));
    }

    private Object[][] sortResults(Object[][] unsortedResults) {
        if (unsortedResults.length > 0 && unsortedResults[0].length > 0) {
            ArrayList<Object[]> list = new ArrayList<Object[]>();
            for (Object[] objects : unsortedResults) {
                list.add(objects);
            }
            Collections.sort(list, new Comparator<Object[]>() {
                @Override
                public int compare(Object[] o1, Object[] o2) {
                    if(((Integer)o1[0]).intValue() > 0 && ((Integer)o2[0]).intValue() > 0){
                        NumericStringComparator comparator = new NumericStringComparator();
                        return comparator.compare((String) o1[1], (String) o2[1]);
                    } else {
                        if(((Integer)o1[0]).intValue() < ((Integer)o2[0]).intValue()){
                            return -1;
                        } else if(((Integer)o1[0]).intValue() > ((Integer)o2[0]).intValue()){
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            });
            return list.toArray(new Object[unsortedResults.length][unsortedResults[0].length]);
        }
        return new Object[0][0];
    }

    private int getLvlCountToRootAudit(CnATreeElement elmt) {
        int count = 1;
        CnATreeElement parent = elmt.getParent();
        while (parent != null && !parent.equals(rootObject)) {
            count++;
            parent = parent.getParent();
        }
        return count;
    }

    private List<Object[]> computeChapters(Integer id) {
        final int dummyDBID = -10;
        List<Object[]> values = new ArrayList<Object[]>(0);
        if (!rootObject.isChildrenLoaded()) {
            loadChildren(rootObject);
        }
        if (id == -1) { // overview case
            for (CnATreeElement e : rootObject.getChildren()) {
                if (e instanceof ControlGroup) {
                    ControlGroup g = (ControlGroup) e;
                    values.add(createValueEntry(g));
                }
            }
        } else if (id == -2 || id == -3) {
            List<Object[]> lvl1Groups = computeChapters(-1);
            for (Object[] oArr : lvl1Groups) {
                int groupId = ((Integer) oArr[0]).intValue();
                ControlGroup g = null;
                if (getCache().get(groupId) != null) {
                    g = (ControlGroup) getCache().get(groupId).getValue();
                } else {
                    LoadCnAElementById command = new LoadCnAElementById(ControlGroup.TYPE_ID, groupId);
                    try {
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        if (command.getFound() != null) {
                            g = (ControlGroup) command.getFound();
                            getCache().put(new Element(groupId, g));
                        }
                    } catch (CommandException e) {
                        log.error("Error while executing command", e);
                    }
                }
                if(g!=null && g.getChildren()!=null) {
                    for (CnATreeElement e : g.getChildren()) {
                        if (e instanceof ControlGroup) {
                            values.add(createValueEntry(e));
                        }
                    }
                }
                break;
            }
        } else if (id == -5) {
            ControlGroup headlineGroup = new ControlGroup();
            headlineGroup.setTitel(rootObject.getTitle() + " Overview");
            headlineGroup.setDbId(dummyDBID);
            values.add(createValueEntry(headlineGroup));
            for (CnATreeElement e : rootObject.getChildren()) {
                if (e instanceof ControlGroup) { // rootControlGroup
                    if (!e.isChildrenLoaded()) {
                        e = loadChildren(e);
                    }
                    for (CnATreeElement elmt : e.getChildren()) {
                        if (elmt instanceof ControlGroup) {
                            if (!elmt.isChildrenLoaded()) {
                                elmt = loadChildren(elmt);
                            }
                            if (!isCnaTreeElementInList(values, elmt) && elmt instanceof ControlGroup) {
                                ControlGroup g = (ControlGroup) elmt;
                                String isOverviewElementString = g.getEntity().getValue(OVERVIEW_PROPERTY);
                                if (isOverviewElementString != null && isOverviewElementString.equals(String.valueOf(OVERVIEW_PROPERTY_TARGET))) {
                                    values.add(createValueEntry(elmt));
                                }
                            }
                        }
                    }
                }
            }
        }else if(id == -10){
            for (CnATreeElement e : rootObject.getChildren()) {
                if (e instanceof ControlGroup) { // rootControlGroup
                    if (!e.isChildrenLoaded()) {
                        e = loadChildren(e);
                    }
                    for (CnATreeElement elmt : e.getChildren()) {
                        if (elmt instanceof ControlGroup) {
                            if (!elmt.isChildrenLoaded()) {
                                elmt = loadChildren(elmt);
                            }
                            if (!isCnaTreeElementInList(values, elmt) && elmt instanceof ControlGroup) {
                                ControlGroup g = (ControlGroup) elmt;
                                String isOverviewElementString = g.getEntity().getValue(OVERVIEW_PROPERTY);
                                if (isOverviewElementString != null && !isOverviewElementString.equals(String.valueOf(OVERVIEW_PROPERTY_TARGET))) {
                                    values.add(createValueEntry(elmt));
                                }
                            }
                        }
                    }
                }
            }
        } else {
            CnATreeElement ce = null;
            if (getCache().get(id) != null) {
                ce = (CnATreeElement) getCache().get(id).getValue();
            } else if (id.intValue() == -1) {
                List<Object[]> id5Groups = computeChapters(-5); 
                for (Object[] o : id5Groups) {
                    int groupid = ((Integer) o[0]).intValue();
                    for(ControlGroup rootChild : loadAllControlgroupChildren(rootObject)){
                        if(rootChild.getDbId().intValue() == groupid && rootChild.getEntity().getValue(OVERVIEW_PROPERTY).equals(String.valueOf(OVERVIEW_PROPERTY_TARGET))){
                                values.add(createValueEntry(rootChild));
                        }
                    }
                }
            } else {
                LoadCnAElementById command1 = new LoadCnAElementById(ControlGroup.TYPE_ID, id);
                try {
                    command1 = ServiceFactory.lookupCommandService().executeCommand(command1);
                    ce = command1.getFound();
                    if (ce == null) {
                        command1 = new LoadCnAElementById(SamtTopic.TYPE_ID, id);
                        ce = command1.getFound();
                    }
                    if (ce != null){
                        getCache().put(new Element(id, ce));
                    }
                } catch (CommandException e) {
                    log.error("Error while executing command", e);
                }
            }
            if (ce != null) {
                ControlGroup placeHolderGroup = new ControlGroup();
                if (ce instanceof ControlGroup) {
                    ControlGroup g = (ControlGroup) ce;
                    if(g.getEntity().getValue(OVERVIEW_PROPERTY).equals(String.valueOf(OVERVIEW_PROPERTY_TARGET))){
                        String placeHolderIdString = PLACEHOLDER_CONTROLGROUP_ID.toString() + g.getDbId();
                        placeHolderGroup.setDbId(Integer.parseInt(placeHolderIdString));
                        if(!g.isChildrenLoaded()){
                            g = (ControlGroup)loadChildren(g);
                        }
                        placeHolderGroup.setChildren(g.getChildren());
                        placeHolderGroup.setTitel("");
                    }
                    if (getLvlCountToRootAudit(g) == 2) { // groups for id = -5
                        for (CnATreeElement elmt : g.getChildren()) {
                            if (elmt instanceof ControlGroup) {
                                values.add(createValueEntry(elmt));
                            }
                        }
                    }
                } 
                if(values.size() == 0 && ce instanceof ControlGroup){
                    values.add(createValueEntry(placeHolderGroup));
                }
            }
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private List<ControlGroup> loadAllControlgroupChildren(CnATreeElement elmt) {
        ArrayList<ControlGroup> list = new ArrayList<ControlGroup>();
        if (elmt.isChildrenLoaded()) {
            if (getCache().get("controlgroupchildren" + elmt.getDbId()) != null) {
                list = (ArrayList<ControlGroup>) getCache().get("controlgroupchildren" + elmt.getDbId()).getValue();
            } else {
                for (CnATreeElement child : elmt.getChildren()) {
                    if (child instanceof ControlGroup) {
                        list.add((ControlGroup) child);
                        list.addAll(loadAllControlgroupChildren(child));
                    }
                }
                getCache().put(new Element("controlgroupchildren" + elmt.getDbId(), list));
            }
        } else {
            elmt = loadChildren(elmt);
            list.addAll(loadAllControlgroupChildren(elmt));
        }
        return list;
    }

    private Object[] createValueEntry(CnATreeElement elmt) {
        if (!elmt.isChildrenLoaded() && elmt.getDbId() > 0) {
            elmt = loadChildren(elmt);
        }
        return new Object[] { elmt.getDbId(), elmt.getTitle() };
    }

    private CnATreeElement loadChildren(CnATreeElement el) {
        if (el.isChildrenLoaded()) {
            return el;
        } else if (getCache().get(el.getUuid()) != null) {
            return (CnATreeElement) getCache().get(el.getUuid()).getValue();
        }
        LoadChildrenForExpansion command;
        command = new LoadChildrenForExpansion(el);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            CnATreeElement newElement = command.getElementWithChildren();
            newElement.setChildrenLoaded(true);
            getCache().put(new Element(el.getUuid(), newElement));
            return newElement;
        } catch (CommandException e) {
            log.error("error while loading children of CnaTreeElment", e);
        }
        return null;
    }

    private boolean isCnaTreeElementInList(List<Object[]> list, CnATreeElement elmtToTest) {
        boolean isInList = false;
        for (Object[] o : list) {
            if (((Integer) o[0]).intValue() == elmtToTest.getDbId()) {
                isInList = true;
                break;
            }
        }
        return isInList;
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
}
