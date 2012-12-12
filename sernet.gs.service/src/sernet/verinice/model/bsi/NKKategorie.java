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

import sernet.verinice.model.common.CnATreeElement;


public class NKKategorie extends CnATreeElement implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "netzkategorie"; //$NON-NLS-1$
	

	public NKKategorie(CnATreeElement model) {
		super(model);
	}
	
	protected NKKategorie() {
		
	}
	@Override
	public String getTitle() {
		return Messages.NKKategorie_0;
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof NetzKomponente )
			return true;
		return false;
	}
}
