/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

public class LoadCnAElementById extends GenericCommand implements ICachedCommand{

	private int id;
	private CnATreeElement found;
    private String typeId;
    
    private boolean resultInjectedFromCache = false;
    private static transient Logger LOG = Logger.getLogger(LoadCnAElementById.class);

	public LoadCnAElementById(String typeId, int id) {
		this.typeId= typeId;
		this.id = id;
	}
	
	public LoadCnAElementById(String typeId, String id){
		this.typeId = typeId;
		try {
			this.id = Integer.parseInt(id);
		} catch(NumberFormatException e) {
			this.id=-1;
		}
	}
	
	public LoadCnAElementById(String typeId, Integer id){
	    this(typeId, id.intValue());
	}
	
	public LoadCnAElementById(){
		// default constructor for use with JavaScript within BIRT
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public void execute() {
	    if(!resultInjectedFromCache){
	        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
	        found = dao.findById(id);
	        HydratorUtil.hydrateElement(dao, found, false);
	    }
	}

	public CnATreeElement getFound() {
		return found;
	}



    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(typeId);
        cacheID.append(String.valueOf(id));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        found = (CnATreeElement)result;
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
        return found;
    }
	
}
