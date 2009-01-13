package sernet.gs.server;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

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
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelComplete;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllTags;


public class TestVeriniceServer extends TestCase {
	
	private ICommandService service;

	public void setUp() throws Exception {
		super.setUp();
		
		// basic Verinice client setup:
		// use remote service to execute commands:
		ServiceFactory.setService(ServiceFactory.REMOTE);
		
		// tell me where to find HitroUI configuration and other stuff:
		WhereAmIUtil.setLocation(WhereAmIUtil.LOCATION_CLIENT);
		
		// initialize HitroUI type factory:
		HitroUtil.getInstance().getTypeFactory();
		
		// get a command service implementation: (remote proxy in this case)
		BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator
		.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator
		.useBeanFactory("ctx");
		service = (ICommandService) beanFactoryReference
			.getFactory().getBean("commandServiceHttpInvoker");
		 
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}
	
//	public void testConnect() throws CommandException {
//		 LoadBSIModelComplete command = new LoadBSIModelComplete(false);
//		 LoadBSIModelComplete result = service.executeCommand(command);
//		 BSIModel model = result.getModel();
//		 
//		 assertNotNull(model);
//		 assertNotNull(model.getDbVersion());
//		 System.out.println(model.getDbVersion());
//		 
//	}
	
	public void testGetMassnahmen() throws CommandException {
		LoadCnAElementByType<MassnahmenUmsetzung> command = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		command = service.executeCommand(command);
		List<MassnahmenUmsetzung> elements = command.getElements();
		assertNotNull(elements);
		for (MassnahmenUmsetzung elmt : elements) {
			System.out.println(elmt.getTitel());
		}
	}
	
	public void testGetMassnahmen2() throws CommandException {
		LoadCnAElementByType<MassnahmenUmsetzung> command = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		command = service.executeCommand(command);
		List<MassnahmenUmsetzung> elements = command.getElements();
		assertNotNull(elements);
		for (MassnahmenUmsetzung elmt : elements) {
			System.out.println(elmt.getTitel());
		}
	}
	
}
