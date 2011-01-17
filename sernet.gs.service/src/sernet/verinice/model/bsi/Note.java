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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bsi;

import java.io.Serializable;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

@SuppressWarnings("serial")
public class Note extends Addition implements Serializable{

	public static final String PROP_NAME = "note_name"; //$NON-NLS-1$
	
	public static final String PROP_TEXT = "note_text"; //$NON-NLS-1$

	public static final String TYPE_ID = "note"; //$NON-NLS-1$
	
	private transient EntityType subEntityType;
	

	public Note() {
		super();
		setEntity(new Entity(TYPE_ID));
	}
	

	public String getTitel() {
		if(getEntity()!=null && getEntity().getProperties(PROP_NAME)!=null && getEntity().getProperties(PROP_NAME).getProperty(0)!=null) {
			return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
		} else {
			return null;
		}
	}
	
	public void setTitel(String titel) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), titel);
	}
	
	
	public String getText() {
		if(getEntity()!=null && getEntity().getProperties(PROP_TEXT)!=null && getEntity().getProperties(PROP_TEXT).getProperty(0)!=null) {
			return getEntity().getProperties(PROP_TEXT).getProperty(0).getPropertyValue();
		} else {
			return null;
		}
	}
	
	public void setText(String text) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_TEXT), text);
	}
	
	public EntityType getEntityType() {
		if (subEntityType == null)
			subEntityType = getTypeFactory().getEntityType(getTypeId());
		return subEntityType;
	}

	
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cnATreeElementId == null) ? 0 : cnATreeElementId.hashCode());
		result = prime * result + ((dbId == null) ? 0 : dbId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		// FIXME ak this will not work when comparing proxies: 
		if (getClass() != obj.getClass())
			return false;
		Note other = (Note) obj;
		if (cnATreeElementId == null) {
			if (other.cnATreeElementId != null)
				return false;
		} else if (!cnATreeElementId.equals(other.cnATreeElementId))
			return false;
		if (dbId == null) {
			if (other.dbId != null)
				return false;
		} else if (!dbId.equals(other.dbId))
			return false;
		return true;
	}

}
