/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah[at]sernet[dot]de>
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Anne Hanekop <ah[at]sernet[dot]de> 	- initial API and implementation
 *     ak[at]sernet[dot]de					- various fixes, adapted to command layer
 ******************************************************************************/
/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;
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

import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;

/**
 * @author ahanekop[at]sernet[dot]de
 *
 */
public class RisikoMassnahmenUmsetzungTransfer extends ByteArrayTransfer {
	
	private static final String TYPE_NAME = "RisikoMassnahmenTransfer";

    private static final int TYPE_ID = registerType(TYPE_NAME);

    private static RisikoMassnahmenUmsetzungTransfer instance = new RisikoMassnahmenUmsetzungTransfer();
    
    private static final Logger LOG = Logger.getLogger(RisikoMassnahmenUmsetzungTransfer.class);

    public static RisikoMassnahmenUmsetzungTransfer getInstance() {
      return instance;
    }

    /**
     *  returns the type id this TransferAgent is able
     *  to process.
     *  
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPE_ID };
	}

	/**
	 *  returns the name of the type this Transferagent
	 *  is able to process.
	 *  
	 *  @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}
	
	/**
	 * converts a Java representation of data into a
	 * platform-specific one.
	 */
	   public void javaToNative (Object data, TransferData transferData){
	        if (data == null || !(validateData(data))) {return;}
	        if (isSupportedType(transferData)) {
	            ArrayList<RisikoMassnahmenUmsetzung> rMassnahmen = new ArrayList<RisikoMassnahmenUmsetzung>(0);
	            if(data instanceof RisikoMassnahmenUmsetzung[]){
	                RisikoMassnahmenUmsetzung[] rMassnahmenElements = (RisikoMassnahmenUmsetzung[]) data;
	                for(RisikoMassnahmenUmsetzung b : rMassnahmenElements){
	                    rMassnahmen.add(b);
	                }
	            } else if (data instanceof RisikoMassnahmenUmsetzung){
	                rMassnahmen.add((RisikoMassnahmenUmsetzung)data);
	            }
	            ByteArrayOutputStream out = null;
	            ObjectOutputStream objectOut = null;
	            try{
	                out = new ByteArrayOutputStream();
	                objectOut = new ObjectOutputStream(out);
	                
	                objectOut.writeObject(rMassnahmen.toArray(new Object[rMassnahmen.size()]));
	                
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
	        return (data instanceof RisikoMassnahmenUmsetzung[]||
	                data instanceof RisikoMassnahmenUmsetzung);
	    }
}
