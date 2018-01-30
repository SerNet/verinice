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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.AccessDeniedException;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ElementChange;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.IGraphCommand;
import sernet.verinice.interfaces.IHibernateCommandService;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.interfaces.IRightsServerHandler;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.ldap.ILdapCommand;
import sernet.verinice.interfaces.ldap.ILdapService;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.UsernameExistsRuntimeException;
import sernet.verinice.service.sync.VnaSchemaVersion;

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
	
	private IGraphService graphService;
	
	private ILdapService ldapService;
    
	private boolean dbOpen = false;
	
	private VeriniceContext.State workObjects;
	
	private IConfigurationService configurationService;
	
	private IRightsServerHandler rightsServerHandler;
	
	IBaseDao<BSIModel, Serializable> dao;
	
	private VnaSchemaVersion vnaSchemaVersion;
	
	private Properties properties;
	
	private Map<Class<? extends ICommand>,Set<String>> commandActionIds;
	

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
	@Override
    public <T extends ICommand> T executeCommand(T command) throws CommandException {
		VeriniceContext.setState(workObjects);
		
		if (!dbOpen)
			throw new CommandException("DB connection closed.");

		
		String username = getAuthService().getUsername();
		if (log.isDebugEnabled()) {
            log.debug("Service executing command: " + command.getClass().getSimpleName() + " / user: " + username); 
		}
		    
		try {
            checkRightsForAction(command, username);
		    
			// inject service and database access:
			command.setDaoFactory(daoFactory);
			command.setCommandService(this);

			// inject authentication service if command is aware of it:
			if (command instanceof IAuthAwareCommand) {
				((IAuthAwareCommand) command).setAuthService(authService);
			}
			
			// inject graph service if command is a IGraphCommand:
            if (command instanceof IGraphCommand) {
                ((IGraphCommand) command).setGraphService(graphService);
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
			    configureFilter(getBsiModelDao());
			} else {
			    disableScopeFilter(getBsiModelDao());
			}
			
			// execute actions, compute results:
			command.execute();
			
			disableFilter(getBsiModelDao());
			
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

    /**
     * Check if the given command is allowed to execute by the given user as
     * defined in the authorization configuration see {@link XmlRightsService}
     * for details. The command to actionid mapping is defined in
     * command-actionid-mapping.xml.
     * 
     * @param command
     *            the command to check
     * @param username
     *            the user to check
     */
    private <T extends ICommand> void checkRightsForAction(T command, String username) {
        Set<String> set = commandActionIds.get(command.getClass());
        if (set != null) {
            for (String actionId : set) {
                if (!rightsServerHandler.isEnabled(username, actionId))
                    // Should the message not be translateable also see
                    // sernet.gs.server.ServerExceptionHandler
                    throw new AccessDeniedException("Im Rechteprofil ist das Ausführen dieser Aktion nicht erlaubt.");
            }
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandService#configureFilter(sernet.verinice.interfaces.IBaseDao)
     */
    @Override
    public void configureFilter(IBaseDao dao) {
        if(authService.isPermissionHandlingNeeded()) {
            if(!hasAdminRole(authService.getRoles())) {
            	if(log.isDebugEnabled()) {
            		log.debug("Enabling security access filter for user: " + authService.getUsername());
            	}
            	setAccessFilterEnabled(true,dao);
            }
            configureScopeFilter(dao);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandService#disableFilter(sernet.verinice.interfaces.IBaseDao)
     */
    @Override
    public void disableFilter(IBaseDao dao) {
        setAccessFilterEnabled(false,dao);
        disableScopeFilter(dao);
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

	public IGraphService getGraphService() {
        return graphService;
    }


    public void setGraphService(IGraphService graphService) {
        this.graphService = graphService;
    }


    public ILdapService getLdapService() {
		return ldapService;
	}

	public void setLdapService(ILdapService ldapService) {
		this.ldapService = ldapService;
	}

    @Override
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


    @Override
    public Properties getProperties() {
        return properties;
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }


    /**
     * 
     */
    private void configureScopeFilter(IBaseDao dao) {
        if(getConfigurationService().isScopeOnly(authService.getUsername())) {
            final Integer userScopeId = getConfigurationService().getScopeId(authService.getUsername());
            dao.executeCallback(new HibernateCallback() {
                @Override
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    session.enableFilter("scopeFilter").setParameter("scopeId", userScopeId);
                    return null;
                }
            });        
        } else {
            disableScopeFilter(dao);
        }
    }
	
    private void setAccessFilterEnabled(boolean enable, IBaseDao dao) {
        if (enable) {
            final Object[] roles = getConfigurationService().getRoles(authService.getUsername());
            dao.executeCallback(new HibernateCallback() {
                @Override
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    session.enableFilter("userAccessReadFilter").setParameterList("currentRoles", roles).setParameter("readAllowed", Boolean.TRUE);
                    return null;
                }
            });
        } else {
            disableScopeFilter(dao);
        }
    }


    private void disableScopeFilter(IBaseDao dao) {
        dao.executeCallback(new HibernateCallback() {
            @Override
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
	
	@Override
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

    public VnaSchemaVersion getVnaSchemaVersion() {
        return vnaSchemaVersion;
    }

    public void setVnaSchemaVersion(VnaSchemaVersion vnaSchemaVersion) {
        this.vnaSchemaVersion = vnaSchemaVersion;
    }

    public void setCommandActionIds(Map<Class<? extends ICommand>, Set<String>> commandActionIds) {
        this.commandActionIds = commandActionIds;
    }
	
}
