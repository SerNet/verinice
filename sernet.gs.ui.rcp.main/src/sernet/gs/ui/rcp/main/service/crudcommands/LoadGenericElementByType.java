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

import java.util.List;

import sernet.verinice.interfaces.GenericCommand;

@SuppressWarnings("serial")
public class LoadGenericElementByType<T> extends GenericCommand {

	private List<T> elements;
	private Class<T> clazz;

	public LoadGenericElementByType(Class<T> type) {
		this.clazz = type;
	}

	public void execute() {
		elements = getDaoFactory().getDAO(clazz).findAll();
	}

	public List<T> getElements() {
		return elements;
	}

}
