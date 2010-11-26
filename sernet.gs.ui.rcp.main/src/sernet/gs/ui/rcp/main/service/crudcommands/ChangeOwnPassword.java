/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.List;

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class ChangeOwnPassword extends GenericCommand implements IAuthAwareCommand, IChangeLoggingCommand {

    private String pass;
    private IAuthService authService;
    private String stationId;
    
    
    /**
     * @param element
     * @param updatePassword
     */
    public ChangeOwnPassword(String password) {
        this.stationId = ChangeLogEntry.STATION_ID;
        this.pass = password;
    }
    
    public IAuthService getAuthService() {
        return this.authService;
    }

    public void setAuthService(IAuthService service) {
        this.authService = service;
    }
    
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.crudcommands.SaveConfiguration#execute()
     */
    @Override
    public void execute() {
        try {
            LoadCurrentUserConfiguration command = new LoadCurrentUserConfiguration();
            command = getCommandService().executeCommand(command);
            Configuration configuration = command.getConfiguration();
            
            String hash = hashPassword(getAuthService().getUsername());
            Property passProperty = configuration.getEntity().getProperties(Configuration.PROP_PASSWORD).getProperty(0);
            Property userProperty = configuration.getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0);
            passProperty.setPropertyValue(hash, false);
            
            getDaoFactory().getDAO(Configuration.class).merge(configuration);
            
        } catch (CommandException e) {
            throw new RuntimeCommandException("Could not change password.", e);
        }
        
    }
    
     /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.crudcommands.SaveConfiguration#hashPassword()
     */
    private String hashPassword(String user) {
        // auth service checks additionally for login status:
        return getAuthService().hashOwnPassword(user, pass);
        
      
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
       return ChangeLogEntry.TYPE_SYSTEM;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return new ArrayList<CnATreeElement>(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

}


