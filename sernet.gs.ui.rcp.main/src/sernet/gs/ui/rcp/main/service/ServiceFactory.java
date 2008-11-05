package sernet.gs.ui.rcp.main.service;

import springClient.SpringClientPlugin;

public class ServiceFactory {
	
	private static final String HUISERVICE = "huiService";

	public IHuiService getHuiService() {
		return (IHuiService) SpringClientPlugin.getDefault().getBeanFactory()
				.getBean(HUISERVICE);
	}
}
