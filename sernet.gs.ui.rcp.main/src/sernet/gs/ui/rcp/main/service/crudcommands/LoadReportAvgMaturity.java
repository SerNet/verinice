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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.CSRMassnahmenSummaryHome;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 *
 */
public class LoadReportAvgMaturity extends GenericCommand implements ICachedCommand {
    
    private transient Logger log = Logger.getLogger(LoadReportAvgMaturity.class);
    
    private int matCount = 0;
    private double matSum = 0.0;
    
    private List<List<String>> result;
    
    private Integer rootElmt;
    
    private boolean resultInjectedFromCache = false;
    
    public static final String[] COLUMNS = new String[]{"avgMaturity", "SGID"};
    
    public LoadReportAvgMaturity(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            result = new ArrayList<List<String>>(0);
            try{
                FindSGCommand command = new FindSGCommand(true, rootElmt);
                command = getCommandService().executeCommand(command);
                ControlGroup cg = command.getSelfAssessmentGroup();
                ArrayList<ControlGroup> list = new ArrayList<ControlGroup>();
                list.add(cg);
                for(ControlGroup g : list){
                    CSRMassnahmenSummaryHome dao = new CSRMassnahmenSummaryHome();
                    Map<String, Double> items1 = dao.getControlGroups(g);
                    for(Entry<String, Double> entry : items1.entrySet()){
                        addMaturity(entry.getValue());
                    }
                }
                ArrayList<String> tmplist = new ArrayList<String>(0);
                tmplist.add(String.valueOf(getMaturityAvg()));
                tmplist.add(String.valueOf(cg.getDbId()));
                result.add(tmplist);
            } catch (Exception e){
                getLog().error("Error while computing avgMaturity", e);
            }
        }
    }
        
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportAvgMaturity.class);
        }
        return log;
    }

    private void addMaturity(double mat){
        if(mat > 0){
            matSum += mat;
            matCount++;
        }
    }

    private String getMaturityAvg(){
        final int precision = 3;
        if(matCount > 0){
            double d = (double)matSum / (double)matCount;
            return String.valueOf(round(d, precision));
        } else {return "0.0";}
    }
    
    private double round(double value, int precision)
    {
        final double powBase = 10d;
        double rounded = Math.round(value * Math.pow(powBase, precision));
        return rounded / Math.pow(powBase, precision);
    } 
    
    public List<List<String>> getResult(){
        return result;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getCanonicalName());
        sb.append(String.valueOf(rootElmt));
        return sb.toString();
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

}
