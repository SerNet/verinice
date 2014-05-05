package sernet.springclient;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.WebServiceTemplate;

public class WebServiceClient {

    private final WebServiceTemplate webServiceTemplate = new WebServiceTemplate();

    public void setDefaultUri(String defaultUri) {
        webServiceTemplate.setDefaultUri(defaultUri);
    }

    // send to the configured default URI
    public void simpleSendAndReceive() throws FileNotFoundException {
    	String path = System.getProperty("user.home") + System.getProperty("file.separator") + "foo.xml";
    	if(Logger.getLogger(WebServiceClient.class).isDebugEnabled()){
    	    Logger.getLogger(WebServiceClient.class).debug("Path:\t" + path);
    	}
    	StreamSource source = new StreamSource(new FileReader(path));
    	StreamResult result = new StreamResult(System.out);
        setDefaultUri("http://localhost:8080/veriniceserver/sync/syncService");
        webServiceTemplate.sendSourceAndReceiveToResult(source, result);
    }
    public void simpleSendAndReceive(File file) throws FileNotFoundException {
      	String path = System.getProperty("user.home") + System.getProperty("file.separator") + "requestMarkus.xml";
      	if(Logger.getLogger(WebServiceClient.class).isDebugEnabled()){
      	    Logger.getLogger(WebServiceClient.class).debug("Path:\t" + path);
      	}
      	StreamSource source = new StreamSource(file);
      	StreamResult result = new StreamResult(System.out);
        setDefaultUri("http://localhost:8080/veriniceserver/sync/syncService");
        webServiceTemplate.sendSourceAndReceiveToResult(source, result);
    }
}