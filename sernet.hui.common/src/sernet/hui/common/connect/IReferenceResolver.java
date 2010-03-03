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
package sernet.hui.common.connect;

import java.util.List;

import sernet.hui.common.multiselectionlist.IMLPropertyOption;

/**
 * Must be implemented for the underlying data model and ORM mapper.
 * Resolves entities from the database that are referenced by other entities.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface IReferenceResolver {

	// TODO akoderman DB-IDs are used as identifiers for refernces, but these are not mapped as such in the Database. 
	// Consequently, when the import/export feature is ready, we must make sure that all exported references are correctly handled during
	// import, or the references will be lost
	
	
	/**
	 * Get all entities of the given type.
	 */
	public List<IMLPropertyOption> getAllEntitesForType(String referencedEntityTypeId);

	/**
	 * Allows for the creation of new entities on the spot.
	 * 
	 * @param parentEntity 
	 * @param newName 
	 */
	public void addNewEntity(Entity parentEntity, String newName);

	/**
	 * Get only those entities that match the given referenced IDs.
	 * 
	 * @param referencedEntityTypeId
	 * @param references
	 * @return
	 */
	public List<IMLPropertyOption> getReferencedEntitesForType(
			String referencedEntityTypeId, List<Property> references);

}
