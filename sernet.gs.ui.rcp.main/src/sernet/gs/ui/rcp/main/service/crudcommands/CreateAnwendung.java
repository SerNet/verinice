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
package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.common.CnATreeElement;

public class CreateAnwendung extends CreateElement {

	public CreateAnwendung(CnATreeElement container, Class type) {
		super(container, type);
	}
	
	@Override
	public void execute() {
		super.execute();
		if (super.child instanceof Anwendung) {
			Anwendung anwendung = (Anwendung) child;
			anwendung.createCategories();
		}
	}
	
	@Override
	public Anwendung getNewElement() {
		return (Anwendung) super.getNewElement();
	}

}
