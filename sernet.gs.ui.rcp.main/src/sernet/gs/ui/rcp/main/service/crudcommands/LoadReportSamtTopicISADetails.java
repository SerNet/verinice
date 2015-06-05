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

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportSamtTopicISADetails extends GenericCommand implements ICachedCommand{
    
    private static transient Logger log = Logger.getLogger(LoadReportSamtTopicISADetails.class);
    private static final String PROP_SAMT_RISK = "samt_topic_audit_ra";	
    private static final String SAMT_TOPIC_ISA_FINDINGS = "samt_topic_audit_findings";
    
    private boolean resultInjectedFromCache = false;

    public static final String[] COLUMNS = new String[] { 
        "TITLE",
        "FINDINGS",
        "MATURITY",
        "RISK",
        "DBID"
        };
    private Integer rootElmt;
    
    private List<List<String>> result;
    
    public LoadReportSamtTopicISADetails(Integer root){
        this.rootElmt = root;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    public void execute() {
        if(!resultInjectedFromCache){
            try {
                result = new ArrayList<List<String>>(0);
                ControlGroup cg = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(rootElmt);
                CnATreeElement elmt = cg;
                if(cg == null){
                    RetrieveCnATreeElement command = new RetrieveCnATreeElement("cnatreeelement", rootElmt);
                    command = getCommandService().executeCommand(command);
                    elmt = command.getElement();
                }
                LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, elmt.getDbId(), true);
                command = getCommandService().executeCommand(command);
                for(CnATreeElement c : command.getElements()){
                    ArrayList<String> list = new ArrayList<String>(0);
                    if(SamtTopic.class.isInstance(c)){
                        SamtTopic st = (SamtTopic)c;

                        list.add(st.getTitle()); // add title

                        list.add(st.getEntity().getSimpleValue(SAMT_TOPIC_ISA_FINDINGS)); // add findings

                        list.add(String.valueOf(Integer.parseInt(st.getEntity().getValue(SamtTopic.PROP_MATURITY)))); // add maturity

                        list.add(String.valueOf(Integer.parseInt(st.getEntity().getValue(PROP_SAMT_RISK)))); // add risk    

                        list.add(String.valueOf(st.getDbId())); // add dbid
                    }

                    // add results for samttopic to resultlist
                    result.add(list);
                }

            } catch (CommandException e) {
                getLog().error("");
            }
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
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.result;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportSamtTopicISADetails.class);
        }
        return log;
    }
}
