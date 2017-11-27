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
package sernet.verinice.model.bsi;

import java.util.Collection;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;

public class Server extends CnATreeElement 
	implements IBSIStrukturElement {
	
    private transient Logger log = Logger.getLogger(Server.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(Server.class);
        }
        return log;
    }
    
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "server"; //$NON-NLS-1$
	public static final String PROP_NAME = "server_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "server_kuerzel"; //$NON-NLS-1$
	@Deprecated
	public static final String P_ADMIN_OLD = "server_admin"; //$NON-NLS-1$
	public static final String PROP_TAG			= "server_tag"; //$NON-NLS-1$
    
    public static final String REL_SERVER_RAUM = "server_raum_located"; //$NON-NLS-1$
	
	@Deprecated
	public static final String P_ANWENDER_OLD = "server_anwender"; //$NON-NLS-1$
	public static final String PROP_ERLAEUTERUNG = "server_erlaeuterung"; //$NON-NLS-1$
	private static final String PROP_ANZAHL = "server_anzahl"; //$NON-NLS-1$
	
	public static final String PROP_VERTRAULICHKEIT_BEGRUENDUNG = "server_vertraulichkeit_begruendung"; //$NON-NLS-1$
	public static final String PROP_VERFUEGBARKEIT_BEGRUENDUNG = "server_verfuegbarkeit_begruendung"; //$NON-NLS-1$
	public static final String PROP_INTEGRITAET_BEGRUENDUNG = "server_integritaet_begruendung"; //$NON-NLS-1$
    
    public static final String PROP_ESA_ENTSCHEIDUNG_DURCH = "server_ergaenzendeanalyse_entscheidung_durch"; //$NON-NLS-1$
    public static final String PROP_ESA_ENTSCHEIDUNG_AM = "server_ergaenzendeanalyse_entscheidung_am"; //$NON-NLS-1$
    public static final String PROP_ESA_ENTSCHEIDUNG_BIS = "server_ergaenzendeanalyse_entscheidung_bis"; //$NON-NLS-1$
	
	private final IReevaluator schutzbedarfProvider 
				= new ProtectionRequirementsAdapter(this);
				
	private final ILinkChangeListener linkChangeListener
				= new MaximumProtectionRequirementsListener(this);

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public Server(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
	    // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
    }
	
	@Override
    public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}
	@Override
    public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	@Override
    public int getSchicht() {
		return 3;
	}
	
	protected Server() {
		
	}
	
	@Override
    public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	@Override
	public String getTitle() {
		return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return CnaStructureHelper.canContain(obj);
	}
	
	@Override
	public ILinkChangeListener getLinkChangeListener() {
		return linkChangeListener;
	}

	@Override
	public IReevaluator getProtectionRequirementsProvider() {
		return schutzbedarfProvider;
	}

	public void setErlaeuterung(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ERLAEUTERUNG), name);
	}
	
	public void setKuerzel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_KUERZEL), name);
	}

	public void setAnzahl(int anzahl) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ANZAHL), Integer.toString(anzahl));
	}

}
