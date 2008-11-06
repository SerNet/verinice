package sernet.gs.ui.rcp.main.service;

import sernet.springclient.SpringClientPlugin;

public class ServiceFactory {
	
	private static final String HUISERVICE = "huiService";

	public IHuiService getHuiService() {
		return (IHuiService) SpringClientPlugin.getDefault().getBeanFactory()
				.getBean(HUISERVICE);
	}
}
