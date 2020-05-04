/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.search;

import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;

/**
 * Implementing classes creates JSON documents for indexing in ElasticSearch
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IJsonBuilder {

    /**
     * @return A JSON document from an element for indexing in ElasticSearch
     */
    String getJson(CnATreeElement element);

    default boolean isIndexableElement(CnATreeElement element) {
        return !(element instanceof ImportIsoGroup || element instanceof ImportBsiGroup
                || element instanceof ImportBpGroup);
    }
}
