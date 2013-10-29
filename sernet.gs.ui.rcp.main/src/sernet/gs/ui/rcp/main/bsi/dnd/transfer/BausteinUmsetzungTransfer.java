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

import sernet.verinice.model.bsi.BausteinUmsetzung;

/**
 *
 */
public final class BausteinUmsetzungTransfer extends ByteArrayTransfer {
    
    private static final String TYPENAME_BAUSTEINELEMENT = BausteinUmsetzung.class.getCanonicalName();
    private static final int TYPEID_BAUSTEINELEMENT = registerType(TYPENAME_BAUSTEINELEMENT);

    private static final Logger LOG = Logger.getLogger(BausteinUmsetzungTransfer.class);
    
    private static BausteinUmsetzungTransfer instance = new BausteinUmsetzungTransfer();
    
    public static BausteinUmsetzungTransfer getInstance(){
        return instance;
    }
    
    private BausteinUmsetzungTransfer(){}
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[]{TYPENAME_BAUSTEINELEMENT};
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[]{TYPEID_BAUSTEINELEMENT};
    }
    
    public void javaToNative (Object data, TransferData transferData){
        if (data == null || !(validateData(data))){ return;}
        if (isSupportedType(transferData)) {
            ArrayList<BausteinUmsetzung> bausteine = new ArrayList<BausteinUmsetzung>(0);
            if(data instanceof BausteinUmsetzung[]){
                BausteinUmsetzung[] bausteinElements = (BausteinUmsetzung[]) data;
                for(BausteinUmsetzung b : bausteinElements){
                    bausteine.add(b);
                }
            } else if (data instanceof BausteinUmsetzung){
                bausteine.add((BausteinUmsetzung)data);
            }
            ByteArrayOutputStream out = null;
            ObjectOutputStream objectOut = null;
            try{
                out = new ByteArrayOutputStream();
                objectOut = new ObjectOutputStream(out);
                
                objectOut.writeObject(bausteine.toArray(new Object[bausteine.size()]));
                
                super.javaToNative(out.toByteArray(), transferData);
            } catch (IOException e){
                LOG.error("Error while serializing object for dnd", e);
            } finally {
                if(out != null && objectOut != null){
                    try {
                        out.close();
                        objectOut.close();
                    } catch (IOException e) {
                        LOG.error("Error while closing stream", e);
                    }
                }
            }
        }
    }
    
    public Object nativeToJava(TransferData transferData){
        Object o = null;
        if(isSupportedType(transferData)){
            byte[] bs = (byte[]) super.nativeToJava(transferData);
            ByteArrayInputStream bis = new ByteArrayInputStream(bs);
            ObjectInput in;
            try {
                in = new ObjectInputStream(bis);
                o = in.readObject();
                bis.close();
                in.close();
            } catch (OptionalDataException e){
                LOG.error("Wrong data", e);
            } catch (IOException e) {
                LOG.error("Error while transfering dnd object back to java", e);
            } catch (ClassNotFoundException e) {
                LOG.error("Error while transfering dnd object back to java", e);
            }
        }
        return o;
    }
    
    private boolean validateData(Object data){
        return (data instanceof BausteinUmsetzung||
                data instanceof BausteinUmsetzung[]);
    }

}
