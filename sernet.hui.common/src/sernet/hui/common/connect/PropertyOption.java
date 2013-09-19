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


import sernet.hui.common.multiselectionlist.ICheckBoxHandler;
import sernet.hui.common.multiselectionlist.IContextMenuListener;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;

/**
 * A possible option for a property, as predefined in the XML description.
 * I.e. "brown" for property type "colour".
 * 
 * @author prack
 * @version $Id: PropertyOption.java,v 1.3 2006/06/02 15:04:21 aprack Exp $
 */
public class PropertyOption implements IMLPropertyOption {		

    public static final String SINGLESELECTDUMMYVALUE = "SingleSelectDummyValue";
    
	private String id;
	private String name;
	private ICheckBoxHandler checkboxHandler;
    private Integer numericValue;
	
	public ICheckBoxHandler getCheckboxHandler() {
		return checkboxHandler;
	}

	public PropertyOption()
	{
		// Intentionally does nothing.
	}
	
	public PropertyOption(String id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	public IContextMenuListener getContextMenuListener() {
		return null;
	}

	/* (non-Javadoc)
	 * @see sernet.hui.common.multiselectionlist.IMLPropertyOption#setCheckboxHandler(sernet.hui.common.multiselectionlist.ICheckBoxHandler)
	 */
	public void setCheckboxHandler(ICheckBoxHandler checkBoxHandler) {
		this.checkboxHandler = checkBoxHandler;
	}

    /**
     * @param attribute
     */
    public void setValue(Integer attribute) {
        this.numericValue = attribute;
    }

    /**
     * @return
     */
    public Integer getValue() {
        return numericValue;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getName();
    }
	
}
