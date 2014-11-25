/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
package sernet.hui.swt.widgets;


/**
 * Adds and removes a special behavior to
 * sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditor.
 * 
 * See sernet.gs.ui.rcp.main.bsi.editors.InheritanceBehavio 
 * for an exemplary implementation.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IEditorBehavior {
    
    /**
     * Initializing the behavior
     */
    void init();
    
    /**
     * Adds the behavior to the editor fields 
     * e.g. by adding listeners to some fields
     */
    void addBehavior();
    
    /**
     * Removes the same behavior added in addBehavior()
     * e.g. by removing some listeners
     */
    void removeBehavior();
}
