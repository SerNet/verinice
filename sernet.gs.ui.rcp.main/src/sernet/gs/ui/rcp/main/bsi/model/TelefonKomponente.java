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

import java.util.Collection;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class TelefonKomponente extends CnATreeElement 
	implements IBSIStrukturElement  {
	
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID 		= "tkkomponente"; //$NON-NLS-1$
	public static final String PROP_NAME 		= "tkkomponente_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL	= "tkkomponente_kuerzel"; //$NON-NLS-1$
	@Deprecated
	public static final String P_ADMIN_OLD 		= "tkkomponente_admin"; //$NON-NLS-1$
	@Deprecated
	public static final String P_ANWENDER_OLD	 	= "tkkomponente_anwender"; //$NON-NLS-1$
	public static final String PROP_TAG			= "tkkomponente_tag"; //$NON-NLS-1$
	public static final String PROP_ERLAEUTERUNG = "tkkomponente_erlaeuterung"; //$NON-NLS-1$
	private static final String PROP_ANZAHL = "tkkomponente_anzahl"; //$NON-NLS-1$
	
	private final ISchutzbedarfProvider schutzbedarfProvider 
		= new SchutzbedarfAdapter(this);


	private final ILinkChangeListener linkChangeListener
		= new MaximumSchutzbedarfListener(this);
	

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public TelefonKomponente(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(getEntityType().getPropertyType(PROP_NAME), 
				"Neue TK-Komponente");
	}
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}
	
	public int getSchicht() {
		return 3;
	}
	
	protected TelefonKomponente() {
		
	}
	
	@Override
	public String getTitel() {
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
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	@Override
	public ILinkChangeListener getLinkChangeListener() {
		return linkChangeListener;
	}

	@Override
	public ISchutzbedarfProvider getSchutzbedarfProvider() {
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
