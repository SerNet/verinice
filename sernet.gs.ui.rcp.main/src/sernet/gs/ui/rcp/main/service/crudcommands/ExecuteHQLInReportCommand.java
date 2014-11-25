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

import java.util.Collections;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;

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
        this.hqlParams = params;
        this.typeId = typeId;
    }
    
    public ExecuteHQLInReportCommand(String hql, Object[] params, Class typeId){
        this.hql = hql;
        this.hqlParams = params;
        this.typeId = typeId;
    }
    
    public ExecuteHQLInReportCommand(String hql, String[] paramNames, Object[] params, Class typeId){
        this.hql = hql;
        this.hqlParams = params;
        this.typeId = typeId;
        this.paramNames = paramNames;
    }    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
    
        if(!resultInjectedFromCache){
            if(typeId instanceof String){
                results = getDaoFactory().getDAO((String)typeId).findByQuery(hql, hqlParams);
            } else if (typeId instanceof Class && paramNames == null){
                results = getDaoFactory().getDAO((Class)typeId).findByQuery(hql, hqlParams);
            } else if(typeId instanceof Class && paramNames != null){
                results = getDaoFactory().getDAO((Class)typeId).findByQuery(hql, paramNames, hqlParams);
            } else {
                results = Collections.emptyList();
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
        return results;
    }
    
    public Object getResult(){
        return results;
    }
    

}
