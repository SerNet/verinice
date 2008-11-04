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

	public static final String P_KAPITEL = "mnums_id"; //$NON-NLS-1$
	public static final String P_NAME = "mnums_name"; //$NON-NLS-1$
	public static final String P_SIEGEL = "mnums_siegel"; //$NON-NLS-1$
	public static final String P_LEBENSZYKLUS = "mnums_lebenszyklus"; //$NON-NLS-1$
	public static final String P_UMSETZUNGBIS = "mnums_umsetzungbis"; //$NON-NLS-1$
	public static final String P_UMSETZUNGDURCH_LINK = "mnums_umsetzungdurch_link"; //$NON-NLS-1$

	public static final String P_NAECHSTEREVISIONAM = "mnums_naechsterevisionam"; //$NON-NLS-1$
	public static final String P_NAECHSTEREVISIONDURCH_LINK = "mnums_naechsterevisiondurch_link"; //$NON-NLS-1$
	public static final String P_LETZTEREVISIONDURCH_LINK = "mnums_letzterevisiondurch_link";

	public static final String P_UMSETZUNG = "mnums_umsetzung"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_NEIN = "mnums_umsetzung_nein"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_JA = "mnums_umsetzung_ja"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_TEILWEISE = "mnums_umsetzung_teilweise"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_ENTBEHRLICH = "mnums_umsetzung_entbehrlich"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_UNBEARBEITET = "mnums_umsetzung_unbearbeitet"; //$NON-NLS-1$
	public static final String P_URL = "mnums_url"; //$NON-NLS-1$
	public static final String P_STAND = "mnums_stand";
	public static final String P_ERLAEUTERUNG = "mnums_erlaeuterung";

	public static final String P_KOSTEN_PTFIX = "mnums_kosten_ptfix";
	public static final String P_KOSTEN_PTVAR = "mnums_kosten_ptvar";
	public static final String P_KOSTEN_PTPERIOD = "mnums_kosten_ptperiod";

	public static final String P_KOSTEN_SACHFIX = "mnums_kosten_sachfix";
	public static final String P_KOSTEN_SACHVAR = "mnums_kosten_sachvar";
	public static final String P_KOSTEN_SACHPERIOD = "mnums_kosten_sachperiod";

	public static final String P_KOSTEN_SACHPERIOD_TAG 	= "mnums_kosten_sachperiod_tag";
	public static final String P_KOSTEN_SACHPERIOD_WOCHE 	= "mnums_kosten_sachperiod_woche";
	public static final String P_KOSTEN_SACHPERIOD_MONAT 	= "mnums_kosten_sachperiod_monat";
	public static final String P_KOSTEN_SACHPERIOD_QUARTAL 	= "mnums_kosten_sachperiod_quartal";
	public static final String P_KOSTEN_SACHPERIOD_JAHR 	= "mnums_kosten_sachperiod_jahr";

	public static final String P_KOSTEN_PTPERIOD_TAG 		= "mnums_kosten_ptperiod_tag";
	public static final String P_KOSTEN_PTPERIOD_WOCHE 		= "mnums_kosten_ptperiod_woche";
	public static final String P_KOSTEN_PTPERIOD_MONAT 		= "mnums_kosten_ptperiod_monat";
	public static final String P_KOSTEN_PTPERIOD_QUARTAL 	= "mnums_kosten_ptperiod_quartal";
	public static final String P_KOSTEN_PTPERIOD_JAHR 		= "mnums_kosten_ptperiod_jahr";

	// deprecated fields for persons, are now replaced by linked person entities
	// only used for migration of old values:
	@Deprecated
	public static final String P_UMSETZUNGDURCH_OLD = "mnums_umsetzungdurch"; //$NON-NLS-1$
	@Deprecated
	public static final String P_NAECHSTEREVISIONDURCH_OLD = "mnums_naechsterevisiondurch"; //$NON-NLS-1$
	@Deprecated
	public static final String P_LETZTEREVISIONDURCH_OLD = "mnums_letzterevisiondurch";

	private EntityType entityType;

	private static Pattern kapitelPattern = Pattern.compile("(\\d+)\\.(\\d+)"); //$NON-NLS-1$

	public MassnahmenUmsetzung(CnATreeElement parent) {
		super(parent);
		if (entityType == null)
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
	}

	protected MassnahmenUmsetzung() {

	}

	public static String[] getUmsetzungStati() {
		return new String[] { P_UMSETZUNG_NEIN, P_UMSETZUNG_JA,
				P_UMSETZUNG_TEILWEISE, P_UMSETZUNG_ENTBEHRLICH,
				P_UMSETZUNG_UNBEARBEITET };
	}

	public static String[] getStufen() {
		return new String[] { "A", //$NON-NLS-1$
				"B", //$NON-NLS-1$
				"C", //$NON-NLS-1$
				"Z" //$NON-NLS-1$
		};
	}

	public boolean isStufeA() {
		return (getStufe() == 'A');
	}

	public boolean isStufeB() {
		return isStufeA() || getStufe() == 'B';
	}

	public boolean isStufeC() {
		return isStufeB() || getStufe() == 'C';
	}

	public boolean isStufeZ() {
		return isStufeC() || getStufe() == 'Z';
	}

	public void setStufe(char stufe) {
		PropertyType type = entityType.getPropertyType(P_SIEGEL);
		getEntity().setSimpleValue(type, Character.toString(stufe));
	}

	public void setUmsetzung(String status) {
		PropertyType type = entityType.getPropertyType(P_UMSETZUNG);
		getEntity().setSimpleValue(type, status);
	}

	public char getStufe() {
		return getEntity().getSimpleValue(P_SIEGEL).length() > 0 ? getEntity()
				.getSimpleValue(P_SIEGEL).charAt(0) : ' ';
	}

	public String getUmsetzungDurch() {
		return getEntity().getSimpleValue(P_UMSETZUNGDURCH_LINK);
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
	public String getTitel() {
		return getEntity().getSimpleValue(P_KAPITEL) //$NON-NLS-1$
				+ " [" + getEntity().getSimpleValue(P_SIEGEL)
				+ "] "
				+ getEntity().getSimpleValue(P_NAME);
	}

	public String getKapitel() {
		return getEntity().getSimpleValue(P_KAPITEL);
	}

	public int[] getKapitelValue() {
		int[] kapitel = new int[2];
		Matcher m = kapitelPattern.matcher(getEntity()
				.getSimpleValue(P_KAPITEL));
		if (m.find()) {
			try {
				kapitel[0] = Integer.parseInt(m.group(1));
				kapitel[1] = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {
				Logger.getLogger(this.getClass()).error(
						Messages.MassnahmenUmsetzung_12, e);
			}
		}
		return kapitel;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	public String getUmsetzung() {
		PropertyList properties = getEntity().getProperties(P_UMSETZUNG);
		if (properties == null || properties.getProperties() == null
				|| properties.getProperties().size() < 1)
			return P_UMSETZUNG_UNBEARBEITET;

		Property property = properties.getProperty(0);
		if (property != null && !property.getPropertyValue().equals(""))
			return property.getPropertyValue();
		return P_UMSETZUNG_UNBEARBEITET;
	}

	public Date getUmsetzungBis() {
		if (getEntity().getProperties(P_UMSETZUNGBIS).getProperty(0) == null)
			return null;

		String dateString = getEntity().getProperties(P_UMSETZUNGBIS)
				.getProperty(0).getPropertyValue();

		if (dateString == null || dateString.length() == 0)
			return null;
		return new Date(Long.parseLong(dateString));
	}

	public Date getNaechsteRevision() {
		PropertyList properties = getEntity().getProperties(
				P_NAECHSTEREVISIONAM);
		Property property = properties.getProperty(0);
		if (property == null)
			return null;
		String dateString = property.getPropertyValue();
		if (dateString == null || dateString.length() == 0)
			return null;
		return new Date(Long.parseLong(dateString));

	}

	public String getRevisionDurch() {
		return getEntity().getSimpleValue(P_NAECHSTEREVISIONDURCH_LINK);
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

	public String getLebenszyklus() {
		return getEntity().getSimpleValue(P_LEBENSZYKLUS);
	}

	public boolean isCompleted() {
		if (getUmsetzung().equals(P_UMSETZUNG_JA)
				|| getUmsetzung().equals(P_UMSETZUNG_ENTBEHRLICH))
			return true;
		return false;
	}

}
