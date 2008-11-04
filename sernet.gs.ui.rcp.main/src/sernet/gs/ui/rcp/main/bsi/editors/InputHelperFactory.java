package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
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

	private static IInputHelper schutzbedarfHelper;
	private static IInputHelper tagHelper;

	public static void setInputHelpers(EntityType entityType,
				HitroUIComposite huiComposite2) {

//		if (personHelper == null) {
//			personHelper = new IInputHelper() {
//				public String[] getSuggestions() {
//					ArrayList<Person> personen 
//						= CnAElementFactory.getCurrentModel().getPersonen();
//					String[] titles = new String[personen.size()];
//					int i=0;
//					for (Person person : personen) {
//						titles[i++] = person.getTitel();
//					}
//					return titles.length > 0 
//						? titles
//						: new String[] {Messages.InputHelperFactory_0};
//				}
//			};
//		}
		
		if (tagHelper == null) {
			tagHelper = new IInputHelper() {
				public String[] getSuggestions() {
					List<String> tags = CnAElementFactory.getCurrentModel().getTags();
					String[] tagArray = (String[]) tags.toArray(new String[tags.size()]);
					for (int i = 0; i < tagArray.length; i++) {
						tagArray[i] = tagArray[i] + " ";
					}
					return tagArray.length > 0 
						? tagArray
						: new String[] {};
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
		
		
		// Tag Helpers:
		huiComposite2.setInputHelper(Anwendung.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		huiComposite2.setInputHelper(Client.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		huiComposite2.setInputHelper(Gebaeude.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		huiComposite2.setInputHelper(NetzKomponente.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		huiComposite2.setInputHelper(Person.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		huiComposite2.setInputHelper(Raum.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		huiComposite2.setInputHelper(Server.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		huiComposite2.setInputHelper(SonstIT.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		huiComposite2.setInputHelper(TelefonKomponente.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD);
		
		setSchutzbedarfHelpers(entityType, huiComposite2);
	}

	private static void setSchutzbedarfHelpers(EntityType entityType, HitroUIComposite huiComposite2) {
		for (PropertyGroup group : entityType.getPropertyGroups()) {
			for (PropertyType type : group.getPropertyTypes()) {
				if (Schutzbedarf.isIntegritaetBegruendung(type.getId())
						|| Schutzbedarf.isVerfuegbarkeitBegruendung(type.getId())
						|| Schutzbedarf.isVertraulichkeitBegruendung(type.getId())) {
					huiComposite2.setInputHelper(type.getId(), schutzbedarfHelper, IInputHelper.TYPE_REPLACE);
				}
			}
		}
		
	}
}
