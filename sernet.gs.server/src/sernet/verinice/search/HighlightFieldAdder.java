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
package sernet.verinice.search;

import org.elasticsearch.action.search.SearchRequestBuilder;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class HighlightFieldAdder {

    private HighlightFieldAdder() {
        super();
    }
    
    public static SearchRequestBuilder addAll(SearchRequestBuilder requestBuilder) {
        for(EntityType type : HUITypeFactory.getInstance().getAllEntityTypes()) {
            add(type, requestBuilder);
        }
        return requestBuilder;
    }
    
    public static SearchRequestBuilder add(String highlightedField, SearchRequestBuilder requestBuilder) {
        return requestBuilder.addHighlightedField(highlightedField);
    }

    private static SearchRequestBuilder add(EntityType type, SearchRequestBuilder requestBuilder) {
        for(String propertyTypeId : type.getAllPropertyTypeIds()){
            requestBuilder.addHighlightedField(propertyTypeId);
        }
        return requestBuilder;
    }

    
}
