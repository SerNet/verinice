/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportElements;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadISAReportChapterList extends GenericCommand implements ICachedCommand{

    private static transient Logger log = Logger.getLogger(LoadISAReportChapterList.class);
    
    public static final String[] COLUMNS = new String[]{"dbid", "title"};
    
    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";
    private static final int OVERVIEW_PROPERTY_TARGET = 0;
    
    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;
    
    private Integer rootElmt;
    
    private List<List<String>> result;
    
    private boolean resultInjectedFromCache = false;

    public LoadISAReportChapterList(Integer root){
        result = new ArrayList<List<String>>(0);
        this.rootElmt = root;
    }

    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            for(ControlGroup g : getControlGroups(rootElmt)){
                result.add(createValueEntry(g));
            }
        }
    }
    
    private List<ControlGroup> getControlGroups(Integer root){
        ArrayList<ControlGroup> retList = new ArrayList<ControlGroup>(0);
        Set<ControlGroup> alreadySeen = new HashSet<ControlGroup>(0);
        try {
            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, root, true);
            command = getCommandService().executeCommand(command);
            List<CnATreeElement> groups = command.getElements();
            if(groups.size() == 1 && groups.get(0).getDbId().equals(root)){
                groups.clear();
                groups.addAll(command.getElements(ControlGroup.TYPE_ID, groups.get(0)));
            }
            for(CnATreeElement e : groups){
                if(e instanceof ControlGroup){
                    ControlGroup c = (ControlGroup)e;
                    if(!alreadySeen.contains(c)){
                        alreadySeen.add(c);
                        if(e.getParent() instanceof ControlGroup &&
                                c.getEntity().getSimpleValue(OVERVIEW_PROPERTY)
                                .equals(String.valueOf(OVERVIEW_PROPERTY_TARGET))
                                && containsSamtTopicsOnly(c)){ // avoids rootControlGroup
                            retList.add(c);
                        }
                    }
                }
            }
        } catch (CommandException e) {
            getLog().error("Error while determing controlgroups");
        }
        retList.trimToSize();
        Collections.sort(retList, new Comparator<ControlGroup>() {

            @Override
            public int compare(ControlGroup o1, ControlGroup o2) {
                NumericStringComparator comp = new NumericStringComparator();
                return comp.compare(o1.getTitle(), o2.getTitle());
            }
        });
        return retList;
    }
    
    /**
     * if group has a child that is not a samttopic, return false (recursivly)
     * @param group
     * @return
     */
    private boolean containsSamtTopicsOnly(ControlGroup group){
        if(group.getChildren().size() == 0){
            return false;
        }
        for(CnATreeElement child : group.getChildren()){
            if(!(child instanceof SamtTopic)){
                return false;
            } 
        }
        return true;
    }
    
    private List<String> createValueEntry(CnATreeElement elmt) {
        if (!elmt.isChildrenLoaded() && elmt.getDbId() > 0) {
            elmt = loadChildren(elmt);
        }
        ArrayList<String> list = new ArrayList<String>();
        list.add(String.valueOf(elmt.getDbId()));
        list.add(elmt.getTitle());
        return list;
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
            command = getCommandService().executeCommand(command);
            CnATreeElement newElement = command.getElementWithChildren();
            newElement.setChildrenLoaded(true);
            getCache().put(new Element(el.getUuid(), newElement));
            return newElement;
        } catch (CommandException e) {
            getLog().error("error while loading children of CnaTreeElment", e);
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
    
    public List<List<String>> getResult(){
        return result;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        return cacheID.toString();
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (ArrayList<List<String>>)result;
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
        return result;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(this.getClass());
        }
        return log;
    }
}
