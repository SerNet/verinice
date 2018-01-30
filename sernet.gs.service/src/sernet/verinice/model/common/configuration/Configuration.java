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

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.ITypedElement;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;

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
public class Configuration implements Serializable, ITypedElement, Comparable<Configuration> {

    private static final Logger LOG = Logger.getLogger(Configuration.class);
    
	private Entity entity;
	
	public static final String TYPE_ID = "configuration";

	public static final String ROLE_TYPE_ID = "role";
	
	public static final String PROP_USERNAME = "configuration_benutzername"; //$NON-NLS-1$
	public static final String PROP_PASSWORD = "configuration_passwort"; //$NON-NLS-1$
	public static final String PROP_ROLES = "configuration_rolle"; //$NON-NLS-1$
	public static final String PROP_LICENSED_CONTENT_IDS = "configuration_licensedcontent_ids"; //$NON-NLS-1$
	
	public static final String PROP_NOTIFICATION = "configuration_mailing_yesno"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_EMAIL = "configuration_mailing_email"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_LICENSE = "configuration_mailing_license"; //$NON-NLS-1$
	
	public static final String PROP_NOTIFICATION_GLOBAL = "configuration_mailing_owner"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_GLOBAL_ALL = "configuration_mailing_owner_all"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_GLOBAL_SELF = "configuration_mailing_owner_self"; //$NON-NLS-1$
	
	public static final String PROP_NOTIFICATION_EXPIRATION = "configuration_mailing_expiring"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_EXPIRATION_DAYS = "configuration_mailing_expiredays"; //$NON-NLS-1$

	public static final String PROP_NOTIFICATION_MEASURE_MODIFICATION = "configuration_mailing_measure_modification"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_MEASURE_MODIFICATION_YES = "configuration_mailing_measure_modification_yesno_yes"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_MEASURE_MODIFICATION_NO = "configuration_mailing_measure_modification_yesno_no"; //$NON-NLS-1$
    
	public static final String PROP_NOTIFICATION_MEASURE_ASSIGNMENT = "configuration_mailing_assigned"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_MEASURE_ASSIGNMENT_YES = "configuration_mailing_assigned_yesno_yes"; //$NON-NLS-1$
	public static final String PROP_NOTIFICATION_MEASURE_ASSIGNMENT_NO = "configuration_mailing_assigned_yesno_no"; //$NON-NLS-1$

	public static final String PROP_ISADMIN = "configuration_isadmin"; //$NON-NLS-1$
	public static final String PROP_ISADMIN_YES = "configuration_isadmin_yes"; //$NON-NLS-1$
	public static final String PROP_ISADMIN_NO = "configuration_isadmin_no"; //$NON-NLS-1$

    public static final String PROP_ISLOCALADMIN = "configuration_islocaladmin"; //$NON-NLS-1$
    public static final String PROP_ISLOCALADMIN_YES = "configuration_islocaladmin_yes"; //$NON-NLS-1$
    public static final String PROP_ISLOCALADMIN_NO = "configuration_islocaladmin_no"; //$NON-NLS-1$
		
	public static final String PROP_WEB = "configuration_web"; //$NON-NLS-1$
    public static final String PROP_WEB_YES = "configuration_web_yes"; //$NON-NLS-1$
    public static final String PROP_WEB_NO = "configuration_web_no"; //$NON-NLS-1$
    
    public static final String PROP_RCP = "configuration_rcp"; //$NON-NLS-1$
    public static final String PROP_RCP_YES = "configuration_rcp_yes"; //$NON-NLS-1$
    public static final String PROP_RCP_NO = "configuration_rcp_no"; //$NON-NLS-1$
    
	public static final String PROP_AUDITOR_NOTIFICATION_GLOBAL = "configuration_auditmailing_owner"; //$NON-NLS-1$
	public static final String PROP_AUDITOR_NOTIFICATION_GLOBAL_ALL = "configuration_auditmailing_owner_all"; //$NON-NLS-1$
    public static final String PROP_AUDITOR_NOTIFICATION_GLOBAL_SELF = "configuration_auditmailing_owner_self"; //$NON-NLS-1$
	
	public static final String PROP_AUDITOR_NOTIFICATION_EXPIRATION = "configuration_auditmailing_expiring"; //$NON-NLS-1$
	public static final String PROP_AUDITOR_NOTIFICATION_EXPIRATION_DAYS = "configuration_auditmailing_expiredays"; //$NON-NLS-1$

	public static final String PROP_SCOPE = "configuration_scope"; //$NON-NLS-1$
    public static final String PROP_SCOPE_YES = "configuration_scope_yes"; //$NON-NLS-1$
    public static final String PROP_SCOPE_NO = "configuration_scope_no"; //$NON-NLS-1$

    public static final String PROP_DEACTIVATED = "configuration_deactivated"; //$NON-NLS-1$
    public static final String PROP_DEACTIVATED_TRUE = "1"; //$NON-NLS-1$
    public static final String PROP_DEACTIVATED_FALSE = "0"; //$NON-NLS-1$
    
    private static final NumericStringComparator NSC = new NumericStringComparator();
    
	private CnATreeElement person;
	
	private Integer dbId;
	
	private String userNew;
	private String passNew;

	public static Configuration createDefaultAccount() {
	    Configuration account = new Configuration();
	    account.addRole(IRightsService.USERDEFAULTGROUPNAME);
	    account.setAdminUser(false);
	    account.setScopeOnly(false);
	    account.setWebUser(true);
	    account.setRcpUser(true);
	    account.setNotificationExpirationDays(14);
        account.setAuditorNotificationExpirationDays(14);
	    return account;
	}
	
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
	
	public String getEmail() {
        return entity.getSimpleValue(PROP_NOTIFICATION_EMAIL);
    }

	public void setPass(String pass) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_PASSWORD);
		entity.setSimpleValue(type, pass);
	}
	
	public String getPass() {
        return entity.getSimpleValue(PROP_PASSWORD);
    }

	public void addRole(String name) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_ROLES);
		entity.createNewProperty(type, name);
		if (LOG.isDebugEnabled()) {
            LOG.debug("Role " + name + " added to account.");
        }
	}
	
	public boolean deleteRole(String name) {
		// cannot delete the special user role:
		if (name.equals(getUser())){
			return false;
		}
		
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_ROLES);
		entity.remove(type, name);
		if (LOG.isDebugEnabled()) {
            LOG.debug("Role " + name + " deleted from account.");
		}
		return true;
	}
	
	/**
	 * adds licenseId assignment to this user. this allows the user to work with
	 * the content covered by a license represented by a {@link LicenseManagementEntry}
	 * that is referenced via the paramater licenseId.
	 * 
	 * To prevent confusion with duplicate database entries, (which could 
	 * result in unauthorized content access) in this special case,
	 * the property list is checked for property existance before adding 
	 * a new property
	 * 
	 * 
	 * @param licenseId - the licenseId that should be assigned to this user
	 */
	public void addLicensedContentId(String licenseId){
	    PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_LICENSED_CONTENT_IDS);
	    List<Property> propertyList = getEntity().getProperties(type.getId()).getProperties();

	    for (Property p : propertyList){
	        if (licenseId.equals(p.getPropertyValue())){
	            return;
	        }
	    }
	    propertyList.add(entity.createNewProperty(type, licenseId));
	    getEntity().getProperties(type.getId()).setProperties(propertyList);
	    
	    
	    if (LOG.isDebugEnabled()) {
	        LOG.debug("LicenseId " + licenseId + " added to account.");
	    }
	}
	
	/**
	 * get all Ids of licenses that are assigned to this user
	 * @return
	 */
	public Set<String> getAllLicenseIds(){
	    Set<String> allIds = new HashSet<>();
	       PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_LICENSED_CONTENT_IDS);
	        List<Property> propertyList = getEntity().getProperties(type.getId()).getProperties();
	        for(Property p : propertyList){
	            allIds.add(p.getPropertyValue());
	        }
	            
	    return allIds;        
	}
	
	/**
	 * removes one licenseId assignment from this user, given by paramater 
	 * licenseId
	 * @param licenseId - the id to remove
	 */
	public void removeLicensedContentId(String licenseId) {
	    PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_LICENSED_CONTENT_IDS);
	    entity.remove(type, licenseId);
	    if (LOG.isDebugEnabled()) {
	        LOG.debug("LicenseId " + licenseId + " deleted from account.");
	    }
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

    public boolean isLocalAdminUser() {
        return isRawPropertyValueEqual(PROP_ISLOCALADMIN, PROP_ISLOCALADMIN_YES);
	}
	
    public boolean isDeactivatedUser() {
        return isRawPropertyValueEqual(PROP_DEACTIVATED, PROP_DEACTIVATED_TRUE);
    }
    
    public void setIsDeactivatedUser(boolean isDeactivated) {
        PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_DEACTIVATED);
        if(type==null) {
            LOG.error("Can not set property: " + PROP_DEACTIVATED + ". Property type is not defined, check SNCA.xml.");
            return;
        }
        if(isDeactivated) {
            entity.setSimpleValue(type, PROP_DEACTIVATED_TRUE);
        } else {
            entity.setSimpleValue(type, PROP_DEACTIVATED_FALSE);
        }
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
	
    public void setLocalAdminUser(boolean isLocalAdmin) {
        PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_ISLOCALADMIN);
        if (isLocalAdmin) {
            entity.setSimpleValue(type, PROP_ISLOCALADMIN_YES);
        } else {
            entity.setSimpleValue(type, PROP_ISLOCALADMIN_NO);
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
	
	public boolean getNotificationLicense(){
	    return "1".equals(entity.getSimpleValue(PROP_NOTIFICATION_LICENSE));
	}
	
	public void setNotificationLicense(boolean enabled){
	    PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_LICENSE);
	    entity.setSimpleValue(type, (enabled) ? "1" : "0");
	}
	
	public void setNotificationGlobal(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_GLOBAL);
		entity.setSimpleValue(type, (b ? PROP_NOTIFICATION_GLOBAL_ALL : PROP_NOTIFICATION_GLOBAL_SELF));
	}
	
	public boolean isNotificationGlobal() {
		return isRawPropertyValueEqual(PROP_NOTIFICATION_GLOBAL, PROP_NOTIFICATION_GLOBAL_ALL);
	}
	
	public void setNotificationMeasureModification(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_MEASURE_MODIFICATION);
		entity.setSimpleValue(type, (b ? "configuration_mailing_measure_modification_yesno_yes" : "configuration_mailing_measure_modification_yesno_no"));
	}
	
	public boolean isNotificationMeasureModification() {
		return isRawPropertyValueEqual(PROP_NOTIFICATION_MEASURE_MODIFICATION, "configuration_mailing_measure_modification_yesno_yes");
	}
	
	public void setNotificationMeasureAssignment(boolean b) {
		PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_NOTIFICATION_MEASURE_ASSIGNMENT);
        entity.setSimpleValue(type, (b ? PROP_NOTIFICATION_MEASURE_ASSIGNMENT_YES : PROP_NOTIFICATION_MEASURE_ASSIGNMENT_NO));
	}
	
	public boolean isNotificationMeasureAssignment() {
		return isRawPropertyValueEqual(PROP_NOTIFICATION_MEASURE_ASSIGNMENT, PROP_NOTIFICATION_MEASURE_ASSIGNMENT_YES);
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
		entity.setSimpleValue(type, (b ? PROP_AUDITOR_NOTIFICATION_GLOBAL_ALL : PROP_AUDITOR_NOTIFICATION_GLOBAL_SELF));
	}
	
	public void setAuditorNotificationGlobal(String value) {
	    if(value!=null && (!PROP_AUDITOR_NOTIFICATION_GLOBAL_ALL.equals(value)) && (!PROP_AUDITOR_NOTIFICATION_GLOBAL_SELF.equals(value))) {
	        throw new IllegalArgumentException("Value of property " + PROP_AUDITOR_NOTIFICATION_GLOBAL + " can only set to " + PROP_AUDITOR_NOTIFICATION_GLOBAL_ALL + " or " + PROP_AUDITOR_NOTIFICATION_GLOBAL_SELF);
	    }
        PropertyType type = getTypeFactory().getPropertyType(Configuration.TYPE_ID, PROP_AUDITOR_NOTIFICATION_GLOBAL);
        entity.setSimpleValue(type, value);
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
	
	/**
	 * gets all licenseIds the user is allowed to use
	 * one licenseId references a {@link LicenseManagementEntry} that
	 * defines the access to content that is covered by special license
	 * @return set of strings representing the ids assigned to this user
	 */
	public Set<String> getAssignedLicenseIds(){
	    List<Property> properties = entity.getProperties(Configuration.PROP_LICENSED_CONTENT_IDS).getProperties();
	    
	    Set<String> licenseIds;
	    
	    if (properties != null){
	        licenseIds = new HashSet<>(properties.size());
	        for (Property p : properties){
	            licenseIds.add(p.getPropertyValue());
	        }
	    } else {
	        licenseIds = new HashSet<>();
	    }
	    
	    return licenseIds;
	            
	}
	
	
	/**
	 * removes all licenseId assignments from this user
	 */
	public void removeAllLicenseIds(){
	    Set<String> allLicenseIds = getAssignedLicenseIds();
	    for (String licenseId : allLicenseIds){
	        removeLicensedContentId(licenseId);
	    }
	}
	
	public Set<String> getRoles() {
		return getRoles(true);
	}
	
	public void deleteAllRoles() {
	    Set<String> allRoles = getRoles();
	    for (String role : allRoles) {
            deleteRole(role);
        }
	}
	
	public Set<String> getStandartGroups() {
        Set<String> groupsOfAccount = getRoles(false);
        Set<String> standartGroupsOfAccount = new HashSet<String>();
        for (String group : groupsOfAccount) {
            if (ArrayUtils.contains(IRightsService.STANDARD_GROUPS, group)) {
                standartGroupsOfAccount.add(group);
            }
        }      
        return standartGroupsOfAccount;
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

	@Override
    public int hashCode()
	{
		return dbId.hashCode();
	}
	
	@Override
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
	
	@Override
    public int compareTo(Configuration c) {
	    int rc = 0;
        if (c == null) {
            rc = -1;
        } else {
            // e1 and e2 != null
            rc = compareNullSafe(c);
        }
        return rc;
    }
	
	 private int compareNullSafe(Configuration c) {
	    int rc = 0;
        if ((this.getUser() == null && c.getUser() != null)) {
            rc = 1;
        } else if (c.getUser() == null && this.getUser() != null) {
            rc = -1;
        } else {
            // e1 and e2 != null
            rc = compareNullSafe(c.getUser());
        }
        return rc;
    }

    private int compareNullSafe(String login2) {
        return NSC.compare(this.getUser().toLowerCase(), login2.toLowerCase());
    }

    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
	protected HUITypeFactory getTypeFactory() {
		return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Login: ").append(getUser());
        sb.append("\nis deactivated: ").append(isDeactivatedUser());
        sb.append("\nis admin: ").append(isAdminUser());
        sb.append("\nis scope only: ").append(isScopeOnly());
        sb.append("\nis web user: ").append(isWebUser());
        sb.append("\nis rcp user: ").append(isRcpUser());
        sb.append("\nEmail: ").append(getNotificationEmail());
        sb.append("\nMailbenachrichtigung aktivieren: ").append(isNotificationEnabled());
        sb.append("\nAlle Massnahmen / nur eigene: ").append(isNotificationGlobal());
        sb.append("\nNeue zu pruefende Aufgaben: ").append(isNotificationMeasureAssignment());
        sb.append("\nAenderung an Massnahmen: ").append(isNotificationMeasureModification());
        sb.append("\nTerminwarnung: an / aus: ").append(isNotificationExpirationEnabled());
        sb.append("\nTerminwarnung: bei Ablauf in X Tagen: ").append(getNotificationExpirationDays());
        sb.append("\n(Auditor) Alle Massnahmen / nur eigene: ").append(isAuditorNotificationGlobal());
        sb.append("\n(Auditor) Terminwarnung: an / aus: ").append(isAuditorNotificationExpirationEnabled());
        sb.append("\n(Auditor) Terminwarnung: bei Ablauf in X Tagen: ").append(getAuditorNotificationExpirationDays());
        return sb.toString();
	}

    public String getUserNew() {
        return userNew;
    }

    public void setUserNew(String userNew) {
        this.userNew = userNew;
    }

    public String getPassNew() {
        return passNew;
    }

    public void setPassNew(String passNew) {
        this.passNew = passNew;
    }
}
