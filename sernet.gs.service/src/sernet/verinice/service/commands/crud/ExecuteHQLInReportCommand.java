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

import java.util.Collections;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.interfaces.IReportHQLService;
import sernet.verinice.model.report.HQLSecurityException;

/**
 * Command to enable report designers to execute hql from within a rptdesign file.
 * typeId is needed to determine which dao should be used to execute query, if cnatreelement dao is needed,
 * use constructor with parameter of type Class (CnATreeElement.class) in dataset query.
 * Attention: be sure to define right columns in setup query and cast the return value if needed
 */
public class ExecuteHQLInReportCommand extends GenericCommand implements ICachedCommand {

    private static final Logger LOG = Logger.getLogger(ExecuteHQLInReportCommand.class);
    
    private boolean resultInjectedFromCache = false;
    
    private String hql;
    
    private Object[] hqlParams;
    
    private String[] paramNames;
    
    private Object typeId;
    
    private Object results;
    
    public ExecuteHQLInReportCommand(String hql, Object[] params, String typeId){
        this.hql = hql;
        setHqlParams(params);
        this.typeId = typeId;
    }
    
    public ExecuteHQLInReportCommand(String hql, Object[] params, Class typeId){
        this.hql = hql;
        setHqlParams(params);
        this.typeId = typeId;
    }
    
    public ExecuteHQLInReportCommand(String hql, String[] paramNames, Object[] params, Class typeId){
        this.hql = hql;
        setHqlParams(params);
        this.typeId = typeId;
        setParamNames(paramNames);
    }    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {

        if(!resultInjectedFromCache){
            try{
                IBaseDao dao = null;
                if(typeId instanceof String){
                    dao = getDaoFactory().getDAO((String)typeId);
                } else if (typeId instanceof Class){
                    dao = getDaoFactory().getDAO((Class<?>)typeId);
                }
                if(getReportHQLService().isQueryAllowed(hql)){
                    if (paramNames == null || paramNames.length == 0){
                        results = dao.findByQuery(hql, hqlParams);
                    } else {
                        results = dao.findByQuery(hql, paramNames, hqlParams);
                    } 
                } else {
                    throw new RuntimeException(new HQLSecurityException("HQL-Query:\n\t" + hql + "\nviolates verinice security policies, execution of query denied"));
                }
            } catch (Exception t){
                LOG.error("Exception occurred", t);
            }
        }
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getCanonicalName());
        sb.append(hql);
        for(Object param : this.hqlParams){
            sb.append(String.valueOf(param));
        }
        sb.append(typeId);
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.results = result;
        resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return getResult();
    }
    
    public Object getResult(){
        if(results == null){
            return Collections.emptyList();
        }
        return results;
    }

    public void setHqlParams(Object[] hqlParams) {
        this.hqlParams = (hqlParams!=null) ? hqlParams.clone() : null;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = (paramNames!=null) ? paramNames.clone() : null;
    }
    
    public IReportHQLService getReportHQLService(){
        return (IReportHQLService)VeriniceContext.get(VeriniceContext.REPORT_HQL_SERVICE);
    }
}
