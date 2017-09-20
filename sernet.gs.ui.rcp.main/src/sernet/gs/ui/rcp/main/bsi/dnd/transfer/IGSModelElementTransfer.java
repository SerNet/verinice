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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;

import sernet.gs.model.IGSModel;

/**
 *
 */
public final class IGSModelElementTransfer extends VeriniceElementTransfer {

    private static final Logger log = Logger.getLogger(IGSModelElementTransfer.class);
    
    private static final String TYPENAME_IGSMODELELEMENT = "igsModelElement";
    private static final int TYPEID_IGSMODELELEMENT = registerType(TYPENAME_IGSMODELELEMENT);
    
    private static IGSModelElementTransfer instance = new IGSModelElementTransfer();
    
    public static IGSModelElementTransfer getInstance(){
        return instance;
    }
    
    private IGSModelElementTransfer(){}
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[]{TYPENAME_IGSMODELELEMENT};
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[]{TYPEID_IGSMODELELEMENT};
    }
    
    public void javaToNative (Object data, TransferData transferData){
        if (data == null || !(validateData(data))) {return;}
        if (isSupportedType(transferData)) {
            ArrayList<IGSModel> igsElements = new ArrayList<IGSModel>(0);
            if(data instanceof IGSModel[]){
                IGSModel[] igsModelElements = (IGSModel[]) data;
                for(IGSModel b : igsModelElements){
                    igsElements.add(b);
                }
            } else if (data instanceof IGSModel){
                igsElements.add((IGSModel)data);
            }
            ByteArrayOutputStream out = null;
            ObjectOutputStream objectOut = null;
            try{
                out = new ByteArrayOutputStream();
                objectOut = new ObjectOutputStream(out);
                
                objectOut.writeObject(igsElements.toArray(new Object[igsElements.size()]));
                
                super.javaToNative(out.toByteArray(), transferData);
            } catch (IOException e){
                log.error("Error while serializing object for dnd", e);
            } finally {
                if(out != null && objectOut != null){
                    try {
                        out.close();
                        objectOut.close();
                    } catch (IOException e) {
                        log.error("Error while closing stream", e);
                    }
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer#
     * validateData(java.lang.Object)
     */
    @Override
    public boolean validateData(Object data){
        return (data instanceof IGSModel[]||
                data instanceof IGSModel);
    }

}
