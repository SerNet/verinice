package sernet.gs.ui.rcp.main.service;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

public class ServerCommandService {

	private static ICommandService commandService;

	public static ICommandService getCommandService() {
		return commandService;
	}

	public void setCommandService(ICommandService commandService) {
		ServerCommandService.commandService = commandService;
	}

	
}
