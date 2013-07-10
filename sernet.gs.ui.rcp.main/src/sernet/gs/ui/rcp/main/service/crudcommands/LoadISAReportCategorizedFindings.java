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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Finding;

/**
 *  returns title of all "assessment: finding"-objects which are categorized insufficient or good and 
 *  are children of an given audit (root) 
 */
public class LoadISAReportCategorizedFindings extends GenericCommand implements ICachedCommand {

    private int rootElmt;
    private String categorie;
    
    private static final Logger LOG = Logger.getLogger(LoadISAReportCategorizedFindings.class);
    
    public static final String[] COLUMNS = new String[]{
                                            "TITLE"
    };
    
    public static final String FINDING_CATEGORIZATION_GOOD = "finding_classification_good";
    public static final String FINDING_CATEGORIZATION_INSUFFICIENT = "finding_classification_insufficient";
    private static final String FINDING_CATEGORIZATION = "finding_user_classification";
    
    private boolean resultInjectedFromCache = false;
    
    private List<List<String>> results;
    
    public LoadISAReportCategorizedFindings(int root, String categorie){
        this.rootElmt = root;
        this.categorie = categorie;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try{
                results = new ArrayList<List<String>>(0);
                Audit audit = (Audit)getDaoFactory().getDAO(Audit.TYPE_ID).findById(Integer.valueOf(rootElmt));
                LoadReportElements findingLoader = new LoadReportElements(Finding.TYPE_ID, rootElmt);
                findingLoader = getCommandService().executeCommand(findingLoader);

                for(CnATreeElement elmt : findingLoader.getElements()){
                    if(elmt.getTypeId().equals(Finding.TYPE_ID) && elmt.getEntity().getOptionValue(FINDING_CATEGORIZATION).equals(categorie)){
                        ArrayList<String> result = new ArrayList<String>(0);
                        result.add(elmt.getTitle());
                        results.add(result);
                    }
                }
            } catch(CommandException e){
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
        cacheID.append(categorie);
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
