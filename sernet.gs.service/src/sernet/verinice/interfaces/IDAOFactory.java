/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.io.Serializable;

import sernet.gs.model.Gefaehrdung;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.ITypedElement;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
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
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.Permission;
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

public interface IDAOFactory {

	/**
	 * Setter method used by spring to inject DAO.
	 */
	void setEntityDao(IBaseDao<Entity, Integer> entityDao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	void setGefaehrdungDao(IBaseDao<Gefaehrdung, Integer> dao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	void setBausteinVorschlagDao(
			IBaseDao<BausteinVorschlag, Integer> dao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	void setConfigurationDao(IBaseDao<Gefaehrdung, Integer> dao);

	void setchangeLogEntryDAO(
			IBaseDao<ChangeLogEntry, Integer> dao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	void setOwnGefaehrdungDao(
			IBaseDao<OwnGefaehrdung, Integer> dao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	void setPropertyListDao(
			IBaseDao<PropertyList, Integer> propertyListDao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	void setPropertyDao(IBaseDao<Property, Integer> propertyDao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	void setCnaLinkDao(IBaseDao<CnALink, Integer> dao);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setAnwendungDAO(IBaseDao<Anwendung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setAnwendungenKategorieDAO(
			IBaseDao<AnwendungenKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setBausteinUmsetzungDAO(
			IBaseDao<BausteinUmsetzung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setBSIModelDAO(IBaseDao<BSIModel, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setClientDAO(IBaseDao<Client, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setClientsKategorieDAO(
			IBaseDao<ClientsKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setDatenverarbeitungDAO(
			IBaseDao<Datenverarbeitung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setFinishedRiskAnalysisDAO(
			IBaseDao<FinishedRiskAnalysis, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setGebaeudeDAO(IBaseDao<Gebaeude, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setGebaeudeKategorieDAO(
			IBaseDao<GebaeudeKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setGefaehrdungsUmsetzungDAO(
			IBaseDao<GefaehrdungsUmsetzung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setITVerbundDAO(IBaseDao<ITVerbund, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setMassnahmenUmsetzungDAO(
			IBaseDao<MassnahmenUmsetzung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setNetzKomponenteDAO(
			IBaseDao<NetzKomponente, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setNKKategorieDAO(
			IBaseDao<NKKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setPermissionDAO(IBaseDao<Permission, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setPersonDAO(IBaseDao<Person, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setPersonengruppenDAO(
			IBaseDao<Personengruppen, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setPersonenKategorieDAO(
			IBaseDao<PersonenKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setRaeumeKategorieDAO(
			IBaseDao<RaeumeKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setRaumDAO(IBaseDao<Raum, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setServerDAO(IBaseDao<Server, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setServerKategorieDAO(
			IBaseDao<ServerKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setSonstigeITKategorieDAO(
			IBaseDao<SonstigeITKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setSonstITDAO(IBaseDao<SonstIT, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setStellungnahmeDSBDAO(
			IBaseDao<StellungnahmeDSB, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setTelefonKomponenteDAO(
			IBaseDao<TelefonKomponente, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setTKKategorieDAO(
			IBaseDao<TKKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setVerantwortlicheStelleDAO(
			IBaseDao<VerantwortlicheStelle, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setVerarbeitungsangabenDAO(
			IBaseDao<Verarbeitungsangaben, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setZweckbestimmungDAO(
			IBaseDao<Zweckbestimmung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setRisikoMassnahmeDAO(
			IBaseDao<RisikoMassnahme, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setOwnGefaehrdungDAO(
			IBaseDao<OwnGefaehrdung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	void setFinishedRiskAnalysisListsDAO(
			IBaseDao<FinishedRiskAnalysisLists, Integer> daoToSet);

	void setNoteDAO(IBaseDao<Note, Integer> daoToSet);

	void setAttachmentDAO(IBaseDao<Attachment, Integer> daoToSet);

	void setAdditionDAO(IBaseDao<Addition, Integer> daoToSet);

	void setAttachmentFileDAO(
			IBaseDao<AttachmentFile, Integer> daoToSet);

	void setISO27KModelDAO(
			IBaseDao<ISO27KModel, Integer> daoToSet);

	void setOrganizationDAO(
			IBaseDao<Organization, Integer> daoToSet);

	void setAssetGroupDAO(IBaseDao<AssetGroup, Integer> daoToSet);

	void setAssetDAO(IBaseDao<Asset, Integer> daoToSet);

	void setControlGroupDAO(
			IBaseDao<ControlGroup, Integer> daoToSet);

	void setControlDAO(IBaseDao<Control, Integer> daoToSet);

	void setAuditGroupDAO(IBaseDao<AuditGroup, Integer> daoToSet);

	void setAuditDAO(IBaseDao<Audit, Integer> daoToSet);

	void setExceptionGroupDAO(
			IBaseDao<ExceptionGroup, Integer> daoToSet);

	void setExceptionDAO(IBaseDao<Exception, Integer> daoToSet);

	void setPersonGroupDAO(
			IBaseDao<PersonGroup, Integer> daoToSet);

	void setPersonIsoDAO(IBaseDao<PersonIso, Integer> daoToSet);

	void setRequirementGroupDAO(
			IBaseDao<RequirementGroup, Integer> daoToSet);

	void setRequirementDAO(
			IBaseDao<Requirement, Integer> daoToSet);

	void setIncidentGroupDAO(
			IBaseDao<IncidentGroup, Integer> daoToSet);

	void setIncidentDAO(IBaseDao<Incident, Integer> daoToSet);

	void setIncidentScenarioGroupDAO(
			IBaseDao<IncidentScenarioGroup, Integer> daoToSet);

	void setIncidentScenarioDAO(
			IBaseDao<IncidentScenario, Integer> daoToSet);

	void setResponseGroupDAO(
			IBaseDao<ResponseGroup, Integer> daoToSet);

	void setResponseDAO(IBaseDao<Response, Integer> daoToSet);

	void setThreatGroupDAO(
			IBaseDao<ThreatGroup, Integer> daoToSet);

	void setThreatDAO(IBaseDao<Threat, Integer> daoToSet);

	void setVulnerabilityGroupDAO(
			IBaseDao<VulnerabilityGroup, Integer> daoToSet);

	void setVulnerabilityDAO(
			IBaseDao<Vulnerability, Integer> daoToSet);

	void setDocumentGroupDAO(
			IBaseDao<DocumentGroup, Integer> daoToSet);

	void setDocumentDAO(IBaseDao<Document, Integer> daoToSet);

	void setEvidenceGroupDAO(
			IBaseDao<EvidenceGroup, Integer> daoToSet);

	void setEvidenceDAO(IBaseDao<Evidence, Integer> daoToSet);

	void setInterviewGroupDAO(
			IBaseDao<InterviewGroup, Integer> daoToSet);

	void setInterviewDAO(IBaseDao<Interview, Integer> daoToSet);

	void setFindingGroupDAO(
			IBaseDao<FindingGroup, Integer> daoToSet);

	void setFindingDAO(IBaseDao<Finding, Integer> daoToSet);

	void setProcessGroupDAO(
			IBaseDao<ProcessGroup, Integer> daoToSet);

	void setProcessDAO(
			IBaseDao<sernet.verinice.model.iso27k.Process, Integer> daoToSet);

	void setRecordGroupDAO(
			IBaseDao<RecordGroup, Integer> daoToSet);

	void setRecordDAO(IBaseDao<Record, Integer> daoToSet);

	void setSamtTopicDAO(IBaseDao<SamtTopic, Integer> daoToSet);

	void setImportIsoDAO(IBaseDao<SamtTopic, Integer> daoToSet);
	
	void setImportBsiDAO(IBaseDao<SamtTopic, Integer> daoToSet);

	/**
     * Returns a special Dao for use 
     * in command {@link UpdateElementEntity}
     * 
     * @return a UpdateElementEntity Dao
     */
    IElementEntityDao getElementEntityDao();

    void setElementEntityDao(IElementEntityDao elementEntityDao);
	
    /**
     * @return
     */
    IAttachmentDao getAttachmentDao();
    
    void setAttachmentDao(IAttachmentDao attachmentDao);
    
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
	<T> IBaseDao<T, Serializable> getDAO(Class<T> daotype);

	IBaseDao getDAOforTypedElement(ITypedElement object);

	/**
	 * @param typeId
	 * @return
	 */
	IBaseDao getDAO(String typeId);

}