package sernet.gs.ui.rcp.main.common.model.configuration;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

/**
 * Configuration item. Actual configuration values are saved in Entity.
 * Can be linked to a person for individual configuration items such as usernames / passwords 
 * and other personal settings.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class Configuration implements Serializable {

	private Entity entity;
	
	private static final String TYPE_ID = "configuration";
	
	private Person person;
	
	private Integer dbId;
	

	public Configuration() {
		setEntity(new Entity(TYPE_ID));
	}

	private void setEntity(Entity entity) {
		this.entity = entity;		
	}

	public Entity getEntity() {
		return entity;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	
	
}
