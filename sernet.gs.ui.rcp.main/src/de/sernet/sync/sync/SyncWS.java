
package de.sernet.sync.sync;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
/*
	@WebService(name = "syncWS", targetNamespace = "http://www.sernet.de/sync/sync")
	@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
	@XmlSeeAlso({
	    de.sernet.sync.sync.ObjectFactory.class,
	    de.sernet.sync.data.ObjectFactory.class,
	    de.sernet.sync.mapping.ObjectFactory.class
	})
*/
public interface SyncWS extends Remote {

    /**
     * 
     * @param syncRequest
     * @return
     *     returns de.sernet.sync.sync.SyncResponse
     */
/* 	@WebMethod
    @WebResult(name = "syncResponse", targetNamespace = "http://www.sernet.de/sync/sync", partName = "syncResponse")*/
    public SyncResponse sync(
/*	@WebParam(name = "syncRequest", targetNamespace = "http://www.sernet.de/sync/sync", partName = "syncRequest")*/
        SyncRequest syncRequest) throws RemoteException;

}
