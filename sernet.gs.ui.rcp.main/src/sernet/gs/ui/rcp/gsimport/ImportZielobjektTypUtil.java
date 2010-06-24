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
import sernet.verinice.model.bsi.SonstigeITKategorie;
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
