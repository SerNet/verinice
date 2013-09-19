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
package sernet.hui.common.connect;

import java.io.Serializable;

/**
 * Describes a possible relation from one HUI entity to another.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class HuiRelation implements Serializable {

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getId() {
		return id;
	}

	private String id;
	private String to;
	private String name; 
	private String reversename; 
	/**
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    private String tooltip;
	private String from;
	
	public String getReversename() {
		return reversename;
	}

	public void setReversename(String reversename) {
		this.reversename = reversename;
	}


	/**
	 * @param attribute
	 */
	public HuiRelation(String id) {
		this.id = id;
	}

    /**
     * @param sourceTypeId
     */
    public void setFrom(String sourceTypeId) {
        this.from = sourceTypeId;
    }
    
    


}
