/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.samt.SamtTopic;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class ControlGroup extends Group<Control> {
    
    private static final Logger LOG = Logger.getLogger(ControlGroup.class);

	public static final String TYPE_ID = "controlgroup"; //$NON-NLS-1$
	public static final String PROP_NAME = "controlgroup_name"; //$NON-NLS-1$
	
	public static final String[] CHILD_TYPES = new String[] {Control.TYPE_ID,SamtTopic.TYPE_ID};
	
	public ControlGroup() {
		super();
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
	}
	
	public ControlGroup(CnATreeElement parent) {
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
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitle()
	 */
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#setTitel(java.lang.String)
	 */
	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.Group#getChildTypes()
	 */
	@Override
	public String[] getChildTypes() {
		return CHILD_TYPES;
	}

}
