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
package sernet.verinice.service.commands.crud;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * This command finds a configuration (account) by a given person which is linked
 * to the configuration. If no configuration is found which is linked to the
 * person configuration is null.
 * 
 * Use method getConfiguration() to return the configuration.
 */
public class LoadConfigurationByUser extends GenericCommand {

    private static final long serialVersionUID = -8792664615062443804L;
    private transient Logger log = Logger.getLogger(LoadConfigurationByUser.class);

    private Configuration configuration;
    private CnATreeElement person;

    public LoadConfigurationByUser(CnATreeElement person) {
        this.person = person;
    }

    /*
     * (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        IBaseDao<Configuration, Serializable> confDao = getDaoFactory().getDAO(Configuration.class);
        List<Configuration> configurationList = confDao.findAll();
        for (Configuration configurationCurrent : configurationList) {
            try {
                CnATreeElement personConfiguration = (CnATreeElement) configurationCurrent.getPerson();
                if (personConfiguration!=null && personConfiguration.getUuid().equals(person.getUuid())) {
                    configuration = configurationCurrent;
                    break;
                }
            } catch (Exception e) {
                log.error("Error", e);
            }
        }
    }

    /**
     * Returns the configuration (account) which is linked to the person.
     * If no configuration is found which is linked to the
     * person configuration is null.
     * 
     * @return The configuration (account) which is linked to the person or null
     */
    public Configuration getConfiguration() {
        return configuration;
    }

}
