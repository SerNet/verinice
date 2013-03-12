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

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class IncidentScenario extends CnATreeElement implements IISO27kElement {

    private static final Logger LOG = Logger.getLogger(IncidentScenario.class);
    
	public static final String TYPE_ID = "incident_scenario";  //$NON-NLS-1$
	public static final String PROP_ABBR = "incident_scenario_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "incident_scenario_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "incident_scenario_tag"; //$NON-NLS-1$
	public static final String PROP_PROBABILITY = "incscen_likelihood"; //$NON-NLS-1$
	public static final String PROP_GSM_ISM_SCENARIO_CVSS = "gsm_ism_scenario_cvss"; //$NON-NLS-1$

    public static final String REL_INCSCEN_ASSET = "rel_incscen_asset"; //$NON-NLS-1$
	
	
	/**
	 * Creates an empty scenario
	 */
	public IncidentScenario() {
		super();
		setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
	}
	
	public IncidentScenario(CnATreeElement parent) {
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
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	@Override
    public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	@Override
    public String getAbbreviation() {
		return getEntity().getSimpleValue(PROP_ABBR);
	}

	public String getProbability() {
	    return getEntity().getSimpleValue(PROP_PROBABILITY);
	}
	
	public Double getGsmCvss() {  
	    String value = getEntity().getSimpleValue(PROP_GSM_ISM_SCENARIO_CVSS);
	    if(value==null || value.isEmpty()) {
	        return null;
	    }	    
        try {
            return convertToDouble(value);
        } catch (java.lang.NumberFormatException e) {
            LOG.error("Can not convert CVSS string to number (Double), string is: " + value, e);
            return null;
        }
    }
	
    private Double convertToDouble(String value) {
        if(value==null || value.isEmpty()) {
            return null;
        }
        // replace "," with "."
        value = value.replace(',', '.');
        return Double.valueOf(value);
    }

    public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}
	
	@Override
    public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
}
