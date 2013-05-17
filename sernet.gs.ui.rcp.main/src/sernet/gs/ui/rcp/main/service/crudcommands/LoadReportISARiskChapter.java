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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportISARiskChapter extends GenericCommand implements ICachedCommand{
    
    private static transient Logger log = Logger.getLogger(LoadReportISARiskChapter.class);
    
    public static final String[] COLUMNS = new String[]{"RISK_NO",
                                                         "RISK_LOW",
                                                         "RISK_MEDIUM",
                                                         "RISK_HIGH",
                                                         "RISK_VERYHIGH",
                                                         "CHAPTERNAME"};
    
    private static final int RISK_NO = -1;
    private static final int RISK_LOW = 0;
    private static final int RISK_MEDIUM = 1;
    private static final int RISK_HIGH = 2;
    private static final int RISK_VERYHIGH = 3;
    private static final String RISK_PROPERTY = "samt_topic_audit_ra";
    private static final String PROP_CG_ISISAELMNT = "controlgroup_is_NoIso_group";


    private Integer rootElmt;
    private Integer rootSgGroup;
    private Map<String, Integer[]> results;
    // caches if group is relevant
    private Set<String> groupCache;
    // caches if samtTopic was already iterated
    private Set<String> samtCache;
    
    private List<SamtTopic> resultTopics;
    
    private boolean resultInjectedFromCache = false;
    
    public LoadReportISARiskChapter(Integer root){
        this.rootElmt = root;
        this.results = new HashMap<String, Integer[]>(0);
        this.groupCache = new HashSet<String>(0);
        this.samtCache = new HashSet<String>(0);
        this.resultTopics = new ArrayList<SamtTopic>(0);
    }
    
    public LoadReportISARiskChapter(Integer root, Integer rootSG){
        this(root);
        this.rootSgGroup = rootSG;
    }
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try{
                ControlGroup samtRootGroup = null;
                if(rootSgGroup != null){
                    samtRootGroup = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(rootSgGroup);
                } else {
                    FindSGCommand c1 = new FindSGCommand(true, rootElmt);
                    c1 = getCommandService().executeCommand(c1);
                    samtRootGroup = c1.getSelfAssessmentGroup();
                }
                LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, samtRootGroup.getDbId(), true);
                command = getCommandService().executeCommand(command);
                for(CnATreeElement e : command.getElements()){
                    if(e instanceof SamtTopic && isRelevantChild((SamtTopic)e) && !samtCache.contains(e.getUuid())){
                        String parentTitle = e.getParent().getTitle();
                        Integer[] values = null;
                        if(!results.containsKey(parentTitle)){
                            results.put(parentTitle, new Integer[]{0,0,0,0,0});
                        }
                        values = results.get(parentTitle);
                        switch(e.getNumericProperty(RISK_PROPERTY)){
                        case RISK_NO:
                            values[0]++; break;
                        case RISK_LOW:
                            values[1]++; break;
                        case RISK_MEDIUM:
                            values[2]++; break;
                        case RISK_HIGH:
                            values[3]++; break;
                        case RISK_VERYHIGH:
                            values[4]++; break;
                        default:
                            break;
                        }
                        results.put(parentTitle, values);
                        samtCache.add(e.getUuid());
                        resultTopics.add((SamtTopic)e);
                    }
                }

            } catch (CommandException e){
                log.error("Error while executing command", e);
            }
        }
    }
    
    private boolean isRelevantChild(SamtTopic topic){
        if(topic.getParent() instanceof ControlGroup){
            ControlGroup parent = (ControlGroup)topic.getParent();
            if(groupCache.contains(parent.getUuid())){
               return true; 
            } else {
                if(!parent.isChildrenLoaded()){
                    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[]{parent.getDbId()});
                    try {
                        command = getCommandService().executeCommand(command);
                        parent = (ControlGroup)command.getElements().get(0);
                        parent.setChildrenLoaded(true);
                    } catch (CommandException e) {
                        log.error("Error while executing command", e);
                    }
                }
                if(parent.getEntity().getSimpleValue(PROP_CG_ISISAELMNT).equals("0")){
                    groupCache.add(parent.getUuid());
                    return true;
                }
            }
        }
        return false;
    }
    
    public List<List<String>> getResult(){
        ArrayList<String> unsortedKeyList = new ArrayList<String>(results.size());
        unsortedKeyList.addAll(results.keySet());
        ArrayList<List<String>> result = new ArrayList<List<String>>(0);
        for(int i = 0; i < unsortedKeyList.size(); i++){
            String key = unsortedKeyList.get(i);
            List<String> tmpList = transformArrayToList(results.get(key)); 
            tmpList.add(key);
            result.add(tmpList);
        }
        result.trimToSize();
        Collections.sort(result, new Comparator<List<String>>() {

            @Override
            public int compare(List<String> o1, List<String> o2) {
                NumericStringComparator nc = new NumericStringComparator();
                return nc.compare(o1.get(o1.size() - 1), o2.get(o2.size() - 1));
            }
        });
        return result;
    }
    
    private List<String> transformArrayToList(Object[] array){
        List<String> list = new ArrayList<String>(0);
        for(int i = 0; i < array.length; i++){
            Object o = array[i];
            String result = null;
            if(o instanceof Integer){
                result = ((Integer)o).toString();
                
            } else if(o instanceof String){
                result = (String)o;
            }
            if(result == null){
                result = "";
            }
            list.add(result);
        }
        return list;
    }
    
    public List<SamtTopic> getSamtTopics(){
        return resultTopics;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        if(rootSgGroup != null){
            cacheID.append(String.valueOf(rootSgGroup));
        } else {
            cacheID.append("null");
        }
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        if(result instanceof Object[]){
            Object[] array = (Object[])result;
            this.results = (HashMap<String, Integer[]>)array[0];
            this.resultTopics = (ArrayList<SamtTopic>)array[1];
            resultInjectedFromCache = true;
            if(log.isDebugEnabled()){
                log.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
            }
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        Object[] cacheableResults = new Object[2];
        cacheableResults[0] = this.results;
        cacheableResults[1] = resultTopics;
        return cacheableResults;
    }
}
