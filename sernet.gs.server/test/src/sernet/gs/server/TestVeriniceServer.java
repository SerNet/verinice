package sernet.gs.server;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.connect.HibernateBaseDao;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.WhereAmIUtil;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.ICommand;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelComplete;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadMassnahmenTitles;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllTags;


public class TestVeriniceServer extends TestCase {
	
	private ICommandService commandService;
	private BeanFactoryReference beanFactoryReference;

	public void setUp() throws Exception {
		super.setUp();
		
		// basic Verinice client setup:
		// use remote service to execute commands:
		ServiceFactory.setService(ServiceFactory.REMOTE);
		
		// tell me where to find HitroUI configuration and other stuff:
		WhereAmIUtil.setLocation(WhereAmIUtil.LOCATION_CLIENT);
		
		// initialize HitroUI type factory:
		HitroUtil.getInstance().init("http://localhost:2010/veriniceserver");
		
		// get a command service implementation: (remote proxy in this case)
		BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator
		.getInstance();
		beanFactoryReference = beanFactoryLocator
		.useBeanFactory("ctx");
		
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("admin", "passwort");

		HttpState httpState = (HttpState) beanFactoryReference
		.getFactory().getBean("httpState");
		 httpState.setCredentials(AuthScope.ANY, creds);
		 
		commandService = (ICommandService) beanFactoryReference
			.getFactory().getBean("commandService");
		
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testConnect() throws CommandException {
		LoadBSIModel command = new LoadBSIModel();
		 command = commandService.executeCommand(command);
		 BSIModel model = command.getModel();
		 
		 assertNotNull(model);
		 assertNotNull(model.getDbVersion());
		 System.out.println(model.getDbVersion());
		 
	}
	
	public void testGetMassnahmen() throws CommandException {
		LoadCnAElementByType<MassnahmenUmsetzung> command = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		command = commandService.executeCommand(command);
		List<MassnahmenUmsetzung> elements = command.getElements();
		assertNotNull(elements);
		for (MassnahmenUmsetzung elmt : elements) {
			System.out.println(elmt.getTitel());
		}
		System.out.println(elements.size());
	}
	
	public void testGetMassnahmenTitles() throws CommandException {
		LoadMassnahmenTitles<MassnahmenUmsetzung> command = new LoadMassnahmenTitles<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		command = commandService.executeCommand(command);
		List<MassnahmenUmsetzung> elements = command.getElements();
		assertNotNull(elements);
		for (MassnahmenUmsetzung elmt : elements) {
			System.out.println(elmt.getTitel());
		}
		System.out.println(elements.size());
	}
	
	public void testLoadBausteine() throws CommandException {
		LoadBausteine command = new LoadBausteine();
		command = commandService.executeCommand(command);
		List<Baustein> bausteine = command.getBausteine();
		assertTrue(bausteine.size()>0);
	}
	
	public void testAuthentication() {
		 IAuthService authService = (IAuthService) beanFactoryReference
		 .getFactory().getBean("authService");

		 //print the available roles
		 String[] roles = authService.getRoles();
		 for (int i = 0; i < roles.length; i++) {
			 System.out.println("Role:" + roles[i]);
		 }
	}
	
	public void testReadWriteConfiguration() throws CommandException {
		LoadConfiguration command = new LoadConfiguration(null);
		command = commandService.executeCommand(
				command);
		sernet.gs.ui.rcp.main.common.model.configuration.Configuration configuration = command.getConfiguration();

		if (configuration == null) {
			// create new configuration
			Logger
					.getLogger(this.getClass())
					.debug(
							"No config found, creating new configuration object.");
			CreateConfiguration command2 = new CreateConfiguration(null);
			command2 =commandService
					.executeCommand(command2);
			configuration = command2.getConfiguration();
			assertNotNull(configuration);
		}
	}
	
	
}
