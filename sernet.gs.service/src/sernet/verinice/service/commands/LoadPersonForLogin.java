/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LoadPersonForLogin extends GenericCommand {

    private static String HQL = "select conf.dbId from Configuration as conf " + //$NON-NLS-1$
            "inner join conf.entity as entity " + //$NON-NLS-1$
            "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
            "inner join propertyList.properties as props " + //$NON-NLS-1$
            "where props.propertyType = ? " + //$NON-NLS-1$
            "and props.propertyValue like ?"; //$NON-NLS-1$
    
    private String login;
    private CnATreeElement person;
    
    /**
     * @param login
     */
    public LoadPersonForLogin(String login) {
        this.login = login;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        Object[] params = new Object[]{Configuration.PROP_USERNAME,login};        
        List<Integer> configurationList = getConfigurationDao().findByQuery(HQL,params);
        Integer dbId = null;
        if (configurationList != null && configurationList.size() == 1) {
            dbId = (Integer) configurationList.get(0);
            loadPerson(dbId);
        }
    }
    
    private void loadPerson(Integer dbId) {
        if(dbId!=null) {
            String HQL = "from Configuration as conf " + //$NON-NLS-1$
            "inner join fetch conf.person as person " + //$NON-NLS-1$
            "inner join fetch person.entity as entity " + //$NON-NLS-1$
            "inner join fetch entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
            "inner join fetch propertyList.properties as props " + //$NON-NLS-1$
            "where conf.dbId = ? "; //$NON-NLS-1$
            
            Object[] params = new Object[]{dbId};        
            List<Configuration> configurationList = getConfigurationDao().findByQuery(HQL,params);
            for (Configuration configuration : configurationList) {
                person  = configuration.getPerson();              
            }
        }   
        
    }
    
    public CnATreeElement getPerson() {
        return person;
    }

    protected IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return getDaoFactory().getDAO(Configuration.class);
    }
    
    protected IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

}
