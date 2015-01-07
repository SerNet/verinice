/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.accountgroup;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import sernet.verinice.rcp.Messages;

/**
 *
 */
public class AccountLabelProvider extends ColumnLabelProvider {
    
    public AccountLabelProvider(){};
    
    @Override
    public String getText(Object element) {
        String text = Messages.UserprofileDialog_17;
        if (element instanceof String) {
            text = (String)element;
        }
        else {
            if(element != null){
                text = element.toString();
            } else {
                text = "No Label available";
            }
        }
        return text;
    }

}
