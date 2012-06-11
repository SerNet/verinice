/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

/**
 *
 */
public class DNDHelper {
    
    private static final Logger LOG = Logger.getLogger(DNDHelper.class);
    
    public static List arrayToList(Object data){
        ArrayList<Object> list = new ArrayList<Object>();
        if(data instanceof Object[]){
            Object[] o = (Object[])data;
            for(Object object : o){
                list.add(object);
            }
        } else if(data instanceof Collection){
            Collection tmp = (Collection)data;
            for(Object tmpO : tmp){
                list.add(tmpO);
            }
        } else if(data instanceof Object){
            list.add(data);
        }
        return list;
    }

}
