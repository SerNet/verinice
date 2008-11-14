package sernet.gs.ui.rcp.main.bsi.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;

public class Person extends CnATreeElement
implements IBSIStrukturElement {
	
	public static final String PROP_TAG			= "person_tag";
	private static final String P_NAME = "nachname"; //$NON-NLS-1$
	private static final String P_VORNAME = "vorname"; //$NON-NLS-1$
	private static final String PROP_KUERZEL = "person_kuerzel"; //$NON-NLS-1$
	private static final String P_ROLLEN = "person_rollen";
	private static EntityType entityType;

	// ID must correspond to entity definition in entitytype XML description:
	public static final String TYPE_ID = "person"; //$NON-NLS-1$
	public static final String PROP_ERLAEUTERUNG = "person_erlaeuterung";
	
	
	public Person(CnATreeElement parent) {
		super(parent);
		if (entityType == null)
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(P_NAME),
				Messages.Person_3);
	}
	
	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	private Person() {
		
	}
	
	public int getSchicht() {
		return 0;
	}
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_NAME), name);
	}
	
	@Override
	public String getTitel() {
		if (getEntity() == null)
			return "";
		
		StringBuffer buff = new StringBuffer();
		buff.append(getEntity().getSimpleValue(P_VORNAME));
		if (buff.length() > 0)
			buff.append(" "); //$NON-NLS-1$
		buff.append( getEntity().getSimpleValue(P_NAME));
		
		String rollen = getRollen();
		if (rollen.length() > 0)
			buff.append(" [" + rollen + "]");
		
		return buff.toString();
	}
	
	public String getFullName() {
		if (getEntity() == null)
			return "";
		
		StringBuffer buff = new StringBuffer();
		buff.append(getEntity().getSimpleValue(P_VORNAME));
		if (buff.length() > 0)
			buff.append(" "); //$NON-NLS-1$
		buff.append( getEntity().getSimpleValue(P_NAME));
		
		return buff.toString();
	}
	
	private String getRollen() {
		if (getEntity() == null)
			return "";
		
		StringBuffer buf = new StringBuffer();
		PropertyList propertyList = getEntity().getProperties(P_ROLLEN);
		PropertyType type = HUITypeFactory.getInstance().getPropertyType(TYPE_ID, P_ROLLEN);
		List<Property> properties = propertyList.getProperties();
		
		if (properties == null)
			return "";
		
		for (Iterator iter = properties.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			String rolle = type.getOption(prop.getPropertyValue()).getName();
			buf.append(rolle);
			if (iter.hasNext())
				buf.append(", ");
		}
		return buf.toString();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public void addChild(CnATreeElement child) {
		// Person doesn't have children
	}

	public void setErlaeuterung(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_ERLAEUTERUNG), name);
	}
	
	public String getErlaeuterung() {
		return getEntity().getSimpleValue(PROP_ERLAEUTERUNG);
	}
	
	public void setKuerzel(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_KUERZEL), name);
	}

	public boolean hasRole(Property role) {
		if (getRollen().indexOf(role.getPropertyValue()) > -1)
			return true;
		return false;
	}

	public boolean addRole(String name) {
		PropertyType propertyType = entityType.getPropertyType(P_ROLLEN);
		ArrayList<IMLPropertyOption> options = propertyType.getOptions();
		for (IMLPropertyOption option : options) {
			if (option.getName().equals(name)) {
				getEntity().createNewProperty(propertyType, option.getId());
				return true;
			}
		}
		return false;
	}
	
	
}
