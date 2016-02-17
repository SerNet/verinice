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
 *     Robert Schuster <r.schuster@tarent.de> - use HibernateCallback
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

@SuppressWarnings({ "serial", "restriction" })
public class LoadPolymorphicCnAElementById extends GenericCommand implements ICachedCommand{

    private Integer[] ids;

    private List<CnATreeElement> list = new ArrayList<CnATreeElement>();
    
    private boolean resultInjectedFromCache = false;

    public LoadPolymorphicCnAElementById(Integer[] ds) {
        ids = (ds != null) ? ds.clone() : null;
    }

    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            if (ids == null || ids.length == 0 || ids[0] == null) {
                return;
            }

            IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);

            list = dao.findByCallback(new Callback(ids));
            HydratorUtil.hydrateElements(dao, list, false);
        }
    }

    public List<CnATreeElement> getElements() {
        return list;
    }

    private static class Callback implements HibernateCallback, Serializable {

        private Integer[] ids;

        Callback(Integer[] ids) {
            this.ids = (ids != null) ? ids.clone() : null;
        }

        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query query = session.createQuery("from CnATreeElement cte where cte.dbId in (:ids)").setParameterList("ids", ids);
            return query.list();
        }

    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        for(Integer i : ids){
            cacheID.append(String.valueOf(i));
        }
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.list = (ArrayList<CnATreeElement>)result;
        resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.list;
    }
    
}
