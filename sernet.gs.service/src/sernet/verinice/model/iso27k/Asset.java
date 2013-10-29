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
import sernet.verinice.model.bsi.ISchutzbedarfProvider;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Asset extends CnATreeElement implements IISO27kElement, IISO27kGroup {

	public static final String TYPE_ID = "asset"; //$NON-NLS-1$
	public static final String PROP_ABBR = "asset_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "asset_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "asset_tag"; //$NON-NLS-1$
	
	public static final String[] CHILD_TYPES = new String[] {
        ControlGroup.TYPE_ID,
        Control.TYPE_ID
    };
	
	// all risk management constants are in AssetValueService.java
	
	
	
    private final ISchutzbedarfProvider schutzbedarfProvider = new AssetValueAdapter(this);
    private final ILinkChangeListener linkChangeListener = new MaximumAssetValueListener(this);

    @Override
    public ILinkChangeListener getLinkChangeListener() {
        return linkChangeListener;
    }
    @Override
    public ISchutzbedarfProvider getSchutzbedarfProvider() {
        return schutzbedarfProvider;
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
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	public String getAbbreviation() {
		return getEntity().getSimpleValue(PROP_ABBR);
	}
	
	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}
	
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
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
