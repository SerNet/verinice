package sernet.hui.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.log4j.Logger;

import com.sleepycat.dbxml.XmlException;

import sernet.hui.common.connect.Entity;
import sernet.hui.server.connect.xml.EntityHomeXML;
import sernet.snutils.DBException;

@WebService(name="EntityHome", serviceName="EntityHomeService")
public class EntityHomeServiceImpl {

	@WebMethod(operationName="findByID")
	public Entity findByID(String id) throws DBException {
		try {
			EntityHomeXML eHome = EntityHomeXML.getInstance();
			eHome.open();
			return eHome.findById(id);
		} catch (XmlException e) {
			Logger.getLogger(EntityHomeServiceImpl.class).error(e);
		}
		return null;
	}
}
