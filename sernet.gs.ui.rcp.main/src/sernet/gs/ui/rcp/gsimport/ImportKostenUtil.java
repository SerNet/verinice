package sernet.gs.ui.rcp.gsimport;

import java.util.List;

import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;

public class ImportKostenUtil {

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
					if (GS_ZEITEN[i].equals(zeitname))
						return vnZeiten[i];
				}
			}
		}
		return "";
	}

}
