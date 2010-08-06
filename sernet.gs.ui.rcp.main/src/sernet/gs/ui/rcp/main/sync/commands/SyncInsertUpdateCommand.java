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
import java.util.Iterator;
import java.util.List;

import org.hibernate.proxy.HibernateProxy;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateImportITVerbund;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByExternalID;
import sernet.gs.ui.rcp.main.sync.InvalidRequestException;
import sernet.gs.ui.rcp.main.sync.SyncNamespaceUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class SyncInsertUpdateCommand extends GenericCommand {
	/*
	 * Since we are possibly instantiating objects from verinice business
	 * classes without having access to a tree with categories as parent nodes,
	 * we have to map huientitytype --> category manually:
	 */

	private static HashMap<String, String> containerTypes = new HashMap<String, String>();
	private static HashMap<String, Class> typeIdClass = new HashMap<String, Class>();

	static {
		containerTypes.put("anwendung", "anwendungenkategorie");
		containerTypes.put("client", "clientskategorie");
		containerTypes.put("gebaeude", "gebaeudekategorie");
		containerTypes.put("netzkomponente", "netzkategorie");
		containerTypes.put("person", "personkategorie");
		containerTypes.put("raum", "raeumekategorie");
		containerTypes.put("server", "serverkategorie");
		containerTypes.put("sonstit", "sonstitkategorie");
		containerTypes.put("tkkomponente", "tkkategorie");

		typeIdClass.put("anwendung", Anwendung.class);
		typeIdClass.put("client", Client.class);
		typeIdClass.put("gebaeude", Gebaeude.class);
		typeIdClass.put("netzkomponente", NetzKomponente.class);
		typeIdClass.put("person", Person.class);
		typeIdClass.put("raum", Raum.class);
		typeIdClass.put("server", Server.class);
		typeIdClass.put("sonstit", SonstIT.class);
		typeIdClass.put("tkkomponente", TKKategorie.class);
	}

	private String sourceId;
	private Element syncDataElement, syncMappingElement;
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

	public SyncInsertUpdateCommand(String sourceId, Element syncDataElement,
			Element syncMappingElement, boolean insert, boolean update,
			List<String> errorList) {
		this.sourceId = sourceId;
		this.syncDataElement = syncDataElement;
		this.syncMappingElement = syncMappingElement;
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

		// retreive ITVerbuende from model:
		List<ITVerbund> itVerbuende = model.getItverbuende();
		ITVerbund importITVerbund = null;

		for (ITVerbund v : itVerbuende)
			if (v.getSourceId() != null && v.getSourceId().equals(sourceId))
				importITVerbund = v;

		if (null == importITVerbund)
			try {
				importITVerbund = createNewItVerbund(model, sourceId);
			} catch (Exception e1) {
				throw new RuntimeCommandException(
						"Fehler beim Anlegen eines ITVerbundes");
			}

		Iterator iterator = syncDataElement.getChildren("syncObject",
				SyncNamespaceUtil.DATA_NS).iterator();

		while (iterator.hasNext()) {
			Element obj = (Element) iterator.next();

			String extId = obj.getAttributeValue("extId");
			// String extId = obj.getAttributeValue( "externalId" );
			String extObjectType = obj.getAttributeValue("extObjectType");
			boolean setAttributes = false;

			// we have to retreive the information from the mapping data,
			// which huientitytype corresponds with the current object's
			// external object type, given by extObjectType. Therefore, search
			// the element node with (object type)extId = obj.extObjectType from
			// syncMapping:

			Element mapObj = null;

			try {
				// XPath Expression:
				// //map:mapObjectType[@map:extId="extObjectType"]
				String xPathExpression = "//map:mapObjectType[@extId=\""
						+ extObjectType + "\"]";
				// String xPathExpression =
				// "//map:mapObjectType[@externalId=\""+extObjectType + "\"]";
				XPath xPath = XPath.newInstance(xPathExpression);
				xPath.addNamespace(SyncNamespaceUtil.DATA_NS);
				xPath.addNamespace(SyncNamespaceUtil.MAPPING_NS);
				xPath.addNamespace(SyncNamespaceUtil.SYNC_NS);
				mapObj = (Element) xPath.selectSingleNode(syncMappingElement);
			} catch (JDOMException e) {
				errorList
						.add("Konnte XPath-Ausdruck zur Selektion der Objekttypen nicht anwenden!");
				return;
			}

			if (null == mapObj) {
				// liefert fehler-response
				errorList
						.add("Konnte kein mapObjectType-Element finden für den Objekttypen "
								+ extObjectType);
				return;
			}

			// this element "knows", which huientitytype is applicable and
			// how the associated properties have to be mapped!
			String veriniceObjectType = mapObj.getAttributeValue("intId");

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
				CnATreeElement container = findContainerFor(importITVerbund,
						veriniceObjectType);

				try {
					// create new object in db...
					CreateElement<CnATreeElement> createElement = new CreateElement<CnATreeElement>(
							container, getClassFromTypeId(veriniceObjectType));
					ServiceFactory.lookupCommandService().executeCommand(
							createElement);
					elementInDB = createElement.getNewElement();

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
				Iterator attrIterator = obj.getChildren("syncAttribute",
						SyncNamespaceUtil.DATA_NS).iterator();

				while (attrIterator.hasNext()) {
					Element attr = (Element) attrIterator.next();
					String attrExtId = attr.getAttributeValue("name");
					String attrValue = attr.getAttributeValue("value");
					Element mapAttr = null;

					try // to find corresponding <mapAttributeType>-Element:
					{
						// XPath Expression:
						// //mapObjectType[@extId="extObjectType"]/mapAttributeType[@extId="attrExtId"]
						String xPathExpression = "//map:mapObjectType[@extId=\""
								+ extObjectType
								+ "\"]/map:mapAttributeType[@extId=\""
								+ attrExtId + "\"]";
						// String xPathExpression =
						// "//map:mapObjectType[@externalId=\""+extObjectType +
						// "\"]/map:mapAttributeType[@extId=\"" + attrExtId
						// +"\"]";
						XPath xPath = XPath.newInstance(xPathExpression);
						xPath.addNamespace(SyncNamespaceUtil.DATA_NS);
						xPath.addNamespace(SyncNamespaceUtil.MAPPING_NS);
						xPath.addNamespace(SyncNamespaceUtil.SYNC_NS);
						mapAttr = (Element) xPath
								.selectSingleNode(this.syncMappingElement);
					} catch (JDOMException e) {
						this.errorList
								.add("Konnte XPath-Ausdruck zur Selektion der Attribute nicht anwenden!");
					}

					if (null == mapAttr)
						this.errorList
								.add("Konnte kein mapAttributeType-Element finden für das Attribut "
										+ attrExtId + " von " + extObjectType);
					else {
						String attrIntId = mapAttr.getAttributeValue("intId");
						PropertyType propertyType = elementInDB.getEntityType()
								.getPropertyType(attrIntId);

						if (null != propertyType)
							elementInDB.setSimpleProperty(attrIntId, attrValue);
						// else: ignore this attribute!
					}

				} // for <syncAttribute>
			} // if( null != ... )
		} // for <syncObject>
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
		command = ServiceFactory.lookupCommandService().executeCommand(command);

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
	private CnATreeElement findContainerFor(ITVerbund verbund,
			String veriniceObjectType) {
		String containerType = containerTypes.get(veriniceObjectType);

		CnATreeElement container = verbund.getCategory(containerType);

		// TODO: passt das so?
		// teilweise gab's Probleme mit dem Objekt verbund.getCategory(),
		// welches
		// z.B. beim Einfügen in eine vorher bereits einmal "benutzte" Kategorie
		// nur
		// ein HibernateProxy war und nicht das "echte" Objekt?!
		if (container instanceof HibernateProxy)
			return (CnATreeElement) ((HibernateProxy) container)
					.getHibernateLazyInitializer().getImplementation();
		else
			return container;
	}

	/************************************************************
	 * createNewItVerbund()
	 * 
	 * Create a new ITVerbund in the given model.
	 * 
	 * @param model
	 * @param sourceId
	 * @return
	 * @throws Exception
	 ************************************************************/
	private ITVerbund createNewItVerbund(BSIModel model, String sourceId)
			throws Exception {
		// we can use the command, or the factory to build the ITVerbund
		// Using the factory is better, since it automatically sets the
		// model as parent etc.):

		CreateImportITVerbund command = new CreateImportITVerbund(model,
				ITVerbund.class, sourceId);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		CnATreeElement itverbund = command.getNewElement();

		return (ITVerbund) itverbund;
	}

	/************************************************************
	 * getClassFromTypeId()
	 * 
	 * @param typeId
	 * @return the corresponding Class
	 ************************************************************/
	private Class getClassFromTypeId(String typeId) {
		return typeIdClass.get(typeId);
	}
}
