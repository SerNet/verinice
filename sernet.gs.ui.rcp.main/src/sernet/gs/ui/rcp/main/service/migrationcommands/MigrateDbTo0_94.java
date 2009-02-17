package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
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
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ds.model.Datenverarbeitung;
import sernet.gs.ui.rcp.main.ds.model.Personengruppen;
import sernet.gs.ui.rcp.main.ds.model.StellungnahmeDSB;
import sernet.gs.ui.rcp.main.ds.model.VerantwortlicheStelle;
import sernet.gs.ui.rcp.main.ds.model.Verarbeitungsangaben;
import sernet.gs.ui.rcp.main.ds.model.Zweckbestimmung;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyList;

/**
 * Adds UUID to all required objects.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MigrateDbTo0_94 extends DbMigration {

	private Class[] cnatreeSubclasses = new Class[] {
			Anwendung.class,
			AnwendungenKategorie.class,
			BausteinUmsetzung.class,
			BSIModel.class,
			Client.class,
			ClientsKategorie.class,
			Datenverarbeitung.class,
			FinishedRiskAnalysis.class,
			Gebaeude.class,
			GebaeudeKategorie.class,
			GefaehrdungsUmsetzung.class,
			ITVerbund.class,
			MassnahmenUmsetzung.class,
			NetzKomponente.class,
			NKKategorie.class,
			Person.class,
			Personengruppen.class,
			PersonenKategorie.class,
			RaeumeKategorie.class,
			Raum.class,
			Server.class,
			ServerKategorie.class,
			SonstigeITKategorie.class,
			SonstIT.class,
			StellungnahmeDSB.class,
			TelefonKomponente.class,
			TKKategorie.class,
			VerantwortlicheStelle.class,
			Verarbeitungsangaben.class,
			Zweckbestimmung.class,
			RisikoMassnahme.class,
			OwnGefaehrdung.class,
			FinishedRiskAnalysisLists.class

	};

	@Override
	public double getVersion() {
		return 0.94D;
	}

	public void execute() {
		for (Class clazz : cnatreeSubclasses) {
			setUUID(clazz);
		}
		
		
		List<PropertyList> list = getDaoFactory().getDAO(PropertyList.class).findAll();
		for (PropertyList propertyList : list) {
			propertyList.setUuid(UUID.randomUUID().toString());
		}
		
		List<Entity> list2 = getDaoFactory().getDAO(Entity.class).findAll();
		Logger.getLogger(this.getClass()).debug("Generating UUIDs all elements of type " + Entity.class.getSimpleName());
		for (Entity element : list2) {
			element.setUuid(UUID.randomUUID().toString());
		}

		List<FinishedRiskAnalysisLists> list3 = getDaoFactory().getDAO(FinishedRiskAnalysisLists.class).findAll();
		Logger.getLogger(this.getClass()).debug("Generating UUIDs all elements of type " + FinishedRiskAnalysisLists.class.getSimpleName());
		for (FinishedRiskAnalysisLists element : list3) {
			element.setUuid(UUID.randomUUID().toString());
		}
		
		List<Gefaehrdung> list4 = getDaoFactory().getDAO(Gefaehrdung.class).findAll();
		Logger.getLogger(this.getClass()).debug("Generating UUIDs all elements of type " + Gefaehrdung.class.getSimpleName());
		for (Gefaehrdung element : list4) {
			element.setUuid(UUID.randomUUID().toString());
		}

		List<OwnGefaehrdung> list5 = getDaoFactory().getDAO(OwnGefaehrdung.class).findAll();
		Logger.getLogger(this.getClass()).debug("Generating UUIDs all elements of type " + OwnGefaehrdung.class.getSimpleName());
		for (OwnGefaehrdung element : list5) {
			element.setUuid(UUID.randomUUID().toString());
		}
		
		super.updateVersion();
	}

	private <T extends CnATreeElement> void setUUID(Class<T> clazz) {
		Logger.getLogger(this.getClass()).debug("Generating UUIDs all elements of type " + clazz.getSimpleName());
		List<T> list = getDaoFactory().getDAO(clazz).findAll();
		for (T element : list) {
			element.setUuid(UUID.randomUUID().toString());
		}		
	}


	
	

}
