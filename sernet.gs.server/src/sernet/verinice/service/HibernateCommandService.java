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
 *     Robert Schuster <r.schuster@tarent.de> - added support for access control
 ******************************************************************************/
package sernet.verinice.service;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbpm.pvm.internal.cmd.GetResourceAsStreamCmd;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.common.ApplicationRoles;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ElementChange;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IHibernateCommandService;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.interfaces.IRightsServerHandler;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.interfaces.bpm.IProcessServiceGeneric;
import sernet.verinice.interfaces.ldap.ILdapCommand;
import sernet.verinice.interfaces.ldap.ILdapService;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.UsernameExistsRuntimeException;

/**
 * Command service that executes commands using hibernate DAOs to access the
 * database.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HibernateCommandService implements ICommandService, IHibernateCommandService {
	
	private Logger log = Logger.getLogger(HibernateCommandService.class);

	// injected by spring
	private DAOFactory daoFactory;
	
	private ICommandExceptionHandler exceptionHandler;
	
	private IAuthService authService;
	
	private ILdapService ldapService;
    
	private boolean dbOpen = false;
	
	private VeriniceContext.State workObjects;
	
	private IConfigurationService configurationService;
	
	private IRightsServerHandler rightsServerHandler;
	
	IBaseDao<BSIModel, Serializable> dao;
	
	private Properties properties;
	
	/**
	 * This method is encapsulated in a transaction by the Spring container.
	 * Hibernate session will be opened before this method executes the given
	 * command and closed afterwards.
	 * 
	 * Database access in a single transaction is thereby enabled for the
	 * command, the necessary data access objects can be requested from the
	 * given DAO factory.
	 * 
	 * A command can execute other commands to fulfill its purpose using the
	 * reference to the command service.
	 */
	public <T extends ICommand> T executeCommand(T command) throws CommandException {
		VeriniceContext.setState(workObjects);
		
		if (!dbOpen)
			throw new CommandException("DB connection closed.");

		
		if (log.isDebugEnabled()) {
			log.debug("Service executing command: " + command.getClass().getSimpleName() + " / user: " + getAuthService().getUsername()); 
		}
		
		
		try {
			// inject service and database access:
			command.setDaoFactory(daoFactory);
			command.setCommandService(this);

			// inject authentication service if command is aware of it:
			if (command instanceof IAuthAwareCommand) {
				((IAuthAwareCommand) command).setAuthService(authService);
			}
			
			// inject ldap service if command is aware of it:
			if (command instanceof ILdapCommand) {
				ILdapCommand ldapCommand = (ILdapCommand) command;
				if(getLdapService()==null) {
					log.warn("LDAP service is not configured.");
				}
				ldapCommand.setLdapService(getLdapService());
			}
			
			// When a command is being executed that should be subject to access
			// control (this is the default) and the logged in user is non-
			// privileged the filter is configured and activated.
			if (authService.isPermissionHandlingNeeded() && !(command instanceof INoAccessControl) ) {
			    if(!hasAdminRole(authService.getRoles())) {
    				if(log.isDebugEnabled()) {
    					log.debug("Enabling security access filter for user: " + authService.getUsername());
    				}
    				setAccessFilterEnabled(true);
			    }
			    configureScopeFilter();
			} else {
			    disableScopeFilter();
			}
			
			// execute actions, compute results:
			command.execute();
			
			setAccessFilterEnabled(false);
			disableScopeFilter();
			
			// log changes:
			if (command instanceof IChangeLoggingCommand) {
				log((IChangeLoggingCommand) command);
			}			
			
			// clean up:
			command.clear();
		} catch (UsernameExistsRuntimeException e) {
			log.info("Username is not available: " + e.getUsername());
			if (log.isDebugEnabled()) {
				log.debug("stacktrace: ", e);
			}
			if (exceptionHandler != null)
				exceptionHandler.handle(e);
		} catch (Exception e) {
			log.error("Error while executing command", e);
			// TODO ak kein exception handler -> initialization must have gone wrong, abort application completely?
			if (exceptionHandler != null)
				exceptionHandler.handle(e);
		}
		return command;
	}


    private void log(IChangeLoggingCommand notifyCommand) {
		List<ElementChange> elementChanges = notifyCommand.getChanges();
		for (ElementChange changedElement : elementChanges) {
			
			// save reference to element, if it has not been deleted:
			CnATreeElement referencedElement = null;
			if (changedElement.getChangeType() != ChangeLogEntry.TYPE_DELETE) {
				referencedElement = changedElement.getElement();
			}
				
			ChangeLogEntry logEntry = new ChangeLogEntry(changedElement.getElement(),
			        changedElement.getChangeType(),
					getAuthService().getUsername(),
					notifyCommand.getStationId(),
					changedElement.getTime());
			log(logEntry, referencedElement);
		}
	}

	/**
	 * @param logEntry
	 */
	private void log(ChangeLogEntry logEntry, CnATreeElement referencedElement) {
		log.debug("Logging change type '" + logEntry.getChangeDescription() + "' for element of type " + logEntry.getElementClass() + " with ID " + logEntry.getElementId());
		daoFactory.getDAO(ChangeLogEntry.class).saveOrUpdate(logEntry);
	}

	/**
	 * Injected by spring framework
	 * 
	 * @param daoFactory
	 */
	public void setDaoFactory(DAOFactory daoFactory) {
		dbOpen = true;
		this.daoFactory = daoFactory;
	}

	public ICommandExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	public void setExceptionHandler(ICommandExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public IAuthService getAuthService() {
		return authService;
	}

	public void setAuthService(IAuthService authService) {
		this.authService = authService;
	}

	public ILdapService getLdapService() {
		return ldapService;
	}

	public void setLdapService(ILdapService ldapService) {
		this.ldapService = ldapService;
	}

    public void setWorkObjects(VeriniceContext.State workObjects) {
		this.workObjects = workObjects;
	}

	public VeriniceContext.State getWorkObjects() {
		return workObjects;
	}
	
	
    
    /**
     * @return the configurationService
     */
    public IConfigurationService getConfigurationService() {
        return configurationService;
    }


    /**
     * @param configurationService the configurationService to set
     */
    public void setConfigurationService(IConfigurationService configurationService) {
        this.configurationService = configurationService;
    }


    /**
     * @return the rightsService
     */
    public IRightsServerHandler getRightsServerHandler() {
        return rightsServerHandler;
    }


    /**
     * @param rightsService the rightsService to set
     */
    public void setRightsServerHandler(IRightsServerHandler rightsServerHandler) {
        this.rightsServerHandler = rightsServerHandler;
    }


    public Properties getProperties() {
        return properties;
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }


    /**
     * 
     */
    private void configureScopeFilter() {
        if(getConfigurationService().isScopeOnly(authService.getUsername())) {
            final Integer userScopeId = getConfigurationService().getScopeId(authService.getUsername());
            getBsiModelDao().executeCallback(new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    session.enableFilter("scopeFilter").setParameter("scopeId", userScopeId);
                    return null;
                }
            });        
        } else {
            disableScopeFilter();
        }
    }
	
    private void setAccessFilterEnabled(boolean enable) {
        if (enable) {
            final Object[] roles = getConfigurationService().getRoles(authService.getUsername());
            getBsiModelDao().executeCallback(new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    session.enableFilter("userAccessReadFilter").setParameterList("currentRoles", roles).setParameter("readAllowed", Boolean.TRUE);
                    return null;
                }
            });
        } else {
            disableScopeFilter();
        }
    }


    /**
     * 
     */
    private void disableScopeFilter() {
        getBsiModelDao().executeCallback(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.disableFilter("scopeFilter");
                return null;
            }
        });
    }
	
	private boolean hasAdminRole(String[] roles) {
	    // FIXME : check scope
	    if(roles!=null) {
    		for (String r : roles) {
    			if (ApplicationRoles.ROLE_ADMIN.equals(r))
    				return true;
    		}	
	    }
		return false;
	}
	
	public void discardUserData(){
	    getConfigurationService().discardUserData();
	    getRightsServerHandler().discardData();
	}
	
	private IBaseDao<BSIModel, Serializable> getBsiModelDao() {
	    if(dao==null) {
	        dao = daoFactory.getDAO(BSIModel.class);
	    }
	    return dao;
	}
	
}
