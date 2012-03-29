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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    private static RisikoMassnahmenUmsetzungTransfer INSTANCE = new RisikoMassnahmenUmsetzungTransfer();

    public static RisikoMassnahmenUmsetzungTransfer getInstance() {
      return INSTANCE;
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
	public void javaToNative(Object data, TransferData transferData) {
		if (!(data instanceof RisikoMassnahmenUmsetzung[]))
			return;
		RisikoMassnahmenUmsetzung[] items = (RisikoMassnahmenUmsetzung[]) data;

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream(out);
			dataOut.writeInt(items.length);
			for (int i = 0; i < items.length; i++) {
				dataOut.writeUTF(items[i].getText());
				//dataOut.writeUTF(items[i].getDecscription());
			}
			dataOut.close();
			out.close();
			super.javaToNative(out.toByteArray(), transferData);
		} catch (IOException e) {
			Logger.getLogger(this.getClass()).debug(e.toString());
		}
	}
	
	
	public Object nativeToJava(TransferData transferData) {
		// no native transfer provided
		return null;
	}
}
