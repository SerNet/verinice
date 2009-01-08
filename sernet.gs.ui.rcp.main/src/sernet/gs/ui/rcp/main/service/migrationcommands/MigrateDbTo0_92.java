package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.resources.SaveContext;
import org.eclipse.core.internal.resources.mapping.ChangeDescription;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
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
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelComplete;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
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


	private List<Person> personen;

	
	public void run() throws Exception {
		Logger.getLogger(this.getClass()).debug("Updating DB model to V 0.92.");
		createNewFieldsArray();
		LoadElementByType<Person> command = new LoadElementByType<Person>(Person.class);
		ServiceFactory.lookupCommandService().executeCommand(command);
		personen = command.getElements();
		
	
		//progress.beginTask("Aktualisiere DB auf V 0.92... Bitte warten...", IProgress.UNKNOWN_WORK);
		Logger.getLogger(this.getClass()).debug("Aktualisiere DB auf V 0.92... Bitte warten...");
		
		
		LoadBSIModelComplete command2 = new LoadBSIModelComplete(false); /* skip massnahmen */
		ServiceFactory.lookupCommandService().executeCommand(command2);
		List<CnATreeElement> allElements = command2.getModel().getAllElementsFlatList(false);

		//progress.beginTask("Migriere verknüpfte Personen...", allElements.size());
		Logger.getLogger(this.getClass()).debug("Migriere verknüpfte Personen...");
		List<CnATreeElement> changedElements = migratePersonFields(allElements);
		
		//progress.beginTask("Speichere alle veränderten Objekte. Bitte warten...", IProgress.UNKNOWN_WORK);
		Logger.getLogger(this.getClass()).debug("Speichere alle veränderten Objekte. Bitte warten...");

		UpdateMultipleElements command3 = new UpdateMultipleElements(changedElements);
		getCommandService().executeCommand(command3);
		
		BSIModel model = command2.getModel();
		model.setDbVersion(getVersion());
		SaveElement<BSIModel> command4 = new SaveElement<BSIModel>(model);
		getCommandService().executeCommand(command4);
		//progress.done();
	}
	
	


	private List<CnATreeElement> migratePersonFields(List<CnATreeElement> allElements) {
		List<CnATreeElement> changeElements = new ArrayList<CnATreeElement>();
		for (CnATreeElement element : allElements) {
//			progress.worked(1);
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

	@Override
	public double getVersion() {
		return 0.92D;
	}




	public void execute() {
		try {
			run();
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}

}
