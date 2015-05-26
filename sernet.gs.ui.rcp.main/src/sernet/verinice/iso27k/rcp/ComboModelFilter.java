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
public class ComboModelFilter implements IComboModelFilter<CnATreeElement> {

    private String filter;
    
    public ComboModelFilter() {
        super();
    }

    public ComboModelFilter(String filter) {
        super();
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.IComboModelFilter#isVisible(java.lang.Object)
     */
    @Override
    public boolean isVisible(CnATreeElement element) {
        return (getFilter()==null) ? true : element.getTitle().toLowerCase().contains(getFilter().toLowerCase());
    }
    
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

}
