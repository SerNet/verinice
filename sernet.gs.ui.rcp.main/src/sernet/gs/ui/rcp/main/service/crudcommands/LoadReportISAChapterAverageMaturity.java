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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportISAChapterAverageMaturity extends GenericCommand implements ICachedCommand {
    
    private static transient Logger LOG = Logger.getLogger(LoadReportISAChapterAverageMaturity.class);
    private static final String PROP_ISATOPIC_MATURITY = "samt_topic_maturity";
    
    public static String[] COLUMNS = new String[]{"maturityAverage"};
    private Integer rootElmnt;
    
    private List<List<String>> result;
    
    private boolean resultInjectedFromCache = false;
    
    public LoadReportISAChapterAverageMaturity(Integer root) {
        this.rootElmnt = root;
    }
    
    public LoadReportISAChapterAverageMaturity(String root){
        this.rootElmnt = Integer.valueOf(Integer.parseInt(root));
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            int topicCount = 0;
            int maturityCount = 0;
            result = new ArrayList<List<String>>(0);
            ArrayList<String> list = new ArrayList<String>(0);
            DecimalFormat df = new DecimalFormat("0.00");
            try {
                LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, rootElmnt);
                command = getCommandService().executeCommand(command);
                List<CnATreeElement> elements = command.getElements();
                for(CnATreeElement e : elements){
                    if(e instanceof SamtTopic){
                        SamtTopic topic = (SamtTopic)e;
                        topicCount++;
                        maturityCount += Integer.parseInt(topic.getEntity().getSimpleValue(PROP_ISATOPIC_MATURITY));
                    }
                }

                double d = new Double(maturityCount) / new Double(topicCount);
                String s = df.format(d);
                list.add(s);
                result.add(list);
            } catch (CommandException e) {
                LOG.error("Error while determing samt topics", e);
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
        cacheID.append(String.valueOf(rootElmnt));
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
        return result;
    }

    
}
