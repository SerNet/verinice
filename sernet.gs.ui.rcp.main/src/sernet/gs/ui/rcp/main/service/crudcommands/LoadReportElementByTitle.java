/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
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
import java.lang.reflect.Field;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 */
@SuppressWarnings("serial")
public class LoadReportElementByTitle<T extends CnATreeElement> extends GenericCommand implements ICachedCommand{

	private transient Logger log = Logger
		.getLogger(LoadReportElementByTitle.class);
	
	private CnATreeElement result;
	
	private Class<T> clazz;
	
	private String title;
	
    private boolean resultInjectedFromCache = false;
	
	public LoadReportElementByTitle(Class<T> type, String title){
		this.clazz = type;
		this.title = title;
	}

	@Override
	public void execute() {
	    if(!resultInjectedFromCache){
	        //		IBaseDao<T, Serializable> dao = getDaoFactory().getDAO(clazz);
	        DetachedCriteria criteria = DetachedCriteria.forClass(Property.class);
	        String propType = null;
	        Field field;
	        try {
	            field = Class.forName(clazz.getName()).getField("PROP_NAME");
	            if(field != null){
	                propType = (String)field.get(clazz);
	            }
	            criteria.add(Restrictions.eq("propertyType", propType));
	            criteria.add(Restrictions.like("propertyValue", title));
	            IBaseDao<Property, Integer> dao = getDaoFactory().getDAO(Property.TYPE_ID);
	            List<Property>resultList = dao.findByCriteria(criteria);
	            for(Property p : resultList){
	                IBaseDao<PropertyList, Serializable> listDao = getDaoFactory().getDAO(PropertyList.class);
	                criteria = DetachedCriteria.forClass(PropertyList.class);
	                criteria.add(Restrictions.eq("dbId", p.getDbId()));
	                List<PropertyList> pList = listDao.findByCriteria(criteria);
	                IBaseDao<Entity, Serializable> entityDao = getDaoFactory().getDAO(Entity.class);
	                criteria = DetachedCriteria.forClass(Entity.class);
	                criteria.add(Restrictions.eq("dbId", pList.get(0).getEntityId()));
	                List<Entity> eList = entityDao.findByCriteria(criteria);

	                IBaseDao<T, Serializable> objectDao = getDaoFactory().getDAO(clazz);
	                criteria = DetachedCriteria.forClass(clazz);
	                criteria.add(Restrictions.eq("entity", eList.get(0)));
	                result = clazz.cast(objectDao.findByCriteria(criteria).get(0));
	            }
	        } catch (SecurityException e) {
	            log.error("Reflection error", e);
	        } catch (NoSuchFieldException e) {
	            log.error("Reflection error", e);
	        } catch (ClassNotFoundException e) {
	            log.error("Class not found while using reflection API", e);
	        } catch (IllegalArgumentException e) {
	            log.error("Wrong usage of arguments while using field.getField() of reflection API", e); 
	        } catch (IllegalAccessException e) {
	            log.error("Wrong access to a field, using the reflection API", e);
	        }
	    }
	}
	
	public CnATreeElement getResult() {
		return result;
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(clazz.getCanonicalName());
        cacheID.append(String.valueOf(title));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (CnATreeElement)result;
        resultInjectedFromCache = true;
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }
    
    public Logger getLog(){
        if(log == null){
            log = Logger.getLogger(this.getClass());
        }
        return log;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return null;
    }

}
