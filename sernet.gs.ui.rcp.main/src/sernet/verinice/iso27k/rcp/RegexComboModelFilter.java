/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RegexComboModelFilter implements IComboModelFilter<CnATreeElement> {

    private String filter;
    
    public RegexComboModelFilter() {
        super();
    }

    public RegexComboModelFilter(String filter) {
        super();
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.IComboModelFilter#isVisible(java.lang.Object)
     */
    @Override
    public boolean isVisible(CnATreeElement element) {
        if(getFilter()==null) {
            return true;
        }
        String title = element.getTitle().toLowerCase();
        return title.matches(getFilter());
    }
    
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        filter = filter.replace("*", ".*");
        filter = filter.replace("?", ".?");
        
        this.filter = new StringBuilder().append(".*").append(filter.toLowerCase()).append(".*").toString();
    }

}
