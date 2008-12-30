package sernet.gs.ui.rcp.main.service;

import java.io.Serializable;
import java.util.HashMap;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.ClientsKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.GebaeudeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.NKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
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
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.ds.model.Datenverarbeitung;
import sernet.gs.ui.rcp.main.ds.model.Personengruppen;
import sernet.gs.ui.rcp.main.ds.model.StellungnahmeDSB;
import sernet.gs.ui.rcp.main.ds.model.VerantwortlicheStelle;
import sernet.gs.ui.rcp.main.ds.model.Verarbeitungsangaben;
import sernet.gs.ui.rcp.main.ds.model.Zweckbestimmung;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;

public class DAOFactory {
	
	// injected by spring
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
	public void setPropertyDao(IBaseDao<Property, Integer> propertyDao) {
		daos.put(Property.class, propertyDao);
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

    
    
	public <T> IBaseDao<T, Serializable> getDAO(Class<T> daotype) {
		return  daos.get(daotype);
	}
}
