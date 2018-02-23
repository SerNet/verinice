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

import java.util.Hashtable;
import java.util.Map;

import sernet.verinice.model.common.CnATreeElement;

/**
 * ElementMapperFactory is used by command {@link LoadUnifyMapping} to create a mapping between
 * {@link CnATreeElement}s. Returns a IElementMapper for elements for a given id.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class ElementMapperFactory {
    
    public static final String DEFAULT_MAPPER_ID = IsaMapper.ID;
    
    private static final Map<String, IElementMapper> MAPPER_MAP; 
    static {
        MAPPER_MAP = new Hashtable<>();
        MAPPER_MAP.put(IsaMapper.ID,new IsaMapper());
        MAPPER_MAP.put(Isa20Mapper.ID,new Isa20Mapper());
        MAPPER_MAP.put(DEFAULT_MAPPER_ID,new IsaMapper());
    }
    
    public static IElementMapper getMapper(String id) {
        if(id==null) {
            id = DEFAULT_MAPPER_ID;
        }
        IElementMapper result = MAPPER_MAP.get(id);
        if(result==null) {
            result = MAPPER_MAP.get(DEFAULT_MAPPER_ID);
        }
        return result;
    }

    private ElementMapperFactory() {

    }
}
