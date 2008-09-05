package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Schutzbedarf;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.IInputHelper;

public class InputHelperFactory {

	private static IInputHelper personHelper;
	private static IInputHelper schutzbedarfHelper;

	public static void setInputHelpers(EntityType entityType,
				HitroUIComposite huiComposite2) {
		if (personHelper == null) {
			personHelper = new IInputHelper() {
				public String[] getSuggestions() {
					ArrayList<Person> personen 
						= CnAElementFactory.getCurrentModel().getPersonen();
					String[] titles = new String[personen.size()];
					int i=0;
					for (Person person : personen) {
						titles[i++] = person.getTitel();
					}
					return titles.length > 0 
						? titles
						: new String[] {Messages.InputHelperFactory_0};
				}
			};
		}
		
		if (schutzbedarfHelper == null) {
			schutzbedarfHelper = new IInputHelper() {
				public String[] getSuggestions() {
					return new String[] {
							Schutzbedarf.MAXIMUM,
							Messages.InputHelperFactory_2,
							Messages.InputHelperFactory_3
					};
				}
			};
		}
		
		huiComposite2.setInputHelper(BausteinUmsetzung.P_ERFASSTDURCH, personHelper);
		huiComposite2.setInputHelper(BausteinUmsetzung.P_GESPRAECHSPARTNER, personHelper);
		huiComposite2.setInputHelper(MassnahmenUmsetzung.P_NAECHSTEREVISIONDURCH, personHelper);
		huiComposite2.setInputHelper(MassnahmenUmsetzung.P_LETZTEREVISIONDURCH, personHelper);
		huiComposite2.setInputHelper(MassnahmenUmsetzung.P_UMSETZUNGDURCH, personHelper);
		
		huiComposite2.setInputHelper(Client.P_ADMIN, personHelper);
		huiComposite2.setInputHelper(Client.P_ANWENDER, personHelper);
		huiComposite2.setInputHelper(SonstIT.P_ADMIN, personHelper);
		huiComposite2.setInputHelper(SonstIT.P_ANWENDER, personHelper);
		huiComposite2.setInputHelper(Server.P_ADMIN, personHelper);
		huiComposite2.setInputHelper(Server.P_ANWENDER, personHelper);
		huiComposite2.setInputHelper(TelefonKomponente.P_ADMIN, personHelper);
		huiComposite2.setInputHelper(TelefonKomponente.P_ANWENDER, personHelper);
		huiComposite2.setInputHelper(Anwendung.PROP_BENUTZER, personHelper);
		huiComposite2.setInputHelper(Anwendung.PROP_EIGENTUEMER, personHelper);

		huiComposite2.setInputHelper(IDatenschutzElement.P_ABTEILUNG, personHelper);
		huiComposite2.setInputHelper(IDatenschutzElement.P_FACHLICHVERANTWORTLICHER, personHelper);
		huiComposite2.setInputHelper(IDatenschutzElement.P_ITVERANTWORTLICHER, personHelper);
		
		
		
		setSchutzbedarfHelpers(entityType, huiComposite2);
	}

	private static void setSchutzbedarfHelpers(EntityType entityType, HitroUIComposite huiComposite2) {
		for (PropertyGroup group : entityType.getPropertyGroups()) {
			for (PropertyType type : group.getPropertyTypes()) {
				if (Schutzbedarf.isIntegritaetBegruendung(type.getId())
						|| Schutzbedarf.isVerfuegbarkeitBegruendung(type.getId())
						|| Schutzbedarf.isVertraulichkeitBegruendung(type.getId())) {
					huiComposite2.setInputHelper(type.getId(), schutzbedarfHelper);
				}
			}
		}
		
	}
}
