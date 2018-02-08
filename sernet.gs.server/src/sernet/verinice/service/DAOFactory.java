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
package sernet.verinice.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.model.Gefaehrdung;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.ITypedElement;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.IAttachmentDao;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.interfaces.IElementEntityDao;
import sernet.verinice.interfaces.IFinishedRiskAnalysisListsDao;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpDocument;
import sernet.verinice.model.bp.elements.BpIncident;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.BpRecord;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpDocumentGroup;
import sernet.verinice.model.bp.groups.BpIncidentGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BpRecordGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.DeviceGroup;
import sernet.verinice.model.bp.groups.IcsSystemGroup;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.bsi.Addition;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.BausteinVorschlag;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Note;
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
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
import sernet.verinice.model.ds.Zweckbestimmung;
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
import sernet.verinice.model.iso27k.Exception;
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
import sernet.verinice.service.commands.UpdateElementEntity;

/**
 * Registry for DAOs for different types of objects. DAOs are managed by and injected by the Spring framework. 
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DAOFactory implements IDAOFactory {
	
	private final Logger log = Logger.getLogger(DAOFactory.class);
	
	
	/**
	 * Special Dao for use in command {@link UpdateElementEntity}
	 */
	private IElementEntityDao elementEntityDao;
	
	private IAttachmentDao attachmentDao;

	private IFinishedRiskAnalysisListsDao finishedRiskAnalysisListsDao;
	
	// injected by spring
	@SuppressWarnings("unchecked")
	private Map<Class, IBaseDao> daosByClass = new HashMap<Class, IBaseDao>(); 
	
	private Map<String, IBaseDao> daosByTypeID = new HashMap<String, IBaseDao>(); 
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	@Override
    public void setEntityDao(IBaseDao<Entity, Integer> entityDao) {
		daosByClass.put(Entity.class, entityDao);
		daosByTypeID.put(Entity.TYPE_ID, entityDao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	@Override
    public void setGefaehrdungDao(IBaseDao<Gefaehrdung, Integer> dao) {
		daosByClass.put(Gefaehrdung.class, dao);
		daosByTypeID.put(Gefaehrdung.TYPE_ID, dao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	@Override
    public void setBausteinVorschlagDao(IBaseDao<BausteinVorschlag, Integer> dao) {
		daosByClass.put(BausteinVorschlag.class, dao);
		daosByTypeID.put(BausteinVorschlag.TYPE_ID, dao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	@Override
    public void setConfigurationDao(IBaseDao<Gefaehrdung, Integer> dao) {
		daosByClass.put(Configuration.class, dao);
		daosByTypeID.put(Configuration.TYPE_ID, dao);
	}
	
	
	@Override
	public void setAccountGroupDao(IBaseDao<Gefaehrdung, Integer> dao){
	    daosByClass.put(AccountGroup.class, dao);
	    daosByTypeID.put(AccountGroup.TYPE_ID, dao);
	}

	@Override
    public void setchangeLogEntryDAO(IBaseDao<ChangeLogEntry, Integer> dao) {
		daosByClass.put(ChangeLogEntry.class, dao);
		daosByTypeID.put(ChangeLogEntry.TYPE_ID, dao);
	}
	
	
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	@Override
    public void setOwnGefaehrdungDao(IBaseDao<OwnGefaehrdung, Integer> dao) {
		daosByClass.put(OwnGefaehrdung.class, dao);
		daosByTypeID.put(OwnGefaehrdung.TYPE_ID, dao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	@Override
    public void setPropertyListDao(IBaseDao<PropertyList, Integer> propertyListDao) {
		daosByClass.put(PropertyList.class, propertyListDao);
		daosByTypeID.put(PropertyList.TYPE_ID, propertyListDao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	@Override
    public void setPropertyDao(IBaseDao<Property, Integer> propertyDao) {
		daosByClass.put(Property.class, propertyDao);
		daosByTypeID.put(Property.TYPE_ID, propertyDao);
	}

	/**
	 * Setter method used by spring to inject DAO.
	 */
	@Override
    public void setCnaLinkDao(IBaseDao<CnALink, Integer> dao) {
		daosByClass.put(CnALink.class, dao);
		daosByTypeID.put(CnALink.TYPE_ID, dao);
	}
	
	/**
     * Setter method used by spring to inject DAO.
     */
    public void setCnaTreeElementDao(IBaseDao<CnATreeElement, Integer> dao) {
        daosByClass.put(CnATreeElement.class, dao);
        daosByTypeID.put("cnatreeelement", dao);
    }
	
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setAnwendungDAO(IBaseDao<Anwendung, Integer> daoToSet) {
        daosByClass.put(Anwendung.class, daoToSet);
        daosByTypeID.put(Anwendung.TYPE_ID, daoToSet);
    }
    
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setAnwendungenKategorieDAO(IBaseDao<AnwendungenKategorie, Integer> daoToSet) {
    	daosByClass.put(AnwendungenKategorie.class, daoToSet);
    	daosByTypeID.put(AnwendungenKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setBausteinUmsetzungDAO(IBaseDao<BausteinUmsetzung, Integer> daoToSet) {
        daosByClass.put(BausteinUmsetzung.class, daoToSet);
        daosByTypeID.put(BausteinUmsetzung.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setBSIModelDAO(IBaseDao<BSIModel, Integer> daoToSet) {
        daosByClass.put(BSIModel.class, daoToSet);
        daosByTypeID.put(BSIModel.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setClientDAO(IBaseDao<Client, Integer> daoToSet) {
        daosByClass.put(Client.class, daoToSet);
        daosByTypeID.put(Client.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setClientsKategorieDAO(IBaseDao<ClientsKategorie, Integer> daoToSet) {
        daosByClass.put(ClientsKategorie.class, daoToSet);
        daosByTypeID.put(ClientsKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setDatenverarbeitungDAO(IBaseDao<Datenverarbeitung, Integer> daoToSet) {
        daosByClass.put(Datenverarbeitung.class, daoToSet);
        daosByTypeID.put(Datenverarbeitung.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setFinishedRiskAnalysisDAO(IBaseDao<FinishedRiskAnalysis, Integer> daoToSet) {
        daosByClass.put(FinishedRiskAnalysis.class, daoToSet);
        daosByTypeID.put(FinishedRiskAnalysis.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setGebaeudeDAO(IBaseDao<Gebaeude, Integer> daoToSet) {
        daosByClass.put(Gebaeude.class, daoToSet);
        daosByTypeID.put(Gebaeude.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setGebaeudeKategorieDAO(IBaseDao<GebaeudeKategorie, Integer> daoToSet) {
        daosByClass.put(GebaeudeKategorie.class, daoToSet);
        daosByTypeID.put(GebaeudeKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setGefaehrdungsUmsetzungDAO(IBaseDao<GefaehrdungsUmsetzung, Integer> daoToSet) {
        daosByClass.put(GefaehrdungsUmsetzung.class, daoToSet);
        daosByTypeID.put(GefaehrdungsUmsetzung.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setITVerbundDAO(IBaseDao<ITVerbund, Integer> daoToSet) {
        daosByClass.put(ITVerbund.class, daoToSet);
        daosByTypeID.put(ITVerbund.TYPE_ID, daoToSet);
    }

    /**
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setSecureItVerbundDAO(IBaseDao<ITVerbund, Integer> daoToSet) {
        daosByClass.put(ITVerbund.class, daoToSet);
        daosByTypeID.put(ITVerbund.SECURE_TYPE_ID, daoToSet);
    }


    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setMassnahmenUmsetzungDAO(IBaseDao<MassnahmenUmsetzung, Integer> daoToSet) {
        daosByClass.put(MassnahmenUmsetzung.class, daoToSet);
        daosByTypeID.put(MassnahmenUmsetzung.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setNetzKomponenteDAO(IBaseDao<NetzKomponente, Integer> daoToSet) {
        daosByClass.put(NetzKomponente.class, daoToSet);
        daosByTypeID.put(NetzKomponente.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setNKKategorieDAO(IBaseDao<NKKategorie, Integer> daoToSet) {
        daosByClass.put(NKKategorie.class, daoToSet);
        daosByTypeID.put(NKKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setPermissionDAO(IBaseDao<Permission, Integer> daoToSet) {
        daosByClass.put(Permission.class, daoToSet);
        daosByTypeID.put(Permission.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setPersonDAO(IBaseDao<Person, Integer> daoToSet) {
        daosByClass.put(Person.class, daoToSet);
        daosByTypeID.put(Person.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setPersonengruppenDAO(IBaseDao<Personengruppen, Integer> daoToSet) {
        daosByClass.put(Personengruppen.class, daoToSet);
        daosByTypeID.put(Personengruppen.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setPersonenKategorieDAO(IBaseDao<PersonenKategorie, Integer> daoToSet) {
        daosByClass.put(PersonenKategorie.class, daoToSet);
        daosByTypeID.put(PersonenKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setRaeumeKategorieDAO(IBaseDao<RaeumeKategorie, Integer> daoToSet) {
        daosByClass.put(RaeumeKategorie.class, daoToSet);
        daosByTypeID.put(RaeumeKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setRaumDAO(IBaseDao<Raum, Integer> daoToSet) {
        daosByClass.put(Raum.class, daoToSet);
        daosByTypeID.put(Raum.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setServerDAO(IBaseDao<Server, Integer> daoToSet) {
        daosByClass.put(Server.class, daoToSet);
        daosByTypeID.put(Server.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setServerKategorieDAO(IBaseDao<ServerKategorie, Integer> daoToSet) {
        daosByClass.put(ServerKategorie.class, daoToSet);
        daosByTypeID.put(ServerKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setSonstigeITKategorieDAO(IBaseDao<SonstigeITKategorie, Integer> daoToSet) {
        daosByClass.put(SonstigeITKategorie.class, daoToSet);
        daosByTypeID.put(SonstigeITKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setSonstITDAO(IBaseDao<SonstIT, Integer> daoToSet) {
        daosByClass.put(SonstIT.class, daoToSet);
        daosByTypeID.put(SonstIT.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setStellungnahmeDSBDAO(IBaseDao<StellungnahmeDSB, Integer> daoToSet) {
        daosByClass.put(StellungnahmeDSB.class, daoToSet);
        daosByTypeID.put(StellungnahmeDSB.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setTelefonKomponenteDAO(IBaseDao<TelefonKomponente, Integer> daoToSet) {
        daosByClass.put(TelefonKomponente.class, daoToSet);
        daosByTypeID.put(TelefonKomponente.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setTKKategorieDAO(IBaseDao<TKKategorie, Integer> daoToSet) {
        daosByClass.put(TKKategorie.class, daoToSet);
        daosByTypeID.put(TKKategorie.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setVerantwortlicheStelleDAO(IBaseDao<VerantwortlicheStelle, Integer> daoToSet) {
        daosByClass.put(VerantwortlicheStelle.class, daoToSet);
        daosByTypeID.put(VerantwortlicheStelle.TYPE_ID, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setVerarbeitungsangabenDAO(IBaseDao<Verarbeitungsangaben, Integer> daoToSet) {
        daosByClass.put(Verarbeitungsangaben.class, daoToSet);
        daosByTypeID.put(Verarbeitungsangaben.TYPE_ID, daoToSet);
    }

    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setZweckbestimmungDAO(IBaseDao<Zweckbestimmung, Integer> daoToSet) {
        daosByClass.put(Zweckbestimmung.class, daoToSet);
        daosByTypeID.put(Zweckbestimmung.TYPE_ID, daoToSet);
    }
	
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setRisikoMassnahmeDAO(IBaseDao<RisikoMassnahme, Integer> daoToSet) {
        daosByClass.put(RisikoMassnahme.class, daoToSet);
        daosByTypeID.put(RisikoMassnahme.TYPE_ID, daoToSet);
    }

    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setOwnGefaehrdungDAO(IBaseDao<OwnGefaehrdung, Integer> daoToSet) {
    	daosByClass.put(OwnGefaehrdung.class, daoToSet);
    	daosByTypeID.put(OwnGefaehrdung.TYPE_ID, daoToSet);
    }
    
    /** 
     * Setter method used by spring to inject DAO.
     */
    @Override
    public void setFinishedRiskAnalysisListsDAO(IBaseDao<FinishedRiskAnalysisLists, Integer> daoToSet) {
    	daosByClass.put(FinishedRiskAnalysisLists.class, daoToSet);
    	daosByTypeID.put(FinishedRiskAnalysisLists.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setNoteDAO(IBaseDao<Note, Integer> daoToSet) {
    	daosByClass.put(Note.class, daoToSet);
    	daosByTypeID.put(Note.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setAttachmentDAO(IBaseDao<Attachment, Integer> daoToSet) {
    	daosByClass.put(Attachment.class, daoToSet);
    	daosByTypeID.put(Attachment.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setAdditionDAO(IBaseDao<Addition, Integer> daoToSet) {
    	daosByClass.put(Addition.class, daoToSet);
    	daosByTypeID.put(Addition.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setAttachmentFileDAO(IBaseDao<AttachmentFile, Integer> daoToSet) {
    	daosByClass.put(AttachmentFile.class, daoToSet);
    	daosByTypeID.put(AttachmentFile.TYPE_ID, daoToSet);
    }
    
    /* ISO27000 Daos */
    
    @Override
    public void setISO27KModelDAO(IBaseDao<ISO27KModel, Integer> daoToSet) {
    	daosByClass.put(ISO27KModel.class, daoToSet);
    	daosByTypeID.put(ISO27KModel.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setOrganizationDAO(IBaseDao<Organization, Integer> daoToSet) {
    	daosByClass.put(Organization.class, daoToSet);
    	daosByTypeID.put(Organization.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setAssetGroupDAO(IBaseDao<AssetGroup, Integer> daoToSet) {
    	daosByClass.put(AssetGroup.class, daoToSet);
    	daosByTypeID.put(AssetGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setAssetDAO(IBaseDao<Asset, Integer> daoToSet) {
    	daosByClass.put(Asset.class, daoToSet);
    	daosByTypeID.put(Asset.TYPE_ID, daoToSet);
    }
    
    @Override 
    public void setUnsecureAssetDAO(IBaseDao<Asset, Integer> daoToSet){
        daosByTypeID.put(Asset.UNSECURE_TYPE_ID, daoToSet);
    }
    
    @Override 
    public void setUnsecureIncidentScenarioDAO(IBaseDao<IncidentScenario, Integer> daoToSet){
        daosByTypeID.put(IncidentScenario.UNSECURE_TYPE_ID, daoToSet);
    }
    
    @Override
    public void setControlGroupDAO(IBaseDao<ControlGroup, Integer> daoToSet) {
    	daosByClass.put(ControlGroup.class, daoToSet);
    	daosByTypeID.put(ControlGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setControlDAO(IBaseDao<Control, Integer> daoToSet) {
    	daosByClass.put(Control.class, daoToSet);
    	daosByTypeID.put(Control.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setAuditGroupDAO(IBaseDao<AuditGroup, Integer> daoToSet) {
    	daosByClass.put(AuditGroup.class, daoToSet);
    	daosByTypeID.put(AuditGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setAuditDAO(IBaseDao<Audit, Integer> daoToSet) {
    	daosByClass.put(Audit.class, daoToSet);
    	daosByTypeID.put(Audit.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setExceptionGroupDAO(IBaseDao<ExceptionGroup, Integer> daoToSet) {
    	daosByClass.put(ExceptionGroup.class, daoToSet);
    	daosByTypeID.put(ExceptionGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setExceptionDAO(IBaseDao<Exception, Integer> daoToSet) {
    	daosByClass.put(Exception.class, daoToSet);
    	daosByTypeID.put(Exception.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setPersonGroupDAO(IBaseDao<PersonGroup, Integer> daoToSet) {
    	daosByClass.put(PersonGroup.class, daoToSet);
    	daosByTypeID.put(PersonGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setPersonIsoDAO(IBaseDao<PersonIso, Integer> daoToSet) {
    	daosByClass.put(PersonIso.class, daoToSet);
    	daosByTypeID.put(PersonIso.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setRequirementGroupDAO(IBaseDao<RequirementGroup, Integer> daoToSet) {
    	daosByClass.put(RequirementGroup.class, daoToSet);
    	daosByTypeID.put(RequirementGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setRequirementDAO(IBaseDao<Requirement, Integer> daoToSet) {
    	daosByClass.put(Requirement.class, daoToSet);
    	daosByTypeID.put(Requirement.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setIncidentGroupDAO(IBaseDao<IncidentGroup, Integer> daoToSet) {
    	daosByClass.put(IncidentGroup.class, daoToSet);
    	daosByTypeID.put(IncidentGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setIncidentDAO(IBaseDao<Incident, Integer> daoToSet) {
    	daosByClass.put(Incident.class, daoToSet);
    	daosByTypeID.put(Incident.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setIncidentScenarioGroupDAO(IBaseDao<IncidentScenarioGroup, Integer> daoToSet) {
    	daosByClass.put(IncidentScenarioGroup.class, daoToSet);
    	daosByTypeID.put(IncidentScenarioGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setIncidentScenarioDAO(IBaseDao<IncidentScenario, Integer> daoToSet) {
    	daosByClass.put(IncidentScenario.class, daoToSet);
    	daosByTypeID.put(IncidentScenario.TYPE_ID, daoToSet);
    }
  
    @Override
    public void setResponseGroupDAO(IBaseDao<ResponseGroup, Integer> daoToSet) {
    	daosByClass.put(ResponseGroup.class, daoToSet);
    	daosByTypeID.put(ResponseGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setResponseDAO(IBaseDao<Response, Integer> daoToSet) {
    	daosByClass.put(Response.class, daoToSet);
    	daosByTypeID.put(Response.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setThreatGroupDAO(IBaseDao<ThreatGroup, Integer> daoToSet) {
    	daosByClass.put(ThreatGroup.class, daoToSet);
    	daosByTypeID.put(ThreatGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setThreatDAO(IBaseDao<Threat, Integer> daoToSet) {
    	daosByClass.put(Threat.class, daoToSet);
    	daosByTypeID.put(Threat.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setVulnerabilityGroupDAO(IBaseDao<VulnerabilityGroup, Integer> daoToSet) {
    	daosByClass.put(VulnerabilityGroup.class, daoToSet);
    	daosByTypeID.put(VulnerabilityGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setVulnerabilityDAO(IBaseDao<Vulnerability, Integer> daoToSet) {
    	daosByClass.put(Vulnerability.class, daoToSet);
    	daosByTypeID.put(Vulnerability.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setDocumentGroupDAO(IBaseDao<DocumentGroup, Integer> daoToSet) {
    	daosByClass.put(DocumentGroup.class, daoToSet);
    	daosByTypeID.put(DocumentGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setDocumentDAO(IBaseDao<Document, Integer> daoToSet) {
    	daosByClass.put(Document.class, daoToSet);
    	daosByTypeID.put(Document.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setEvidenceGroupDAO(IBaseDao<EvidenceGroup, Integer> daoToSet) {
    	daosByClass.put(EvidenceGroup.class, daoToSet);
    	daosByTypeID.put(EvidenceGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setEvidenceDAO(IBaseDao<Evidence, Integer> daoToSet) {
    	daosByClass.put(Evidence.class, daoToSet);
    	daosByTypeID.put(Evidence.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setInterviewGroupDAO(IBaseDao<InterviewGroup, Integer> daoToSet) {
    	daosByClass.put(InterviewGroup.class, daoToSet);
    	daosByTypeID.put(InterviewGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setInterviewDAO(IBaseDao<Interview, Integer> daoToSet) {
    	daosByClass.put(Interview.class, daoToSet);
    	daosByTypeID.put(Interview.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setFindingGroupDAO(IBaseDao<FindingGroup, Integer> daoToSet) {
    	daosByClass.put(FindingGroup.class, daoToSet);
    	daosByTypeID.put(FindingGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setFindingDAO(IBaseDao<Finding, Integer> daoToSet) {
    	daosByClass.put(Finding.class, daoToSet);
    	daosByTypeID.put(Finding.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setProcessGroupDAO(IBaseDao<ProcessGroup, Integer> daoToSet) {
    	daosByClass.put(ProcessGroup.class, daoToSet);
    	daosByTypeID.put(ProcessGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setProcessDAO(IBaseDao<sernet.verinice.model.iso27k.Process, Integer> daoToSet) {
    	daosByClass.put(sernet.verinice.model.iso27k.Process.class, daoToSet);
    	daosByTypeID.put(sernet.verinice.model.iso27k.Process.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setRecordGroupDAO(IBaseDao<RecordGroup, Integer> daoToSet) {
    	daosByClass.put(RecordGroup.class, daoToSet);
    	daosByTypeID.put(RecordGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setRecordDAO(IBaseDao<Record, Integer> daoToSet) {
    	daosByClass.put(Record.class, daoToSet);
    	daosByTypeID.put(Record.TYPE_ID, daoToSet);
    }
    
    /* Self Assessment (SAMT) Daos */
    
    @Override
    public void setSamtTopicDAO(IBaseDao<SamtTopic, Integer> daoToSet) {
        daosByClass.put(SamtTopic.class, daoToSet);
        daosByTypeID.put(SamtTopic.TYPE_ID, daoToSet);
    }
    
    /* Miscellaneous Daos */
    
    @Override
    public void setImportIsoDAO(IBaseDao<ImportIsoGroup, Integer> daoToSet) {
        daosByClass.put(ImportIsoGroup.class, daoToSet);
        daosByTypeID.put(ImportIsoGroup.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setImportBsiDAO(IBaseDao<ImportBsiGroup, Integer> daoToSet) {
        daosByClass.put(ImportBsiGroup.class, daoToSet);
        daosByTypeID.put(ImportBsiGroup.TYPE_ID, daoToSet);
    }
    
	/**
     * Returns a special Dao for use 
     * in command {@link UpdateElementEntity}
     * 
	 * @return a UpdateElementEntity Dao
	 */
	@Override
    public IElementEntityDao getElementEntityDao() {
        return elementEntityDao;
    }

    @Override
    public void setElementEntityDao(IElementEntityDao elementEntityDao) {
        this.elementEntityDao = elementEntityDao;
    }

    @Override
    public IAttachmentDao getAttachmentDao() {
        return attachmentDao;
    }

    @Override
    public void setAttachmentDao(IAttachmentDao attachmentDao) {
        this.attachmentDao = attachmentDao;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDAOFactory#getFinishedRiskAnalysisListsDao()
     */
    @Override
    public IFinishedRiskAnalysisListsDao getFinishedRiskAnalysisListsDao() {
        // TODO Auto-generated method stub
        return finishedRiskAnalysisListsDao;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDAOFactory#setFinishedRiskAnalysisListsDao(sernet.verinice.interfaces.IFinishedRiskAnalysisListsDao)
     */
    @Override
    public void setFinishedRiskAnalysisListsDao(IFinishedRiskAnalysisListsDao dao) {
        this.finishedRiskAnalysisListsDao = dao;
        
    }
    
    @Override
    @SuppressWarnings("unchecked")
	/**
	 *  Tries to find a DAO by class.
	 *  If you pass a proxy (class enhanced by cglib), this method tries to find
	 *  a DAO that works, but it still doesn't work when a proxied class is passed 
	 *  for a supertype of the actual type (i.e. CnaTreeElement for a Control).
	 *  
	 *  In short, when you're passing a Control.class this method will work.
	 *  When you're passing the result of control.getClass() it probably wont.
	 *  
	 *  Instead of this method, you should always use the getDAOforTypedElement() method when you 
	 *  want to get a DAO for an instantiated object.
	 */
	public <T> IBaseDao<T, Serializable> getDAO(Class<T> daotype) {
		IBaseDao dao = daosByClass.get(daotype);
		if (dao != null){
			return dao;
		}
		for (Class clazz : daosByClass.keySet()) {
			if (clazz.isAssignableFrom(daotype)){
				return daosByClass.get(clazz);
			}
		}
		
		if(daotype!=null) {
			log.error("No dao found for class: " + daotype.getName());
		} else {
			log.warn("dao-type-class is null, could not return dao");
		}
		return null;
	}
	

	
	@Override
    public IBaseDao getDAOforTypedElement(ITypedElement object) {
	    return daosByTypeID.get(object.getTypeId());
	}

    @Override
    public IBaseDao getDAO(String typeId) {
        return daosByTypeID.get(typeId);
    }

    @Override
    public void setImportBpDAO(IBaseDao<ImportBpGroup, Integer> daoToSet) {
        daosByClass.put(ImportBpGroup.class, daoToSet);
        daosByTypeID.put(ImportBpGroup.TYPE_ID, daoToSet);        
    }

    @Override
    public void setApplicationDAO(IBaseDao<Application, Integer> daoToSet) {
        daosByClass.put(Application.class, daoToSet);
        daosByTypeID.put(Application.TYPE_ID, daoToSet);        
    }

    @Override
    public void setBusinessProcessDAO(IBaseDao<BusinessProcess, Integer> daoToSet) {
        daosByClass.put(BusinessProcess.class, daoToSet);
        daosByTypeID.put(BusinessProcess.TYPE_ID, daoToSet);        
    }

    @Override
    public void setIcsSystemDAO(IBaseDao<IcsSystem, Integer> daoToSet) {
        daosByClass.put(IcsSystem.class, daoToSet);
        daosByTypeID.put(IcsSystem.TYPE_ID, daoToSet);        
    }

    @Override
    public void setItNetworkDAO(IBaseDao<ItNetwork, Integer> daoToSet) {
        daosByClass.put(ItNetwork.class, daoToSet);
        daosByTypeID.put(ItNetwork.TYPE_ID, daoToSet);        
    }

    @Override
    public void setItSystemDAO(IBaseDao<ItSystem, Integer> daoToSet) {
        daosByClass.put(ItSystem.class, daoToSet);
        daosByTypeID.put(ItSystem.TYPE_ID, daoToSet);    
    }

    @Override
    public void setBpPersonDAO(IBaseDao<BpPerson, Integer> daoToSet) {
        daosByClass.put(BpPerson.class, daoToSet);
        daosByTypeID.put(BpPerson.TYPE_ID, daoToSet);        
    }

    @Override
    public void setBpThreatDAO(IBaseDao<BpThreat, Integer> daoToSet) {
        daosByClass.put(BpThreat.class, daoToSet);
        daosByTypeID.put(BpThreat.TYPE_ID, daoToSet);        
        
    }

    @Override
    public void setBpRequirementDAO(IBaseDao<BpRequirement, Integer> daoToSet) {
        daosByClass.put(BpRequirement.class, daoToSet);
        daosByTypeID.put(BpRequirement.TYPE_ID, daoToSet);        
    }

    @Override
    public void setNetworkDAO(IBaseDao<Network, Integer> daoToSet) {
        daosByClass.put(Network.class, daoToSet);
        daosByTypeID.put(Network.TYPE_ID, daoToSet);        
    }

    @Override
    public void setDeviceDAO(IBaseDao<Device, Integer> daoToSet) {
        daosByClass.put(Device.class, daoToSet);
        daosByTypeID.put(Device.TYPE_ID, daoToSet);        
    }

    @Override
    public void setRoomDAO(IBaseDao<Room, Integer> daoToSet) {
        daosByClass.put(Room.class, daoToSet);
        daosByTypeID.put(Room.TYPE_ID, daoToSet);        
    }

    @Override
    public void setSafeguardDAO(IBaseDao<Safeguard, Integer> daoToSet) {
        daosByClass.put(Safeguard.class, daoToSet);
        daosByTypeID.put(Safeguard.TYPE_ID, daoToSet);        
    }

    @Override
    public void setBpModelDAO(IBaseDao<BpModel, Integer> daoToSet) {
        daosByClass.put(BpModel.class, daoToSet);
        daosByTypeID.put(BpModel.TYPE_ID, daoToSet);        
    }

    @Override
    public void setApplicationGroupDAO(IBaseDao<ApplicationGroup, Integer> daoToSet) {
        daosByClass.put(ApplicationGroup.class, daoToSet);
        daosByTypeID.put(ApplicationGroup.TYPE_ID, daoToSet);        
    }

    @Override
    public void setBpPersonGroupDAO(IBaseDao<BpPersonGroup, Integer> daoToSet) {
        daosByClass.put(BpPersonGroup.class, daoToSet);
        daosByTypeID.put(BpPersonGroup.TYPE_ID, daoToSet);
    }

    @Override
    public void setBpRequirementGroupDAO(IBaseDao<BpRequirementGroup, Integer> daoToSet) {
        daosByClass.put(BpRequirementGroup.class, daoToSet);
        daosByTypeID.put(BpRequirementGroup.TYPE_ID, daoToSet);
    }

    @Override
    public void setBpThreatGroupDAO(IBaseDao<BpThreatGroup, Integer> daoToSet) {
        daosByClass.put(BpThreatGroup.class, daoToSet);
        daosByTypeID.put(BpThreatGroup.TYPE_ID, daoToSet);
    }

    @Override
    public void setBusinessProcessGroupDAO(IBaseDao<BusinessProcessGroup, Integer> daoToSet) {
        daosByClass.put(BusinessProcessGroup.class, daoToSet);
        daosByTypeID.put(BusinessProcessGroup.TYPE_ID, daoToSet);        
    }

    @Override
    public void setIcsSystemGroupDAO(IBaseDao<IcsSystemGroup, Integer> daoToSet) {
        daosByClass.put(IcsSystemGroup.class, daoToSet);
        daosByTypeID.put(IcsSystemGroup.TYPE_ID, daoToSet);        
    }

    @Override
    public void setItSystemGroupDAO(IBaseDao<ItSystemGroup, Integer> daoToSet) {
        daosByClass.put(ItSystemGroup.class, daoToSet);
        daosByTypeID.put(ItSystemGroup.TYPE_ID, daoToSet);        
    }

    @Override
    public void setNetworkGroupDAO(IBaseDao<NetworkGroup, Integer> daoToSet) {
        daosByClass.put(NetworkGroup.class, daoToSet);
        daosByTypeID.put(NetworkGroup.TYPE_ID, daoToSet);        
    }

    @Override
    public void setDeviceGroupDAO(IBaseDao<DeviceGroup, Integer> daoToSet) {
        daosByClass.put(DeviceGroup.class, daoToSet);
        daosByTypeID.put(DeviceGroup.TYPE_ID, daoToSet);        
    }

    @Override
    public void setRoomGroupDAO(IBaseDao<RoomGroup, Integer> daoToSet) {
        daosByClass.put(RoomGroup.class, daoToSet);
        daosByTypeID.put(RoomGroup.TYPE_ID, daoToSet);        
    }

    @Override
    public void setSafeguardGroupDAO(IBaseDao<SafeguardGroup, Integer> daoToSet) {
        daosByClass.put(SafeguardGroup.class, daoToSet);
        daosByTypeID.put(SafeguardGroup.TYPE_ID, daoToSet);      
    }

    @Override
    public void setCatalogModelDAO(IBaseDao<CatalogModel, Integer> daoToSet) {
        daosByClass.put(CatalogModel.class, daoToSet);
        daosByTypeID.put(CatalogModel.TYPE_ID, daoToSet);
    }
    
    @Override
    public void setBpDocumentDAO(IBaseDao<BpDocument, Integer> daoToSet) {
        daosByClass.put(BpDocument.class, daoToSet);
        daosByTypeID.put(BpDocument.TYPE_ID, daoToSet);
        
    }
    @Override
    public void setBpDocumentGroupDAO(IBaseDao<BpDocumentGroup, Integer> daoToSet) {
        daosByClass.put(BpDocumentGroup.class, daoToSet);
        daosByTypeID.put(BpDocumentGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setBpIncidentDAO(IBaseDao<BpIncident, Integer> daoToSet) {
        daosByClass.put(BpIncident.class, daoToSet);
        daosByTypeID.put(BpIncident.TYPE_ID, daoToSet);
    }
    @Override
    public void setBpIncidentGroupDAO(IBaseDao<BpIncidentGroup, Integer> daoToSet) {
        daosByClass.put(BpIncidentGroup.class, daoToSet);
        daosByTypeID.put(BpIncidentGroup.TYPE_ID, daoToSet);
    }
    @Override
    public void setBpRecordDAO(IBaseDao<BpRecord, Integer> daoToSet) {
        daosByClass.put(BpRecord.class, daoToSet);
        daosByTypeID.put(BpRecord.TYPE_ID, daoToSet);
    }
    @Override
    public void setBpRecordGroupDAO(IBaseDao<BpRecordGroup, Integer> daoToSet) {
        daosByClass.put(BpRecordGroup.class, daoToSet);
        daosByTypeID.put(BpRecordGroup.TYPE_ID, daoToSet);
    }

}
