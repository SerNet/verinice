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

import java.util.Collection;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public interface IISO27kElement {

	public String getTypeId();
	
	/**
	 * 
	 * REeturns the title of this element
	 * @return
	 */
	public String getTitle();
	
	/**
     * Sets the title of of this element
     * 
     * TODO: change method name to "setTit>>>le<<<"
     * 
     * @param name 
     */
    public void setTitel(String name);
	
	public Collection<? extends String> getTags();
	
	public String getAbbreviation();
}
