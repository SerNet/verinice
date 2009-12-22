/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.common.model;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateBaustein;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateLink;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelForTreeView;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCurrentUserConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshMultipleElements;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveLink;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllTags;

/**
 * DAO class for model objects. Uses Hibernate as persistence framework.
 * 
 * @author koderman@sernet.de
 * 
 */

public class CnAElementHome {
	
	private final Logger log = Logger.getLogger(CnAElementHome.class);
	
	private Set<String> roles = null;

	private static CnAElementHome instance;

	private static final String QUERY_FIND_BY_ID = "from "
			+ CnATreeElement.class.getName() + " as element "
			+ "where element.dbId = ?";

	private static final String QUERY_FIND_CHANGES_SINCE = "from "
			+ ChangeLogEntry.class.getName() + " as change "
			+ "where change.changetime > ? " + "and not change.stationId = ? "
			+ "order by changetime";

	private ICommandService commandService;

	private CnAElementHome() {
		// singleton
	}

	public static CnAElementHome getInstance() {
		if (instance == null) {
			instance = new CnAElementHome();
		}
		return instance;
	}

	public boolean isOpen() {
		return commandService != null;
	}

	public void open(IProgress monitor) throws Exception {
		open(CnAWorkspace.getInstance().getConfDir(), monitor);
	}

	public void preload(String confDir) {
		// do nothing
	}

	public void open(String confDir, IProgress monitor) throws Exception {
		monitor.beginTask("Initialisiere Service-Layer...", IProgress.UNKNOWN_WORK);
		ServiceFactory.openCommandService();
		commandService = ServiceFactory.lookupCommandService();
	}
	
	public void open() throws Exception {
		// causes NoClassDefFoundError: org/eclipse/ui/plugin/AbstractUIPlugin in web environment
		// TODO: fix this dependency to eclipse related classes.
		ServiceFactory.openCommandService();
		commandService = createCommandService();
	}
	
	private ICommandService createCommandService() {
		commandService = ServiceFactory.lookupCommandService();
		return commandService;
	}

	public void close() {
		ServiceFactory.closeCommandService();
		commandService = null;
	}

	public <T extends CnATreeElement> T save(T element) throws Exception {
			Logger.getLogger(this.getClass()).debug(
					"Saving new element: " + element);
			SaveElement<T> saveCommand = new SaveElement<T>(element);
			saveCommand = commandService.executeCommand(saveCommand);
			return saveCommand.getElement();
	}
	
	public <T extends CnATreeElement> T save(CnATreeElement container, Class<T> type) throws Exception {
		Logger.getLogger(this.getClass()).debug(
				"Creating new element in " + container);
		CreateElement<T> saveCommand = new CreateElement<T>(container, type);
		saveCommand = commandService.executeCommand(saveCommand);
		return saveCommand.getNewElement();
	}
	
	public BausteinUmsetzung save(CnATreeElement container, Baustein baustein) throws Exception {
		Logger.getLogger(this.getClass()).debug(
				"Creating new element in " + container);
		CreateBaustein saveCommand = new CreateBaustein(container, baustein);
		saveCommand = commandService.executeCommand(saveCommand);
		return saveCommand.getNewElement();
	}
	
	public CnALink createLink(CnATreeElement dropTarget, CnATreeElement dragged) throws CommandException {
		Logger.getLogger(this.getClass()).debug(
				"Saving new link from " + dropTarget + " to " + dragged);
		CreateLink command = new CreateLink(dropTarget, dragged);
		command = commandService.executeCommand(command);
		
		return command.getLink();
	}
	
	public void remove(CnATreeElement element) throws Exception {
		Logger.getLogger(this.getClass()).debug("Deleting " + element.getTitel());
		RemoveElement command = new RemoveElement(element);
		command = commandService.executeCommand(command);
	}

	public void remove(CnALink element) throws Exception {
		RemoveLink command = new RemoveLink(element.getId());
		command = commandService.executeCommand(command);
	}

	public CnATreeElement update(CnATreeElement element) throws Exception {	
		UpdateElement command = new UpdateElement(element, true, ChangeLogEntry.STATION_ID);
		command = commandService.executeCommand(command);
		return (CnATreeElement) command.getElement();
	}

	public void update(List<? extends CnATreeElement> elements)
			throws StaleObjectStateException, CommandException {
		UpdateMultipleElements command = new UpdateMultipleElements(elements, ChangeLogEntry.STATION_ID);
		command = commandService.executeCommand(command);
	}

	public void refresh(List<? extends CnATreeElement> elements) throws CommandException {
		RefreshMultipleElements command = new RefreshMultipleElements(elements);
		command = commandService.executeCommand(command);
	}

	/**
	 * Load object with given ID for given class.
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 * @throws CommandException 
	 */
	@SuppressWarnings("unchecked")
	public CnATreeElement loadById(Class<? extends CnATreeElement> clazz, int id) throws CommandException {
		LoadCnAElementById command = new LoadCnAElementById(clazz, id);
		command = commandService.executeCommand(command);
		return command.getFound();
	}

	

	/**
	 * Load whole model from DB (lazy). Proxies will be instantiated by
	 * hibernate on first access.
	 * 
	 * @param nullMonitor
	 * 
	 * @return BSIModel object which is the top level object of the model
	 *         hierarchy.
	 * @throws Exception
	 */
	public BSIModel loadModel(IProgress nullMonitor) throws Exception {
		Logger.getLogger(this.getClass()).debug("Loading model instance");

		nullMonitor.setTaskName("Lade Grundschutz Modell...");
		
		LoadBSIModelForTreeView command = new LoadBSIModelForTreeView();
		command = commandService.executeCommand(command);
		BSIModel model = command.getModel();
		return model;
	}

	/**
	 * Refresh given object from the database, looses all changes made in
	 * memory, sets element and all properties to actual state in database.
	 * 
	 * Does not reload children or other collections of this object.
	 * 
	 * @param cnAElement
	 * @throws CommandException 
	 */
	public void refresh(CnATreeElement cnAElement) throws CommandException {
		RefreshElement command = new RefreshElement(cnAElement);
		command = commandService.executeCommand(command);
		CnATreeElement refreshedElement = command.getElement();
		cnAElement.setEntity(refreshedElement.getEntity());
	}

	public List<ITVerbund> getItverbuende() throws CommandException {
		LoadCnAElementByType<ITVerbund> command = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
		command = commandService.executeCommand(command);
		return command.getElements();
	}

	public List<Person> getPersonen() throws CommandException {
		LoadCnAElementByType<Person> command = new LoadCnAElementByType<Person>(Person.class);
		command = commandService.executeCommand(command);
		return command.getElements();
		
	}

	public List<String> getTags() throws CommandException {
		FindAllTags command = new FindAllTags();
		command = commandService.executeCommand(command);
		return command.getTags();
	}
	
	/**
	 * Returns whether it is allowed to perform a delete
	 * operation on the given element.
	 * 
	 * @param cte
	 * @return
	 */
	public boolean isDeleteAllowed(CnATreeElement cte)
	{
		// Category objects cannot be deleted.
		if (cte instanceof IBSIStrukturKategorie)
			return false;
		
		// ITVerbund instances can be removed when
		// one has write access to it (There is no parent to check).
		if (cte instanceof ITVerbund)
			return isWriteAllowed(cte);
		
		// For normal CnATreeElement instance we need write privileges
		// on the instance and its parent to be able to delete it.
		return isWriteAllowed(cte) && isWriteAllowed(cte.getParent());
	}
	
	/**
	 * Returns whether it is allowed to perform a delete
	 * operation on the given {@link CnALink} element.
	 * 
	 * <p>The link can be deleted if write permissions
	 * exist on the {@link CnATreeElement} instance
	 * the link belongs to.</p>
	 * 
	 * @param cte
	 * @return
	 */
	public boolean isDeleteAllowed(CnALink cl)
	{
		return isWriteAllowed(cl.getDependant());
	}
	
	/**
	 * Returns whether it is allowed to perform the creation
	 * of a new child element on the given element.
	 * 
	 * @param cte
	 * @return
	 */
	public boolean isNewChildAllowed(CnATreeElement cte)
	{
		return cte instanceof BSIModel || isWriteAllowed(cte);
	}
	
	/**
	 * Returns whether it is allowed to perform modifications
	 * to properties of this element.
	 * 
	 * @param cte
	 * @return
	 */
	public boolean isWriteAllowed(CnATreeElement cte)
	{
		
		// FIXME akoderman: this should only be used to determine icon state on the client. write check needs to implemented on the server side on saveOrUpdate() / merge() as well
		
		if(commandService == null) {
			createCommandService();
		}
		
		// Short cut: If no permission handling is needed than all objects are writable.
		if (!ServiceFactory.isPermissionHandlingNeeded())
			return true;
		
		// Short cut 2: If we are the admin, then everything is writable as well.
		if (AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN } ))
			return true;
		
		if (roles == null)
		{
			LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();
			try {
				lcuc = (LoadCurrentUserConfiguration) commandService.executeCommand(lcuc);
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler bei Abfrage der Schreibrechte.");
				return false;
			}
			
			Configuration c = lcuc.getConfiguration();
			
			// No configuration for the current user (anymore?). Then nothing is writable. 
			if (c == null)
				return false;
			
			roles = c.getRoles();
		}
		
		for (Permission p : cte.getPermissions())
		{
			if (p.isWriteAllowed()
				&& roles.contains(p.getRole()))
				return true;
		}
		
		return false;
	}

}
