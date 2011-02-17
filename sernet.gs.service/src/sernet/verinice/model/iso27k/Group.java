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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import sernet.verinice.model.common.CnATreeElement;

/**
 * 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public abstract class Group<T> extends CnATreeElement implements IISO27kGroup {
	
	public Group() {
		super();
	}
	
	public String getAbbreviation() {
	    return "";
	}
	
	/**
	 * @param parent
	 */
	public Group(CnATreeElement parent) {
		super(parent);
	}

	/**
	 * Returns a array of child-type-ids of this group.
	 * Implemnt this and use TYPE_ID of childs
	 * 
	 * @return array of child-type-ids
	 */
	public abstract String[] getChildTypes();
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#canContain(java.lang.Object)
	 */
	@Override
	public boolean canContain(Object obj) {
		boolean canContain = false;
		if(obj instanceof CnATreeElement) {
			CnATreeElement element = (CnATreeElement)obj;
			canContain = Arrays.asList(getChildTypes()).contains(element.getTypeId()) 
						 || this.getTypeId().equals(element.getTypeId());
		}
		return canContain;
	}
	
	public Collection<? extends String> getTags() {
		// empty, override this to add tags to groups
		// dont't forget to add a huiproperty to your SNCA.xml
		return Collections.emptyList();
	}
}
