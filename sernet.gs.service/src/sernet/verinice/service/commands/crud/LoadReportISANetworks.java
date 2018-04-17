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
package sernet.verinice.service.commands.crud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.Retriever;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 * computes all network scans for a given audit, network scans needs to be organized within a 
 * parent controlgroup which is called "01.02 Networkscans", containing controlgroups itself, which a 
 * instances for a single network
 * these networks (controlgroups) are containing isa questions (samtTopics) which a representing a single scan
 * a scan can reference an evidence object which references an attachment (png/jpg) which is calculated also in case of existance
 */
public class LoadReportISANetworks extends GenericCommand implements ICachedCommand{

    private Integer rootElmt;
    
    private boolean resultInjectedFromCache = false;
    
    private static transient Logger LOG = Logger.getLogger(LoadReportISANetworks.class);
    
    private static final String NETWORKS_ROOTGROUPNAME = "01.02 Networkscans";
    
    private static final String SAMTTOPICS_FINDINGS = "samt_topic_audit_findings";
    
    private static final String CONTROLGROUP_OBJECTTYPE = ControlGroup.TYPE_ID;
    
    public static final String[] NETWORKCOLUMNS = new String[] {
                                        "NETWORK_TITLE",
                                        "FINDINGS",
                                        "CONTROLS", 
                                        "DBID",
                                        "NR"
    };

    private static final String CONTROLGROUP_TITLE = ControlGroup.PROP_NAME;
    
    private List<List<String>> networkResults;
    
    public LoadReportISANetworks(Integer root){
        this.rootElmt = root;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            networkResults = new ArrayList<List<String>>(0);
            ControlGroup networkScansGroup = null;
            try {
                networkScansGroup = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(getRootNetworkGroup(getRootAuditScopeID()));
                if(networkScansGroup == null){
                    getLog().error("NetworkRootGroup:\t" + NETWORKS_ROOTGROUPNAME + " not found! Qutting Command and returning empty list");
                    return;
                }
                LoadReportElements cgFinder = new LoadReportElements(ControlGroup.TYPE_ID, networkScansGroup.getDbId(), true);
                cgFinder = getCommandService().executeCommand(cgFinder);
                List<CnATreeElement> cList = new ArrayList<CnATreeElement>();
                cList.addAll(cgFinder.getElements(ControlGroup.TYPE_ID, networkScansGroup));
                for(CnATreeElement c : cList){
                    if(c instanceof ControlGroup){
                        List<String> networkResult = new ArrayList<String>(0);
                        ControlGroup network = (ControlGroup)Retriever.checkRetrieveElementAndChildren(c);
                        networkResult.add(network.getTitle());
                        StringBuilder[] sb = computeFindingsAndMeasures(network);
                        // add findings to result
                        networkResult.add(sb[0].toString());
                        // add measures to result
                        networkResult.add(sb[1].toString());
                        // add dbid
                        networkResult.add(String.valueOf(network.getDbId()));
                        
                        networkResults.add(networkResult);
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

    private StringBuilder[] computeFindingsAndMeasures(ControlGroup network) {
        StringBuilder[] retVal = new StringBuilder[2];
        // findings
        retVal[0] = new StringBuilder();
        // derivedMeasures
        retVal[1] = new StringBuilder();
        Iterator<CnATreeElement> iterator = network.getChildren().iterator();
        while(iterator.hasNext()){
            CnATreeElement e = iterator.next();
            if(e.getTypeId().equals(SamtTopic.TYPE_ID)){
                SamtTopic t = (SamtTopic)e;
                retVal[1].append(t.getTitle());
                retVal[0].append(t.getEntity().getSimpleValue(SAMTTOPICS_FINDINGS));
                if(iterator.hasNext()){
                    retVal[0].append("\n\n");
                    retVal[1].append("\n");
                }
                
                
            }
        }
        return retVal;
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
    
    private int getRootNetworkGroup(int rootScopeID) {
        List<Object> hqlResult;
        String hql = "select elmt.dbId from CnATreeElement elmt " + // NON-NLS-1$
                     "inner join elmt.entity as entity " + // NON-NLS-1$
                     "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                     "inner join propertyList.properties as props " + //$NON-NLS-1$"
                     "where elmt.objectType = ? " +
                     "and elmt.scopeId = ? " + //$NON-NLS-1$"
                     "and props.propertyType = ? " + //$NON-NLS-1$
                     "and props.propertyValue = ? "; //$NON-NLS-1$"
                     
        Object[] params = new Object[]{CONTROLGROUP_OBJECTTYPE, rootScopeID, CONTROLGROUP_TITLE, NETWORKS_ROOTGROUPNAME };
        hqlResult =  getDaoFactory().getDAO(ControlGroup.TYPE_ID).findByQuery(hql, params);
        if (hqlResult != null && hqlResult.size() == 1) {
            if(hqlResult.get(0) instanceof Integer){
                return ((Integer)hqlResult.get(0)).intValue();
            }
        }
        return -1;
    }

    private int getRootAuditScopeID() {
        String scopeIDhql = "select scopeId from CnATreeElement where dbId = ?";
        Object[] scopeIDparams = new Object[]{this.rootElmt};
        int rootScopeID = -1;
        List<Object> hqlResult   = getDaoFactory().getDAO(Audit.TYPE_ID).findByQuery(scopeIDhql, scopeIDparams);
        if (hqlResult != null && hqlResult.size() == 1) {
            if(hqlResult.get(0) instanceof Integer){
                rootScopeID = ((Integer)hqlResult.get(0)).intValue();
            }
        }
        return rootScopeID;
    }
    
    public List<List<String>> getResult(){
        return networkResults;
    }
}
