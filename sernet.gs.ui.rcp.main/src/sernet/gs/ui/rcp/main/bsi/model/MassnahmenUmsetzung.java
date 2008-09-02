package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;

public class MassnahmenUmsetzung extends CnATreeElement {

	public static final String TYPE_ID = "mnums"; //$NON-NLS-1$

	public static final String P_KAPITEL 		= "mnums_id"; //$NON-NLS-1$
	public static final String P_NAME 		= "mnums_name"; //$NON-NLS-1$
	public static final String P_SIEGEL		= "mnums_siegel"; //$NON-NLS-1$
	public static final String P_LEBENSZYKLUS	= "mnums_lebenszyklus"; //$NON-NLS-1$
	public static final String P_UMSETZUNGBIS	= "mnums_umsetzungbis"; //$NON-NLS-1$
	public static final String P_UMSETZUNGDURCH	= "mnums_umsetzungdurch"; //$NON-NLS-1$

	public static final String P_NAECHSTEREVISIONAM	= "mnums_naechsterevisionam"; //$NON-NLS-1$
	public static final String P_NAECHSTEREVISIONDURCH	= "mnums_naechsterevisiondurch"; //$NON-NLS-1$
	public static final String P_LETZTEREVISIONDURCH    = "mnums_letzterevisiondurch";

	public static final String P_UMSETZUNG 	= "mnums_umsetzung"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_NEIN 		= "mnums_umsetzung_nein"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_JA 			= "mnums_umsetzung_ja"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_TEILWEISE 	= "mnums_umsetzung_teilweise"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_ENTBEHRLICH	= "mnums_umsetzung_entbehrlich"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_UNBEARBEITET = "mnums_umsetzung_unbearbeitet"; //$NON-NLS-1$

	public static final String P_URL = "mnums_url"; //$NON-NLS-1$

	private static final String P_STAND = "mnums_stand";


	private EntityType entityType;

	
	private static Pattern kapitelPattern = Pattern.compile("(\\d+)\\.(\\d+)"); //$NON-NLS-1$

	public MassnahmenUmsetzung(CnATreeElement parent) {
		super(parent);
		if (entityType == null)
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
	}

	private MassnahmenUmsetzung() {

	}
	
	public static String[] getUmsetzungStati() {
		return new String[] {
				P_UMSETZUNG_NEIN,
				P_UMSETZUNG_JA,
				P_UMSETZUNG_TEILWEISE,
				P_UMSETZUNG_ENTBEHRLICH,
				P_UMSETZUNG_UNBEARBEITET
		};
	}
	
	public static String[] getStufen() {
		return new String[] {
				"A", //$NON-NLS-1$
				"B", //$NON-NLS-1$
				"C", //$NON-NLS-1$
				"Z" //$NON-NLS-1$
		};
	}
	
	public  boolean isStufeA() {
		return (getStufe() == 'A');
	}
	
	public boolean isStufeB() {
		return isStufeA()
			|| getStufe() == 'B';
	}
	
	public boolean isStufeC() {
		return isStufeB()
			|| getStufe() == 'C';
	}
	
	public boolean isStufeZ() {
		return isStufeC()
			|| getStufe() == 'Z';
	}
	
	public void setStufe(char stufe) {
		PropertyType type = entityType.getPropertyType(P_SIEGEL);
		getEntity().setSimpleValue(type, 
				Character.toString(stufe));
	}
	
	public char getStufe() {
		return getEntity().getSimpleValue(P_SIEGEL).length() > 0 
			? getEntity().getSimpleValue(P_SIEGEL).charAt(0)
					: ' ';
	}

	public String getUmsetzungDurch() {
		return getEntity().getSimpleValue(P_UMSETZUNGDURCH);
	}
	
	public void setLebenszyklus(String lz) {
		PropertyType type = entityType.getPropertyType(P_LEBENSZYKLUS);
		getEntity().setSimpleValue(type, lz);
		
	}

	public void setKapitel(String kap) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_KAPITEL), kap);
	}

	public void setName(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_NAME), name);
	}

	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(P_KAPITEL) //$NON-NLS-1$
			+ " [" + getEntity().getSimpleValue(P_SIEGEL) + "] "
				+ getEntity().getSimpleValue(P_NAME);
	}
	
	public int[] getKapitelValue() {
		int[] kapitel = new int[2];
		Matcher m = kapitelPattern.matcher(getEntity().getSimpleValue(P_KAPITEL));
		if (m.find()) {
			try {
				kapitel[0] = Integer.parseInt(m.group(1));
				kapitel[1] = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {
				Logger.getLogger(this.getClass())
					.error(Messages.MassnahmenUmsetzung_12, e);
			}
		}
		return kapitel;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	public String getUmsetzung( ) {
		PropertyList properties = getEntity().getProperties(P_UMSETZUNG);
		if (properties == null || properties.getProperties() == null|| properties.getProperties().size() < 1) 
			return P_UMSETZUNG_UNBEARBEITET;
			
		Property property = properties.getProperty(0);
		if (property != null && !property.getPropertyValue().equals(""))
			return property.getPropertyValue();
		return P_UMSETZUNG_UNBEARBEITET;
	}

	public Date getUmsetzungBis() {
		if (getEntity().getProperties(P_UMSETZUNGBIS).getProperty(0) == null)
			return null;
		
		String dateString = getEntity().getProperties(P_UMSETZUNGBIS).getProperty(0)
			.getPropertyValue();
		
		if (dateString == null || dateString.length() == 0)
			return null;
		return new Date(Long.parseLong(dateString));
	}

	public Date getNaechsteRevision() {
		PropertyList properties = getEntity().getProperties(P_NAECHSTEREVISIONAM);
		Property property = properties.getProperty(0);
		if (property == null)
			return null;
		String dateString = property.getPropertyValue();
		if (dateString == null || dateString.length() == 0)
			return null;
		return new Date(Long.parseLong(dateString));

	}

	public String getRevisionDurch() {
		return getEntity().getSimpleValue(P_NAECHSTEREVISIONDURCH);
	}

	public String getUrl() {
		return getEntity().getSimpleValue(P_URL);
	}

	public void setUrl(String url2) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_URL), url2);
	}

	public String getStand() {
		return getEntity().getSimpleValue(P_STAND);
	}

	public void setStand(String stand) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_STAND), stand);
	}

	/**
	 * Returns the name of the MassnahnenUmsetzung.
	 * 
	 * @author ahanekop@sernet.de
	 * @return the name of the MassnahnenUmsetzung
	 */
	public String getName() {
		return getEntity().getSimpleValue(P_NAME);
	}
	
}
