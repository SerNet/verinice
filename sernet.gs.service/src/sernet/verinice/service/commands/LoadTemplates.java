/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
 *  
 * This program is free software: you can redistribute it and/or   
 * modify it under the terms of the GNU Lesser General Public License   
 * as published by the Free Software Foundation, either version 3   
 * of the License, or (at your option) any later version.  
 * This program is distributed in the hope that it will be useful,      
 * but WITHOUT ANY WARRANTY; without even the implied warranty   
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    
 * See the GNU Lesser General Public License for more details.  
 *  
 * You should have received a copy of the GNU Lesser General Public License  
 * along with this program.   
 * If not, see <http://www.gnu.org/licenses/>.  
 *   
 * Contributors:  
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de> 
 */
public class LoadTemplates extends GenericCommand {

    private static final long serialVersionUID = -8755177313752809710L;

    private transient Logger log = Logger.getLogger(LoadTemplates.class);

    private final CnATreeElement inputElement;
    private Set<CnATreeElement> templates;

    /**
     * @param implElement
     */
    public LoadTemplates(CnATreeElement implElement) {
        super();
        this.inputElement = implElement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if (inputElement.isImplementation()) {
            loadTemplates();
        } else if (inputElement.isTemplate()) {
            loadImplementation();
        } else {
            templates = Collections.emptySet();
        }
    }

    private void loadTemplates() {
        templates = new HashSet<CnATreeElement>();
        templates.add(loadMasterTemplate());

        // TODO: add all templates from childs inputElement.getChildren()
    }

    private CnATreeElement loadMasterTemplate() {
        List<CnATreeElement> list = loadAllElementsWithSameEntityId();

        for (CnATreeElement element : list) {
            if (element.isTemplate())
                return element;
        }
        // should never happen
        return inputElement;
    }

    private void loadImplementation() {
        templates = new HashSet<CnATreeElement>();

        List<CnATreeElement> list = loadAllElementsWithSameEntityId();

        if (list == null || list.isEmpty()) {
            if (getLog().isInfoEnabled()) {
                getLog().info("No implementation of template with entity db-id: " + inputElement.getEntity().getDbId() + " found.");
            }
            return;
        }
        list.remove(inputElement);
        templates.addAll(list);
    }

    private List<CnATreeElement> loadAllElementsWithSameEntityId() {
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        List<CnATreeElement> list = dao.findByCallback(new Callback(inputElement.getEntity().getDbId()));

        if (list == null || list.isEmpty()) {
            String message = "No master template for element with entity db-id: " + inputElement.getEntity().getDbId() + " found.";
            getLog().error(message);
            throw new RuntimeException(message);
        }
        return list;
    }

    private static class Callback implements HibernateCallback {
        private Integer id;

        Callback(Integer id) {
            this.id = id;
        }

        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query query = session.createQuery("from CnATreeElement element " + "where element.entity.dbId = :id").setParameter("id", id);
            return query.list();
        }

    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadTemplates.class);
        }
        return log;
    }

    public Set<CnATreeElement> getTemplates() {
        return templates;
    }
}
