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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.CnATypeMapper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByExternalID;
import sernet.gs.ui.rcp.main.sync.InvalidRequestException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
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
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.iso27k.VulnerabilityGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.LoadBSIModel;
import sernet.verinice.service.iso27k.LoadImportObjectsHolder;
import sernet.verinice.service.iso27k.LoadModel;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncLink;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.data.SyncObject.SyncAttribute;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;

@SuppressWarnings("serial")
public class SyncInsertUpdateCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(SyncInsertUpdateCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SyncInsertUpdateCommand.class);
        }
        return log;
    }
    
    private static HashMap<String, String> containerTypes = new HashMap<String, String>();
  

    private String sourceId;
    private SyncMapping syncMapping;
    private SyncData syncData;
    private String userName;

	private boolean insert, update;
    private List<String> errorList;

    private int inserted = 0, updated = 0;

    private Map<Class,CnATreeElement> containerMap = new HashMap<Class,CnATreeElement>(2);

    private Set<CnATreeElement> elementSet = new HashSet<CnATreeElement>();
    
    private transient Map<String, CnATreeElement> idElementMap = new HashMap<String, CnATreeElement>();
    
    public int getUpdated() {
        return updated;
    }

    public int getInserted() {
        return inserted;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public SyncInsertUpdateCommand(
    		String sourceId, 
    		SyncData syncData, 
    		SyncMapping syncMapping,
    		String userName,
    		boolean insert, 
    		boolean update, 
    		List<String> errorList) {
        this.sourceId = sourceId;
        this.syncData = syncData;
        this.syncMapping = syncMapping;
        this.userName = userName;
        this.insert = insert;
        this.update = update;
        this.errorList = errorList;
    }

    /**
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
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    public void execute() {
        for (SyncObject so : syncData.getSyncObject()) {
            importObject(null, so);
        } // for <syncObject>
        for (SyncLink syncLink : syncData.getSyncLink()) {
            importLink(syncLink);
        }
    }

    private void importObject(CnATreeElement parent, SyncObject so) {
        String extId = so.getExtId();
        String extObjectType = so.getExtObjectType();
        if (getLog().isDebugEnabled()) {
            getLog().debug("Importing element type: " + extObjectType + ", extId: " + extId + "...");
        }
        
        boolean setAttributes = false;

        MapObjectType mot = getMap(extObjectType);

        if (mot == null) {
            final String message = "Could not find mapObjectType-Element for XML type: " + extObjectType;
            getLog().error(message);
            errorList.add(message);
            return;
        }

        // this element "knows", which huientitytype is applicable and
        // how the associated properties have to be mapped!
        String veriniceObjectType = mot.getIntId();

        CnATreeElement elementInDB = findDbElement(sourceId, extId);
        if (elementInDB != null) {
            if (update) {
                /*** UPDATE: ***/
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element found in db: updating, uuid: " + elementInDB.getUuid());
                }
                setAttributes = true;
                updated++;
            } else {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element found in db, update disabled, uuid: " + elementInDB.getUuid());
                }
                // do not update this object's attributes!
                setAttributes = false;
            }
        }
        
        // If no previous object was found in the database and the 'insert'
        // flag is given, create a new object.
        if (elementInDB == null && insert) {
            // Each new object needs a parent. The top-level element(s) in the
            // import set might not automatically have one. For those objects it is
            // neccessary to use the 'import root object' instead.
            CnATypeMapper typeMapper = new CnATypeMapper();
            Class clazz = typeMapper.getClassFromTypeId(veriniceObjectType);
            parent = (parent == null) ? accessContainer(clazz) : parent;

            try {
                // create new object in db...
                CreateElement<CnATreeElement> newElement = new CreateElement<CnATreeElement>(parent, clazz, true, false);
                newElement = getCommandService().executeCommand(newElement);
                elementInDB = newElement.getNewElement();

                // ...and set its sourceId and extId:
                elementInDB.setSourceId(sourceId);
                elementInDB.setExtId(extId);
                
                if(elementInDB instanceof Organization || elementInDB instanceof ITVerbund) {
                    addElement(elementInDB);
                }
                
                setAttributes = true;
                inserted++;
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element inserted, uuid: " + elementInDB.getUuid());
                }
            } catch (Exception e) {
                getLog().error("Error while inserting element, type: " + extObjectType + ", extId: " + extId, e);
                errorList.add("Konnte " + veriniceObjectType + "-Objekt nicht erzeugen.");
            }
        }

        /*
         * Now if we should update an existing object or created a new object,
         * set the associated attributes:
         */
        if (null != elementInDB && setAttributes) {
            // for all <syncAttribute>-Elements below current
            // <syncObject>...
            HUITypeFactory huiTypeFactory = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
            for (SyncAttribute sa : so.getSyncAttribute()) {
                String attrExtId = sa.getName();
                List<String> attrValues = sa.getValue();

                MapAttributeType mat = getMapAttribute(mot, attrExtId);

                if (mat == null) {
                    final String message = "Could not find mapObjectType-Element for XML attribute type: " + attrExtId + " of type: " + extObjectType;
                    getLog().error(message);
                    this.errorList.add(message);
                } else {
                    String attrIntId = mat.getIntId();
                    elementInDB.getEntity().importProperties(huiTypeFactory,attrIntId, attrValues);
                    addElement(elementInDB);
                }

            } // for <syncAttribute>
            IBaseDao<CnATreeElement, Serializable> dao = (IBaseDao<CnATreeElement, Serializable>) getDaoFactory().getDAO(elementInDB.getTypeId());
            elementInDB = dao.merge(elementInDB);
        } // if( null != ... )

        idElementMap.put(elementInDB.getExtId(), elementInDB);
        
        // Handle all the child objects.
        for (SyncObject child : so.getChildren()) {
            // The object that was created or modified during the course of
            // this method call is the parent for the import of the
            // child elements.
            if (getLog().isDebugEnabled() && child!=null) {
                getLog().debug("Child found, type: " + child.getExtObjectType() + ", extId: " + child.getExtId());
            }
            importObject(elementInDB, child);
        }
    }
    
    /**
     * @param syncLink
     * @throws CommandException 
     */
    private void importLink(SyncLink syncLink) {
        String dependantId = syncLink.getDependant();
        String dependencyId = syncLink.getDependency();
        CnATreeElement dependant = idElementMap.get(dependantId);
        if(dependant==null) {     
        	dependant = findDbElement(this.sourceId, dependantId);
            if(dependant==null) {
                getLog().error("Can not import link. dependant not found in xml file and db, dependant ext-id: " + dependantId + " dependency ext-id: " + dependencyId);
                return;
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("dependant not found in XML file but in db, ext-id: " + dependantId);
            }
        }
        CnATreeElement dependency = idElementMap.get(dependencyId);
        if(dependency==null) { 
        	dependency = findDbElement(this.sourceId, dependencyId);
            if(dependency==null) {
                getLog().error("Can not import link. dependency not found in xml file and db, dependency ext-id: " + dependencyId + " dependant ext-id: " + dependantId);
                return;
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("dependency not found in XML file but in db, ext-id: " + dependencyId);
            }
        }
        
        CnALink link = new CnALink(dependant,dependency,syncLink.getRelationId(),syncLink.getComment());
        
        String titleDependant = "unknown";
    	String titleDependency = "unknown";
    	if (getLog().isDebugEnabled()) {     	
			try { 
				titleDependant = dependant.getTitle();
				titleDependency = dependency.getTitle();
			} catch(Exception e) {
				getLog().debug("Error while reading title.", e);
			}
    	}
    	
        if(isNew(link)) {
	        dependant.addLinkDown(link);
	        dependency.addLinkUp(link);
	        if (getLog().isDebugEnabled()) {
	        	getLog().debug("Creating new link from: " + titleDependant + " to: " + titleDependency + "...");
			}
	        getDaoFactory().getDAO(CnALink.class).saveOrUpdate(link);
        } else if (getLog().isDebugEnabled()) {
        	getLog().debug("Link exists: " + titleDependant + " to: " + titleDependency);
		}
        
    }

    private boolean isNew(CnALink link) {
    	String hql = "from CnALink as link where link.id.dependantId=? and link.id.dependencyId=? and (link.id.typeId=? or link.id.typeId=?)";
    	String relationId = link.getRelationId();
    	String relationId2 = relationId;
    	if(CnALink.Id.NO_TYPE.equals(relationId)) {
    		relationId2 = "";
    	}
    	if(relationId!=null && relationId.isEmpty()) {
    		relationId2 = CnALink.Id.NO_TYPE;
    	}
    	Object[] paramArray = new Object[]{
    			link.getDependant().getDbId(),
    			link.getDependency().getDbId(),
    			relationId,
    			relationId2};
    	List result = getDaoFactory().getDAO(CnALink.class).findByQuery(hql, paramArray); 	
		return result==null || result.isEmpty();
	}

	private MapObjectType getMap(String extObjectType) {
        for (MapObjectType mot : syncMapping.getMapObjectType()) {
            if (extObjectType.equals(mot.getExtId())) {
                return mot;
            }
        }

        return null;
    }

    private SyncMapping.MapObjectType.MapAttributeType getMapAttribute(MapObjectType mot, String extObjectType) {
        for (SyncMapping.MapObjectType.MapAttributeType mat : mot.getMapAttributeType()) {
            if (extObjectType.equals(mat.getExtId())) {
                return mat;
            }
        }

        return null;
    }

    /**
     * Query element (by externalID) from DB, which has been previously
     * synchronized from the given sourceID.
     * 
     * @param sourceId
     * @param externalId
     * @return the CnATreeElement from the query or null, if nothing was found
     * @throws RuntimeException if more than one element is found
     */
    private CnATreeElement findDbElement(String sourceId, String externalId) {
    	CnATreeElement result = null;
    	// use a new crudCommand (load by external, source id):
        LoadCnAElementByExternalID command = new LoadCnAElementByExternalID(sourceId, externalId);
        try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			final String message = "Error while loading element by source and externeal id";
    		log.error(message,e);
			throw new RuntimeCommandException(message,e);
		}
        List<CnATreeElement> foundElements = command.getElements();
        if (foundElements != null) {
        	if(foundElements.size()==1) {  
        		result = foundElements.get(0);
        	}
        	if(foundElements.size()>1) {  
        		final String message = "Found more than one element with source-id: " + sourceId + " and externeal-id: " + externalId;
        		log.error(message);
    			throw new RuntimeCommandException(message);
        	}
        }
        return result;
    }

    /**
     * Find appropriate Category within the tree for a given object type.
     * 
     * @param verbund
     * @param veriniceObjectType
     * @return the Category - CnATreeElement
     */
    private CnATreeElement findContainerFor(CnATreeElement root, String veriniceObjectType) {

        // If in doubt the root for imported objects should always be used.
        return root;
    }

   

    /**
     * If during the import action an object has to be created for which no
     * parent is available (or can be found) the artificial 'rootImportObject'
     * should be used.
     * 
     * <p>
     * This method should <em>onl</em> be called when the 'rootImportObject' is
     * definitely needed and going to be used because the root object is not
     * only created but also automatically persisted in the database. If it were
     * not used later on the user would see an object node in the object tree.
     * </p>
     * @param clazz 
     * 
     * @return
     */
    private CnATreeElement accessContainer(Class clazz) {
        // Create the importRootObject if it does not exist yet
        // and set the 'importRootObject' variable.
    	CnATreeElement container = containerMap.get(clazz);
        if (container==null) {
            LoadImportObjectsHolder cmdLoadContainer = new LoadImportObjectsHolder(clazz);
            try {
                cmdLoadContainer = ServiceFactory.lookupCommandService().executeCommand(cmdLoadContainer);
            } catch (CommandException e) {
                errorList.add("Fehler beim Ausführen von LoadBSIModel.");
                throw new RuntimeCommandException("Fehler beim Anlegen des Behälters für importierte Objekte.");
            }
            container = cmdLoadContainer.getHolder();
            if(container==null) {
                container = createContainer(clazz);
            }    
            // load the parent
            container.getParent().getTitle();
            containerMap.put(clazz,container);
        } 
        return container;
    }

    private CnATreeElement createContainer(Class clazz) {
        if(LoadImportObjectsHolder.isImplementation(clazz, IBSIStrukturElement.class)) {
            return createBsiContainer();
        } else {
            return createIsoContainer();
        }     
    }
    
    private CnATreeElement createBsiContainer() {
        LoadBSIModel cmdLoadModel = new LoadBSIModel();
        try {
            cmdLoadModel = ServiceFactory.lookupCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
            errorList.add("Fehler beim Ausführen von LoadBSIModel.");
            throw new RuntimeCommandException("Fehler beim Anlegen des Behaelters für importierte Objekte.");
        }
        BSIModel model = cmdLoadModel.getModel();
        ImportBsiGroup holder = null;
        try {
            holder = new ImportBsiGroup(model);
            addPermissions(holder);
            getDaoFactory().getDAO(ImportBsiGroup.class).saveOrUpdate(holder);
        } catch (Exception e1) {
            throw new RuntimeCommandException("Fehler beim Anlegen des Behaelters für importierte Objekte.");
        }
        return holder;
    }
    
    private CnATreeElement createIsoContainer() {
        LoadModel cmdLoadModel = new LoadModel();
        try {
            cmdLoadModel = ServiceFactory.lookupCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
            errorList.add("Fehler beim Ausführen von LoadBSIModel.");
            throw new RuntimeCommandException("Fehler beim Anlegen des Behaelters für importierte Objekte.");
        }
        ISO27KModel model = cmdLoadModel.getModel();
        ImportIsoGroup holder = null;
        try {
            holder = new ImportIsoGroup(model);
            addPermissions(holder);
            getDaoFactory().getDAO(ImportIsoGroup.class).saveOrUpdate(holder);
        } catch (Exception e1) {
            throw new RuntimeCommandException("Fehler beim Anlegen des Behälters für importierte Objekte.");
        }
        return holder;
    }
    
    private void addPermissions(CnATreeElement element) {
        // We use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new Organization.
        HashSet<Permission> auditPerms = new HashSet<Permission>();
        auditPerms.add(Permission.createPermission(element, getUserName(), true, true));
        element.setPermissions(auditPerms);
    }
    
    protected void addElement(CnATreeElement element) {
        if(elementSet==null) {
            elementSet = new HashSet<CnATreeElement>();
        }
        // load the parent
        element.getParent().getTitle();
        elementSet.add(element);
    }

    public Map<Class, CnATreeElement> getContainerMap() {
		return containerMap;
	}

	public Set<CnATreeElement> getElementSet() {
        return elementSet;
    }

    protected String getUserName() {
		return userName;
	}

	protected void setUserName(String userName) {
		this.userName = userName;
	}

}
