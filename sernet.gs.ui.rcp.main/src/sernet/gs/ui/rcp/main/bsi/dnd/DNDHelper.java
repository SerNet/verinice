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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BausteinElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BausteinUmsetzungTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IBSIStrukturElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IGSModelElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kGroupTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ItemTransfer;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.service.iso27k.Item;

/**
 *
 */
public final class DNDHelper {
    
    private static final Logger LOG = Logger.getLogger(DNDHelper.class);
    
    private static Class<?> classes[] = new Class[]{Baustein.class,
                                             Massnahme.class,
                                             Gefaehrdung.class,
                                             IBSIStrukturElement.class,
                                             IISO27kElement.class,
                                             Item.class};
    
    private static Class<?> transferClasses[] = new Class[]{ BausteinElementTransfer.class,
                                                             BausteinUmsetzungTransfer.class,
                                                             IBSIStrukturElementTransfer.class,
                                                             ISO27kElementTransfer.class,
                                                             ISO27kGroupTransfer.class,
                                                             ItemTransfer.class,
                                                             IGSModelElementTransfer.class
                                                };
    
    private static final String STD_ERR_MSG = "Error while casting dnd list";
    private static final String ERR_MSG = "Error:";
    
    private DNDHelper(){}
    
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
    
    /**
     * creates an array of one of the types contained in classes,
     * so transferclasses can check for correct type of dnd'ed array
     * @param source
     * @return
     */
    public static Object[] castDataArray(Object[] source){
        List<?> dest = null;
        Class<?> type = Object.class;
        for(Class<?> c : classes){
            if(source.length > 0 && c.isInstance(source[0])){
                type = c;
                break;
            }
        }
        dest = createListOfType(type);
        for(Object o : source){
            if(type.isInstance(o)){
                Method m;
                try {
                    m = dest.getClass().getDeclaredMethod("add", new Class[]{Object.class});
                    m.invoke(dest, type.cast(o));
                } catch (SecurityException e) {
                    LOG.error(STD_ERR_MSG, e);
                } catch (NoSuchMethodException e) {
                    LOG.error(STD_ERR_MSG, e);
                } catch (IllegalArgumentException e) {
                    LOG.error(STD_ERR_MSG, e);
                } catch (IllegalAccessException e) {
                    LOG.error(STD_ERR_MSG, e);
                } catch (InvocationTargetException e) {
                    LOG.error(STD_ERR_MSG, e);
                }
            } else {
                return new Object[0];
            }
        }
        
        return dest.toArray((Object[])Array.newInstance(type, dest.size()));
    }
    
    private static <T> List<T> createListOfType(Class<T> type){
        return new ArrayList<T>();
    }
    
    public static String getTransferType(TransferData transferData){
        for(Class<?> clazz : transferClasses){
            try {
                Method getTypeIDs = clazz.getMethod("getTypeIds", null);
                if(!getTypeIDs.isAccessible()){
                    getTypeIDs.setAccessible(true);
                }
                int[] typeIDs = (int[]) getTypeIDs.invoke(transferData, null);
                for(int typeID : typeIDs){
                    if(Integer.parseInt(String.valueOf(transferData.type)) == typeID){
                        return clazz.getCanonicalName();
                    }
                }
            } catch (SecurityException e) {
                LOG.error(ERR_MSG, e);
            } catch (NoSuchMethodException e) {
                LOG.error(ERR_MSG, e);
            } catch (IllegalArgumentException e) {
                LOG.error(ERR_MSG, e);
            } catch (IllegalAccessException e) {
                LOG.error(ERR_MSG, e);
            } catch (InvocationTargetException e) {
                LOG.error(ERR_MSG, e);
            }
            
        }
        return "";
    }

}
