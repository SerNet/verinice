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
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Finding;

/**
 *
 */
public class LoadReportISASignificantFindings extends GenericCommand implements ICachedCommand{
    
    private transient Logger log = Logger.getLogger(LoadReportISASignificantFindings.class);
    
    private static final String SHOW_FINDING_IN_REPORT = "finding_showInISAReport";
    private static final String FINDING_DESCRIPTION = "finding_desc";
    
    private List<List<String>> result;
    
    private Integer rootElmt;
    
    public static final String[] COLUMNS = new String[]{"TITLE", "DESCRIPTION"};
    
    private boolean resultInjectedFromCache = false;
    
    public LoadReportISASignificantFindings(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try {
                result = new ArrayList<List<String>>(0);
                LoadReportElements command = new LoadReportElements(Finding.TYPE_ID, rootElmt, true);
                command = getCommandService().executeCommand(command);
                if (command.getElements() == null || command.getElements().size() == 0) {
                    return;
                }
                for(CnATreeElement c : command.getElements()){
                    if(c instanceof Finding){
                        Finding f = (Finding)c;
                        if(Integer.parseInt(f.getEntity().getSimpleValue(SHOW_FINDING_IN_REPORT)) == 1){
                            ArrayList<String> row = new ArrayList<String>(0);
                            row.add(f.getTitle());
                            row.add(f.getEntity().getSimpleValue(FINDING_DESCRIPTION));
                            result.add(row);
                        }
                    }
                }
            } catch (CommandException e) {
                getLog().error("Error while computing report findings", e);
            }
        }
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportISASignificantFindings.class);
        }
        return log;
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
        return result;
    }

}
