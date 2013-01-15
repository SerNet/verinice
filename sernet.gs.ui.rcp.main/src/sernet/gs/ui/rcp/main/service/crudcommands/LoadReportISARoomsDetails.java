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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportISARoomsDetails extends GenericCommand implements ICachedCommand{

    private static final Logger LOG = Logger.getLogger(LoadReportISARoomsDetails.class);
    
    private static final String CHAPTER_PREFIX_PATTERN = ".*(\\d+)\\.?(\\d+)?";
    
    private List<List<String>> results;
    
    private Integer roomID;
    
    private static final String SAMT_DEVIATION_PROP = "samt_topic_audit_devi";
    private static final String SAMT_RISK_PROP = "samt_topic_audit_ra";
    
    public static final String[] ROOMCOLUMNS = new String[] { 
                                            "CONTROLID",
                                            "TITLE",
                                            "RESULT",
                                            "DEVIATION",
                                            "RISK"
    };
    
    private boolean resultInjectedFromCache = false;
    
    public LoadReportISARoomsDetails(Integer roomID){
        this.roomID = roomID;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            results = new ArrayList<List<String>>();
            LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, roomID, true);
            try {
                command = getCommandService().executeCommand(command);
                List<CnATreeElement> stList = command.getElements();
                Collections.sort(stList, new NumericStringComparator());
                for(CnATreeElement e : stList){
                    List<String> result = new ArrayList<String>();
                    SamtTopic topic = (SamtTopic)e;
                    result.add(getControlID(topic.getTitle()));
                    result.add(getControlTitleWithoutID(topic.getTitle()));
                    result.add(String.valueOf(topic.getMaturity()));
                    result.add(String.valueOf(topic.getEntity().getSimpleValue(SAMT_DEVIATION_PROP)));
                    result.add(String.valueOf(topic.getEntity().getSimpleValue(SAMT_RISK_PROP)));
                    results.add(result);
                }
            } catch (CommandException e) {
                LOG.error("Error while computing roomDetails", e);
            }
        }
    }
    
    private String getControlID(String title){
        Pattern pattern = Pattern.compile(CHAPTER_PREFIX_PATTERN);
        Matcher matcher = pattern.matcher(title);
        if(matcher.find()){
            return matcher.group();
        }
        return "";
    }
    
    private String getControlTitleWithoutID(String title){
        Pattern pattern = Pattern.compile(CHAPTER_PREFIX_PATTERN);
        Matcher matcher = pattern.matcher(title);
        if(matcher.find()){
            String group = matcher.group();
            return title.substring(title.indexOf(group) + group.length()).trim();
        }
        return title;
    }
    
    public List<List<String>> getResults(){
        return results;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(roomID));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        results = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
        if(LOG.isDebugEnabled()){
            LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return results;
    }
}
