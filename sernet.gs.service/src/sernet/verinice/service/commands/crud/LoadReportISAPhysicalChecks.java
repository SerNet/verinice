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
package sernet.verinice.service.commands.crud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.Retriever;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 * returns implementation lvl count, severity lvl count and name for all it-rooms of one given assessment
 * used in a dataset in isareport (2013)
 */
public class LoadReportISAPhysicalChecks extends GenericCommand implements ICachedCommand{
    
    private transient Logger log = Logger.getLogger(LoadReportISAPhysicalChecks.class);
    
    private int rootElement;

    private List<List<String>> results;
    
    private static final String CONTROLGROUP_OBJECTTYPE = ControlGroup.TYPE_ID;
    
    private static final String CONTROLGROUP_TITLE = ControlGroup.PROP_NAME;
    
    private static final String ITROOM_ROOTGROUPNAME = "01.03 Room Checks";
    
    private static final String SEVERITY_PROPERTY = "samt_topic_audit_ra";
    
    private static final String IMPLEMENTED_PROPERTY = "samt_topicimplemented";
    
    private static final String VALUE_IMPLEMENTED_YES = "samt_topicimplemented_yes";
    private static final String VALUE_IMPLEMENTED_NO = "samt_topicimplemented_no";
    private static final String VALUE_IMPLEMENTED_PARTLY = "samt_topicimplemented_partly";
    private static final String VALUE_IMPLEMENTED_NA = "samt_topicimplemented_na";
    

    public static final String[] COLUMNS = new String[]{
                                            "ROOMTITLE",
                                            "IMPLEMENTATION_YES",
                                            "IMPLEMENTATION_NO",
                                            "IMPLEMENTATION_NA",
                                            "IMPLEMANTATION_PARTLY",
                                            "SEVERITY_NONE",
                                            "SEVERITY_LOW",
                                            "SEVERITY_MIDDLE",
                                            "SEVERITY_HIGH",
                                            "SEVERITY_VERYHIGH"
                                            
    };

    private boolean resultInjectedFromCache = false;

    public LoadReportISAPhysicalChecks(int root){
        this.rootElement = root;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            results = new ArrayList<List<String>>(0);
            ControlGroup roomRootGroup = null;
            int rootScopeID = -1;
            // determine itroom root group
            List<Object> hqlResult;
            rootScopeID = getRootAuditScopeID();  
            int roomRootGroupDbId = getRootITRoomGroup(rootScopeID);
            roomRootGroup = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(roomRootGroupDbId);
            roomRootGroup = (ControlGroup)Retriever.checkRetrieveElementAndChildren(roomRootGroup);
            if(getLog().isDebugEnabled()){
                getLog().debug("RootRoomGroup:\t" + roomRootGroup.getUuid());
            }
            if(roomRootGroup != null && roomRootGroup.getChildrenAsArray() != null){
                for(CnATreeElement e : roomRootGroup.getChildrenAsArray()){
                    if(e.getTypeId().equals(ControlGroup.TYPE_ID)){
                        ArrayList<String> result = new ArrayList<String>(0);
                        int implementation_yes = 0;
                        int implementation_no = 0;
                        int implementation_partly = 0;
                        int implementation_na = 0;
                        int severity_none = 0;
                        int severity_low = 0;
                        int severity_middle = 0;
                        int severity_high = 0;
                        int severity_veryhigh = 0;
                        e = Retriever.checkRetrieveElementAndChildren(e);
                        if(getLog().isDebugEnabled()){
                            getLog().debug("Inspecting Room:\t" + e.getUuid());
                        }
                        for(CnATreeElement c : e.getChildren()){
                            c = Retriever.checkRetrieveElement(c);
                            if(getLog().isDebugEnabled()){
                                getLog().debug("Inspecting SamtTopic:\t" + c.getUuid());
                            }
                            if(c.getTypeId().equals(SamtTopic.TYPE_ID)){
                                SamtTopic t = (SamtTopic)c;
                                int severity = Integer.parseInt(t.getEntity().getSimpleValue(SEVERITY_PROPERTY));
                                switch(severity){
                                case -1:
                                    severity_none++;
                                    break;
                                case 0:
                                    severity_low++;
                                    break;
                                case 1:
                                    severity_middle++;
                                    break;
                                case 2:
                                    severity_high++;
                                    break;
                                case 3:
                                    severity_veryhigh++;
                                    break;
                                default:
                                    break;
                                }
                                String implementation = t.getEntity().getOptionValue(IMPLEMENTED_PROPERTY);
                                if(implementation == null){
                                    getLog().warn("Implementation for SamtTopic " + t.getUuid() + " not set");
                                } else if(implementation.equals(VALUE_IMPLEMENTED_NA)){
                                    implementation_na++;
                                } else if(implementation.equals(VALUE_IMPLEMENTED_NO)){
                                    implementation_no++;
                                } else if(implementation.equals(VALUE_IMPLEMENTED_PARTLY)){
                                    implementation_partly++;
                                } else if(implementation.equals(VALUE_IMPLEMENTED_YES)){
                                    implementation_yes++;
                                }


                            }
                        }
                        result.add(e.getTitle());
                        result.add(String.valueOf(implementation_yes));
                        result.add(String.valueOf(implementation_no));
                        result.add(String.valueOf(implementation_na));
                        result.add(String.valueOf(implementation_partly));
                        result.add(String.valueOf(severity_none));
                        result.add(String.valueOf(severity_low));
                        result.add(String.valueOf(severity_middle));
                        result.add(String.valueOf(severity_high));
                        result.add(String.valueOf(severity_veryhigh));
                        if(result.size() > 0){
                            results.add(result);
                        }
                    } 
                }
            }
            Collections.sort(results, new Comparator<List<String>>() {

                @Override
                public int compare(List<String> o1, List<String> o2) {
                    NumericStringComparator nsc = new NumericStringComparator();
                    return nsc.compare(o1.get(0), o2.get(0));
                }
                
            });
        }
    }

    private int getRootITRoomGroup(int rootScopeID) {
        List<Object> hqlResult;
        String hql = "select elmt.dbId from CnATreeElement elmt " + // NON-NLS-1$
                     "inner join elmt.entity as entity " + // NON-NLS-1$
                     "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                     "inner join propertyList.properties as props " + //$NON-NLS-1$"
                     "where elmt.objectType = ? " +
                     "and elmt.scopeId = ? " + //$NON-NLS-1$"
                     "and props.propertyType = ? " + //$NON-NLS-1$
                     "and props.propertyValue = ? "; //$NON-NLS-1$"
                     
        Object[] params = new Object[]{CONTROLGROUP_OBJECTTYPE, rootScopeID, CONTROLGROUP_TITLE, ITROOM_ROOTGROUPNAME };
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
        Object[] scopeIDparams = new Object[]{this.rootElement};
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
        return results;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElement));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.results = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return results;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(this.getClass());
        }
        return log;
    }


}
