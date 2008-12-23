package sernet.gs.ui.rcp.main.service;

import java.io.File;

import org.springframework.beans.factory.BeanFactory;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.springclient.SpringClientPlugin;

public abstract class ServiceFactory {
	
	
	private static final String HUISERVICE = "huiService";
	private static final String BEAN_REF_FACTORY = "beanRefFactory.xml";
	private static final String CONTEXT_LOCAL = "ctxHibernate";

	
	public static final int LOCAL 	= 0;
	public static final int REMOTE	= 1;

	private static int locality = LOCAL;
	
	public static void setService(int locality) {
		ServiceFactory.locality = locality;
	}
	
	public static ICommandService lookupCommandService() {
		switch (locality) {
		case LOCAL:
			return getLocalCommandService();
		}
		return getRemoteCommandService();
	}
	
	private static ICommandService getRemoteCommandService() {
		// TODO implement
		return null;
	}

	private static ICommandService getLocalCommandService() {
		return (ICommandService) SpringClientPlugin.getDefault()
			.getBeanFactory("file://" + CnAWorkspace.getInstance().getConfDir() + File.separator + BEAN_REF_FACTORY,
					CONTEXT_LOCAL)
				.getBean(HUISERVICE);
	}
}
