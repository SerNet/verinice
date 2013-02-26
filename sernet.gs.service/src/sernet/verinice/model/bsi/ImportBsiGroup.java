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
package sernet.verinice.model.bsi;

import java.util.Collection;
import java.util.Collections;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class ImportBsiGroup extends CnATreeElement implements IBSIStrukturElement {
	
    public static final String TYPE_ID = ImportBsiGroup.class.getSimpleName();

	public ImportBsiGroup(CnATreeElement model) {
		super(model);
		setEntity(new Entity(TYPE_ID));
	}

	protected ImportBsiGroup() {
	}

	@Override
	public String getTitle() {
		return Messages.ImportBsiGroup_0;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
    public boolean canContain(Object obj) {
        if (obj instanceof ITVerbund ){
            return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIStrukturElement#getKuerzel()
     */
    @Override
    public String getKuerzel() {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.iso27k.IISO27kElement#getTags()
     */
    @Override
    public Collection<? extends String> getTags() {
        return Collections.emptyList();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIStrukturElement#getSchicht()
     */
    @Override
    public int getSchicht() {
        return 1;
    }
	
}
