package sernet.gs.ui.rcp.main.service;

import java.io.File;

import org.springframework.beans.factory.BeanFactory;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.springclient.SpringClientPlugin;

public class ServiceFactory {
	
	private static final String HUISERVICE = "huiService";
	private static final String BEAN_REF_FACTORY = "beanRefFactory.xml";
	private static final String CONTEXT_LOCAL = "ctxHibernate";

	public IHuiService getHuiServiceLocal() {
		return (IHuiService) SpringClientPlugin.getDefault()
			.getBeanFactory("file://" + CnAWorkspace.getInstance().getConfDir() + File.separator + BEAN_REF_FACTORY,
					CONTEXT_LOCAL)
				.getBean(HUISERVICE);
	}
}
