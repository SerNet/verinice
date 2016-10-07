/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd.transfer;

import java.io.*;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * Abstract to provide a nativeToJava method which is not different between the
 * ElementTransfer classes.
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public abstract class VeriniceElementTransfer extends ByteArrayTransfer {


    protected transient Logger log = null;

    protected Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(VeriniceElementTransfer.class);
        }
        return log;
    }

    public Object nativeToJava(TransferData transferData) {
        Object o = null;
        if (transferData == null) {
            getLog().error("transferData is null");
        }
        if (isSupportedType(transferData)) {
            byte[] bs = (byte[]) super.nativeToJava(transferData);
            if (bs != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(bs);
                ObjectInput in;
                try {
                    in = new ObjectInputStream(bis);
                    o = in.readObject();
                    bis.close();
                    in.close();
                } catch (OptionalDataException e) {
                    getLog().error("Wrong data", e);
                } catch (IOException | ClassNotFoundException e) {
                    getLog().error("Error while transfering dnd object back to java", e);
                }
            } else {
                getLog().error("bs is null");
                if (transferData == null) {
                    getLog().error("transferData also");
                }
            }
        }
        return o;
    }

    public abstract boolean validateData(Object data);

    public void doJavaToNative(byte[] byteArray, TransferData transferData) {
        super.javaToNative(byteArray, transferData);
    }
}
