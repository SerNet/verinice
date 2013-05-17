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
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportISAPhysicalChecks extends GenericCommand implements ICachedCommand{
    
    private transient Logger log = Logger.getLogger(LoadReportISAPhysicalChecks.class);
    
    private Integer rootElement;

    private List<List<String>> results;
    
    private static final String REL_SAMT_INTERVIEW = "rel_interview_samt_topic_included";
    
    private static final String INTERVIEW_SHOW_IN_REPORT = "interview_showInISAReport";
    
    private static final String INTERVIEW_AUDIT_ACTION_DESCRIPTION = "interview_audit_action_description";

    public static final String[] COLUMNS = new String[]{
                                            "TITLE",
                                            "DESCRIPTION"
    };

    private boolean resultInjectedFromCache = false;

    public LoadReportISAPhysicalChecks(Integer root){
        this.rootElement = root;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            results = new ArrayList<List<String>>();
            LoadReportISARiskChapter chapterLoader = new LoadReportISARiskChapter(rootElement);
            try {
                chapterLoader = getCommandService().executeCommand(chapterLoader);
                for(SamtTopic topic : chapterLoader.getSamtTopics()){
                    for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(topic, Interview.TYPE_ID).entrySet()){
                        Interview interview = null;
                        if(entry.getKey() instanceof Interview){
                            interview = (Interview)entry.getKey();
                        }
                        if(entry.getValue().getRelationId().equals(REL_SAMT_INTERVIEW) && 
                                interview.getEntity().getSimpleValue(INTERVIEW_SHOW_IN_REPORT).equals("1")){
                            ArrayList<String> result = new ArrayList<String>();
                            result.add(interview.getTitle());
                            result.add(interview.getEntity().getSimpleValue(INTERVIEW_AUDIT_ACTION_DESCRIPTION));
                            results.add(result);
                        }
                    }
                }
            } catch (CommandException e) {
                getLog().error("Error while executing command", e);
            }
        }
    }
    
    public List<List<String>> getResult(){
        return results;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElement));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.results = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return results;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(this.getClass());
        }
        return log;
    }


}
