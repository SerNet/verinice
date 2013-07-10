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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 *
 */
public class LoadReportISANetworks extends GenericCommand implements ICachedCommand{

    private Integer rootElmt;
    private Integer sgdbid;
    
    private boolean resultInjectedFromCache = false;
    
    private static transient Logger LOG = Logger.getLogger(LoadReportISANetworks.class);
    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";
    
    public static final String[] NETWORKCOLUMNS = new String[] {
                                        "NETWORK_TITLE",
                                        "NR"
    };
    
    private List<List<String>> networkResults;
    
    public LoadReportISANetworks(Integer root, Integer sgdbid){
        this.rootElmt = root;
        this.sgdbid = sgdbid;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            networkResults = new ArrayList<List<String>>(0);
            ControlGroup rootControlGroup = null;
            try {
                if(sgdbid != null){
                    rootControlGroup = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(sgdbid);
                } else {
                    FindSGCommand command = new FindSGCommand(true, rootElmt);
                    command = getCommandService().executeCommand(command);
                    rootControlGroup = command.getSelfAssessmentGroup();
                }
                LoadReportElements cgFinder = new LoadReportElements(ControlGroup.TYPE_ID, rootControlGroup.getDbId(), true);
                cgFinder = getCommandService().executeCommand(cgFinder);
                List<CnATreeElement> cList = new ArrayList<CnATreeElement>();
                cList.addAll(cgFinder.getElements(ControlGroup.TYPE_ID, rootControlGroup));
                for(CnATreeElement c : cList){
                    if(c instanceof ControlGroup){
                        ControlGroup group = (ControlGroup)Retriever.checkRetrieveElementAndChildren(c);
                        String overview = group.getEntity().getSimpleValue(OVERVIEW_PROPERTY);
                        String network = group.getEntity().getSimpleValue("controlgroup_is_network");
                        String title = group.getTitle();
                        if(group.getEntity().getSimpleValue(OVERVIEW_PROPERTY).equals("1") &&
                                group.getEntity().getSimpleValue("controlgroup_is_network").equals("1")){
                            List<String> networkResult = new ArrayList<String>();
                            networkResult.add(group.getTitle());
                            networkResults.add(networkResult);
                        }
                    }
                }
            } catch (CommandException e){
                getLog().error("Error while executing command", e);
            }
        }
        Collections.sort(networkResults, new Comparator<List<String>>() {

            @Override
            public int compare(List<String> o1, List<String> o2) {
                NumericStringComparator nc = new NumericStringComparator();
                return nc.compare(o1.get(0), o2.get(0));
            }
        });
        for(List<String> list : networkResults){
            list.add(String.valueOf(networkResults.indexOf(list) + 1));
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
            networkResults = (ArrayList<List<String>>)array[0];
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
        Object[] result = new Object[1];
        result[0] = networkResults;
        return result;
    }
    
    private Logger getLog(){
        if(LOG == null){
            LOG = Logger.getLogger(LoadReportISANetworks.class);
        }
        return LOG;
    }

    public List<List<String>> getNetworkResults() {
        return networkResults;
    }

}
