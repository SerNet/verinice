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
package sernet.verinice.service.model;

import java.util.Map;
import java.util.Set;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public interface IObjectModelService {


    void init();

    Set<String> getRelations(String fromEntityTypeID, String toEntityTypeID);

    Set<String> getPossibleRelationPartners(String typeID);

    Set<String> getAllTypeIDs();

    Set<String> getPossibleProperties(String typeID);

    String getLabel(String id);

    String getRelationLabel(String id);

    Set<String> getPossibleChildren(String typeID);

    Set<String> getPossibleParents(String typeID);

    Map<String, Set<String>> getAllPossibleChildren();

    Map<String, Set<String>> getAllPossibleParents();

    ObjectModelContainer loadAll();

}
