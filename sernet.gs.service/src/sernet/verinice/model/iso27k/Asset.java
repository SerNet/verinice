/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.Arrays;
import java.util.Collection;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;

/**
 * An asset from the ISO/IEC 27000 standard.
 * See https://en.wikipedia.org/wiki/ISO/IEC_27000-series for details
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Asset extends CnATreeElement implements IISO27kElement, IISO27kGroup {

	public static final String TYPE_ID = "asset"; //$NON-NLS-1$
	public static final String UNSECURE_TYPE_ID = "unsecureAssetDAO"; //$NON-NLS-1$
	public static final String PROP_ABBR = "asset_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "asset_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "asset_tag"; //$NON-NLS-1$
	public static final String ASSET_VALUE_CONFIDENTIALITY = "asset_value_confidentiality"; //$NON-NLS-1$
    public static final String ASSET_VALUE_INTEGRITY = "asset_value_integrity"; //$NON-NLS-1$
    public static final String ASSET_VALUE_AVAILABILITY = "asset_value_availability"; //$NON-NLS-1$

    public static final String ASSET_VALUE_METHOD_CONFIDENTIALITY = "asset_value_method_confidentiality"; //$NON-NLS-1$
    public static final String ASSET_VALUE_METHOD_INTEGRITY = "asset_value_method_integrity"; //$NON-NLS-1$
    public static final String ASSET_VALUE_METHOD_AVAILABILITY = "asset_value_method_availability"; //$NON-NLS-1$
	
    public static final String ASSET_WITHOUT_NA_PLANCONTROLRISK_A = "asset_risk_without_na_plancontrolvalue_a";
    public static final String ASSET_WITHOUT_NA_PLANCONTROLRISK_I = "asset_risk_without_na_plancontrolvalue_i";
    public static final String ASSET_WITHOUT_NA_PLANCONTROLRISK_C = "asset_risk_without_na_plancontrolvalue_c";
    public static final String ASSET_PLANCONTROLRISK_A = "asset_riskwplancontrolvalue_a";
    public static final String ASSET_PLANCONTROLRISK_I = "asset_riskwplancontrolvalue_i";
    public static final String ASSET_PLANCONTROLRISK_C = "asset_riskwplancontrolvalue_c";
    public static final String ASSET_CONTROLRISK_A = "asset_riskwcontrolvalue_a";
    public static final String ASSET_CONTROLRISK_I = "asset_riskwcontrolvalue_i";
    public static final String ASSET_CONTROLRISK_C = "asset_riskwcontrolvalue_c";
    public static final String ASSET_RISK_A = "asset_riskvalue_a";
    public static final String ASSET_RISK_I = "asset_riskvalue_i";
    public static final String ASSET_RISK_C = "asset_riskvalue_c";

    
	public static final String REL_ASSET_PERSON_RESPO = "rel_asset_person_respo"; //$NON-NLS-1$
	
	public static final String[] CHILD_TYPES = new String[] {
        ControlGroup.TYPE_ID,
        Control.TYPE_ID
    };
	
	// all risk management constants are in AssetValueService.java
	
	
	
    private final IReevaluator protectionRequirementsProvider = new ProtectionRequirementsValueAdapter(this);
    private final ILinkChangeListener linkChangeListener = new MaximumProtectionRequirementsValueListener(this);
    
    
    @Override
    public ILinkChangeListener getLinkChangeListener() {
        return linkChangeListener;
    }
    @Override
    public IReevaluator getProtectionRequirementsProvider() {
        return protectionRequirementsProvider;
    }

	/**
	 * Creates an empty asset
	 */
	public Asset() {
		super();
		setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
	}
	
	public Asset(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
		// sets the localized title via HUITypeFactory from message bundle
		setTitel(getTypeFactory().getMessage(TYPE_ID));
	}
	
	public Asset(CnATreeElement parent, String title) {
        this(parent);
        if(title!=null) {
            setTitel(title);
        }
    }
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitel()
	 */
	@Override
	public String getTitle() {
		return getEntity().getPropertyValue(PROP_NAME);
	}
	
	@Override
    public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	@Override
    public String getAbbreviation() {
		return getEntity().getPropertyValue(PROP_ABBR);
	}
	
	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}
	
	@Override
    public Collection<String> getTags() {
		return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
	}
	
	/* (non-Javadoc)
     * @see sernet.verinice.iso27k.model.Group#getChildTypes()
     */
    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#canContain(java.lang.Object)
     */
    @Override
    public boolean canContain(Object obj) {
        boolean canContain = false;
        if(obj instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement)obj;
            canContain = Arrays.asList(getChildTypes()).contains(element.getTypeId());
        }
        return canContain;
    }

}
