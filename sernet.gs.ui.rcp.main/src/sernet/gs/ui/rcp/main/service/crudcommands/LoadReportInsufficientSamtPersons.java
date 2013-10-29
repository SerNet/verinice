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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportInsufficientSamtPersons extends GenericCommand implements ICachedCommand{
    
    private static final Logger LOG = Logger.getLogger(LoadReportInsufficientSamtPersons.class);

    private static final String PROP_CLASSIFICATION_INSUFFICIENT = "samt_classification_insufficient";
    private static final String PROP_SAMT_CLASSIFICATION = "samt_user_classification";
    private static final String PROP_REL_SAMTTOPIC_PERSONISO_RESP = "rel_samttopic_person-iso_resp";
    
    private boolean resultInjectedFromCache = false;
    
    public static final String[] COLUMNS = new String[] { 
        "PERSON_NAME"
        };
    private Integer rootElmt;
    
    private List<List<String>> result;
    
    public LoadReportInsufficientSamtPersons(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            result = new ArrayList<List<String>>(0);
            Set<List<String>> personSet = new HashSet<List<String>>(0);
            try{
                LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, rootElmt, true);
                command = getCommandService().executeCommand(command);
                for(CnATreeElement c : command.getElements()){
                    if(SamtTopic.class.isInstance(c)){
                        SamtTopic t = (SamtTopic)c;
                        Entity ent = ((Entity)getDaoFactory().getDAO(Entity.TYPE_ID).initializeAndUnproxy(t.getEntity()));
                        String optionValue = ent.getOptionValue(PROP_SAMT_CLASSIFICATION);
                        if(optionValue != null && optionValue.equals(PROP_CLASSIFICATION_INSUFFICIENT)){
                            for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(t, PersonIso.TYPE_ID).entrySet()){
                                if(CnALink.isDownwardLink(t, entry.getValue()) && entry.getValue().getRelationId().equals(PROP_REL_SAMTTOPIC_PERSONISO_RESP)){
                                    PersonIso e = (PersonIso)getDaoFactory().getDAO(PersonIso.TYPE_ID).initializeAndUnproxy(entry.getKey());
                                    ArrayList<String> list = new ArrayList<String>(0);
                                    list.add(e.getSurname() + ", " + e.getName());
                                    personSet.add(list);
                                }
                            }
                        }
                    }
                }
            } catch (CommandException e){
                LOG.error("Error determing persons linked with insufficient samttopics", e);
            }
            result.addAll(personSet);
        }
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
        if(LOG.isDebugEnabled()){
            LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.result;
    }
    
}
