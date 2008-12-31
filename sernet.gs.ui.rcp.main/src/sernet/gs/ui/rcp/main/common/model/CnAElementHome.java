package sernet.gs.ui.rcp.main.common.model;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisListsHome;
import sernet.gs.ui.rcp.main.service.HuiServiceTest;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelComplete;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshMultipleElements;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllTags;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

/**
 * DAO class for model objects. Uses Hibernate as persistence framework.
 * 
 * @author koderman@sernet.de
 * 
 */

public class CnAElementHome {

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
		// TODO recreate hibernate config and recreate session factory bean (spring)
		return true;
	}

	public void open(IProgress monitor) throws Exception {
		open(CnAWorkspace.getInstance().getConfDir(), monitor);
	}

	public void preload(String confDir) {
		// do nothing
	}

	public void open(String confDir, IProgress monitor) throws Exception {
		// TODO use preference to decide local or remote service usage
		ServiceFactory.setService(ServiceFactory.LOCAL);
		commandService = ServiceFactory.lookupCommandService();
	}

	public void close() {
		// do nothing
	}

	public void save(CnATreeElement element) throws Exception {
			Logger.getLogger(this.getClass()).debug(
					"Saving new element: " + element);
			SaveElement<CnATreeElement> saveCommand = new SaveElement<CnATreeElement>(element);
			commandService.executeCommand(saveCommand);
	}
	
	public void save(CnALink link) throws Exception {
		Logger.getLogger(this.getClass()).debug(
				"Saving new link: " + link);
		SaveElement<CnALink> saveCommand = new SaveElement<CnALink>(link);
		commandService.executeCommand(saveCommand);
}
	
	

	private void logChange(CnATreeElement element, int changeType) {
		// TODO implement
	}


	public void remove(CnATreeElement element) throws Exception {
		RemoveElement command = new RemoveElement(element);
		commandService.executeCommand(command);
	}

	public void remove(CnALink element) throws Exception {
		RemoveElement command = new RemoveElement(element);
		commandService.executeCommand(command);
	}

	public void update(CnATreeElement element) throws Exception {	
		UpdateElement command = new UpdateElement(element);
		commandService.executeCommand(command);
	}

	public void update(List<? extends CnATreeElement> elements)
			throws StaleObjectStateException {
		UpdateMultipleElements command = new UpdateMultipleElements(elements);
		commandService.executeCommand(command);
	}

	public void refresh(List<? extends CnATreeElement> elements) {
		RefreshMultipleElements command = new RefreshMultipleElements(elements);
		commandService.executeCommand(command);
	}

	/**
	 * Load object with given ID for given class.
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public CnATreeElement loadById(Class<? extends CnATreeElement> clazz, int id) {
		LoadElementById command = new LoadElementById(clazz, id);
		commandService.executeCommand(command);
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
		
		LoadBSIModel command = new LoadBSIModel();
		commandService.executeCommand(command);
		BSIModel model = command.getModel();
		return model;
	}

	/**
	 * Refresh given object from the database, looses all changes made in
	 * memory, sets element and all properties to actual state in database.
	 * 
	 * @param cnAElement
	 */
	public void refresh(CnATreeElement cnAElement) {
		RefreshElement command = new RefreshElement(cnAElement);
		commandService.executeCommand(command);
	}

	public List<ChangeLogEntry> loadChangesSince(Date lastUpdate) {
		// TODO implement
		return null;
	}
	
	public List<ITVerbund> getItverbuende() {
		LoadElementByType<ITVerbund> command = new LoadElementByType<ITVerbund>(ITVerbund.class);
		commandService.executeCommand(command);
		return command.getElements();
	}

	public List<ITVerbund> getItverbuendeHydrated() {
		LoadBSIModelComplete command = new LoadBSIModelComplete();
		ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getModel().getItverbuende();
	}

	public List<Person> getPersonen() {
		LoadElementByType<Person> command = new LoadElementByType<Person>(Person.class);
		commandService.executeCommand(command);
		return command.getElements();
		
	}

	public List<String> getTags() {
		FindAllTags command = new FindAllTags();
		commandService.executeCommand(command);
		return command.getTags();
	}
}
