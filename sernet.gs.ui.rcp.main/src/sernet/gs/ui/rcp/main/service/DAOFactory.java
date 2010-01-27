/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.model.Addition;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.model.AttachmentFile;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinVorschlag;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.ClientsKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.GebaeudeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.NKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Note;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.RaeumeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.ServerKategorie;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.Permission;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.ds.model.Datenverarbeitung;
import sernet.gs.ui.rcp.main.ds.model.Personengruppen;
import sernet.gs.ui.rcp.main.ds.model.StellungnahmeDSB;
import sernet.gs.ui.rcp.main.ds.model.VerantwortlicheStelle;
import sernet.gs.ui.rcp.main.ds.model.Verarbeitungsangaben;
import sernet.gs.ui.rcp.main.ds.model.Zweckbestimmung;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.iso27k.model.Asset;
import sernet.verinice.iso27k.model.AssetGroup;
import sernet.verinice.iso27k.model.Audit;
import sernet.verinice.iso27k.model.AuditGroup;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.Document;
import sernet.verinice.iso27k.model.DocumentGroup;
import sernet.verinice.iso27k.model.Evidence;
import sernet.verinice.iso27k.model.EvidenceGroup;
import sernet.verinice.iso27k.model.Exception;
import sernet.verinice.iso27k.model.ExceptionGroup;
import sernet.verinice.iso27k.model.Finding;
import sernet.verinice.iso27k.model.FindingGroup;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Incident;
import sernet.verinice.iso27k.model.IncidentGroup;
import sernet.verinice.iso27k.model.IncidentScenario;
import sernet.verinice.iso27k.model.IncidentScenarioGroup;
import sernet.verinice.iso27k.model.Interview;
import sernet.verinice.iso27k.model.InterviewGroup;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.model.PersonGroup;
import sernet.verinice.iso27k.model.PersonIso;
import sernet.verinice.iso27k.model.Requirement;
import sernet.verinice.iso27k.model.RequirementGroup;
import sernet.verinice.iso27k.model.Response;
import sernet.verinice.iso27k.model.ResponseGroup;
import sernet.verinice.iso27k.model.Threat;
import sernet.verinice.iso27k.model.ThreatGroup;
import sernet.verinice.iso27k.model.Vulnerability;
import sernet.verinice.iso27k.model.VulnerabilityGroup;

/**
 * Registry for DAOs for different types of objects. DAOs are managed by and injected by the Spring framework. 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DAOFactory {
	
	private final Logger log = Logger.getLogger(DAOFactory.class);
	
	// injected by spring
	@SuppressWarnings("unchecked")
	private HashMap<Class, IBaseDao> daos = new HashMap<Class, IBaseDao>(); 
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	public void setEntityDao(IBaseDao<Entity, Integer> entityDao) {
		daos.put(Entity.class, entityDao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	public void setGefaehrdungDao(IBaseDao<Gefaehrdung, Integer> dao) {
		daos.put(Gefaehrdung.class, dao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	public void setBausteinVorschlagDao(IBaseDao<BausteinVorschlag, Integer> dao) {
		daos.put(BausteinVorschlag.class, dao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	public void setConfigurationDao(IBaseDao<Gefaehrdung, Integer> dao) {
		daos.put(Configuration.class, dao);
	}

	public void setchangeLogEntryDAO(IBaseDao<ChangeLogEntry, Integer> dao) {
		daos.put(ChangeLogEntry.class, dao);
	}
	
	
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	public void setOwnGefaehrdungDao(IBaseDao<OwnGefaehrdung, Integer> dao) {
		daos.put(OwnGefaehrdung.class, dao);
	}
	
	/**
	 * Setter method used by spring to inject DAO.
	 */
	public void setPropertyListDao(IBaseDao<PropertyList, Integer> propertyListDao) {
		daos.put(PropertyList.class, propertyListDao);
	}

	/**
	 * Setter method used by spring to inject DAO.
	 */
	public void setCnaLinkDao(IBaseDao<CnALink, Integer> dao) {
		daos.put(CnALink.class, dao);
	}
	
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setAnwendungDAO(IBaseDao<Anwendung, Integer> daoToSet) {
        daos.put(Anwendung.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setAnwendungenKategorieDAO(IBaseDao<AnwendungenKategorie, Integer> daoToSet) {
    	daos.put(AnwendungenKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setBausteinUmsetzungDAO(IBaseDao<BausteinUmsetzung, Integer> daoToSet) {
        daos.put(BausteinUmsetzung.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setBSIModelDAO(IBaseDao<BSIModel, Integer> daoToSet) {
        daos.put(BSIModel.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setClientDAO(IBaseDao<Client, Integer> daoToSet) {
        daos.put(Client.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setClientsKategorieDAO(IBaseDao<ClientsKategorie, Integer> daoToSet) {
        daos.put(ClientsKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setDatenverarbeitungDAO(IBaseDao<Datenverarbeitung, Integer> daoToSet) {
        daos.put(Datenverarbeitung.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setFinishedRiskAnalysisDAO(IBaseDao<FinishedRiskAnalysis, Integer> daoToSet) {
        daos.put(FinishedRiskAnalysis.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setGebaeudeDAO(IBaseDao<Gebaeude, Integer> daoToSet) {
        daos.put(Gebaeude.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setGebaeudeKategorieDAO(IBaseDao<GebaeudeKategorie, Integer> daoToSet) {
        daos.put(GebaeudeKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setGefaehrdungsUmsetzungDAO(IBaseDao<GefaehrdungsUmsetzung, Integer> daoToSet) {
        daos.put(GefaehrdungsUmsetzung.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setITVerbundDAO(IBaseDao<ITVerbund, Integer> daoToSet) {
        daos.put(ITVerbund.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setMassnahmenUmsetzungDAO(IBaseDao<MassnahmenUmsetzung, Integer> daoToSet) {
        daos.put(MassnahmenUmsetzung.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setNetzKomponenteDAO(IBaseDao<NetzKomponente, Integer> daoToSet) {
        daos.put(NetzKomponente.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setNKKategorieDAO(IBaseDao<NKKategorie, Integer> daoToSet) {
        daos.put(NKKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setPermissionDAO(IBaseDao<Permission, Integer> daoToSet) {
        daos.put(Permission.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setPersonDAO(IBaseDao<Person, Integer> daoToSet) {
        daos.put(Person.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setPersonengruppenDAO(IBaseDao<Personengruppen, Integer> daoToSet) {
        daos.put(Personengruppen.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setPersonenKategorieDAO(IBaseDao<PersonenKategorie, Integer> daoToSet) {
        daos.put(PersonenKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setRaeumeKategorieDAO(IBaseDao<RaeumeKategorie, Integer> daoToSet) {
        daos.put(RaeumeKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setRaumDAO(IBaseDao<Raum, Integer> daoToSet) {
        daos.put(Raum.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setServerDAO(IBaseDao<Server, Integer> daoToSet) {
        daos.put(Server.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setServerKategorieDAO(IBaseDao<ServerKategorie, Integer> daoToSet) {
        daos.put(ServerKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setSonstigeITKategorieDAO(IBaseDao<SonstigeITKategorie, Integer> daoToSet) {
        daos.put(SonstigeITKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setSonstITDAO(IBaseDao<SonstIT, Integer> daoToSet) {
        daos.put(SonstIT.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setStellungnahmeDSBDAO(IBaseDao<StellungnahmeDSB, Integer> daoToSet) {
        daos.put(StellungnahmeDSB.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setTelefonKomponenteDAO(IBaseDao<TelefonKomponente, Integer> daoToSet) {
        daos.put(TelefonKomponente.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setTKKategorieDAO(IBaseDao<TKKategorie, Integer> daoToSet) {
        daos.put(TKKategorie.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setVerantwortlicheStelleDAO(IBaseDao<VerantwortlicheStelle, Integer> daoToSet) {
        daos.put(VerantwortlicheStelle.class, daoToSet);
    }
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setVerarbeitungsangabenDAO(IBaseDao<Verarbeitungsangaben, Integer> daoToSet) {
        daos.put(Verarbeitungsangaben.class, daoToSet);
    }

    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setZweckbestimmungDAO(IBaseDao<Zweckbestimmung, Integer> daoToSet) {
        daos.put(Zweckbestimmung.class, daoToSet);
    }
	
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setRisikoMassnahmeDAO(IBaseDao<RisikoMassnahme, Integer> daoToSet) {
        daos.put(RisikoMassnahme.class, daoToSet);
    }

    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setOwnGefaehrdungDAO(IBaseDao<OwnGefaehrdung, Integer> daoToSet) {
    	daos.put(OwnGefaehrdung.class, daoToSet);
    }
    
    /** 
     * Setter method used by spring to inject DAO.
     */
    public void setFinishedRiskAnalysisListsDAO(IBaseDao<FinishedRiskAnalysisLists, Integer> daoToSet) {
    	daos.put(FinishedRiskAnalysisLists.class, daoToSet);
    }

    public void setRisikoMassnahmeUmsetzungDAO(IBaseDao<RisikoMassnahmenUmsetzung, Integer> daoToSet) {
    	daos.put(RisikoMassnahmenUmsetzung.class, daoToSet);
    }
    
    public void setNoteDAO(IBaseDao<Note, Integer> daoToSet) {
    	daos.put(Note.class, daoToSet);
    }
    
    public void setAttachmentDAO(IBaseDao<Attachment, Integer> daoToSet) {
    	daos.put(Attachment.class, daoToSet);
    }
    
    public void setAdditionDAO(IBaseDao<Addition, Integer> daoToSet) {
    	daos.put(Addition.class, daoToSet);
    }
    
    public void setAttachmentFileDAO(IBaseDao<AttachmentFile, Integer> daoToSet) {
    	daos.put(AttachmentFile.class, daoToSet);
    }
    
    public void setISO27KModelDAO(IBaseDao<ISO27KModel, Integer> daoToSet) {
    	daos.put(ISO27KModel.class, daoToSet);
    }
    
    public void setOrganizationDAO(IBaseDao<Organization, Integer> daoToSet) {
    	daos.put(Organization.class, daoToSet);
    }
    
    public void setAssetGroupDAO(IBaseDao<AssetGroup, Integer> daoToSet) {
    	daos.put(AssetGroup.class, daoToSet);
    }
    public void setAssetDAO(IBaseDao<Asset, Integer> daoToSet) {
    	daos.put(Asset.class, daoToSet);
    }
    
    public void setControlGroupDAO(IBaseDao<ControlGroup, Integer> daoToSet) {
    	daos.put(ControlGroup.class, daoToSet);
    }
    public void setControlDAO(IBaseDao<Control, Integer> daoToSet) {
    	daos.put(Control.class, daoToSet);
    }
    
    public void setAuditGroupDAO(IBaseDao<AuditGroup, Integer> daoToSet) {
    	daos.put(AuditGroup.class, daoToSet);
    }
    public void setAuditDAO(IBaseDao<Audit, Integer> daoToSet) {
    	daos.put(Audit.class, daoToSet);
    }
    
    public void setExceptionGroupDAO(IBaseDao<ExceptionGroup, Integer> daoToSet) {
    	daos.put(ExceptionGroup.class, daoToSet);
    }
    public void setExceptionDAO(IBaseDao<Exception, Integer> daoToSet) {
    	daos.put(Exception.class, daoToSet);
    }
    
    public void setPersonGroupDAO(IBaseDao<PersonGroup, Integer> daoToSet) {
    	daos.put(PersonGroup.class, daoToSet);
    }
    public void setPersonIsoDAO(IBaseDao<PersonIso, Integer> daoToSet) {
    	daos.put(PersonIso.class, daoToSet);
    }
    
    public void setRequirementGroupDAO(IBaseDao<RequirementGroup, Integer> daoToSet) {
    	daos.put(RequirementGroup.class, daoToSet);
    }
    public void setRequirementDAO(IBaseDao<Requirement, Integer> daoToSet) {
    	daos.put(Requirement.class, daoToSet);
    }
    
    public void setIncidentGroupDAO(IBaseDao<IncidentGroup, Integer> daoToSet) {
    	daos.put(IncidentGroup.class, daoToSet);
    }
    public void setIncidentDAO(IBaseDao<Incident, Integer> daoToSet) {
    	daos.put(Incident.class, daoToSet);
    }
    
    public void setIncidentScenarioGroupDAO(IBaseDao<IncidentScenarioGroup, Integer> daoToSet) {
    	daos.put(IncidentScenarioGroup.class, daoToSet);
    }
    public void setIncidentScenarioDAO(IBaseDao<IncidentScenario, Integer> daoToSet) {
    	daos.put(IncidentScenario.class, daoToSet);
    }
  

    public void setResponseGroupDAO(IBaseDao<ResponseGroup, Integer> daoToSet) {
    	daos.put(ResponseGroup.class, daoToSet);
    }
    public void setResponseDAO(IBaseDao<Response, Integer> daoToSet) {
    	daos.put(Response.class, daoToSet);
    }
    
    public void setThreatGroupDAO(IBaseDao<ThreatGroup, Integer> daoToSet) {
    	daos.put(ThreatGroup.class, daoToSet);
    }
    public void setThreatDAO(IBaseDao<Threat, Integer> daoToSet) {
    	daos.put(Threat.class, daoToSet);
    }
    
    public void setVulnerabilityGroupDAO(IBaseDao<VulnerabilityGroup, Integer> daoToSet) {
    	daos.put(VulnerabilityGroup.class, daoToSet);
    }
    public void setVulnerabilityDAO(IBaseDao<Vulnerability, Integer> daoToSet) {
    	daos.put(Vulnerability.class, daoToSet);
    }
    
    public void setDocumentGroupDAO(IBaseDao<DocumentGroup, Integer> daoToSet) {
    	daos.put(DocumentGroup.class, daoToSet);
    }
    public void setDocumentDAO(IBaseDao<Document, Integer> daoToSet) {
    	daos.put(Document.class, daoToSet);
    }
    
    public void setEvidenceGroupDAO(IBaseDao<EvidenceGroup, Integer> daoToSet) {
    	daos.put(EvidenceGroup.class, daoToSet);
    }
    public void setEvidenceDAO(IBaseDao<Evidence, Integer> daoToSet) {
    	daos.put(Evidence.class, daoToSet);
    }
    
    public void setInterviewGroupDAO(IBaseDao<InterviewGroup, Integer> daoToSet) {
    	daos.put(InterviewGroup.class, daoToSet);
    }
    public void setInterviewDAO(IBaseDao<Interview, Integer> daoToSet) {
    	daos.put(Interview.class, daoToSet);
    }
    
    public void setFindingGroupDAO(IBaseDao<FindingGroup, Integer> daoToSet) {
    	daos.put(FindingGroup.class, daoToSet);
    }
    public void setFindingDAO(IBaseDao<Finding, Integer> daoToSet) {
    	daos.put(Finding.class, daoToSet);
    }
    
	@SuppressWarnings("unchecked")
	public <T> IBaseDao<T, Serializable> getDAO(Class<T> daotype) {
		IBaseDao dao = daos.get(daotype);
		if (dao != null)
			return dao;
		
		// we might have been passed a proxy (class enhanced by cglib), so try to find
		// a DAO that works:
		// FIXME akoderman this doesn't work, we still need a better solution for this, you often get a NullPointerException because no DAO was found for a CGLib enhanced obect
		for (Class clazz : daos.keySet()) {
			if (clazz.isAssignableFrom(daotype))
				return daos.get(clazz);
		}
		if(daotype!=null) {
			log.warn("No dao found for class: " + daotype.getName());
		} else {
			log.warn("dao-type-class is null, could not return dao");
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> IBaseDao<T, Serializable> getDAOForObject(Object o) {
		Set<Entry<Class, IBaseDao>> entrySet = daos.entrySet();
		for (Entry<Class, IBaseDao> entry : entrySet) {
			if (entry.getKey().isInstance(o)) {
				return  entry.getValue();
			}
		}
		return null;
	}
}
