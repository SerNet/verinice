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
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
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
	public abstract void setEntityDao(IBaseDao<Entity, Integer> entityDao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setGefaehrdungDao(IBaseDao<Gefaehrdung, Integer> dao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setBausteinVorschlagDao(
			IBaseDao<BausteinVorschlag, Integer> dao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setConfigurationDao(IBaseDao<Gefaehrdung, Integer> dao);

	public abstract void setchangeLogEntryDAO(
			IBaseDao<ChangeLogEntry, Integer> dao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setOwnGefaehrdungDao(
			IBaseDao<OwnGefaehrdung, Integer> dao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setPropertyListDao(
			IBaseDao<PropertyList, Integer> propertyListDao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setPropertyDao(IBaseDao<Property, Integer> propertyDao);

	/**
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setCnaLinkDao(IBaseDao<CnALink, Integer> dao);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setAnwendungDAO(IBaseDao<Anwendung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setAnwendungenKategorieDAO(
			IBaseDao<AnwendungenKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setBausteinUmsetzungDAO(
			IBaseDao<BausteinUmsetzung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setBSIModelDAO(IBaseDao<BSIModel, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setClientDAO(IBaseDao<Client, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setClientsKategorieDAO(
			IBaseDao<ClientsKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setDatenverarbeitungDAO(
			IBaseDao<Datenverarbeitung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setFinishedRiskAnalysisDAO(
			IBaseDao<FinishedRiskAnalysis, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setGebaeudeDAO(IBaseDao<Gebaeude, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setGebaeudeKategorieDAO(
			IBaseDao<GebaeudeKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setGefaehrdungsUmsetzungDAO(
			IBaseDao<GefaehrdungsUmsetzung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setITVerbundDAO(IBaseDao<ITVerbund, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setMassnahmenUmsetzungDAO(
			IBaseDao<MassnahmenUmsetzung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setNetzKomponenteDAO(
			IBaseDao<NetzKomponente, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setNKKategorieDAO(
			IBaseDao<NKKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setPermissionDAO(IBaseDao<Permission, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setPersonDAO(IBaseDao<Person, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setPersonengruppenDAO(
			IBaseDao<Personengruppen, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setPersonenKategorieDAO(
			IBaseDao<PersonenKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setRaeumeKategorieDAO(
			IBaseDao<RaeumeKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setRaumDAO(IBaseDao<Raum, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setServerDAO(IBaseDao<Server, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setServerKategorieDAO(
			IBaseDao<ServerKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setSonstigeITKategorieDAO(
			IBaseDao<SonstigeITKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setSonstITDAO(IBaseDao<SonstIT, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setStellungnahmeDSBDAO(
			IBaseDao<StellungnahmeDSB, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setTelefonKomponenteDAO(
			IBaseDao<TelefonKomponente, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setTKKategorieDAO(
			IBaseDao<TKKategorie, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setVerantwortlicheStelleDAO(
			IBaseDao<VerantwortlicheStelle, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setVerarbeitungsangabenDAO(
			IBaseDao<Verarbeitungsangaben, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setZweckbestimmungDAO(
			IBaseDao<Zweckbestimmung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setRisikoMassnahmeDAO(
			IBaseDao<RisikoMassnahme, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setOwnGefaehrdungDAO(
			IBaseDao<OwnGefaehrdung, Integer> daoToSet);

	/** 
	 * Setter method used by spring to inject DAO.
	 */
	public abstract void setFinishedRiskAnalysisListsDAO(
			IBaseDao<FinishedRiskAnalysisLists, Integer> daoToSet);

	public abstract void setNoteDAO(IBaseDao<Note, Integer> daoToSet);

	public abstract void setAttachmentDAO(IBaseDao<Attachment, Integer> daoToSet);

	public abstract void setAdditionDAO(IBaseDao<Addition, Integer> daoToSet);

	public abstract void setAttachmentFileDAO(
			IBaseDao<AttachmentFile, Integer> daoToSet);

	public abstract void setISO27KModelDAO(
			IBaseDao<ISO27KModel, Integer> daoToSet);

	public abstract void setOrganizationDAO(
			IBaseDao<Organization, Integer> daoToSet);

	public abstract void setAssetGroupDAO(IBaseDao<AssetGroup, Integer> daoToSet);

	public abstract void setAssetDAO(IBaseDao<Asset, Integer> daoToSet);

	public abstract void setControlGroupDAO(
			IBaseDao<ControlGroup, Integer> daoToSet);

	public abstract void setControlDAO(IBaseDao<Control, Integer> daoToSet);

	public abstract void setAuditGroupDAO(IBaseDao<AuditGroup, Integer> daoToSet);

	public abstract void setAuditDAO(IBaseDao<Audit, Integer> daoToSet);

	public abstract void setExceptionGroupDAO(
			IBaseDao<ExceptionGroup, Integer> daoToSet);

	public abstract void setExceptionDAO(IBaseDao<Exception, Integer> daoToSet);

	public abstract void setPersonGroupDAO(
			IBaseDao<PersonGroup, Integer> daoToSet);

	public abstract void setPersonIsoDAO(IBaseDao<PersonIso, Integer> daoToSet);

	public abstract void setRequirementGroupDAO(
			IBaseDao<RequirementGroup, Integer> daoToSet);

	public abstract void setRequirementDAO(
			IBaseDao<Requirement, Integer> daoToSet);

	public abstract void setIncidentGroupDAO(
			IBaseDao<IncidentGroup, Integer> daoToSet);

	public abstract void setIncidentDAO(IBaseDao<Incident, Integer> daoToSet);

	public abstract void setIncidentScenarioGroupDAO(
			IBaseDao<IncidentScenarioGroup, Integer> daoToSet);

	public abstract void setIncidentScenarioDAO(
			IBaseDao<IncidentScenario, Integer> daoToSet);

	public abstract void setResponseGroupDAO(
			IBaseDao<ResponseGroup, Integer> daoToSet);

	public abstract void setResponseDAO(IBaseDao<Response, Integer> daoToSet);

	public abstract void setThreatGroupDAO(
			IBaseDao<ThreatGroup, Integer> daoToSet);

	public abstract void setThreatDAO(IBaseDao<Threat, Integer> daoToSet);

	public abstract void setVulnerabilityGroupDAO(
			IBaseDao<VulnerabilityGroup, Integer> daoToSet);

	public abstract void setVulnerabilityDAO(
			IBaseDao<Vulnerability, Integer> daoToSet);

	public abstract void setDocumentGroupDAO(
			IBaseDao<DocumentGroup, Integer> daoToSet);

	public abstract void setDocumentDAO(IBaseDao<Document, Integer> daoToSet);

	public abstract void setEvidenceGroupDAO(
			IBaseDao<EvidenceGroup, Integer> daoToSet);

	public abstract void setEvidenceDAO(IBaseDao<Evidence, Integer> daoToSet);

	public abstract void setInterviewGroupDAO(
			IBaseDao<InterviewGroup, Integer> daoToSet);

	public abstract void setInterviewDAO(IBaseDao<Interview, Integer> daoToSet);

	public abstract void setFindingGroupDAO(
			IBaseDao<FindingGroup, Integer> daoToSet);

	public abstract void setFindingDAO(IBaseDao<Finding, Integer> daoToSet);

	public abstract void setProcessGroupDAO(
			IBaseDao<ProcessGroup, Integer> daoToSet);

	public abstract void setProcessDAO(
			IBaseDao<sernet.verinice.model.iso27k.Process, Integer> daoToSet);

	public abstract void setRecordGroupDAO(
			IBaseDao<RecordGroup, Integer> daoToSet);

	public abstract void setRecordDAO(IBaseDao<Record, Integer> daoToSet);

	public abstract void setSamtTopicDAO(IBaseDao<SamtTopic, Integer> daoToSet);

	public abstract void setImportIsoDAO(IBaseDao<SamtTopic, Integer> daoToSet);
	
	public abstract void setImportBsiDAO(IBaseDao<SamtTopic, Integer> daoToSet);

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
	public abstract <T> IBaseDao<T, Serializable> getDAO(Class<T> daotype);

	public abstract IBaseDao getDAOforTypedElement(ITypedElement object);

	/**
	 * @param typeId
	 * @return
	 */
	public abstract IBaseDao getDAO(String typeId);

}