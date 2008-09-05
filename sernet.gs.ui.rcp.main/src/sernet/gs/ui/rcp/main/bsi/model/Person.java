package sernet.gs.ui.rcp.main.bsi.model;


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

public class Person extends CnATreeElement
implements IBSIStrukturElement {
	private static final String P_NAME = "nachname"; //$NON-NLS-1$
	private static final String P_VORNAME = "vorname"; //$NON-NLS-1$
	private static final String PROP_KUERZEL = "person_kuerzel"; //$NON-NLS-1$
	private static final String P_ROLLEN = "person_rollen";
	private static EntityType entityType;

	// ID must correspond to entity definition in entitytype XML description:
	public static final String TYPE_ID = "person"; //$NON-NLS-1$
	
	
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
	
	private Person() {
		
	}
	
	public int getSchicht() {
		return 0;
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
	
	
}
