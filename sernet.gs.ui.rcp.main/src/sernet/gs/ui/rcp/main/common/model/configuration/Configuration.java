/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model.configuration;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

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
	
	public static final String PROP_USERNAME = "configuration_benutzername"; //$NON-NLS-1$
	public static final String PROP_PASSWORD = "configuration_passwort"; //$NON-NLS-1$
	public static final String PROP_ROLES = "configuration_rolle"; //$NON-NLS-1$
	
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

	public void setUser(String user) {
		PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_USERNAME);
		entity.setSimpleValue(type, user);
	}

	public void setPass(String pass) {
		PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_USERNAME);
		entity.setSimpleValue(type, pass);
	}

	public void addRole(String string) {
		PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_ROLES);
		entity.createNewProperty(type, string);
	}
	
	
}
