package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class BausteinUmsetzung extends CnATreeElement {

	public static final String TYPE_ID = "bstumsetzung"; //$NON-NLS-1$
	
	public static final String P_NAME = "bstumsetzung_name"; //$NON-NLS-1$

	public static final String P_NR = "bstumsetzung_nr"; //$NON-NLS-1$

	public static final String P_URL = "bstumsetzung_url"; //$NON-NLS-1$
	
	public static final String P_ERFASSTDURCH = "bstumsetzung_erfasstdurch";

	public static final String P_GESPRAECHSPARTNER= "bstumsetzung_gespraechspartner";

	private EntityType entityType;

	private final static Pattern kapitelPattern 
		= Pattern.compile("(\\d+)\\.(\\d+)"); //$NON-NLS-1$

	private final static String[] schichten = new String[] {
		"1", "2", "3", "4", "5" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	};
	
	private final static String[] schichtenBezeichnung = new String[] {
		Messages.BausteinUmsetzung_9,
		Messages.BausteinUmsetzung_10,
		Messages.BausteinUmsetzung_11,
		Messages.BausteinUmsetzung_12,
		Messages.BausteinUmsetzung_13
	};

	private static final String P_STAND = "bstumsetzung_stand";

	
	public BausteinUmsetzung(CnATreeElement parent) {
		super(parent);
		if (entityType == null)
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
	}
	
	private BausteinUmsetzung() {
		// hibernate constructor
	}
	
	public void setKapitel(String kap) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_NR), kap);
	}

	public void setStand(String stand) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_STAND), stand);
	}
	
	public void setName(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_NAME), name);
	}
	
	
	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(P_NR) 
			+ " " + getEntity().getSimpleValue(P_NAME); //$NON-NLS-1$
	}
	
	public String getKapitel() {
		return getEntity().getSimpleValue(P_NR);
	}
	
	public int[] getKapitelValue() {
		int[] kapitel = new int[2];
		Matcher m = kapitelPattern.matcher(getKapitel());
		if (m.find()) {
			try {
				kapitel[0] = Integer.parseInt(m.group(1));
				kapitel[1] = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {
				Logger.getLogger(this.getClass())
					.error(Messages.BausteinUmsetzung_15, e);
			}
		}
		return kapitel;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	public List<MassnahmenUmsetzung> getMassnahmenUmsetzungen() {
		List<MassnahmenUmsetzung> result = new ArrayList<MassnahmenUmsetzung>(40);
		for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
			MassnahmenUmsetzung mnu = (MassnahmenUmsetzung) iter.next();
			result.add(mnu);
		}
		return result;
	}
	
	/**
	 * Überprüft anhand der umgesetzten Massnahmen, welche Siegestufe
	 * in diesem Baustein bereits erreicht wurde.
	 * 
	 * @return Siegelstufe als 0, A, B, C
	 */
	public char getErreichteSiegelStufe() {
		// starte mit höchster Stufe und reduziere anhand nicht-umgesetzter Massnahmen:
		char erreichteStufe = 'C'; 
		
		// bausteine ohne massnahmen werden als '0' = "keine umsetzungsstufe" dargestellt:
		if (getChildren().size() == 0)
			return '0';
			
		allMassnahmen: for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (!(obj instanceof MassnahmenUmsetzung)) {
				// should never happen:
				Logger.getLogger(this.getClass()).error(obj.getClass());
				return '0';
			}
			
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) obj;
			// prüfe nicht umgesetzte Massnahmen:
			if (mn.getUmsetzung().equals(MassnahmenUmsetzung.P_UMSETZUNG_NEIN)
					|| mn.getUmsetzung().equals(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE)
					|| mn.getUmsetzung().equals(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET)) {
				switch (mn.getStufe()) {
				case 'A':
					// reduzieren auf 0 und return:
					erreichteStufe = '0';
					break allMassnahmen;
				case 'B':
					// reduzieren auf A:
					erreichteStufe = 'A';
				case 'C':
					// reduzieren auf B:
					if (erreichteStufe != 'A')
						erreichteStufe = 'B';
				default:
					// change nothing (Z nicht umgesetzt, optional)	
				}
			}
		}
		return erreichteStufe;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof MassnahmenUmsetzung)
			return true;
		return false;
	}

	public static String[] getSchichten() {
		return schichten;
	}

	public static String[] getSchichtenBezeichnung() {
		return schichtenBezeichnung;
	}

	public String getUrl() {
		return getEntity().getSimpleValue(P_URL);
	}

	public void setUrl(String url2) {
		getEntity().setSimpleValue(entityType.getPropertyType(P_URL), url2);
	}

	public MassnahmenUmsetzung getMassnahmenUmsetzung(String url) {
		for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) iter.next();
			if (mn.getUrl().equals(url))
				return mn;
		}
		return null;
	}

	public String getStand() {
		return getEntity().getSimpleValue(P_STAND);
	}
	
}
