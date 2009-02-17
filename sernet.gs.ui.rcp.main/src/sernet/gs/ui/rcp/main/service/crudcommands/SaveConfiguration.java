package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.springframework.orm.hibernate3.SpringSessionContext;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.hui.common.connect.Property;

/**
 * Save element of type T to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class SaveConfiguration<T extends Configuration> extends GenericCommand {

	private T element;
	private boolean updatePassword;

	public SaveConfiguration(T element, boolean updatePassword) {
		this.element = element;
		this.updatePassword = updatePassword;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao 
			= (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getClass());
//		try {
//			if (updatePassword)
//				hashPassword();
			element = dao.merge(element);
//		} catch (NoSuchAlgorithmException e) {
//			throw new RuntimeCommandException("Fehler beim Verschlüsseln des Passworts.", e);
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeCommandException("Fehler beim Verschlüsseln des Passworts.", e);
//		}
	}

	private void hashPassword() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Property property = element.getEntity().getProperties(Configuration.PROP_PASSWORD).getProperty(0);
		
		MessageDigest digest = MessageDigest.getInstance("MD5");
	    byte[] input = digest.digest(property.getPropertyValue().getBytes("UTF-8"));
	    String hash = new String(Hex.encodeHex(input));
	    property.setPropertyValue(hash, false);
	}

	public T getElement() {
		return element;
	}
	
	

}
