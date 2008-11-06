package sernet.gs.ui.rcp.main.common.model.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.resources.mapping.ChangeDescription;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.DbVersion;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;

/**
 * Converts old text-only person fields to new ones: links to person entities.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MigrateDbTo0_92 extends DbMigration {

	// list of old values to be translated to new linked field:
	private String[] personFieldsOld = new String[] {
			BausteinUmsetzung.P_ERFASSTDURCH_OLD,
			BausteinUmsetzung.P_GESPRAECHSPARTNER_OLD,
			MassnahmenUmsetzung.P_NAECHSTEREVISIONDURCH_OLD,
			MassnahmenUmsetzung.P_LETZTEREVISIONDURCH_OLD,
			MassnahmenUmsetzung.P_UMSETZUNGDURCH_OLD,
			Client.P_ADMIN_OLD,
			Client.P_ANWENDER_OLD, 
			SonstIT.P_ADMIN_OLD,
			SonstIT.P_ANWENDER_OLD,
			Server.P_ADMIN_OLD,
			Server.P_ANWENDER_OLD, 
			TelefonKomponente.P_ADMIN_OLD,
			TelefonKomponente.P_ANWENDER_OLD,
			Anwendung.PROP_BENUTZER_OLD,
			Anwendung.PROP_EIGENTUEMER_OLD, 
			IDatenschutzElement.P_ABTEILUNG_OLD,
			IDatenschutzElement.P_FACHLICHVERANTWORTLICHER_OLD,
			IDatenschutzElement.P_ITVERANTWORTLICHER_OLD };

	private String[] personFieldsNew = new String[personFieldsOld.length];

	private DbVersion dbVersion;

	private ArrayList<Person> personen;

	private IProgress progress;
	
	public MigrateDbTo0_92(DbVersion dbVersion) {
		this.dbVersion = dbVersion;
	}

	public void run(IProgress progress) throws Exception {
		this.progress =progress;
		Logger.getLogger(this.getClass()).debug("Updating DB model to V 0.92.");
		createNewFieldsArray();
		personen = CnAElementFactory.getCurrentModel().getPersonen();
		
		// FIXME use DAO:
		//ServiceFactory factory = new ServiceFactory();
//		// for all fields holding a persons name:
//		for (int i = 0; i < personFieldsOld.length; i++) {
//			// find all saved values for this field:
//			Logger.getLogger(this.getClass()).debug("Converting " + personFieldsOld[i] + " to " + personFieldsNew[i]);
//			//factory.getHuiService().migratePersonsForTypeId(personFieldsOld[i], personFieldsNew[i]);
//		}
//		dbVersion.getLoadedModel().setDbVersion(0.92D);
//		dbVersion.getDbHome().update(dbVersion.getLoadedModel());
		
		progress.beginTask("Aktualisiere DB auf V 0.92... bitte warten...", IProgress.UNKNOWN_WORK);
		List<CnATreeElement> allElements = CnAElementFactory.getCurrentModel().getAllElements();

		progress.beginTask("Migriere verknüpfte Personen...", allElements.size());
		List<CnATreeElement> changedElements = migratePersonFields(allElements);
		
		progress.beginTask("Speichere alle veränderten Objekte...", IProgress.UNKNOWN_WORK);
		CnAElementHome.getInstance().update(changedElements);
		dbVersion.getLoadedModel().setDbVersion(0.92D);
		dbVersion.getDbHome().update(dbVersion.getLoadedModel());
		progress.done();
		
		
	}


	private List<CnATreeElement> migratePersonFields(List<CnATreeElement> allElements) {
		List<CnATreeElement> changeElements = new ArrayList<CnATreeElement>();
		for (CnATreeElement element : allElements) {
			progress.worked(1);
			// migrate element:
			Entity entity = element.getEntity();
			if (entity == null)
				continue;
			for (int i = 0; i < personFieldsOld.length; i++) {
				PropertyList properties = entity.getProperties(personFieldsOld[i]);
				if (properties != null && properties.getProperties() != null) {
					for (Property property : properties.getProperties()) {
						String oldName = property.getPropertyValue();
						if (oldName != null && oldName.length()>0) {
							String actualPersonEntityId = findActualPerson(oldName);
							if (actualPersonEntityId != null) {
								PropertyType newType = HUITypeFactory.getInstance()
									.getEntityType(entity.getEntityType())
									.getPropertyType(personFieldsNew[i]);
								entity.createNewProperty(newType, actualPersonEntityId);
								changeElements.add(element);
							}
						}
					}
				}
			}
		}
		return changeElements;
	}

	private String findActualPerson(String oldName) {
		for (Person person : personen) {
			if (person.getTitel().equals(oldName))
				return person.getEntity().getDbId().toString();
		}
		return null;
	}

	/**
	 * New fields are named like old ones with _link attached:
	 */
	private void createNewFieldsArray() {
		for (int i = 0; i < personFieldsOld.length; i++) {
			personFieldsNew[i] = personFieldsOld[i] + "_link";
		}
	}

}
