/*******************************************************************************
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>
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
 *     Andreas Becker <andreas.r.becker[at]rub[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster[a]tarent[dot]de> - removal of JDom API use
 ******************************************************************************/
package sernet.gs.ui.rcp.main.sync.commands;

import java.util.HashMap;
import java.util.List;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByExternalID;
import sernet.gs.ui.rcp.main.sync.InvalidRequestException;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ImportedObjectsHolder;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.data.SyncObject.SyncAttribute;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;

@SuppressWarnings("serial")
public class SyncInsertUpdateCommand extends GenericCommand {

	private static HashMap<String, String> containerTypes = new HashMap<String, String>();
	private static HashMap<String, Class<? extends CnATreeElement>> typeIdClass = new HashMap<String, Class<? extends CnATreeElement>>();

	static {
		typeIdClass.put(Anwendung.TYPE_ID, Anwendung.class);
		typeIdClass.put(Gebaeude.TYPE_ID, Gebaeude.class);
		typeIdClass.put(Client.TYPE_ID, Client.class);
		typeIdClass.put(Server.TYPE_ID, Server.class);
		typeIdClass.put(SonstIT.TYPE_ID, SonstIT.class);
		typeIdClass.put(TelefonKomponente.TYPE_ID, TelefonKomponente.class);
		typeIdClass.put(Person.TYPE_ID, Person.class);
		typeIdClass.put(NetzKomponente.TYPE_ID, NetzKomponente.class);
		typeIdClass.put(Raum.TYPE_ID, Raum.class);
		typeIdClass.put(AnwendungenKategorie.TYPE_ID, AnwendungenKategorie.class);
		typeIdClass.put(GebaeudeKategorie.TYPE_ID, GebaeudeKategorie.class);
		typeIdClass.put(ClientsKategorie.TYPE_ID, ClientsKategorie.class);
		typeIdClass.put(ServerKategorie.TYPE_ID, ServerKategorie.class);
		typeIdClass.put(SonstigeITKategorie.TYPE_ID, SonstigeITKategorie.class);
		typeIdClass.put(TKKategorie.TYPE_ID, TKKategorie.class);
		typeIdClass.put(PersonenKategorie.TYPE_ID, PersonenKategorie.class);
		typeIdClass.put(NKKategorie.TYPE_ID, NKKategorie.class);
		typeIdClass.put(RaeumeKategorie.TYPE_ID, RaeumeKategorie.class);
		typeIdClass.put(BausteinUmsetzung.TYPE_ID, BausteinUmsetzung.class);
		typeIdClass.put(ITVerbund.TYPE_ID, ITVerbund.class);
		typeIdClass.put(MassnahmenUmsetzung.TYPE_ID, MassnahmenUmsetzung.class);
		typeIdClass.put(Verarbeitungsangaben.TYPE_ID, Verarbeitungsangaben.class);
		typeIdClass.put(Personengruppen.TYPE_ID, Personengruppen.class);
		typeIdClass.put(VerantwortlicheStelle.TYPE_ID, VerantwortlicheStelle.class);
		typeIdClass.put(StellungnahmeDSB.TYPE_ID, StellungnahmeDSB.class);
		typeIdClass.put(Datenverarbeitung.TYPE_ID, Datenverarbeitung.class);
	}

	private String sourceId;
	private SyncMapping syncMapping;
	private SyncData syncData;
	
	private boolean insert, update;
	private List<String> errorList;

	private int inserted = 0, updated = 0;

	public int getUpdated() {
		return updated;
	}

	public int getInserted() {
		return inserted;
	}

	public List<String> getErrorList() {
		return errorList;
	}

	public SyncInsertUpdateCommand(String sourceId, SyncData syncData,
			SyncMapping syncMapping, boolean insert, boolean update,
			List<String> errorList) {
		this.sourceId = sourceId;
		this.syncData = syncData;
		this.syncMapping = syncMapping;
		this.insert = insert;
		this.update = update;
		this.errorList = errorList;
	}

	/************************************************************
	 * execute()
	 * 
	 * Processes the given <syncData> and <syncMapping> elements in order to
	 * insert and/or update objects in(to) the database, according to the flags
	 * insert & update.
	 * 
	 * If there already exists an ITVerbund from a past sync session, this one
	 * (identified by its sourceID) will be used; otherwise this creates a new
	 * one within the BSIModel.
	 * 
	 * @throws InvalidRequestException
	 * @throws CommandException
	 ************************************************************/
	@SuppressWarnings("unchecked")
	public void execute() {
		LoadBSIModel cmdLoadBSIModel = new LoadBSIModel();

		try {
			cmdLoadBSIModel = ServiceFactory.lookupCommandService()
					.executeCommand(cmdLoadBSIModel);
		} catch (CommandException e) {
			e.printStackTrace();
			errorList.add("Fehler beim Ausführen von LoadBSIModel.");
			return;
		}

		BSIModel model = cmdLoadBSIModel.getModel();

		// Try to find an ITVerbund whose sourceId is that of the
		// imported data.
		List<ITVerbund> itVerbuende = model.getItverbuende();
		CnATreeElement importRootObject = null;

		for (ITVerbund v : itVerbuende)
			if (v.getSourceId() != null && v.getSourceId().equals(sourceId))
				importRootObject = v;
		
		// If no such object found then create an artificial root object
		// to hold all imported objects.
		if (importRootObject == null)
			try {
				ImportedObjectsHolder holder = new ImportedObjectsHolder(model);
				getDaoFactory().getDAO(ImportedObjectsHolder.class).saveOrUpdate(holder);
				importRootObject = holder;
			} catch (Exception e1) {
				throw new RuntimeCommandException(
						"Fehler beim Anlegen des Behälters für importierte Objekte.");
			}

		for (SyncObject so : syncData.getSyncObject()) {
			importObject(importRootObject, so);
		} // for <syncObject>
	}
	
	private void importObject(CnATreeElement importRootObject, SyncObject so)
	{
		String extId = so.getExtId();
		String extObjectType = so.getExtObjectType();
		boolean setAttributes = false;

		MapObjectType mot = getMap(extObjectType);

		if (mot == null) {
			errorList.add("Konnte kein mapObjectType-Element finden für den Objekttypen "
							+ extObjectType);
			return;
		}

		// this element "knows", which huientitytype is applicable and
		// how the associated properties have to be mapped!
		String veriniceObjectType = mot.getIntId();

		CnATreeElement elementInDB = null;

		try {
			elementInDB = findDbElement(sourceId, extId);
		} catch (CommandException e1) {
			throw new RuntimeCommandException(e1);
		}

		if (elementInDB != null) // object found!
		{
			if (update) // this object should be updated!
			{
				/*** UPDATE: ***/
				setAttributes = true;
				updated++;
			} else
				// do not update this object's attributes!
				setAttributes = false;
		}
		
		if (null == elementInDB && insert) // nothing found -> create new
											// object, if "insert" flag is
											// set:
		{
			/*** INSERT: ***/
			CnATreeElement container = findContainerFor(importRootObject,
					veriniceObjectType);

			try {
				// create new object in db...
				CreateElement<CnATreeElement> newElement = new CreateElement<CnATreeElement>(
						container, getClassFromTypeId(veriniceObjectType), true);
				getCommandService().executeCommand(
						newElement);
				elementInDB = newElement.getNewElement();

				// ...and set its sourceId and extId:
				elementInDB.setSourceId(sourceId);
				elementInDB.setExtId(extId);

				setAttributes = true;
				inserted++;
			} catch (Exception e) {
				errorList.add("Konnte " + veriniceObjectType
						+ "-Objekt nicht erzeugen.");
				e.printStackTrace();
			}
		}

		/*
		 * Now if we should update an existing object or created a new
		 * object, set the associated attributes:
		 */
		if (null != elementInDB && setAttributes) {
			// for all <syncAttribute>-Elements below current
			// <syncObject>...
			
			for (SyncAttribute sa : so.getSyncAttribute()) {
				String attrExtId = sa.getName();
				String attrValue = sa.getValue();
				
				MapAttributeType mat = getMapAttribute(mot, attrExtId);

				if (mat == null)
					this.errorList
							.add("Konnte kein mapAttributeType-Element finden für das Attribut "
									+ attrExtId + " von " + extObjectType);
				else {
					String attrIntId = mat.getIntId();
					PropertyType propertyType = elementInDB.getEntityType()
							.getPropertyType(attrIntId);

					if (null != propertyType)
						elementInDB.setSimpleProperty(attrIntId, attrValue);
					// else: ignore this attribute!
				}

			} // for <syncAttribute>
		} // if( null != ... )

		// Handle all the child objects.
		for (SyncObject child : so.getChildren())
		{
			importObject(elementInDB, child);
		}
	}
	
	private MapObjectType getMap(String extObjectType)
	{
		for (MapObjectType mot : syncMapping.getMapObjectType())
		{
			if (extObjectType.equals(mot.getExtId()))
					return mot;
		}
		
		return null;
	}
	
	private SyncMapping.MapObjectType.MapAttributeType getMapAttribute(
			MapObjectType mot, String extObjectType) {
		for (SyncMapping.MapObjectType.MapAttributeType mat : mot
				.getMapAttributeType()) {
			if (extObjectType.equals(mat.getExtId())) {
				return mat;
			}
		}

		return null;
	}

	/************************************************************
	 * findDbElement()
	 * 
	 * Query element (by externalID) from DB, which has been previously
	 * synchronized from the given sourceID.
	 * 
	 * @param sourceID
	 * @param externalID
	 * @return the CnATreeElement from the query or null, if nothing was found
	 * @throws CommandException
	 ************************************************************/
	private CnATreeElement findDbElement(String sourceID, String externalID)
			throws CommandException {
		// use a new crudCommand (load by external, source id):
		LoadCnAElementByExternalID command = new LoadCnAElementByExternalID(
				sourceID, externalID);
		command = getCommandService().executeCommand(command);

		List<CnATreeElement> foundElements = command.getElements();

		if (foundElements == null || foundElements.size() == 0)
			return null;
		else
			return foundElements.get(0);
	}

	/************************************************************
	 * findContainerFor()
	 * 
	 * Find appropriate Category within the tree for a given object type.
	 * 
	 * @param verbund
	 * @param veriniceObjectType
	 * @return the Category - CnATreeElement
	 ************************************************************/
	private CnATreeElement findContainerFor(CnATreeElement root,
			String veriniceObjectType) {
	
		// If in doubt the root for imported objects should always be used.
		return root;
	}

	/************************************************************
	 * getClassFromTypeId()
	 * 
	 * @param typeId
	 * @return the corresponding Class
	 ************************************************************/
	@SuppressWarnings("unchecked")
	private Class<CnATreeElement > getClassFromTypeId(String typeId) {
		Class<CnATreeElement> klass = (Class<CnATreeElement>) typeIdClass.get(typeId);
		if (klass == null)
			throw new IllegalStateException(String.format("Type ID '%s' was not available in type map.", typeId));
		
		return klass;
	}
}
