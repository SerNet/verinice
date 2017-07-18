/*******************************************************************************
 * Copyright (c) 2013 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.verinice.service.commands.crud;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.gs.service.Retriever;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.PersonIso;

/**
 *  returns [DATE, TIME, ROLE, SUBJECT] of all audit actions to one given root id
 */
public class LoadReportISAInterviews extends GenericCommand implements ICachedCommand {
    
    private transient Logger log = Logger.getLogger(LoadReportISAInterviews.class);
    
    private SimpleDateFormat dateFormat =  new SimpleDateFormat("dd.MM.yy");
    
    private SimpleDateFormat timeFormat =  new SimpleDateFormat("HH:mm:ss");
    
    private int rootElement;

    private List<List<String>> results;

    private boolean resultInjectedFromCache = false;
    
    public static final String INTERVIEW_DATE = "interview_date";
    public static final String INTERVIEW_TITLE = "interview_name";
    public static final String PERSON_INTERVIEWER_RELATION = "rel_person_interview_intvwer";
    private static final String INTERVIEW_AUDIT_ACTION_PROPERTY = "interview_audit_action";
    private static final String[] ALLOWED_AUDIT_ACTION_TYPES = new String[]{
        "interview_audit_action_type_interview",
        "interview_audit_action_type_inspection"
    };
    
    public static final String[] COLUMNS = new String[]{
        "DATE",
        "TIME",
        "ROLE",
        "SUBJECT"
    };
    
    public LoadReportISAInterviews(int root){
        this.rootElement = root;
    }
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            Date iDate = null;
            ArrayList<String> result = null;
            Interview i = null;
            PersonIso person = null;
            String title = null;
            results = new ArrayList<List<String>>(0);
            try{
                LoadReportElements interviewLoader = new LoadReportElements(Interview.TYPE_ID, rootElement);
                interviewLoader = getCommandService().executeCommand(interviewLoader);
                for(CnATreeElement c : interviewLoader.getElements()){
                    if(c.getTypeId().equals(Interview.TYPE_ID)){
                        i = (Interview)Retriever.checkRetrieveElement(c);
                        // is audit action of allowed type?
                        if(getLog().isDebugEnabled()){
                            getLog().debug("AuditActionType of " + i.getTitle() + ":\t" + getAuditActionType(i));
                        }
                        if(Arrays.asList(ALLOWED_AUDIT_ACTION_TYPES).contains(getAuditActionType(i))){
                            // create local result
                            result = new ArrayList<String>(0);
                            // add date
                            iDate = i.getEntity().getDate(INTERVIEW_DATE);
                            // add date
                            result.add(dateFormat.format(iDate));
                            // add time
                            result.add(timeFormat.format(iDate));
                            // store title for later use
                            title = i.getEntity().getSimpleValue(INTERVIEW_TITLE);
                            // add role
                            i = (Interview)Retriever.checkRetrieveLinks(i, true);
                            StringBuilder sb = new StringBuilder();
                            for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(i, PersonIso.TYPE_ID).entrySet()){
                                if(entry.getValue().getRelationId().equals(PERSON_INTERVIEWER_RELATION)){
                                    person = ((PersonIso)(Retriever.checkRetrieveElement(entry.getKey())));
                                    sb.append(person.getName()).append(" ,").append(person.getSurname()).append("\n");
                                }
                            }
                            if(sb.length() > 0){
                                if(sb.toString().endsWith("\n")){
                                    sb.replace(sb.lastIndexOf("\n"), sb.length() - 1, "");
                                }
                                result.add(sb.toString());
                            }
                            // add empty string if no role is defined
                            if(result.size() == 2){
                                result.add("");
                            }
                            // add subject
                            result.add(title);

                            // add to global results
                            results.add(result);
                        }
                    }
                }
            } catch (CommandException e ){
                getLog().error("Error while executing command", e);
            }
        }
    }
    
    private String getAuditActionType(Interview i){
        return i.getEntity().getOptionValue(INTERVIEW_AUDIT_ACTION_PROPERTY);
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

    public List<List<String>> getResults() {
        return results;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportISAInterviews.class);
        }
        return log;
    }
}
