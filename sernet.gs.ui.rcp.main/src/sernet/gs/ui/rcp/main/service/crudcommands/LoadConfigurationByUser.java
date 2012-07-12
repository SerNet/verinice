/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh[at]sernet[dot]de>.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;

import sernet.gs.ui.rcp.main.common.model.PersonEntityOptionWrapper;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.common.CnATreeElement;

/**
 * 
 */
public class LoadConfigurationByUser extends GenericCommand {

    private transient Logger LOG = Logger.getLogger(LoadConfigurationByUser.class);
    
    private Configuration configuration;
    
    private CnATreeElement pIso;

    public LoadConfigurationByUser(CnATreeElement pIso){
        this.pIso = pIso;
    }
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @SuppressWarnings("restriction")
    @Override
    public void execute() {
        
        IBaseDao<Configuration, Serializable> confDao = getDaoFactory().getDAO(Configuration.class);
        List<Configuration> confs = confDao.findAll();
        
        for (Configuration c : confs)
        {

            try{
                CnATreeElement elmt = (CnATreeElement)c.getPerson();
                if(elmt.getUuid().equals(pIso.getUuid())){
                    configuration = c;
                    break;
                }
            } catch (Exception e){
                LOG.error("Error", e);
            }
        }
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

}
