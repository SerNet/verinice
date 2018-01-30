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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadISAQuestionAuditActions extends GenericCommand implements ICachedCommand {
    
    private int rootElmt;
    
    private static final Logger LOG = Logger.getLogger(LoadISAQuestionAuditActions.class);
    
    public static final String[] COLUMNS = new String[]{
                                            "DATEOFINTERVIEW",
                                            "CONTACTPERSON"
    };
    
    private String INTERVIEW_DATE = "interview_date";
    
    private String INTERVIEW_RESPONSIBLE_PERSON = "rel_person_interview_intvwer";
    
    private boolean resultInjectedFromCache = false;
    
    private List<List<String>> results;
    
    public LoadISAQuestionAuditActions(int root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try{
                results = new ArrayList<List<String>>(0);
                SamtTopic elmt = (SamtTopic)getDaoFactory().getDAO(SamtTopic.TYPE_ID).findById(rootElmt);
                Iterator<Entry<CnATreeElement, CnALink>> iter2 = CnALink.getLinkedElements(elmt, Interview.TYPE_ID).entrySet().iterator();
                while(iter2.hasNext()){
                    Entry<CnATreeElement, CnALink> entry = iter2.next();
                    CnATreeElement keyElmt = entry.getKey();
                    if(keyElmt.getTypeId().equals(Interview.TYPE_ID)){
                        ArrayList<String> result = new ArrayList<String>(0);
                        // initialize elmt if not done (keyElmt needs to be instance of Interview (see getLinkedElements()))
                        if(!(keyElmt instanceof Interview)){
                            keyElmt = (Interview)getDaoFactory().getDAO(Interview.TYPE_ID).initializeAndUnproxy(keyElmt);
                        }
                        
                        Locale locale = Locale.getDefault();
                        DateFormat formatter = new SimpleDateFormat("EE, dd.MM.yyyy", locale);
                        DateFormat destinationFormat = new SimpleDateFormat("yyyy-MM-dd", locale);
                        formatter.setLenient(true);
                        Date fDate = formatter.parse(keyElmt.getEntity().getSimpleValue(INTERVIEW_DATE));
                        result.add(destinationFormat.format(fDate));
                        
                        
                        StringBuilder persons = new StringBuilder();
                        Iterator<Entry<CnATreeElement, CnALink>> iter = CnALink.getLinkedElements(keyElmt, PersonIso.TYPE_ID).entrySet().iterator();
                        while(iter.hasNext()){
                            Entry<CnATreeElement, CnALink> entry2 = iter.next();
                            if(entry2.getValue().getRelationId().equals(INTERVIEW_RESPONSIBLE_PERSON)){
                                persons.append(entry2.getKey().getTitle());
                                if(iter.hasNext()){
                                    persons.append("\n");
                                }
                            }
                        }
                        result.add(persons.toString());
                        results.add(result);
                    }
                }
            } catch (Exception e){
                LOG.error("Error while executing command", e);
            }
        }
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
        if(result instanceof ArrayList<?>){
            this.results = (ArrayList<List<String>>)result;
            resultInjectedFromCache = true;
            if(LOG.isDebugEnabled()){
                LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
            }
        }
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

}
