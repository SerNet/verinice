/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.service.commands.unify;

import java.io.Serializable;

/**
 * Instances of this class are either the source
 * or the destination of a mapping defined in class UnifyMapping.
 * 
 * See comments in class/command Unify to understand what unify means.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @see sernet.verinice.service.commands.unify.UnifyMapping
 */
public class UnifyElement implements Serializable{

    private static final long serialVersionUID = -9047016000393432421L;
    
    private String uuid;    
    private String title;
   
    /**
     * Creates a unify element.
     * 
     * @param uuid The uuid of a verinice element
     * @param title The title of a verinice element
     */
    public UnifyElement(String uuid, String title) {
        super();
        this.uuid = uuid;
        this.title = title;
    }
    

    public String getUuid() {
        return uuid;
    }


    public String getTitle() {
        return title;
    }    
}
