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
 *     Sebastian Hagedorn <sh@sernet.de>
 ******************************************************************************/
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Loads all findings for a given {@link CnATreeElement} (?).
 * 
 * <p>
 * TODO samt: The current implementation generates random values which can
 * change each time the command is invoked. The ids used in here correspond to
 * those used in the {@link LoadChapterListCommand}.
 * </p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * @author Sebastian Hagedorn <sh@sernet.de>
 */
@SuppressWarnings("serial")
public class LoadAllFindingsCommand extends GenericCommand {

    private Object[][] result;

    private Integer id;

    private transient Logger log;

    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;
    
    private static final String SAMT_PERSON_INCHARGE_PROPERTY = "rel_samttopic_person-iso_resp";
    public static final String SAMT_MEASURE_PROPERTY = "samt_topic_controlnote";

    private static Map<Integer, Object[][]> computedData = new HashMap<Integer, Object[][]>();

    public LoadAllFindingsCommand(int id) {
        int id0 = -1;
        if(String.valueOf(id).startsWith(String.valueOf(LoadChapterListCommand.PLACEHOLDER_CONTROLGROUP_ID))){
            String chapterIdString = String.valueOf(id);
            chapterIdString = chapterIdString.substring(String.valueOf(LoadChapterListCommand.PLACEHOLDER_CONTROLGROUP_ID).length());
            id0 = Integer.parseInt(chapterIdString);
        }
        this.id = (id0 > -1) ? id0 : id;
        log = Logger.getLogger(LoadAllFindingsCommand.class);
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

    private Object[][] generateResultEntry(ControlGroup group) {
        final int defaultResultSize = 8;
        Set<SamtTopic> allTopics = getAllSamtTopicChildren(group);
        Object[][] retVal = new Object[allTopics.size()][defaultResultSize];
        Iterator<SamtTopic> iter = allTopics.iterator();
        int count = 0;
        while (iter.hasNext()) {
            retVal[count] = generateSamtEntry(iter.next());
            count++;
        }
        return retVal;
    }
    
    private Object[][] sortResult(Object[][] unsortedResults){
        if (unsortedResults != null && unsortedResults.length > 0 && unsortedResults[0].length > 0) {
            ArrayList<Object[]> list = new ArrayList<Object[]>();
            for (Object[] objects : unsortedResults) {
                list.add(objects);
            }
            Collections.sort(list, new Comparator<Object[]>() {
              @Override
              public int compare(Object[] o1, Object[] o2) {
                  NumericStringComparator comparator = new NumericStringComparator();
                  return comparator.compare((String)o1[1], (String)o2[1]);
              }
            });
            return list.toArray(new Object[unsortedResults.length][unsortedResults[0].length]);
        }
        return new Object[0][0];
    }

    private Object[] generateSamtEntry(SamtTopic topic) {
        String finding = topic.getEntity().getValue(LoadWorstFindingsCommand.SAMT_PROP_FINDING);
        int dev = topic.getNumericProperty(LoadDeviationRiskTableCommand.SAMT_DEVIATION_PROPERTY);
        int risk = topic.getNumericProperty(LoadDeviationRiskTableCommand.SAMT_RISK_PROPERTY);
        String measure = topic.getEntity().getValue(SAMT_MEASURE_PROPERTY);
        String personInCharge = "";
        for (CnALink link : topic.getLinksDown()) {
            if (link.getRelationId().equals(SAMT_PERSON_INCHARGE_PROPERTY)) {
                personInCharge = link.getDependency().getTitle();
                break;
            }
        }
        return new Object[] { topic.getDbId(), topic.getTitle(), finding, topic.getMaturity(), dev, risk, measure, personInCharge };
    }

    @Override
    public void execute() {
        LoadCnAElementById command = new LoadCnAElementById(ControlGroup.TYPE_ID, id);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            if (command.getFound() != null) {
                computedData.put(id, generateResultEntry((ControlGroup) command.getFound()));
            }
        } catch (CommandException e) {
            log.error("Error while executing command", e);
        }
        result = computedData.get(id);
        result = sortResult(result);
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
