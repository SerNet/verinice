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
 ******************************************************************************/
package sernet.verinice.model.bsi;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.common.CnATreeElement;

public class MassnahmenUmsetzung extends CnATreeElement implements IMassnahmeUmsetzung {

	public static final String TYPE_ID = "mnums"; //$NON-NLS-1$

	public static final String P_KAPITEL = "mnums_id"; //$NON-NLS-1$
	public static final String P_NAME = "mnums_name"; //$NON-NLS-1$
	public static final String P_SIEGEL = "mnums_siegel"; //$NON-NLS-1$
	public static final String P_LEBENSZYKLUS = "mnums_lebenszyklus"; //$NON-NLS-1$
	public static final String P_UMSETZUNGBIS = "mnums_umsetzungbis"; //$NON-NLS-1$
	public static final String P_UMSETZUNGDURCH_LINK = "mnums_umsetzungdurch_link"; //$NON-NLS-1$
	public static final String P_INITIIERUNGDURCH_LINK = "mnums_initdurch_link"; //$NON-NLS-1$
	

	public static final String P_NAECHSTEREVISIONAM = "mnums_naechsterevisionam"; //$NON-NLS-1$
	public static final String P_NAECHSTEREVISIONDURCH_LINK = "mnums_naechsterevisiondurch_link"; //$NON-NLS-1$
	public static final String P_LETZTEREVISIONDURCH_LINK = "mnums_letzterevisiondurch_link"; //$NON-NLS-1$

	public static final String P_UMSETZUNG = "mnums_umsetzung"; //$NON-NLS-1$
	// Grundschtuz
	public static final String P_UMSETZUNG_NEIN = "mnums_umsetzung_nein"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_JA = "mnums_umsetzung_ja"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_TEILWEISE = "mnums_umsetzung_teilweise"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_ENTBEHRLICH = "mnums_umsetzung_entbehrlich"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_UNBEARBEITET = "mnums_umsetzung_unbearbeitet"; //$NON-NLS-1$
	// ISO 27001
	public static final String P_UMSETZUNG_PERFORMED = "mnums_umsetzung_performed"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_MANAGED = "mnums_umsetzung_managed"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_ESTABLISHED = "mnums_umsetzung_established"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_PREDICTABLE = "mnums_umsetzung_predictable"; //$NON-NLS-1$
	public static final String P_UMSETZUNG_OPTIMIZING = "mnums_umsetzung_optimizing"; //$NON-NLS-1$
	
	public static final String P_URL = "mnums_url"; //$NON-NLS-1$
	public static final String P_STAND = "mnums_stand"; //$NON-NLS-1$
	public static final String P_ERLAEUTERUNG = "mnums_erlaeuterung"; //$NON-NLS-1$
	public static final String P_BESCHREIBUNG = "mnums_beschreibung"; //$NON-NLS-1$

	public static final String P_KOSTEN_PTFIX = "mnums_kosten_ptfix"; //$NON-NLS-1$
	public static final String P_KOSTEN_PTVAR = "mnums_kosten_ptvar"; //$NON-NLS-1$
	public static final String P_KOSTEN_PTPERIOD = "mnums_kosten_ptperiod"; //$NON-NLS-1$

	public static final String P_KOSTEN_SACHFIX = "mnums_kosten_sachfix"; //$NON-NLS-1$
	public static final String P_KOSTEN_SACHVAR = "mnums_kosten_sachvar"; //$NON-NLS-1$
	public static final String P_KOSTEN_SACHPERIOD = "mnums_kosten_sachperiod"; //$NON-NLS-1$

	public static final String P_KOSTEN_SACHPERIOD_TAG 	= "mnums_kosten_sachperiod_tag"; //$NON-NLS-1$
	public static final String P_KOSTEN_SACHPERIOD_WOCHE 	= "mnums_kosten_sachperiod_woche"; //$NON-NLS-1$
	public static final String P_KOSTEN_SACHPERIOD_MONAT 	= "mnums_kosten_sachperiod_monat"; //$NON-NLS-1$
	public static final String P_KOSTEN_SACHPERIOD_QUARTAL 	= "mnums_kosten_sachperiod_quartal"; //$NON-NLS-1$
	public static final String P_KOSTEN_SACHPERIOD_JAHR 	= "mnums_kosten_sachperiod_jahr"; //$NON-NLS-1$

	public static final String P_KOSTEN_PTPERIOD_TAG 		= "mnums_kosten_ptperiod_tag"; //$NON-NLS-1$
	public static final String P_KOSTEN_PTPERIOD_WOCHE 		= "mnums_kosten_ptperiod_woche"; //$NON-NLS-1$
	public static final String P_KOSTEN_PTPERIOD_MONAT 		= "mnums_kosten_ptperiod_monat"; //$NON-NLS-1$
	public static final String P_KOSTEN_PTPERIOD_QUARTAL 	= "mnums_kosten_ptperiod_quartal"; //$NON-NLS-1$
	public static final String P_KOSTEN_PTPERIOD_JAHR 		= "mnums_kosten_ptperiod_jahr"; //$NON-NLS-1$

	public static final String P_VERANTWORTLICHE_ROLLEN_INITIIERUNG	= "mnums_verantwortlichinit"; //$NON-NLS-1$
	public static final String P_VERANTWORTLICHE_ROLLEN_UMSETZUNG 	= "mnums_verantwortlichums"; //$NON-NLS-1$

	// deprecated text-only fields for person's names, are now replaced by linked person entities
	// only used for migration of old values:
	@Deprecated
	public static final String P_UMSETZUNGDURCH_OLD = "mnums_umsetzungdurch"; //$NON-NLS-1$
	@Deprecated
	public static final String P_NAECHSTEREVISIONDURCH_OLD = "mnums_naechsterevisiondurch"; //$NON-NLS-1$
	@Deprecated
	public static final String P_LETZTEREVISIONDURCH_OLD = "mnums_letzterevisiondurch"; //$NON-NLS-1$

	private static final String P_ENCODING = "mnums_encoding";


	private static Pattern kapitelPattern = Pattern.compile("(\\d+)\\.(\\d+)"); //$NON-NLS-1$

	public MassnahmenUmsetzung(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
	}

	public MassnahmenUmsetzung() {
		// hibernate constructor
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
		PropertyType type = getEntityType().getPropertyType(P_SIEGEL);
		getEntity().setSimpleValue(type, Character.toString(stufe));
	}

	public void setUmsetzung(String status) {
		PropertyType type = getEntityType().getPropertyType(P_UMSETZUNG);
		getEntity().setSimpleValue(type, status);
	}

	public char getStufe() {
		return getEntity().getSimpleValue(P_SIEGEL).length() > 0 ? getEntity()
				.getSimpleValue(P_SIEGEL).charAt(0) : ' ';
	}

	/**
	 * Find and return the person responsible for this control.
	 * 
	 */
	private String getVerantwortliche(String field) {
		String assignedPerson = getEntity().getSimpleValue(field);
		if (assignedPerson != null && assignedPerson.length() > 0){
			return assignedPerson;
		}
		return ""; //$NON-NLS-1$
	}
	
	public String getInitiierungDurch() {
		return getVerantwortliche(P_INITIIERUNGDURCH_LINK);
	}
	
	public String  getUmsetzungDurch( ) {
		return getVerantwortliche(P_UMSETZUNGDURCH_LINK);
	}
	
	public PropertyList getUmsetzungDurchLink() {
	    PropertyList result = null;
	    Map<String,PropertyList> map = getEntity().getTypedPropertyLists();
	    if(map!=null) {
	        result = map.get(MassnahmenUmsetzung.P_UMSETZUNGDURCH_LINK);
	    }
        return result;
	}

	
	public PropertyList getNaechsteRevisionLink() {
        PropertyList result = null;
        Map<String,PropertyList> map = getEntity().getTypedPropertyLists();
        if(map!=null) {
            result = map.get(MassnahmenUmsetzung.P_NAECHSTEREVISIONDURCH_LINK);
        }
        return result;
    }
	
	
	public void addUmsetzungDurch(Person person) {
		PropertyType propertyType = getEntityType().getPropertyType(P_UMSETZUNGDURCH_LINK);
		getEntity().createNewProperty(propertyType, person.getEntity().getDbId().toString());
	}

	public void setLebenszyklus(String lz) {
		PropertyType type = getEntityType().getPropertyType(P_LEBENSZYKLUS);
		getEntity().setSimpleValue(type, lz);

	}

	public void setKapitel(String kap) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_KAPITEL), kap);
	}

	public void setName(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_NAME), name);
	}

	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(P_KAPITEL) //$NON-NLS-1$
				+ " [" + getEntity().getSimpleValue(P_SIEGEL) //$NON-NLS-1$
				+ "] " //$NON-NLS-1$
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
						Messages.MassnahmenUmsetzung_1, e);
			}
		}
		return kapitel;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	public String getUmsetzung() {
	    String umsetzung = P_UMSETZUNG_UNBEARBEITET;
	    PropertyList properties = null;
        Map<String,PropertyList> map = getEntity().getTypedPropertyLists();
        if(map!=null) {
            properties = map.get(MassnahmenUmsetzung.P_UMSETZUNG);
        }       
		if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0) {
    		Property property = properties.getProperty(0);
    		if (property != null && property.getPropertyValue()!=null && !property.getPropertyValue().equals("")){ //$NON-NLS-1$
    		    umsetzung = property.getPropertyValue();
    		}
		}
		return umsetzung;
	}

	public Date getUmsetzungBis() {
	    PropertyList propertyList = null;
	    Date date = null;
        Map<String,PropertyList> map = getEntity().getTypedPropertyLists();
        if(map!=null) {
            propertyList = map.get(MassnahmenUmsetzung.P_UMSETZUNGBIS);
        }
		if (propertyList!=null && propertyList.getProperty(0) != null) {
		    String dateString = propertyList.getProperty(0).getPropertyValue();
		    
		    if (dateString != null && dateString.length() > 0) {
		        date = new Date(Long.parseLong(dateString));
		    }
		}
		return date;
	}
	
	public void setErlaeuterung(String text) {
		if(text!=null) {
			getEntity().setSimpleValue(getEntityType().getPropertyType(P_ERLAEUTERUNG), text);
		}
	}
	
	public String getErlaeuterung() {
		if (getEntity().getProperties(P_ERLAEUTERUNG).getProperty(0) == null){
			return null;
		}
		return getEntity().getProperties(P_ERLAEUTERUNG).getProperty(0).getPropertyValue();
	}
	
	public void setUmsetzungBis(Date date) {
		if(date!=null) {
			getEntity().setSimpleValue(getEntityType().getPropertyType(P_UMSETZUNGBIS), String.valueOf(date.getTime()));
		}
	}
	
	public Date getNaechsteRevision() {
        PropertyList propertyList = null;
        Date date = null;
        Map<String,PropertyList> map = getEntity().getTypedPropertyLists();
        if(map!=null) {
            propertyList = map.get(MassnahmenUmsetzung.P_NAECHSTEREVISIONAM);
        }
        if (propertyList!=null && propertyList.getProperty(0) != null) {
            String dateString = propertyList.getProperty(0).getPropertyValue();
            
            if (dateString != null && dateString.length() > 0) {
                date = new Date(Long.parseLong(dateString));
            }
        }
        return date;
    }

	public String getRevisionDurch() {
		return getEntity().getSimpleValue(P_NAECHSTEREVISIONDURCH_LINK);
	}

	public String getUrl() {
		return getEntity().getSimpleValue(P_URL);
	}

	public void setUrl(String url2) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_URL), url2);
	}

	public String getStand() {
		return getEntity().getSimpleValue(P_STAND);
	}

	public void setStand(String stand) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_STAND), stand);
	}

	public void setEncoding(String enc) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_ENCODING), enc);
	}
	
	/**
	 * Returns the name of the MassnahnenUmsetzung.
	 * 
	 * @author ahanekop[at]sernet[dot]de
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
				|| getUmsetzung().equals(P_UMSETZUNG_ENTBEHRLICH)){
			return true;
		}
		return false;
	}

	public void setVerantwortlicheRollenInitiierung(
			List<String> verantwortlichInitiierung) {
		for (String role : verantwortlichInitiierung) {
			getEntity().createNewProperty(getEntityType().getPropertyType(P_VERANTWORTLICHE_ROLLEN_INITIIERUNG), role);
		}
	}

	public void setVerantwortlicheRollenUmsetzung(
			List<String> verantwortlichUmsetzung) {
		for (String role : verantwortlichUmsetzung) {
			getEntity().createNewProperty(getEntityType().getPropertyType(P_VERANTWORTLICHE_ROLLEN_UMSETZUNG), role);
		}
	}

	public String getParentTitle() {
		return getParent().getParent().getTitle();
	}
	public String getDescription() {
		return getEntity().getSimpleValue(P_BESCHREIBUNG);
	}

	public String getEncoding() {
		return getEntity().getSimpleValue(P_ENCODING);
	}
}
	

	
