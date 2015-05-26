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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.model.bsi.BausteinVorschlag;

public class SubtypenZielobjekte {

    private static final Logger LOG = Logger.getLogger(SubtypenZielobjekte.class);

    private static final int MAPPING_LIST_SIZE = 80;
    
    private List<BausteinVorschlag> mapping = new ArrayList<BausteinVorschlag>(MAPPING_LIST_SIZE);

    private static final String SUBTYP_MAPPING_FILE = "subtyp-baustein.properties"; //$NON-NLS-1$

    public SubtypenZielobjekte() {
        Properties properties = new Properties();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(SUBTYP_MAPPING_FILE);
        try {
            properties.load(stream);
        } catch (IOException e) {
            LOG.error(Messages.SubtypenZielobjekte_1, e);
        }

        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            final String name = entry.getKey().toString();
            String property = entry.getValue().toString();
            BausteinVorschlag vorschlag = new BausteinVorschlag(name, property);
            mapping.add(vorschlag);
        }

    }

    public List<BausteinVorschlag> getMapping() {
        return mapping;
    }
}
