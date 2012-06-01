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
package sernet.verinice.interfaces;

import java.util.Properties;

/**
 * Base class of all command.
 * 
 * See this for details about commands and the command pattern:
 * http://en.wikipedia.org/wiki/Command_pattern
 * 
 * Lifecycle of a command:
 * A command is created an the client,
 * serialized,
 * send to the server, 
 * deserialized,
 * executed,
 * serialized and
 * send back to the client.
 * 
 * Use <code>transient</code> for all member variables, 
 * whichz are only used on the server site at execution time.
 * 
 * @author Alexander Koderman <ak[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public abstract class GenericCommand implements ICommand {
	
	
	private transient IDAOFactory daoFactory;
	private transient ICommandService commandService;
	private Properties properties;

	public void setCommandService(ICommandService service) {
		this.commandService = service;
	}
	
	public void setDaoFactory(IDAOFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public IDAOFactory getDaoFactory() {
		return daoFactory;
	}
	
	public ICommandService getCommandService() {
		return commandService;
	}
	
	public void clear() {
		// default implementation does nothing
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.interfaces.ICommand#getProperties()
	 */
	@Override
	public Properties getProperties() {
	    if(properties==null) {
	        properties = readProperties();
	    }
	    return properties;
	}
	
	private Properties readProperties() {
    	Properties properties = new Properties();  
        String className = this.getClass().getName();
        Properties allProperties = getCommandService().getProperties();
        if(allProperties!=null) {
            for (Object keyObject : allProperties.keySet()) {
                String key = (String) keyObject;
                if(key.startsWith(className)) {
                    properties.put(key.substring(key.indexOf(className)+className.length()+1), allProperties.getProperty(key));
                }
            }
        }
        return properties;
	}
}
