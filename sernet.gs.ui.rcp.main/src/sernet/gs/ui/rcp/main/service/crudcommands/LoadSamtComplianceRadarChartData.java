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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.common.model.CSRMassnahmenSummaryHome;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;

/**
 * Generates data for radarchart in samt-compliance-report
 */
public class LoadSamtComplianceRadarChartData extends GenericCommand implements ICachedCommand{
    
    private static transient Logger log = Logger.getLogger(LoadSamtComplianceRadarChartData.class);
    
    public static final String[] COLUMNS = new String[] { 
        "CATEGORIES",
        "MATURITYDATA",
        "THRESHOLDRED",
        "THRESHOLDYELLOW",
        "THRESHOLDGREEN"
        };

    private List<ArrayList<String>> result;
    
    private Integer rootElmt;
    
    private static final int THRESHHOLDRED = 2;
    private static final int THRESHHOLDYELLOW = 3;
    private static final int THRESHHOLDGREEN = 5;
    
    private boolean resultInjectedFromCache = false;
    
    public LoadSamtComplianceRadarChartData(Integer root){
        this.rootElmt = root;
        result = new ArrayList<ArrayList<String>>(0);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try{
                FindSGCommand samtGroupLoader = new FindSGCommand(true, rootElmt);
                samtGroupLoader = getCommandService().executeCommand(samtGroupLoader);

                CSRMassnahmenSummaryHome dao = new CSRMassnahmenSummaryHome();

                Map<String, Double> items1 = dao.getControlGroups(samtGroupLoader.getSelfAssessmentGroup());
                Set<Entry<String, Double>> entrySet = items1.entrySet();

                for(Entry<String, Double> entry : sort(entrySet)){
                    ArrayList<String> row = new ArrayList<String>();
                    row.add(entry.getKey());
                    row.add(String.valueOf(entry.getValue()));
                    row.add(String.valueOf(THRESHHOLDRED));
                    row.add(String.valueOf(THRESHHOLDYELLOW));
                    row.add(String.valueOf(THRESHHOLDGREEN));
                    row.trimToSize();
                    result.add(row);
                }
            } catch (CommandException e){
                getLog().error("Error while executing command", e);
            }
        }
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadSamtComplianceRadarChartData.class);
        }
        return log;
    }
    
    private List<Entry<String, Double>> sort(Set<Entry<String, Double>> entrySet) {
        ArrayList<Entry<String, Double>> list = new ArrayList<Entry<String,Double>>();
        list.addAll(entrySet);
        Collections.sort(list, new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                NumericStringComparator comparator = new NumericStringComparator();
                return comparator.compare(o1.getKey(), o2.getKey());
            }
        });
        return list;
    }

    public List<ArrayList<String>> getResult() {
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
        this.result = (ArrayList<ArrayList<String>>)result;
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
