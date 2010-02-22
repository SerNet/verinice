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

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("serial")
public class AuditGroup extends Group<Audit> {

	public static final String TYPE_ID = "auditgroup"; //$NON-NLS-1$
	public static final String TITLE_DEFAULT = "Audits"; //$NON-NLS-1$
	public static final String PROP_NAME = "auditgroup_name"; //$NON-NLS-1$
	
	public static final String[] CHILD_TYPES = new String[] {Audit.TYPE_ID};
	
	public AuditGroup() {
		super();
	}
	
	public AuditGroup(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(getEntityType().getPropertyType(PROP_NAME), TITLE_DEFAULT);
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
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
