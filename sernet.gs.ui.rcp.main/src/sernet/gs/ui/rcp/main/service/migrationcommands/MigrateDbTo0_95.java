package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
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
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.RaeumeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.ServerKategorie;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.bsi.model.SubtypenZielobjekte;
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
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyList;

/**
 * Insert subtype-module mapping tables into DB.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MigrateDbTo0_95 extends DbMigration {

	@Override
	public double getVersion() {
		return 0.95D;
	}

	public void execute() throws RuntimeCommandException {
		Logger.getLogger(this.getClass()).debug("Updating DB to V 0.95. ");
		Logger.getLogger(this.getClass()).debug("Inserting: Bausteinvorschläge");
		SubtypenZielobjekte mapping = new SubtypenZielobjekte();
		List<BausteinVorschlag> list = mapping.getMapping();
		UpdateMultipleElements<BausteinVorschlag> command = new UpdateMultipleElements<BausteinVorschlag>(list);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			super.updateVersion();
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}


}
