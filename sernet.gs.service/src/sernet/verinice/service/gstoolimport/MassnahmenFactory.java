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
package sernet.verinice.service.gstoolimport;

import java.util.Date;

import sernet.gs.model.Massnahme;
import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

public class MassnahmenFactory {

    // umsetzungs patterns in verinice
    // leaving out "unbearbeitet" since this is the default:
    private static final String[] UMSETZUNG_STATI_VN = new String[] { MassnahmenUmsetzung.P_UMSETZUNG_NEIN, MassnahmenUmsetzung.P_UMSETZUNG_JA, MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, };

    // umsetzungs patterns in gstool:
    private static final String[] UMSETZUNG_STATI_GST = new String[] { "nein", "ja", "teilweise", "entbehrlich", };
    
	/**
	 * Create MassnahmenUmsetzung (control instance) and add to given BausteinUmsetzung (module instance).
	 * @param bu
	 * @param mn
	 */
	public MassnahmenUmsetzung createMassnahmenUmsetzung(BausteinUmsetzung bu, Massnahme mn, String language) {
		MassnahmenUmsetzung mu = new MassnahmenUmsetzung(bu);
		
		copyValues(mn, mu, language);
		bu.addChild(mu);
		return mu;
	}
	
    public MassnahmenUmsetzung transferUmsetzung(MassnahmenUmsetzung massnahmenUmsetzung, String gstStatus) {
        for (int i = 0; i < UMSETZUNG_STATI_GST.length; i++) {
            if (UMSETZUNG_STATI_GST[i].equals(gstStatus)) {
                massnahmenUmsetzung.setUmsetzung(UMSETZUNG_STATI_VN[i]);
                return massnahmenUmsetzung;
            }
        }
        return massnahmenUmsetzung;
    }

    public MassnahmenUmsetzung transferUmsetzungWithDate(MassnahmenUmsetzung massnahmenUmsetzung, String gstStatus, Date umsetzungUntilDate){
        massnahmenUmsetzung = transferUmsetzung(massnahmenUmsetzung, gstStatus);
        massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_UMSETZUNGBIS, parseDate(umsetzungUntilDate));
        return massnahmenUmsetzung;
    }

    public MassnahmenUmsetzung transferRevision(MassnahmenUmsetzung massnahmenUmsetzung, Date revisionAtDate, Date revisionNextDate, String revisionNotes ){
        massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_LETZTEREVISIONAM, parseDate(revisionAtDate));
        massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_NAECHSTEREVISIONAM, parseDate(revisionNextDate));
        massnahmenUmsetzung.setRevisionBemerkungen(revisionNotes);
        return massnahmenUmsetzung;
    }
    /**
	 * Creyte MassnahmenUmsetzung (control instance) from given Massnahme (control).
	 * 
	 * @param mn
	 * @return
	 */
	public MassnahmenUmsetzung createMassnahmenUmsetzung(Massnahme mn, String language) {
		MassnahmenUmsetzung mu = new MassnahmenUmsetzung();
		mu.setEntity(new Entity(MassnahmenUmsetzung.TYPE_ID));
		copyValues(mn, mu, language);
		return mu;
	}
	
	private void copyValues(Massnahme mn, MassnahmenUmsetzung mu, String language) {
		mu.setKapitel(mn.getId());
		mu.setUrl(mn.getUrl());
		mu.setName(mn.getTitel());
		mu.setLebenszyklus(mn.getLZAsString(language));
		mu.setStufe(mn.getSiegelstufe());
		mu.setStand(mn.getStand());
		mu.setVerantwortlicheRollenInitiierung(mn.getVerantwortlichInitiierung());
		mu.setVerantwortlicheRollenUmsetzung(mn.getVerantwortlichUmsetzung());
	}
	
    private String parseDate(Date date) {
        if (date != null) {
            return Long.toString(date.getTime());
        }
        return "";
    }
	
	
}
