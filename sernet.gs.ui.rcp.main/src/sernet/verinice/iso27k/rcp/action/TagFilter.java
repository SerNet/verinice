/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import sernet.verinice.interfaces.IParameter;

public class TagFilter implements IParameter {
    
    private String[] pattern;
    private boolean filterOrgs;

    public TagFilter() {
    }

    public String[] getPattern() {
        return (pattern != null) ? pattern.clone() : null;
    }

    public void setPattern(String[] newPattern) {
        if (newPattern != null && newPattern.length > 0) {
            pattern = newPattern.clone();          
        } else {
            // deactivate
            pattern = null;      
        }

    }

    public boolean isActive() {
        return getPattern() != null && getPattern().length > 0;
    }

	public boolean isFilterOrg() {
		return filterOrgs;
	}

	public void setFilterOrgs(boolean filterOrgs) {
		this.filterOrgs = filterOrgs;
	}

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.IParameter#getParameter()
     */
    @Override
    public Object getParameter() {
        return getPattern();
    }


}
