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
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * 
 */
public class LoadConfigurationByUser extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadConfigurationByUser.class);
    
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
                log.error("Error", e);
            }
        }
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

}
