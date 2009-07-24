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
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

public class SubtypenZielobjekte {

		List<BausteinVorschlag> mapping = new ArrayList<BausteinVorschlag>(50);
	
		private Properties properties;
		
		private static final String SUBTYP_MAPPING_FILE = "subtyp-baustein.properties";

		public SubtypenZielobjekte() {
			properties = new Properties();
			InputStream stream = getClass().getClassLoader().getResourceAsStream(SUBTYP_MAPPING_FILE);
			try {
				properties.load(stream);
			} catch (IOException e) {
				Logger.getLogger(this.getClass())
					.error("Fehler beim Laden der Zuordnung von Zielobjekt-Typen zu Bausteinen", e);
			}
			
			Set<String> names = properties.stringPropertyNames();
			for (String name : names) {
				String property = properties.getProperty(name);
				BausteinVorschlag vorschlag = new BausteinVorschlag(name, property);
				mapping.add(vorschlag);
			}
			
		}


		public List<BausteinVorschlag> getMapping() {
			return mapping;
		}
}
