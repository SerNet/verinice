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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sernet.verinice.model.common.CnATreeElement;

/**
 * IsaMapper is used by command {@link LoadUnifyMapping} to create a mapping between
 * {@link CnATreeElement}s. IsaMapper is searching for elements with the same
 * number prefix in the destination map as in the source.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaMapper implements IElementMapper {

    public static final String ID = "unify.mapper.isa";
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.commands.unify.IElementMapper#createMapping(java.util.Map, java.util.Map)
     */
    @Override
    public List<UnifyMapping> createMapping(Map<String, CnATreeElement> sourceMap, Map<String, CnATreeElement> destinationMap) {
        List<UnifyMapping> internalMappings = new ArrayList<UnifyMapping>(sourceMap.size());      
        for(Entry<String, CnATreeElement> sourceEntry : sourceMap.entrySet()){
            CnATreeElement source = sourceEntry.getValue();
            UnifyMapping mapping = new UnifyMapping(new UnifyElement(source.getUuid(), source.getTitle())); 
            List<String> destinationKeyList = getDestinationKey(sourceEntry);
            for (String destinationKey : destinationKeyList) {
                CnATreeElement destination = destinationMap.get(destinationKey);
                if(destination!=null) {
                    mapping.addDestinationElement(new UnifyElement(destination.getUuid(), destination.getTitle()));
                }
            }         
            internalMappings.add(mapping);
        }
        return internalMappings;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.commands.unify.IElementMapper#validate(java.util.Map, java.util.Map)
     */
    @Override
    public void validate(Map<String, CnATreeElement> sourceMap, Map<String, CnATreeElement> destinationMap) throws UnifyValidationException {
        // no validation needed in this mapper
        
    }

    protected List<String> getDestinationKey(Entry<String, CnATreeElement> sourceEntry) {
        List<String> destKeyList = new ArrayList<String>(1);
        destKeyList.add(sourceEntry.getKey());
        return destKeyList;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.commands.unify.IElementMapper#getId()
     */
    @Override
    public String getId() {
        return ID;
    }

}
