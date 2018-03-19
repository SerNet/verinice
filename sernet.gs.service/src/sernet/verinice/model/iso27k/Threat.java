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

import java.util.Collection;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A threat from the ISO/IEC 27000 standard.
 * See https://en.wikipedia.org/wiki/ISO/IEC_27000-series for details
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Threat extends CnATreeElement implements IISO27kElement {

	public static final String TYPE_ID = "threat"; //$NON-NLS-1$
	public static final String PROP_ABBR = "threat_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "threat_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "threat_tag"; //$NON-NLS-1$
	public static final String PROP_DESCRIPTION = "threat_description"; //$NON-NLS-1$
    public static final String PROP_THREAT_LIKELIHOOD = "threat_likelihood"; //$NON-NLS-1$
    public static final String PROP_THREAT_IMPACT = "threat_impact"; //$NON-NLS-1$

	public Threat() {
		super();
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
	}
	
	public Threat(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
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

	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	public String getAbbreviation() {
		return getEntity().getPropertyValue(PROP_ABBR);
	}
	
	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}

	public void setDescription(String desc) {
	    getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_DESCRIPTION), desc);
	}
	
	public Collection<String> getTags() {
		return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
	}

    public String getDescription() {
        return getEntity().getPropertyValue(PROP_DESCRIPTION);
    }

}
