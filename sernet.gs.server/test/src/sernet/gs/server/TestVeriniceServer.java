package sernet.gs.server;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.connect.HibernateBaseDao;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.WhereAmIUtil;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelComplete;


public class TestVeriniceServer extends TestCase {
	
	public void setUp() throws Exception {
		super.setUp();
		ServiceFactory.setService(ServiceFactory.REMOTE);
		WhereAmIUtil.setLocation(WhereAmIUtil.LOCATION_CLIENT);
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testConnect() throws CommandException {
		BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator
		.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator
		.useBeanFactory("ctx");
		 ICommandService service = (ICommandService) beanFactoryReference
			.getFactory().getBean("commandServiceHttpInvoker");
		 
		 LoadBSIModelComplete command = new LoadBSIModelComplete(false);
		 service.executeCommand(command);
		 BSIModel model = command.getModel();
		 System.out.println(model.getDbVersion());
		 assertNotNull(model);
		 assertNotNull(model.getDbVersion());
	}
	
}
