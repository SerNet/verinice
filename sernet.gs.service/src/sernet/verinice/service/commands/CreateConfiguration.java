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
package sernet.verinice.service.commands;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

@SuppressWarnings("serial")
public class CreateConfiguration extends GenericCommand {

	private CnATreeElement person;
	private Configuration configuration;

	public CreateConfiguration(CnATreeElement elmt) {
		this.person = elmt;
	}

	@Override
    public void execute() {
	    configuration = new Configuration();
        if (person == null){
            throw new RuntimeCommandException("Default Konfiguration wurde bereits gesetzt.");
        }
        configuration.setPerson(person);
        configuration.addRole(IRightsService.USERDEFAULTGROUPNAME);
        configuration.setAdminUser(false);
        configuration.setScopeOnly(false);
        configuration.setWebUser(true);
        configuration.setRcpUser(true);       
       
        getDaoFactory().getDAO(Configuration.class).saveOrUpdate(configuration);
	}

	public Configuration getConfiguration() {
		return configuration;
	}
	
	

}
