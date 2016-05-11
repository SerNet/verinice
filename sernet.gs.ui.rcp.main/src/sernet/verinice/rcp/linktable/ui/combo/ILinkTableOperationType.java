/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable.ui.combo;

import sernet.verinice.rcp.linktable.ui.LinkTableComposite;

/**
 * Interface for Operation Types used by the UI
 * 
 * Ensures generic usability of {@link LinkTableComposite}.
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public interface ILinkTableOperationType {

    public String getLabel();

    public String getOutput();

    public String getDefaultMessage();

    // TODO insert method --> wait for java8
    // public static ILTR_OPERATION_TYPE getOperationType(String value);
    // public static boolean isRelation(ILinkTableOperationType type);w

    @Override
    public String toString();
}
