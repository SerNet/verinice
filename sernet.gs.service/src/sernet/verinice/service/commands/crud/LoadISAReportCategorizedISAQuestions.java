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
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.samt.SamtTopic;

/**
 *  returns title of all "assessment: finding"-objects which are categorized insufficient or good and 
 *  are children of an given audit (root) 
 */
public class LoadISAReportCategorizedISAQuestions extends GenericCommand implements ICachedCommand {

    private int rootElmt;
    private String categorie;
    
    private static final Logger LOG = Logger.getLogger(LoadISAReportCategorizedISAQuestions.class);
    
    public static final String[] COLUMNS = new String[]{
                                            "TITLE",
                                            "FINDINGS"
    };
    
    
    public static final String SAMTTOPIC_CATEGORIZATION_GOOD = "samt_topic_classification_good";
    public static final String SAMTTOPIC_CATEGORIZATION_INSUFFICIENT = "samt_topic_classification_insufficient";
    private static final String SAMTTOPIC_CATEGORIZATION = "samt_topic_user_classification";
    private static final String SAMTTOPIC_FINDING_PROPERTY = "samt_topic_audit_findings";
    
    private List<List<String>> results;
    
    public LoadISAReportCategorizedISAQuestions(int root, String categorie){
        this.rootElmt = root;
        this.categorie = categorie;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        //        if(!resultInjectedFromCache){
        List<Object> hqlResult;
        String isaQuestionHql = "select elmt.dbId from CnATreeElement elmt " + // NON-NLS-1$
                "inner join elmt.entity as entity " + // NON-NLS-1$
                "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                "inner join propertyList.properties as props " + //$NON-NLS-1$"
                "where elmt.objectType = ? " +
                "and elmt.scopeId = ? " + //$NON-NLS-1$"
                "and props.propertyType = ? " + //$NON-NLS-1$
                "and props.propertyValue = ? "; //$NON-NLS-1$"

        results = new ArrayList<List<String>>(0);

        Object[] params = new Object[]{SamtTopic.TYPE_ID, getRootAuditScopeID(rootElmt), SAMTTOPIC_CATEGORIZATION, categorie };
        hqlResult =  getDaoFactory().getDAO(SamtTopic.TYPE_ID).findByQuery(isaQuestionHql, params);
        if(hqlResult != null && hqlResult.size() > 0){
            for(Object id : hqlResult){
                if(id instanceof Integer){
                    SamtTopic topic = (SamtTopic)getDaoFactory().getDAO(SamtTopic.TYPE_ID).findById((Integer)(id));
                    ArrayList<String> result = new ArrayList<String>(0);
                    result.add(topic.getTitle());
                    result.add(topic.getEntity().getSimpleValue(SAMTTOPIC_FINDING_PROPERTY));
                    
                    results.add(result);
                }
            }
        }


        //        }
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
    
    private int getRootAuditScopeID(int rootScopeID) {
        String scopeIDhql = "select scopeId from CnATreeElement where dbId = ?";
        Object[] scopeIDparams = new Object[]{this.rootElmt};
        List<Object> hqlResult   = getDaoFactory().getDAO(Audit.TYPE_ID).findByQuery(scopeIDhql, scopeIDparams);
        if (hqlResult != null && hqlResult.size() == 1) {
            if(hqlResult.get(0) instanceof Integer){
                rootScopeID = ((Integer)hqlResult.get(0)).intValue();
            }
        }
        return rootScopeID;
    }

}
