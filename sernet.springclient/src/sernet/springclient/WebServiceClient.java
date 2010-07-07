package sernet.springclient;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;

public class WebServiceClient {

  //  private static final String MESSAGE =
    //    "<message ***nächster schritt: nechten webservice aufrufen:*** xmlns=\"http://tempuri.org\">Hello Web Service World</message>";

    
    
    
    private final WebServiceTemplate webServiceTemplate = new WebServiceTemplate();

    public void setDefaultUri(String defaultUri) {
        webServiceTemplate.setDefaultUri(defaultUri);
    }

    // send to the configured default URI
    public void simpleSendAndReceive() throws FileNotFoundException {
      //  StreamSource source = new StreamSource(new StringReader(MESSAGE));
    	String path = System.getProperty("user.home") + System.getProperty("file.separator") + "foo.xml";
    	System.out.println(path);
    	StreamSource source = new StreamSource(new FileReader(path));
    	StreamResult result = new StreamResult(System.out);
        setDefaultUri("http://localhost:8080/veriniceserver/sync/syncService");
        webServiceTemplate.sendSourceAndReceiveToResult(source, result);
    }
    public void simpleSendAndReceive(File file) throws FileNotFoundException {
        //  StreamSource source = new StreamSource(new StringReader(MESSAGE));
      	String path = System.getProperty("user.home") + System.getProperty("file.separator") + "requestMarkus.xml";
      	System.out.println(path);
      	StreamSource source = new StreamSource(file);
      	StreamResult result = new StreamResult(System.out);
        setDefaultUri("http://localhost:8080/veriniceserver/sync/syncService");
        webServiceTemplate.sendSourceAndReceiveToResult(source, result);
    }

    // send to an explicit URI
//    public void customSendAndReceive() {
//        StreamSource source = new StreamSource(new StringReader(MESSAGE));
//        StreamResult result = new StreamResult(System.out);
//        webServiceTemplate.sendSourceAndReceiveToResult("http://localhost:8080/AnotherWebService",
//            source, result);
//    }


}