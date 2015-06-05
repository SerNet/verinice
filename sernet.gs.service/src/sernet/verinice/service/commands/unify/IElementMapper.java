/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.service.commands.unify;

import java.util.List;
import java.util.Map;

import sernet.verinice.model.common.CnATreeElement;

/**
 * IElementMapper is used by command {@link LoadUnifyMapping} to create a mapping between
 * {@link CnATreeElement}s.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IElementMapper {
    
    /**
     * @return The id of this mapper
     */
    String getId();
    
    /**
     * Validates the source and destination elements before mapping is created.
     * If a source or destination element is not valid a {@link UnifyValidationException}
     * is thrown.
     * 
     * @param sourceMap 
     *    A map with all source elements. 
     *    Key is the number in the beginning of the elements title or the whole title if there is no number (e.g. "1.2" for "1.2 IS Risk Management")
     *    Value is the element. 
     * @param destinationMap
     *    A map with all destination elements. 
     *    Key is the number in the beginning of the elements title or the whole title if there is no number (e.g. "1.2" for "1.2 IS Risk Management").
     *    Value is the element. 
     * @throws UnifyValidationException If a source or destination element is not valid 
     */
    void validate(Map<String, CnATreeElement> sourceMap, Map<String, CnATreeElement> destinationMap) throws UnifyValidationException;
    
    /**
     * Creates a mapping between source and destination elements ({@link CnATreeElement}s).
     * 
     * @param sourceMap 
     *    A map with all source elements. 
     *    Key is the number in the beginning of the elements title or the whole title if there is no number (e.g. "1.2" for "1.2 IS Risk Management")
     *    Value is the element. 
     * @param destinationMap
     *    A map with all destination elements. 
     *    Key is the number in the beginning of the elements title or the whole title if there is no number (e.g. "1.2" for "1.2 IS Risk Management").
     *    Value is the element. 
     * @return A mapping between {@link CnATreeElement}s.
     */
    List<UnifyMapping> createMapping(Map<String, CnATreeElement> sourceMap, Map<String, CnATreeElement> destinationMap);
    
}
