/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin <dm[at]sernet[dot]de>.
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
package sernet.verinice.web;

import java.io.Serializable;

/**
 * Interface to implement actions executed in web interface.
 * You can set a label ans an icon to describe the interface.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IActionHandler extends Serializable{
 
    void execute();
    
    String getLabel();
    
    void setLabel(String label);
    
    String getIcon();
    
    void setIcon(String path);
    
    void addElementListeners(IElementListener elementListener);
}
