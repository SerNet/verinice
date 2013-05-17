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
package sernet.gs.ui.rcp.gsimport;

import java.util.HashMap;
import java.util.Map;

import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;

public abstract class ImportZielobjektTypUtil {

	public static final Map<String, String> GS_TYPES = new HashMap<String, String>();
	public static final Map<String, String> GS_ITSYSTEM_SUBTYPES = new HashMap<String, String>();

	static {
	    
		GS_TYPES.put("Mitarbeiter", Person.TYPE_ID);
		GS_TYPES.put("Raum", Raum.TYPE_ID);
		GS_TYPES.put("Anwendung", Anwendung.TYPE_ID);
		GS_TYPES.put("Netz", NetzKomponente.TYPE_ID);
		GS_TYPES.put("IT-Verbund", ITVerbund.TYPE_ID);
		GS_TYPES.put("Gebäude", Gebaeude.TYPE_ID);
		GS_TYPES.put("Informationsverbund", ITVerbund.TYPE_ID);

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

		GS_ITSYSTEM_SUBTYPES.put("PDA",							 SonstIT.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Speichersysteme und Speichernetze",
		        SonstIT.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Smartphone",					 SonstIT.TYPE_ID);
		GS_ITSYSTEM_SUBTYPES.put("Drucker, Kopierer, Multifunktionsgeräte",
		        SonstIT.TYPE_ID);
		
		// new for v 4.7:
		 GS_ITSYSTEM_SUBTYPES.put("[allgemeines Gebäude]", Gebaeude.TYPE_ID);
		 GS_ITSYSTEM_SUBTYPES.put("[allgemeiner Raum]", Raum.TYPE_ID);
		 GS_ITSYSTEM_SUBTYPES.put("Büroraum", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Serverraum", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Datenträgerarchiv", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Raum für technische Infrastruktur", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Schutzschrank", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Häuslicher Arbeitsplatz", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Rechenzentrum", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Mobiler Arbeitsplatz", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Besprechungs-, Veranstaltungs- und Schulungsräume", Raum.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("[allgemeiner Client/PC]", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("[allgemeiner Laptop]", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Client/PC unter DOS", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Laptop unter DOS", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Unix/Linux", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Laptop unter Unix/Linux", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Windows 9x ** OBSOLET", Client.TYPE_ID
         );
		 GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows 9x ** OBSOLET", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Windows NT ** OBSOLET", Client.TYPE_ID
         );
		 GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows NT ** OBSOLET", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Windows 2000", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows 2000", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Internet-PC", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("[allgemeiner Server]", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Server unter Unix/Linux", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Server unter Novell Netware 3.x ** OBSOLET", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Server unter Novell Netware 4.x", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Server unter Windows NT", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Server unter Windows 2000", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("TK-Anlage", TelefonKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Faxgerät", TelefonKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Anrufbeantworter", TelefonKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Mobiltelefon", TelefonKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("zSeries-Mainframe", SonstIT.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Router/Switches", SonstIT.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("PDA", SonstIT.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Client/PC unter Windows XP", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Sicherheitsgateway (Firewall)", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Speichersysteme und Speichernetze", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Server unter Windows 2003", Server.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows XP", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Smartphone", SonstIT.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Drucker, Kopierer, Multifunktionsgeräte", SonstIT.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Client unter Windows Vista", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Laptop unter Windows Vista", Client.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("[allgemeines Netz]", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("heterogenes Netz", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Netz- und Systemmanagement", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Modem", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("VPN", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("ISDN-Anbindung", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Kommunikationsverbindung", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("WLAN", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("VoIP", NetzKomponente.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("[allgemeine Anwendung]", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Datenträgeraustausch", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("E-Mail", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("WWW-Dienst", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Lotus Notes", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Faxserver", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Datenbank", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Novell eDirectory", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Internet Information Server", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Apache Webserver", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Exchange/Outlook 2000", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("SAP", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Mobile Datenträger", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Allgemeiner Verzeichnisdienst", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Active Directory", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("Samba", Anwendung.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("[Mitarbeiterin/Mitarbeiter]", Person.TYPE_ID );
		 GS_ITSYSTEM_SUBTYPES.put("[allgemeiner Informationsverbund]", ITVerbund.TYPE_ID);
	}

	public static String translateZielobjektType(String zoTypeName, String zoSubtypeName) throws GSImportException{
		String type = GS_TYPES.get(zoTypeName);
		if (type == null){
			type = GS_ITSYSTEM_SUBTYPES.get(zoSubtypeName);
		}
		if(type == null){
		    // TODO i8ln this
		    throw new GSImportException("Internes Mapping für den Typ\n " + zoTypeName + " (Subtyp: " + zoSubtypeName + ")\n nicht gefunden"); //NON-NLS-$1
		}
		return type;
	}

}
