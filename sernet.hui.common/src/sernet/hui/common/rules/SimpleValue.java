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
package sernet.hui.common.rules;

public class SimpleValue implements IFillRule {

	private String value;

	public String getValue() {
		return value;
	}

	public void init(String[] params) {
		value = params != null ? params[0] : ""; //$NON-NLS-1$
	}

    /* (non-Javadoc)
     * @see sernet.hui.common.rules.IFillRule#isMultiLanguage()
     */
    public boolean isMultiLanguage() {
        return true;
    }

}
