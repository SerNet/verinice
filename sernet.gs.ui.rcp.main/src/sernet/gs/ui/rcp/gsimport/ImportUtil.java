package sernet.gs.ui.rcp.gsimport;

import java.util.HashMap;
import java.util.Map;

import sernet.gs.reveng.NZielobjekt;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;

public abstract class ImportUtil {

	public static final Map<String, String> GS_TYPES = new HashMap<String, String>();
	public static final Map<String, String> GS_ITSYSTEM_SUBTYPES = new HashMap<String, String>();

	static {
		GS_TYPES.put("Mitarbeiter", Person.TYPE_ID);
		GS_TYPES.put("Raum", Raum.TYPE_ID);
		GS_TYPES.put("Anwendung", Anwendung.TYPE_ID);
		GS_TYPES.put("Netz", NetzKomponente.TYPE_ID);
		GS_TYPES.put("IT-Verbund", ITVerbund.TYPE_ID);
		GS_TYPES.put("Gebäude", Gebaeude.TYPE_ID);

		GS_ITSYSTEM_SUBTYPES.put("allgemeiner Client/PC]",		 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("[allgemeiner Laptop]",		 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Client/PC unter DOS",			 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Laptop unter DOS",			 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Unix/Linux",	 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Laptop unter Unix/Linux",		 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Windows 9x",	 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows 9x",		 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Windows NT",   Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows NT",		 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES
				.put("Client/PC unter Windows 2000", 			 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows 2000",	 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Internet-PC",					 Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Windows XP", 	Client.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows XP",		 Client.TYPE_ID);

		GS_ITSYSTEM_SUBTYPES.put("[allgemeiner Server]",		 Server.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Server unter Unix/Linux", 	Server.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Server unter Netware 3.x",	 Server.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Server unter Netware 4.x", 	Server.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Server unter Windows NT", 	Server.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Server unter Windows 2000",	 Server.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("zSeries-Mainframe",			 Server.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Sicherheitsgateway (Firewall)",
																Server.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Server unter Windows 2003",	 Server.TYPE_ID);

		GS_ITSYSTEM_SUBTYPES.put("TK-Anlage", 					TelefonKomponente.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Faxgerät", 					TelefonKomponente.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Anrufbeantworter",			 TelefonKomponente.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Mobiltelefon",				 TelefonKomponente.TYPE_ID);

		GS_ITSYSTEM_SUBTYPES.put("Router/Switches",				 NetzKomponente.TYPE_ID);

		GS_ITSYSTEM_SUBTYPES.put("PDA",							 SonstigeITKategorie.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Speichersysteme und Speichernetze",
																SonstigeITKategorie.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Smartphone",					 SonstigeITKategorie.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Drucker, Kopierer, Multifunktionsgeräte",
																SonstigeITKategorie.TYPE_ID);

	}

	public static String translateZielobjektType(String zoTypeName, String zoSubtypeName) {
		String type = GS_TYPES.get(zoTypeName);
		if (type == null)
			type = GS_ITSYSTEM_SUBTYPES.get(zoSubtypeName);
		return type;
	}

}
