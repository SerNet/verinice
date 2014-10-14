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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Isa20Mapper is used by command {@link LoadUnifyMapping} to create a mapping between
 * {@link CnATreeElement}s. Isa20Mapper is mapping following the rules for ISA 1 to 2.0
 * migrations.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Isa20Mapper extends IsaMapper implements IElementMapper {

    public static final String ID = "unify.mapper.isa.2.0";
    
    private static final Map<String, String[]> MAP_FROM_ISA_1_TO_2;
    static {
        MAP_FROM_ISA_1_TO_2 = new Hashtable<String, String[]>();
        MAP_FROM_ISA_1_TO_2.put("5.1",new String[]{"5.1"}); 
        MAP_FROM_ISA_1_TO_2.put("6.1",new String[]{"6.1"});
        MAP_FROM_ISA_1_TO_2.put("11.10",new String[]{"6.3"});
        MAP_FROM_ISA_1_TO_2.put("8.1",new String[]{"7.1"});
        MAP_FROM_ISA_1_TO_2.put("8.2",new String[]{"7.2"});
        MAP_FROM_ISA_1_TO_2.put("7.1",new String[]{"8.1"});
        MAP_FROM_ISA_1_TO_2.put("7.2",new String[]{"8.2"});
        MAP_FROM_ISA_1_TO_2.put("10.12",new String[]{"8.3"});        
        MAP_FROM_ISA_1_TO_2.put("11.6",new String[]{"9.1"});
        MAP_FROM_ISA_1_TO_2.put("11.1",new String[]{"9.2"});
        MAP_FROM_ISA_1_TO_2.put("11.2",new String[]{"9.3"});
        MAP_FROM_ISA_1_TO_2.put("11.3",new String[]{"9.4"});
        MAP_FROM_ISA_1_TO_2.put("8.3",new String[]{"9.5"});
        MAP_FROM_ISA_1_TO_2.put("12.1",new String[]{"10.1"});     
        MAP_FROM_ISA_1_TO_2.put("9.1",new String[]{"11.1"});
        MAP_FROM_ISA_1_TO_2.put("9.2",new String[]{"11.2"});
        MAP_FROM_ISA_1_TO_2.put("9.4",new String[]{"11.3"});
        MAP_FROM_ISA_1_TO_2.put("9.5",new String[]{"11.4"});
        MAP_FROM_ISA_1_TO_2.put("10.1",new String[]{"12.1"});
        MAP_FROM_ISA_1_TO_2.put("10.2",new String[]{"12.2"});
        MAP_FROM_ISA_1_TO_2.put("10.4",new String[]{"12.3"});
        MAP_FROM_ISA_1_TO_2.put("10.7",new String[]{"12.4"});
        MAP_FROM_ISA_1_TO_2.put("10.17",new String[]{"12.5"});
        MAP_FROM_ISA_1_TO_2.put("10.16",new String[]{"12.6"});
        MAP_FROM_ISA_1_TO_2.put("12.3",new String[]{"12.7"});
        MAP_FROM_ISA_1_TO_2.put("15.4",new String[]{"12.8"});
        MAP_FROM_ISA_1_TO_2.put("10.8",new String[]{"13.1"});
        MAP_FROM_ISA_1_TO_2.put("11.8",new String[]{"13.3"});
        MAP_FROM_ISA_1_TO_2.put("10.15",new String[]{"13.4"});      
        MAP_FROM_ISA_1_TO_2.put("12.2",new String[]{"14.1","14.2"});       
        MAP_FROM_ISA_1_TO_2.put("6.2",new String[]{"15.1"});
        MAP_FROM_ISA_1_TO_2.put("10.3",new String[]{"15.2"});
        MAP_FROM_ISA_1_TO_2.put("13.1",new String[]{"16.1"});
        MAP_FROM_ISA_1_TO_2.put("13.2",new String[]{"16.2"});
        MAP_FROM_ISA_1_TO_2.put("14.1",new String[]{"17.1"});
        MAP_FROM_ISA_1_TO_2.put("15.1",new String[]{"18.1"});
        MAP_FROM_ISA_1_TO_2.put("15.2",new String[]{"18.2"});
        MAP_FROM_ISA_1_TO_2.put("15.3",new String[]{"18.4"});
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.commands.unify.IsaMapper#getDestinationKey(java.util.Map.Entry)
     */
    @Override
    protected List<String> getDestinationKey(Entry<String, CnATreeElement> sourceEntry) {
        List<String> destKeyList = new ArrayList<String>(1);
        String[] keyArray = MAP_FROM_ISA_1_TO_2.get(sourceEntry.getKey());
        if(keyArray!=null) {
            for (String key : keyArray) {
                destKeyList.add(key);
            }
        }
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
