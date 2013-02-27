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
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Loads the worst finding for a given {@link CnATreeElement} (?).
 * 
 * TODO samt: Needs to be implemented in a way that it really finds out the
 * worst finding of a given element. It is not completely clear whether the
 * element of which this is being done is really a {@link CnATreeElement} or a
 * specific subclass.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * @author Sebastian Hagedorn <sh@sernet.de>
 */
@SuppressWarnings("serial")
public class LoadWorstFindingsCommand extends GenericCommand {

    private Object[][] result;

    private int id;

    private transient Logger log;

    public static final String SAMT_PROP_FINDING = "samt_topic_audit_findings";
    public static final String SAMT_PROP_MEASURE = "samt_topic_controlnote";

    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;
    
    public LoadWorstFindingsCommand(int id) {
        log = Logger.getLogger(LoadWorstFindingsCommand.class);
        int id0 = -1;
        if(String.valueOf(id).startsWith(String.valueOf(LoadChapterListCommand.PLACEHOLDER_CONTROLGROUP_ID))){
            String chapterIdString = String.valueOf(id);
            chapterIdString = chapterIdString.substring(String.valueOf(LoadChapterListCommand.PLACEHOLDER_CONTROLGROUP_ID).length());
            id0 = Integer.parseInt(chapterIdString);
        } 
        this.id = (id0 > -1) ? id0 : id;
    }

    public Object[][] getResult() {
        return (result != null) ? result.clone() : null;
    }

    @SuppressWarnings("unchecked")
    private Set<SamtTopic> getAllSamtTopicChildren(ControlGroup parent) {
        Set<SamtTopic> set = new HashSet<SamtTopic>();
        if (getCache().get(parent.getUuid()) != null) {
            set = (Set<SamtTopic>) getCache().get(parent.getUuid()).getValue();
        } else {
            for (CnATreeElement e : parent.getChildren()) {
                if (e instanceof ControlGroup) {
                    set.addAll(getAllSamtTopicChildren((ControlGroup) e));
                } else if (e instanceof SamtTopic) {
                    set.add((SamtTopic) e);
                }
            }
            getCache().put(new Element(parent.getUuid(), set));
        }
        return set;
    }

    @Override
    public void execute() {
        final int maxDevStart = -2;
        final int maxRiskStart = -2;
        final int defaultResultSize = 6;
        ControlGroup group = null;
        if (getCache().get(id) != null) {
            group = (ControlGroup) getCache().get(id).getValue();
        } else {
            try {
                LoadCnAElementById command = new LoadCnAElementById(ControlGroup.TYPE_ID, id);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                group = (ControlGroup) command.getFound();
                getCache().put(new Element(id, group));
            } catch (CommandException e) {
                log.error("Error while executing command", e);
            }

        }
        int maxDev = maxDevStart; // minimum value is -1
        int maxRisk = maxRiskStart; // minimum value is -1
        List<SamtTopic> worstTopics = new ArrayList<SamtTopic>();
        Set<SamtTopic> allSamtTopics = getAllSamtTopicChildren(group);
        for (SamtTopic topic : allSamtTopics) {
            int curRisk = topic.getNumericProperty(LoadDeviationRiskTableCommand.SAMT_RISK_PROPERTY);
            if (curRisk > maxRisk) {
                maxRisk = curRisk;
            }
        }
        for(SamtTopic topic : allSamtTopics){
            if(topic.getNumericProperty(LoadDeviationRiskTableCommand.SAMT_RISK_PROPERTY) == maxRisk){
                worstTopics.add(topic);
            }
        }
        for(SamtTopic topic : worstTopics){
            int curDev = topic.getNumericProperty(LoadDeviationRiskTableCommand.SAMT_DEVIATION_PROPERTY);
            if(curDev > maxDev){
                maxDev = curDev;
            }
        }
        ArrayList<Object[]> arrayList = new ArrayList<Object[]>();
        for(SamtTopic worstTopic : worstTopics){
            if(worstTopic.getNumericProperty(LoadDeviationRiskTableCommand.SAMT_DEVIATION_PROPERTY) == maxDev){
                String finding = worstTopic.getEntity().getValue(SAMT_PROP_FINDING);
                String measure = worstTopic.getEntity().getValue(SAMT_PROP_MEASURE);
                if(finding != null && !finding.equals("")){
                    arrayList.add(new Object[]{worstTopic.getDbId(), worstTopic.getTitle(), finding, maxDev, maxRisk, measure});
                }
            }
        }
        result = arrayList.toArray(new Object[arrayList.size()][defaultResultSize]);
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
