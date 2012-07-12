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
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Process;

/**
 * 
 */
public class LoadAssetTotalRiskFigures extends GenericCommand {
    
    public static final String[] COLUMNS = new String[] { 
        "RISK_C",
        "RISK_I",
        "RISK_A",
        "Asset"
        };
    
    private Integer rootElmt;
   
    private List<List<String>> result;
    
    private transient Set<Integer> seenAssets;
    private transient Set<Integer> seenScenarios;
    
    public LoadAssetTotalRiskFigures(Integer rootElement){
        this.rootElmt = rootElement;
        result = new ArrayList<List<String>>(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try{
            seenScenarios = new HashSet<Integer>();
            seenAssets = new HashSet<Integer>();
            
            LoadReportElements command = new LoadReportElements(Process.TYPE_ID, rootElmt);
            command = getCommandService().executeCommand(command);
            if (command.getElements() == null || command.getElements().size() == 0) {
                result = new ArrayList<List<String>>(0);
                return;
            }
            List<CnATreeElement> elements = command.getElements();

            for (CnATreeElement process : elements) {
                LoadReportLinkedElements cmnd2 = new LoadReportLinkedElements(Asset.TYPE_ID, process.getDbId(), true, false);
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
    
    private List<String> makeRow(CnATreeElement asset){
        ArrayList<String> row = new ArrayList<String>();

        String risk_c = asset.getEntity().getSimpleValue("asset_riskvalue_c");
        String risk_i = asset.getEntity().getSimpleValue("asset_riskvalue_i");
        String risk_a = asset.getEntity().getSimpleValue("asset_riskvalue_a");
        
        row.add(risk_c);
        row.add(risk_i);
        row.add(risk_a);
        row.add(asset.getTitle());
        
        return row;
        
    }

    public Integer getRootElmt() {
        return rootElmt;
    }

    public List<List<String>> getResult() {
        return result;
    }


}
