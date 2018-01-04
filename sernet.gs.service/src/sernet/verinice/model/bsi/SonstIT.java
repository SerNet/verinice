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

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;

public class SonstIT extends CnATreeElement 
	implements IBSIStrukturElement {
	
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "sonstit"; //$NON-NLS-1$
    public static final String TYPE_ID_HIBERNATE = "sonst-it"; //$NON-NLS-1$
	public static final String PROP_NAME = "sonstit_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "sonstit_kuerzel"; //$NON-NLS-1$
	@Deprecated
	public static final String P_ADMIN_OLD = "sonstit_admin"; //$NON-NLS-1$
	@Deprecated
	public static final String P_ANWENDER_OLD = "sonstit_anwender"; //$NON-NLS-1$
	public static final String PROP_TAG			= "sonstit_tag"; //$NON-NLS-1$
	public static final String PROP_ERLAEUTERUNG = "sonstit_erlaeuterung"; //$NON-NLS-1$
	private static final String PROP_ANZAHL = "sonstit_anzahl"; //$NON-NLS-1$
	
    public static final String PROP_ESA_ENTSCHEIDUNG_DURCH = "sonstit_ergaenzendeanalyse_entscheidung_durch"; //$NON-NLS-1$
    public static final String PROP_ESA_ENTSCHEIDUNG_AM = "sonstit_ergaenzendeanalyse_entscheidung_am"; //$NON-NLS-1$
    public static final String PROP_ESA_ENTSCHEIDUNG_BIS = "sonstit_ergaenzendeanalyse_entscheidung_bis"; //$NON-NLS-1$
	
	private final IReevaluator schutzbedarfProvider 
	= new ProtectionRequirementsAdapter(this);


	private final ILinkChangeListener linkChangeListener
	= new MaximumProtectionRequirementsListener(this);
	

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public SonstIT(CnATreeElement parent) {
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
	
	protected SonstIT() {
		
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
