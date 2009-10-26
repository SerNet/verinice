/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class SonstigeITKategorie extends CnATreeElement 
	implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "sonstitkategorie"; //$NON-NLS-1$

	public SonstigeITKategorie(CnATreeElement parent) {
		super(parent);
	}
	
	protected SonstigeITKategorie() {
		
	}

	@Override
	public String getTitel() {
		return "IT-Systeme: sonstige"; //$NON-NLS-1$
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof SonstIT)
			return true;
		return false;
	}
}
