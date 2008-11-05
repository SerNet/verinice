package sernet.gs.ui.rcp.main.common.model.migration;

import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.common.model.DbVersion;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.Property;

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
	
	public MigrateDbTo0_92(DbVersion dbVersion) {
		this.dbVersion = dbVersion;
	}

	public void run() throws Exception {
		Logger.getLogger(this.getClass()).debug("Updating DB model to V 0.92.");
		createNewFieldsArray();
		ServiceFactory factory = new ServiceFactory();
		
		for (int i = 0; i < personFieldsOld.length; i++) {
			List<Property> properties = factory.getHuiService().findAllPropertiesForTypeId(personFieldsOld[i]);
			for (Property property : properties) {
			}
		}
		
		
//		dbVersion.getLoadedModel().setDbVersion(0.92D);
//		dbVersion.getDbHome().update(dbVersion.getLoadedModel());
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
