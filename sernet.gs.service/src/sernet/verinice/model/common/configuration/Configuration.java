/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster@tarent.de> - reworked to allow custom roles
 ******************************************************************************/
package sernet.verinice.model.common.configuration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.ITypedElement;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Configuration item. Actual configuration values are saved in Entity.
 * Can be linked to a person for individual configuration items such as usernames / passwords 
 * and other personal settings.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class Configuration implements Serializable, ITypedElement {

	private Entity entity;
	
	public static final String TYPE_ID = "configuration";

	public static final String ROLE_TYPE_ID = "role";
	
	public static final String PROP_USERNAME = "configuration_benutzername"; //$NON-NLS-1$
	public static final String PROP_PASSWORD = "configuration_passwort"; //$NON-NLS-1$
	public static final String PROP_ROLES = "configuration_rolle"; //$NON-NLS-1$
	
	public static final String PROP_NOTIFICATION = "configuration_mailing_yesno"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_EMAIL = "configuration_mailing_email"; //$NON-NLS-1$
	
	public static final String PROP_NOTIFICATION_GLOBAL = "configuration_mailing_owner"; //$NON-NLS-1$
	
	public static final String PROP_NOTIFICATION_EXPIRATION = "configuration_mailing_expiring"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_EXPIRATION_DAYS = "configuration_mailing_expiredays"; //$NON-NLS-1$

	public static final String PROP_NOTIFICATION_MEASURE_MODIFICATION = "configuration_mailing_measure_modification"; //$NON-NLS-1$
	
	public static final String PROP_NOTIFICATION_MEASURE_ASSIGNMENT = "configuration_mailing_assigned"; //$NON-NLS-1$

	public static final String PROP_ISADMIN = "configuration_isadmin"; //$NON-NLS-1$
	public static final String PROP_ISADMIN_YES = "configuration_isadmin_yes"; //$NON-NLS-1$
	public static final String PROP_ISADMIN_NO = "configuration_isadmin_no"; //$NON-NLS-1$
		
	public static final String PROP_WEB = "configuration_web"; //$NON-NLS-1$
    public static final String PROP_WEB_YES = "configuration_web_yes"; //$NON-NLS-1$
    public static final String PROP_WEB_NO = "configuration_web_no"; //$NON-NLS-1$
    
    public static final String PROP_RCP = "configuration_rcp"; //$NON-NLS-1$
    public static final String PROP_RCP_YES = "configuration_rcp_yes"; //$NON-NLS-1$
    public static final String PROP_RCP_NO = "configuration_rcp_no"; //$NON-NLS-1$
    
	public static final String PROP_AUDITOR_NOTIFICATION_GLOBAL = "configuration_auditmailing_owner"; //$NON-NLS-1$
	public static final String PROP_AUDITOR_NOTIFICATION_EXPIRATION = "configuration_auditmailing_expiring"; //$NON-NLS-1$
	public static final String PROP_AUDITOR_NOTIFICATION_EXPIRATION_DAYS = "configuration_auditmailing_expiredays"; //$NON-NLS-1$

	public static final String PROP_SCOPE = "configuration_scope"; //$NON-NLS-1$
    public static final String PROP_SCOPE_YES = "configuration_scope_yes"; //$NON-NLS-1$
    public static final String PROP_SCOPE_NO = "configuration_scope_no"; //$NON-NLS-1$
	
	private CnATreeElement person;
	
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

	public CnATreeElement getPerson() {
		return person;
	}

	public void setPerson(CnATreeElement person) {
		this.person = person;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public void setUser(String user) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_USERNAME);
		entity.setSimpleValue(type, user);
	}
	
	public String getUser() {
		return entity.getSimpleValue(PROP_USERNAME);
	}

	public void setPass(String pass) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_PASSWORD);
		entity.setSimpleValue(type, pass);
	}

	public void addRole(String string) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_ROLES);
		entity.createNewProperty(type, string);
	}
	
	public void deleteRole(String string) {
		// cannot delete the special user role:
		if (string.equals(getUser())){
			return;
		}
		
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_ROLES);
		entity.remove(type, string);
	}
	
	public void setNotificationEnabled(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION);
		entity.setSimpleValue(type, (b ? "configuration_mailing_yesno_yes" : "configuration_mailing_yesno_no"));
	}
	
	public boolean isNotificationEnabled() {
		return isRawPropertyValueEqual(PROP_NOTIFICATION, "configuration_mailing_yesno_yes");
	}
	
	public boolean isAdminUser() {
		return isRawPropertyValueEqual(PROP_ISADMIN, PROP_ISADMIN_YES);
	}
	
	public boolean isScopeOnly() {
        return isRawPropertyValueEqual(PROP_SCOPE, PROP_SCOPE_YES);
    }
	
	public void setScopeOnly(boolean scopeOnly) {
	    PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_SCOPE);
        if(scopeOnly) {
            entity.setSimpleValue(type, PROP_SCOPE_YES);
        } else {
            entity.setSimpleValue(type, PROP_SCOPE_NO);
        }
    }
	
	public boolean isWebUser() {
        return !isRawPropertyValueEqual(PROP_WEB, PROP_WEB_NO);
    }
	
	public void setWebUser(boolean webUser) {
        PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_WEB);
        if(webUser) {
            entity.setSimpleValue(type, PROP_WEB_YES);
        } else {
            entity.setSimpleValue(type, PROP_WEB_NO);
        }
    }
	
	public boolean isRcpUser() {
        return !isRawPropertyValueEqual(PROP_RCP, PROP_RCP_NO);
    }
	
	public void setRcpUser(boolean rcpUser) {
        PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_RCP);
        if(rcpUser) {
            entity.setSimpleValue(type, PROP_RCP_YES);
        } else {
            entity.setSimpleValue(type, PROP_RCP_NO);
        }
    }
	
	public void setAdminUser(boolean isAdmin) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_ISADMIN);
		if(isAdmin) {
			entity.setSimpleValue(type, PROP_ISADMIN_YES);
		} else {
			entity.setSimpleValue(type, PROP_ISADMIN_NO);
		}
	}
	
	public void setNotificationExpirationEnabled(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_EXPIRATION);
		entity.setSimpleValue(type, (b ? "configuration_mailing_expiring_yes" : "configuration_mailing_expiring_no"));
	}
	
	public boolean isNotificationExpirationEnabled() {
		return isRawPropertyValueEqual(PROP_NOTIFICATION_EXPIRATION, "configuration_mailing_expiring_yes");
	}
	
	public void setNotificationExpirationDays(int days) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_EXPIRATION_DAYS);
		entity.setSimpleValue(type, String.valueOf(days));
	}
	
	public int getNotificationExpirationDays() {
		String s = entity.getSimpleValue(PROP_NOTIFICATION_EXPIRATION_DAYS);
		if (s != null && s.length() > 0){
			return Integer.parseInt(s);
		}
		// No value set, then say there is no limit.
		return 0;
	}
	
	public String getNotificationEmail() {
		return entity.getSimpleValue(PROP_NOTIFICATION_EMAIL);
	}
	
	public void setNotificationEmail(String email) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_EMAIL);
		entity.setSimpleValue(type, email);
	}
	
	public void setNotificationGlobal(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_GLOBAL);
		entity.setSimpleValue(type, (b ? "configuration_mailing_owner_all" : "configuration_mailing_owner_self"));
	}
	
	public boolean isNotificationGlobal() {
		return isRawPropertyValueEqual(PROP_NOTIFICATION_GLOBAL, "configuration_mailing_owner_all");
	}
	
	public void setNotificationMeasureModification(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_MEASURE_MODIFICATION);
		entity.setSimpleValue(type, (b ? "configuration_mailing_measure_modification_yesno_yes" : "configuration_mailing_measure_modification_yesno_no"));
	}
	
	public boolean isNotificationMeasureModification() {
		return isRawPropertyValueEqual(PROP_NOTIFICATION_MEASURE_MODIFICATION, "configuration_mailing_measure_modification_yesno_yes");
	}
	
	public void setNotificationMeasureAssignment(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_MEASURE_MODIFICATION);
		entity.setSimpleValue(type, (b ? "configuration_mailing_assigned_yesno_yes" : "configuration_mailing_assigned_yesno_no"));
	}
	
	public boolean isNotificationMeasureAssignment() {
		return isRawPropertyValueEqual(PROP_NOTIFICATION_MEASURE_ASSIGNMENT, "configuration_mailing_assigned_yesno_yes");
	}

	public void setAuditorNotificationExpirationEnabled(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_AUDITOR_NOTIFICATION_EXPIRATION);
		entity.setSimpleValue(type, (b ? "configuration_auditmailing_expiring_yes" : "configuration_auditmailing_expiring_no"));
	}
	
	public boolean isAuditorNotificationExpirationEnabled() {
		return isRawPropertyValueEqual(PROP_AUDITOR_NOTIFICATION_EXPIRATION, "configuration_auditmailing_expiring_yes");
	}
	
	public void setAuditorNotificationExpirationDays(int days) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_AUDITOR_NOTIFICATION_EXPIRATION_DAYS);
		entity.setSimpleValue(type, String.valueOf(days));
	}
	
	public int getAuditorNotificationExpirationDays() {
		String s = entity.getSimpleValue(PROP_AUDITOR_NOTIFICATION_EXPIRATION_DAYS);
		if (s != null && s.length() > 0){
			return Integer.parseInt(s);
		}
		// No value set, then say there is no limit.
		return 0;
	}

	public void setAuditorNotificationGlobal(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_AUDITOR_NOTIFICATION_GLOBAL);
		entity.setSimpleValue(type, (b ? "configuration_auditmailing_owner_all" : "configuration_auditmailing_owner_self"));
	}
	
	public boolean isAuditorNotificationGlobal() {
		return isRawPropertyValueEqual(PROP_AUDITOR_NOTIFICATION_GLOBAL, "configuration_auditmailing_owner_all");
	}

	/**
	 * Returns a set of roles the person to which this configuration
	 * belongs is in.
	 * 
	 * <p>The roles returned herein are to be used for checking whether
	 * the user has access to elements of the {@link BSIModel}.</p>
	 * 
	 * <p>In contrast the roles of the type {@link Person} are only meant
	 * for the IT security model.</p>
	 * 
	 * @return
	 */
	public Set<String> getRoles(boolean withUserRole)
	{
		List<Property> properties = entity.getProperties(Configuration.PROP_ROLES).getProperties();
		
		Set<String> roles = null;
			
		if (properties != null){
			roles = new HashSet<String>(properties.size());
			for (Property p : properties){
				roles.add(p.getPropertyValue());
			}
		}
		else {
			roles = new HashSet<String>();
		}
		if (withUserRole) {
			roles.add(getUser());
		}
		return roles;
	}
	
	public Set<String> getRoles() {
		return getRoles(true);
	}
	
	/**
	 * Convenience method that safely checks whether the value of a
	 * given propertytype is equal to a given value.
	 * 
	 * <p>The method assumes that the property is single-valued.</p>
	 * 
	 * <p>The method safely handles the fact that a property might
	 * not exist at all. In this case the result is always
	 * <code>false</code></p>
	 * 
	 * <p>The given value shall not be <code>null</code>, otherwise
	 * a {@link NullPointerException} is thrown.</p>
	 * 
	 * <p>The expected value is compared to the so called raw value of
	 * the property. In the Hitro UI configuration this is what is provided
	 * by the "id" attribute of an <em>option</em> element.</p> 
	 * 
	 * @param expected
	 */
	private boolean isRawPropertyValueEqual(String propertyType, String expected)
	{
		Property p = entity.getProperties(propertyType).getProperty(0);
		if (p != null){
			return expected.equals(p.getPropertyValue());
		}
		return false;
	}

	public int hashCode()
	{
		return dbId.hashCode();
	}
	
	public boolean equals(Object o) {
	    if(o==null) {
	        return false;
	    }
		try {
			Configuration that = (Configuration) o;
			
			// TODO rschuster: Comparing non-saved Configuration instances is troublesome.
			
			return this.dbId.equals(that.dbId);
		}
		catch (ClassCastException cce) {
			return false;
		}
	}

    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }
    
	protected HUITypeFactory getTypeFactory() {
		return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
	}
}
