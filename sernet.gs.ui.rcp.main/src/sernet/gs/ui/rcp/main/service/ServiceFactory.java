package sernet.gs.ui.rcp.main.service;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.springclient.SpringClientPlugin;

public abstract class ServiceFactory {
	
	
	private static final String COMMANDSERVICE 		= "commandService";
	private static final String BEAN_REF_FACTORY	= "beanRefFactory.xml";
	private static final String CONTEXT_LOCAL 		= "ctxHibernate";
	private static final String CONTEXT_REMOTE	 	= "ctxRemote";

	
	public static final int LOCAL 	= 0;
	public static final int REMOTE	= 1;

	private static int locality = LOCAL;
	
	public static void setService(int locality) {
		ServiceFactory.locality = locality;
	}
	
	public static boolean  isUsingRemoteService() {
		return locality == REMOTE;
	}
	
	public static void openCommandService() {
		switch (locality) {
		case LOCAL:
			 openLocalCommandService();
		}
		openRemoteCommandService();
	}
	
	public static void closeCommandService() {
		Logger.getLogger(ServiceFactory.class).debug("Closing bean factory.");
		SpringClientPlugin.getDefault().closeBeanFactory();
	}
	
	public static ICommandService lookupCommandService() {
		 return (ICommandService) SpringClientPlugin.getDefault().getBeanFactory().getBean(COMMANDSERVICE);
	}
	
	private static void openRemoteCommandService() {
		String path = "file://" + CnAWorkspace.getInstance().getConfDir() + File.separator + BEAN_REF_FACTORY;
		Logger.getLogger(ServiceFactory.class).debug("Creating remote bean factory from " + path);
		SpringClientPlugin.getDefault()
		.openBeanFactory(path, CONTEXT_REMOTE);
	}
	
	private static void openLocalCommandService() {
		String path = "file://" + CnAWorkspace.getInstance().getConfDir() + File.separator + BEAN_REF_FACTORY;
		Logger.getLogger(ServiceFactory.class).debug("Creating local bean factory from " + path);
		SpringClientPlugin.getDefault()
		.openBeanFactory(path, CONTEXT_LOCAL);
	}

}
