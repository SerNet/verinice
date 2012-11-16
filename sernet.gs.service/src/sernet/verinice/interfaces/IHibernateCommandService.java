/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import sernet.hui.common.VeriniceContext;

/**
 * This interfaces makes the {@link #setWorkObjects(sernet.hui.common.VeriniceContext.State)}
 * method available for a server-side class which needs access to it.
 * 
 * <p>By not adding the method to {@link ICommandService} the client-side (which
 * does not need it anyway) cannot use it.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public interface IHibernateCommandService {
	
	public void setWorkObjects(VeriniceContext.State workObjects);

}
