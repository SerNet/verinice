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
package sernet.gs.ui.rcp.main.bsi.model;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
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
import sernet.verinice.model.common.CnATreeElement;

public final class CnAElementBuilder {
	private static CnAElementBuilder instance;
	
	private static final Logger LOG = Logger.getLogger(CnAElementBuilder.class);

	private CnAElementBuilder() {
	}

	public static CnAElementBuilder getInstance() {
		if (null == instance) {
			instance = new CnAElementBuilder();
		}
		return instance;
	}

	public CnATreeElement buildAndSave(ITVerbund itverbund, String typeId) throws Exception {
	
		if (Anwendung.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(AnwendungenKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Anwendung.TYPE_ID, null);
		}
		
		else if (Client.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(ClientsKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Client.TYPE_ID, null);
		} 
	
		else if (Server.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(ServerKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Server.TYPE_ID, null);
		}
	
		else if (Person.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(PersonenKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Person.TYPE_ID, null);
		
		}
	
		else if (TelefonKomponente.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(TKKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, TelefonKomponente.TYPE_ID, null);
		}
		
		else if (SonstIT.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(SonstigeITKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, SonstIT.TYPE_ID, null);
		} 
		
		else if (NetzKomponente.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(NKKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, NetzKomponente.TYPE_ID, null);
		}
	
		else if (Gebaeude.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(GebaeudeKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Gebaeude.TYPE_ID, null);
		} 
	
		else if (Raum.TYPE_ID.equals(typeId)) {
			CnATreeElement category = itverbund.getCategory(RaeumeKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Raum.TYPE_ID, null);
		}
		
		// else, build nothing:
		if (LOG.isDebugEnabled()) {
            LOG.debug("Could not create element for typeId: " + typeId);
        }
		return null;
		
	}
}
