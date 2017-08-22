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
package sernet.verinice.model.moditbp.categories;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.moditbp.ModITBPCategory;
import sernet.verinice.model.moditbp.elements.ITSystem;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ITSystemCategory extends CnATreeElement implements ModITBPCategory {
    
    private static final long serialVersionUID = -8454160472717996490L;
    
    public static final String TYPE_ID = "moditbp_itsystemcategory";
    
    public ITSystemCategory(CnATreeElement parent) {
        super(parent);
    }
    
    protected ITSystemCategory() {}

    /* (non-Javadoc)
     * @see sernet.verinice.model.common.CnATreeElement#getTitle()
     */
    @Override
    public String getTitle() {
        return getTypeFactory().getMessage(TYPE_ID);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.common.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    @Override
    public boolean canContain(Object object) {
        return object instanceof ITSystem;
    }

}
