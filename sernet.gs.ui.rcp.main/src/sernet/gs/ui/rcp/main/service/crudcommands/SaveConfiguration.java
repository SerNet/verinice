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
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
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
public class SaveConfiguration<T extends Configuration> extends GenericCommand implements IAuthAwareCommand {

	private T element;
	private boolean updatePassword;
	
	private transient IAuthService authService;

	/**
	 * Save a configuration item
	 * 
	 * @param element item to save / update
	 * @param updatePassword was the password newly entered and needs to be hashed? 
	 */
	public SaveConfiguration(T element, boolean updatePassword) {
		this.element = element;
		this.updatePassword = updatePassword;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao 
			= (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getClass());
			if (updatePassword)
				hashPassword();
			element = dao.merge(element);
	}

	private void hashPassword() {
		Property passProperty = element.getEntity().getProperties(Configuration.PROP_PASSWORD).getProperty(0);
		Property userProperty = element.getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0);
		
		String hash = getAuthService().hashPassword(userProperty.getPropertyValue(), passProperty.getPropertyValue());
	    passProperty.setPropertyValue(hash, false);
	}

	public T getElement() {
		return element;
	}

	public IAuthService getAuthService() {
		return this.authService;
	}

	public void setAuthService(IAuthService service) {
		this.authService = service;
	}
	
	

}
