package sernet.gs.ui.rcp.main.service;

public interface IInternalServer {
	
	void configure(String url, String user, String pass,
			String driver, String dialect);

	void start();
	
	void stop();
	
	boolean isRunning();
}
