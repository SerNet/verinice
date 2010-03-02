/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateITVerbund;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByExternalID;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementsByIds;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementsBySourceID;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;
import sernet.hui.common.connect.PropertyType;



/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class SyncExample {

	private static HashMap<String, String> containerTypes = new HashMap<String, String>();
	
	static {
		containerTypes.put("client", "clientskategorie");
	}
		

	/**
	 * Pseudo-Code als Demo für die einzelnen notwendigen Schritte beim Synchronisieren:
	 */
	public void sync() {
		try {
			// INSERT & UPDATE
		
			// Source-ID aus data.xml lesen
			String sourceID = "myData";
			
			// IT-Verbund zu sourceId suchen, um Objekte dort einzufügen:
			LoadBSIModel command = new LoadBSIModel();
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			BSIModel model = command.getModel();
			
			List<ITVerbund> itverbuende = model.getItverbuende();
			ITVerbund importITVerbund = null;
			for (ITVerbund verbund : itverbuende) {
				if (verbund.getSourceId().equals(sourceID))
					importITVerbund = verbund;
			}

			if (importITVerbund == null ) {
				// kein passender gefunden -> erstmaliger Import von dieser Datenquelle
				// neuen ITVerbund anlegen:
				importITVerbund = createNewItVerbund(model, sourceID);
			}
			
			if (importITVerbund == null)
				throw new Exception("Konnte keinen ITVerbund für die Datenquelle laden oder anlegen.");

			// jetzt data.xml durchiterieren
			// Objekte einzeln durchgehen, einfügen wenn neu, ändern wenn schon vorhanden wie folgt:
			
			// foreach OBJECT aus data.xml:
				String externalID = "12";

				String externalObjectType = "Arbeitsplatz-PC";
				
				// Typ aus mapping.xml auflösen (Übung für den Leser;)
				String veriniceObjectType = "client"; 
				
				// versuchen, das Element in der DB zu finden - wenn es schonmal eingefügt wurde:
				CnATreeElement elementInDB = findDbElement(sourceID, externalID);
				
				if (elementInDB == null) {
					// nix da -> neues Objekt erzeugen: 
					CnATreeElement container = findContainerFor(importITVerbund, veriniceObjectType);
					elementInDB = CnAElementFactory.getInstance().saveNew(container, veriniceObjectType, null);
				}
					
				// wurde schonmal importiert oder wurde gerade neu angelegt -> jetzt Felder mit Eigenschaften füllen:
				// foreach property:
				
					String externalName  = "IP";
					String externalValue = "192.168.1.1";
					
					// internen Namen aus maping.xml auflösen (erneut Übung für den Leser;)
					String internalName = "client_ip";
					
					// einfachen String Wert setzen 
					// (für ein Datum müsste hier übrigens die Zeit in Sekunden seit 1970 drinstehen, 
					// da wäre vermutlich noch ein einfacherer Feldtyp nötig, in dem man z.B. JJJJMMDD eingeben kann
					// dasselbe gilt auch für Mehrach-Auswahlfelder etc.: die können theoretisch alle als String importiert werden
					// evtl. sollte man sich da aber jeweils noch was benutzerfreundlicheres überlegen
					
					// erstmal reicht aber der Normalfall, ein einfacher String:
					PropertyType propertyType = elementInDB.getEntityType().getPropertyType(internalName);
					elementInDB.getEntity().setSimpleValue(propertyType, externalValue);
					
				// ENDE foreach property:
				
			// ENDE foreach OBJECT aus data.xml
					
					
			// DELETE:
			// wenn delete flag in data.xml gesetzt:
			boolean delete = true;
			if (delete) {
				// Liste aller Objekte dieser sourceID in DB abfragen:
				LoadCnAElementsBySourceID command2 = new LoadCnAElementsBySourceID(sourceID);
				command2 = ServiceFactory.lookupCommandService().executeCommand(
						command2);
				List<CnATreeElement> dbElements = command2.getElements();
				
				// Liste aller externalIds in dieser data.xml erzeugen: (XML Gebastel überlass ich wieder Dir):
				Map<String, Object> currentIDs = new HashMap<String, Object>();
				Object gibts = new Object();
				currentIDs.put("12", gibts);
				currentIDs.put("42", gibts);
				currentIDs.put("21", gibts);
				
				// elemente finden, die in der DB sind, in der aktuellen Liste aber fehlen:
				for (CnATreeElement dbElement : dbElements) {
					Object found = currentIDs.get(dbElement.getExternalId());
					if (found == null) {
						// aus DB löschen, wenn delete flag gesetzt ist:
						if (delete) {
							RemoveElement removeCommand = new RemoveElement(dbElement);
							removeCommand = ServiceFactory.lookupCommandService()
									.executeCommand(removeCommand);
						}
					}
						
				}
			}
			
					
		
		
		} catch (CommandException e) {
			ExceptionUtil.log(e, "Fehler beim Einfügen von Objekten.");
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Einfügen von Objekten.");
		}
		
		
	}

	/**
	 * Finde passende Kategorie im Baum für einen Objekttyp.
	 * 
	 * @param veriniceObjectType
	 * @return
	 */
	private CnATreeElement findContainerFor(ITVerbund verbund, String veriniceObjectType) {
		String containerType = containerTypes.get(veriniceObjectType);
		return verbund.getCategory(containerType);
	}

	/**
	 * Suche element in DB, das aus dieser Datenquelle mit dieser ID schonmal geladen wurde.
	 * 
	 * @param sourceID
	 * @param externalID
	 * @return
	 * @throws CommandException 
	 */
	private CnATreeElement findDbElement(String sourceID, String externalID) throws CommandException {
		
		// hier musste ein neues Kommando geschrieben werden (load by external id and source id):
		LoadCnAElementByExternalID command = new LoadCnAElementByExternalID(sourceID, externalID);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		List<CnATreeElement> foundElements = command.getElements();
		if (foundElements == null || foundElements.size() == 0)
			return null;
		else
			return foundElements.get(0);
	}

	/**
	 * @param sourceID
	 * @throws Exception 
	 */
	private ITVerbund createNewItVerbund(BSIModel model, String sourceID) throws Exception {
		// we can use the command, or the factory to build the ITVerbund
		// Using the factory is better, since it automatically sets the model as parent etc.):
		
		CnATreeElement itverbund = CnAElementFactory.getInstance().saveNew(model, ITVerbund.TYPE_ID, null);
		return (ITVerbund) itverbund;
		
	}
}
