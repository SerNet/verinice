/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.model;

import java.util.Collection;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.TagHelper;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
@SuppressWarnings("serial")
public class Control extends CnATreeElement implements IISO27kElement {

	public static final String TYPE_ID = "control"; //$NON-NLS-1$
	public static final String PROP_ABBR = "control_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "control_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "control_tag"; //$NON-NLS-1$
	public static final String PROP_DESC = "control_desc"; //$NON-NLS-1$
	
	public static final String PROP_MATURITY = "control_maturity"; //$NON-NLS-1$
	public static final String PROP_MATURITY_0 = "control_maturity_0"; //$NON-NLS-1$
	public static final String PROP_MATURITY_1 = "control_maturity_1"; //$NON-NLS-1$
	public static final String PROP_MATURITY_2 = "control_maturity_2"; //$NON-NLS-1$
	public static final String PROP_MATURITY_3 = "control_maturity_3"; //$NON-NLS-1$
	public static final String PROP_MATURITY_4 = "control_maturity_4"; //$NON-NLS-1$
	public static final String PROP_MATURITY_5 = "control_maturity_5"; //$NON-NLS-1$
	private static final int SUFFICIENT_MATURITY = 2;
	
	/**
	 * Creates an empty asset
	 */
	public Control() {
		super();
		setEntity(new Entity(TYPE_ID));
	}
	
	public Control(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(getEntityType().getPropertyType(PROP_NAME), "New Control");
	}
	
	public int getMaturity() {
		String maturity = null;
		PropertyList properties = getEntity().getProperties(PROP_MATURITY);
		if (properties == null || properties.getProperties() == null
				|| properties.getProperties().size() < 1)
			return -1;

		Property property = properties.getProperty(0);
		if (property != null && !property.getPropertyValue().equals("")) //$NON-NLS-1$
			maturity = property.getPropertyValue();
		
		if (maturity == null)
			return -1;
		if (maturity.equals(PROP_MATURITY_0))
			return 0;
		if (maturity.equals(PROP_MATURITY_1))
			return 1;
		if (maturity.equals(PROP_MATURITY_2))
			return 2;
		if (maturity.equals(PROP_MATURITY_3))
			return 3;
		if (maturity.equals(PROP_MATURITY_4))
			return 4;
		if (maturity.equals(PROP_MATURITY_5))
			return 5;
		return -1;
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
	
	public String getDescription() {
		return getEntity().getSimpleValue(PROP_DESC);
	}
	
	public void setDescription(String description) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_DESC), description);
	}

	/**
	 * @param control
	 * @return
	 */
	public static boolean isSufficientlyMature(Control control) {
		Logger.getLogger(Control.class).debug("Control maturity: " + control.getMaturity());
		return control.getMaturity() >= Control.SUFFICIENT_MATURITY;
	}

}
