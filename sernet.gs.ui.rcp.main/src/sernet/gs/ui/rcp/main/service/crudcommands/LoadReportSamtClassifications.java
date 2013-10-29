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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportSamtClassifications extends GenericCommand implements ICachedCommand{
    
    private static final Logger LOG = Logger.getLogger(LoadReportSamtClassifications.class);

    private static final String PROP_CLASSIFICATION_NO = "samt_classification_none";
    private static final String PROP_CLASSIFICATION_GOOD = "samt_classification_good";
    private static final String PROP_CLASSIFICATION_INSUFFICIENT = "samt_classification_insufficient";
    private static final String PROP_SAMT_CLASSIFICATION = "samt_user_classification";
    
    
    private Boolean getGood = null;
    
    public static final String[] COLUMNS = new String[] { 
        "samtTitle", "samtDbid"
        };
    private Integer rootElmt;
    
    private List<List<String>> result;
    
    private boolean resultInjectedFromCache = false;

    public LoadReportSamtClassifications(Integer root, boolean getGood){
        this.rootElmt = root;
        this.getGood = Boolean.valueOf(getGood);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            result = new ArrayList<List<String>>(0);
            try{
                LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, rootElmt, true);
                command = getCommandService().executeCommand(command);
                for(CnATreeElement c : command.getElements()){
                    if(c instanceof SamtTopic){
                        SamtTopic t = (SamtTopic)c;

                        String propertyValueToGet = null;
                        if(getGood == null){
                            propertyValueToGet = PROP_CLASSIFICATION_NO;
                        }
                        else if(getGood.booleanValue()){
                            propertyValueToGet = PROP_CLASSIFICATION_GOOD;
                        } else {
                            propertyValueToGet = PROP_CLASSIFICATION_INSUFFICIENT;
                        }
                        if(!t.isChildrenLoaded()){
                            t = (SamtTopic)loadChildren(t, SamtTopic.TYPE_ID);
                        }
                        t = (SamtTopic)getDaoFactory().getDAO(SamtTopic.TYPE_ID).initializeAndUnproxy(t);
                        Entity ent = ((Entity)getDaoFactory().getDAO(Entity.TYPE_ID).initializeAndUnproxy(t.getEntity()));
                        String propValue = null;
                        try{
                            propValue = ent.getOptionValue(PROP_SAMT_CLASSIFICATION);
                        } catch (NullPointerException npe){
                            LOG.error("classification-value not found on samtTopic");
                        }

                        if(propValue != null && propValue.equals(propertyValueToGet)){
                            ArrayList<String> list = new ArrayList<String>(0);
                            list.add(t.getTitle());
                            list.add(String.valueOf(t.getDbId()));
                            result.add(list);
                        }
                    }
                }
            }catch(CommandException e){
                LOG.error("Error while determing samt topics", e);
            }
        }
    }
    
    public List<List<String>> getResult(){
        return result;
    }
    
    private CnATreeElement loadChildren(CnATreeElement el, String typeID) {
        if (el.isChildrenLoaded()) {
            return el;
        } 
        RetrieveInfo ri = new RetrieveInfo().setChildren(true).setChildrenPermissions(true).
                setChildrenProperties(true).setProperties(true);
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeID);
        return dao.findByUuid(el.getUuid(), ri);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        cacheID.append(String.valueOf(getGood));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
        if(LOG.isDebugEnabled()){
            LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
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
