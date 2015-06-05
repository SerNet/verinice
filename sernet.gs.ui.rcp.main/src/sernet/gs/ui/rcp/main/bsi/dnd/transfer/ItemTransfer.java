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
package sernet.gs.ui.rcp.main.bsi.dnd.transfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import sernet.verinice.service.iso27k.Item;

/**
 *
 */
public final class ItemTransfer extends ByteArrayTransfer {
    
    private static final String TYPENAME_ITEMELEMENT = Item.class.getCanonicalName();
    private static final int TYPEID_ITEMELEMENT = registerType(TYPENAME_ITEMELEMENT);
    
    private static Logger log = Logger.getLogger(ItemTransfer.class);
    
    private static ItemTransfer instance = new ItemTransfer();
    
    public static ItemTransfer getInstance(){
        return instance;
    }
    
    private ItemTransfer(){}

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[]{TYPENAME_ITEMELEMENT};
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[]{TYPEID_ITEMELEMENT};
    }
    
    public void javaToNative (Object data, TransferData transferData){
        if (data == null || !(validateData(data))) {return;}
        if (isSupportedType(transferData)) {
            ArrayList<Item> elements = new ArrayList<Item>(0);
            if(data instanceof Item[]){
                Item[] itemElements = (Item[]) data;
                for(Item b : itemElements){
                    elements.add(b);
                }
            } else if (data instanceof Item){
                elements.add((Item)data);
            }
            ByteArrayOutputStream out = null;
            ObjectOutputStream objectOut = null;
            try{
                out = new ByteArrayOutputStream();
                objectOut = new ObjectOutputStream(out);
                
                objectOut.writeObject(elements.toArray(new Object[elements.size()]));
                
                super.javaToNative(out.toByteArray(), transferData);
            } catch (IOException e){
                getLog().error("Error while serializing object for dnd", e);
            } finally {
                if(out != null && objectOut != null){
                    try {
                        out.close();
                        objectOut.close();
                    } catch (IOException e) {
                        getLog().error("Error while closing stream", e);
                    }
                }
            }
        }
    }
    
    public Object nativeToJava(TransferData transferData){
        Object o = null;
        if(transferData == null){
            getLog().error("transferData is null");
        }
        if(isSupportedType(transferData)){
            byte[] bs = (byte[]) super.nativeToJava(transferData);
            if(bs != null){
                ByteArrayInputStream bis = new ByteArrayInputStream(bs);
                ObjectInput in;
                try {
                    in = new ObjectInputStream(bis);
                    o = in.readObject();
                    bis.close();
                    in.close();
                } catch (OptionalDataException e){
                    getLog().error("Wrong data", e);
                } catch (IOException e) {
                    getLog().error("Error while transfering dnd object back to java", e);
                } catch (ClassNotFoundException e) {
                    getLog().error("Error while transfering dnd object back to java", e);
                }
            } else {
                getLog().error("bs is null");
                if(transferData == null){
                    getLog().error("transferData also");
                }
            }
        }
        return o;
    }
    
    private boolean validateData(Object data){
        return data instanceof Item || data instanceof Item[];
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(ItemTransfer.class);
        }
        return log;
    }

}
