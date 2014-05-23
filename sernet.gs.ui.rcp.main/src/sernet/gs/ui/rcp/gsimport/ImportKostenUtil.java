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
package sernet.gs.ui.rcp.gsimport;

import java.util.List;

import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

public final class ImportKostenUtil {
    
    private ImportKostenUtil(){};

	private static final String[] GS_ZEITEN = new String[] {
		"Tag",
		"Woche",
		"Monat",
		"Quartal",
		"Jahr"
	};
	
	private static final String[] VN_PERSONALKOSTEN_ZEITEN = new String[] {
		MassnahmenUmsetzung.P_KOSTEN_PTPERIOD_TAG, 
		MassnahmenUmsetzung.P_KOSTEN_PTPERIOD_WOCHE, 
		MassnahmenUmsetzung.P_KOSTEN_PTPERIOD_MONAT, 
		MassnahmenUmsetzung.P_KOSTEN_PTPERIOD_QUARTAL, 
		MassnahmenUmsetzung.P_KOSTEN_PTPERIOD_JAHR, 
	};
	
	private static final String[] VN_SACHKOSTEN_ZEITEN = new String[] {
		MassnahmenUmsetzung.P_KOSTEN_SACHPERIOD_TAG, 
		MassnahmenUmsetzung.P_KOSTEN_SACHPERIOD_WOCHE, 
		MassnahmenUmsetzung.P_KOSTEN_SACHPERIOD_MONAT, 
		MassnahmenUmsetzung.P_KOSTEN_SACHPERIOD_QUARTAL, 
		MassnahmenUmsetzung.P_KOSTEN_SACHPERIOD_JAHR, 
	};
	
	
	
	public static void importKosten(MassnahmenUmsetzung massnahmenUmsetzung,
			BausteineMassnahmenResult vorlage, List<MbZeiteinheitenTxt> zeiten) {
		
		
		massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_KOSTEN_PTFIX, 
				vorlage.obm.getKostPersFix().toString());
		massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_KOSTEN_PTVAR, 
				vorlage.obm.getKostPersVar().toString());
		if (vorlage.obm.getKostPersVar() > 0) {
			massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_KOSTEN_PTPERIOD, 
					translatePeriod(zeiten, vorlage.obm.getKostPersZeiId(), VN_PERSONALKOSTEN_ZEITEN));
		}

		
		massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_KOSTEN_SACHFIX, 
				vorlage.obm.getKostSachFix().toString());
		massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_KOSTEN_SACHVAR, 
				vorlage.obm.getKostSachVar().toString());
		if (vorlage.obm.getKostSachVar() > 0) {
			massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_KOSTEN_SACHPERIOD, 
					translatePeriod(zeiten, vorlage.obm.getKostSachZeiId(), VN_SACHKOSTEN_ZEITEN));
		}
	}

	private static String translatePeriod(List<MbZeiteinheitenTxt> zeiten, Integer zeiId, String[] vnZeiten) {
		for (MbZeiteinheitenTxt zeit : zeiten) {
			if (zeit.getId().getZeiId().equals(zeiId)) {
				String zeitname = zeit.getName();
				for (int i = 0; i < GS_ZEITEN.length; i++) {
					if (GS_ZEITEN[i].equals(zeitname)){
						return vnZeiten[i];
					}
				}
			}
		}
		return "";
	}

}
