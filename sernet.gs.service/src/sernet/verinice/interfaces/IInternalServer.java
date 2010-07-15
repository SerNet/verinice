package sernet.verinice.interfaces;

import java.net.URL;

public interface IInternalServer {
	
	void setGSCatalogURL(URL url);
	
	void setDSCatalogURL(URL url);
	
	void configure(String url, String user, String pass,
			String driver, String dialect);

	void start() throws IllegalStateException;
	
	void stop();
	
	boolean isRunning();
}
