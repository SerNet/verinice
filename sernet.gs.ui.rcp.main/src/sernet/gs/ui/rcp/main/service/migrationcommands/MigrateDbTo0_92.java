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
package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelComplete;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.IDatenschutzElement;
import sernet.verinice.service.commands.SaveElement;

/**
 * Converts old text-only person fields to new ones: links to person entities.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MigrateDbTo0_92 extends DbMigration {

    private static final Logger LOG = Logger.getLogger(MigrateDbTo0_92.class);
    
	// list of old values to be translated to new linked field:
	@SuppressWarnings("deprecation")
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

	public void run() throws CommandException {
	    LOG.debug("Updating DB model to V 0.92.");
		createNewFieldsArray();
		LoadCnAElementByType<Person> command = new LoadCnAElementByType<Person>(Person.class);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		personen = command.getElements();
		
		LOG.debug("Aktualisiere DB auf V 0.92... Bitte warten...");
			
		LoadBSIModelComplete command2 = new LoadBSIModelComplete(false); /* skip massnahmen */
		command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
		List<CnATreeElement> allElements = command2.getModel().getAllElementsFlatList(false);

		LOG.debug("Migriere verknüpfte Personen...");
		List<CnATreeElement> changedElements = migratePersonFields(allElements);
		
		LOG.debug("Speichere alle veränderten Objekte. Bitte warten...");

		UpdateMultipleElements command3 = new UpdateMultipleElements(changedElements, ChangeLogEntry.STATION_ID);
		getCommandService().executeCommand(command3);
		
		BSIModel model = command2.getModel();
		model.setDbVersion(getVersion());
		SaveElement<BSIModel> command4 = new SaveElement<BSIModel>(model);
		getCommandService().executeCommand(command4);
	}

	private List<CnATreeElement> migratePersonFields(List<CnATreeElement> allElements) {
		List<CnATreeElement> changeElements = new ArrayList<CnATreeElement>();
		for (CnATreeElement element : allElements) {
			Entity entity = element.getEntity();
			if (entity == null){
				continue;
			}
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
			if (person.getTitle().equals(oldName)){
				return person.getEntity().getDbId().toString();
			}
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
	    final double versionToMigrateTo = 0.92D;
		return versionToMigrateTo;
	}

	@Override
    public void execute() {
		try {
			run();
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}

}
