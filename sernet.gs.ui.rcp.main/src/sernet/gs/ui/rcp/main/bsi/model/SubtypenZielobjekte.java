package sernet.gs.ui.rcp.main.bsi.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

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
