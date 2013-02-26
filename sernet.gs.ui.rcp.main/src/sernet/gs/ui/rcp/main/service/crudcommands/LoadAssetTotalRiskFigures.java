/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Process;

/**
 * 
 */
public class LoadAssetTotalRiskFigures extends GenericCommand implements ICachedCommand{
    
    public static final String[] COLUMNS = new String[] { 
        "RISK_C",
        "RISK_I",
        "RISK_A",
        "Asset"
        };
    
    private Integer rootElmt;
   
    private List<List<String>> result;
    
    private transient Set<Integer> seenAssets;
    
    private boolean resultInjectedFromCache = false;
    
    public LoadAssetTotalRiskFigures(Integer rootElement){
        this.rootElmt = rootElement;
        result = new ArrayList<List<String>>(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try{
                seenAssets = new HashSet<Integer>();

                LoadReportElements command = new LoadReportElements(Process.TYPE_ID, rootElmt);
                command = getCommandService().executeCommand(command);
                if (command.getElements() == null || command.getElements().size() == 0) {
                    result = new ArrayList<List<String>>(0);
                    return;
                }
                List<CnATreeElement> elements = command.getElements();

                for (CnATreeElement process : elements) {
                    // use of hashmap that prohibit double asset use allows to use true for doUpLinksAlso parameter
                    // otherwise this would produce wrong results in some reports
                    LoadReportLinkedElements cmnd2 = new LoadReportLinkedElements(Asset.TYPE_ID, process.getDbId(), true, true);
                    cmnd2 = getCommandService().executeCommand(cmnd2);
                    List<CnATreeElement> assets = cmnd2.getElements();
                    for (CnATreeElement asset : assets) {
                        if (  ! (seenAssets.contains(asset.getDbId())) )  {
                            result.add(makeRow(asset));
                            seenAssets.add(asset.getDbId());
                        }
                    }
                }

            } catch (CommandException e){
                throw new RuntimeCommandException(e);
            }
        }
    }
    
    private List<String> makeRow(CnATreeElement asset){
        ArrayList<String> row = new ArrayList<String>();

        String riskC = asset.getEntity().getSimpleValue("asset_riskvalue_c");
        String riskI = asset.getEntity().getSimpleValue("asset_riskvalue_i");
        String riskA = asset.getEntity().getSimpleValue("asset_riskvalue_a");
        
        row.add(riskC);
        row.add(riskI);
        row.add(riskA);
        row.add(asset.getTitle());
        
        return row;
        
    }

    public Integer getRootElmt() {
        return rootElmt;
    }

    public List<List<String>> getResult() {
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
        this.resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.result;
    }
    

}
