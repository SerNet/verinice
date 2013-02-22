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
import sernet.verinice.model.iso27k.ControlGroup;

/**
 *
 */
public class LoadMaturityRadarChartData extends GenericCommand implements ICachedCommand{
    
    private transient Logger log = Logger.getLogger(LoadMaturityRadarChartData.class);
    
    public static final String[] COLUMNS = new String[] { 
        "CATEGORIES",
        "MATURITYDATA",
        "THRESHOLD",
        "PADDING"
        };
    
    private Integer rootElmt;
    private Integer sgdbid;
    
    private static final int THRESHOLD_VALUE = 3;
    private static final int PADDING_VALUE = 4;

    
    private List<List<String>> result;
    
    private ControlGroup samtRootGroup;
    
    private boolean resultInjectedFromCache = false;

    public LoadMaturityRadarChartData(Integer root){
        this.rootElmt = root;
    }
    
    public LoadMaturityRadarChartData(Integer root, Integer samtGroupId){
        this(root);
        this.sgdbid = samtGroupId;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadMaturityRadarChartData.class);
        }
        return log;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            result = new ArrayList<List<String>>(0);
            samtRootGroup = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(sgdbid);
            try{
                ArrayList<ControlGroup> list = new ArrayList<ControlGroup>();
                list.add(samtRootGroup);
                result = getMaturityValues(list);
            } catch (Exception e){
                getLog().error("Error while filling maturityChart dataset", e);
            }
        }
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
    
    public List<List<String>> getResult() {
        Collections.sort(result, new Comparator<List<String>>() {

            @Override
            public int compare(List<String> o1, List<String> o2) {
                NumericStringComparator nc = new NumericStringComparator();
                return nc.compare(o1.get(0), o2.get(0));
            }
        });
         return result;
    }
    
    public List<List<String>> getMaturityValues(List<ControlGroup> groups){
        ArrayList<List<String>> list = new ArrayList<List<String>>(0);
        for(ControlGroup cg : groups){
            try{
                CSRMassnahmenSummaryHome dao = new CSRMassnahmenSummaryHome();

                Map<String, Double> items1 = dao.getControlGroups(cg);
                Set<Entry<String, Double>> entrySet = items1.entrySet();

                for(Entry<String, Double> entry : sort(entrySet)){
                    ArrayList<String> row = new ArrayList<String>();
                    row.add(entry.getKey());
                    row.add(String.valueOf(entry.getValue()));
                    row.add(String.valueOf(THRESHOLD_VALUE));
                    row.add(String.valueOf(PADDING_VALUE));
                    row.trimToSize();
                    list.add(row);
                }
            } catch (CommandException e){
                getLog().error("Error while determing maturity values", e);
            }
        }
        list.trimToSize();
        return list;
    }
    
    public int getSgDBId(){
        return samtRootGroup.getDbId();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        if(sgdbid != null){
            cacheID.append(String.valueOf(sgdbid));
        } else {
            cacheID.append("null");
        }
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        if(result instanceof Object[]){
            Object[] array = (Object[])result;
            this.result = (ArrayList<List<String>>)array[0];
            this.samtRootGroup = (ControlGroup)array[1];
            resultInjectedFromCache = true;
            if(getLog().isDebugEnabled()){
                getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
            }
        }
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        Object[] cacheableResult = new Object[2];
        cacheableResult[0] = this.result;
        cacheableResult[1] = samtRootGroup;
        return cacheableResult;
    }
}
