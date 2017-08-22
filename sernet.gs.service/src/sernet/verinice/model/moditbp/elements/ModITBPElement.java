/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.moditbp.elements;

import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public abstract class ModITBPElement extends CnATreeElement{

    public static final String TYPE_ID = "moditbp_abstractelement"; //$NON-NLS-1$
    
    public static final String PROP_NAME = "mitbpElement_name";
    
    public ModITBPElement (CnATreeElement parent) {
        super(parent);
    }
    
    protected ModITBPElement() {}
    
    public String getTitle() {
        return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
    };
    
    public String getTypeId() {
        return TYPE_ID;
    };
    
   

}
