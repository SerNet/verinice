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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;

import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class TransferUtil {

    private static final Logger LOG = Logger.getLogger(TransferUtil.class);

    public static void iSO27KtoNative(VeriniceElementTransfer transfer, Object data,
            TransferData transferData) {
        if (data == null || !(transfer.validateData(data))) {
            return;
        }
        if (transfer.isSupportedType(transferData)) {
            ArrayList<IISO27kElement> elements = new ArrayList<IISO27kElement>(0);
            if (data instanceof IISO27kElement[]) {
                IISO27kElement[] bausteinElements = (IISO27kElement[]) data;
                for (IISO27kElement b : bausteinElements) {
                    elements.add(b);
                }
            } else if (data instanceof IISO27kElement) {
                elements.add((IISO27kElement) data);
            }
            ByteArrayOutputStream out = null;
            ObjectOutputStream objectOut = null;
            try {
                out = new ByteArrayOutputStream();
                objectOut = new ObjectOutputStream(out);

                objectOut.writeObject(elements.toArray(new Object[elements.size()]));

                transfer.doJavaToNative(out.toByteArray(), transferData);
            } catch (IOException e) {
                LOG.error("Error while serializing object for dnd", e);
            } finally {
                if (out != null && objectOut != null) {
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

    public static void bSIStrukturElementToNative(VeriniceElementTransfer transfer, Object data,
            TransferData transferData) {
        if (data == null || !(transfer.validateData(data))) {
            return;
        }
        if (transfer.isSupportedType(transferData)) {
            ArrayList<IBSIStrukturElement> elements = new ArrayList<IBSIStrukturElement>(0);
            if (data instanceof IBSIStrukturElement[]) {
                IBSIStrukturElement[] bsiElements = (IBSIStrukturElement[]) data;
                for (IBSIStrukturElement b : bsiElements) {
                    elements.add(b);
                }
            } else if (data instanceof IBSIStrukturElement) {
                elements.add((IBSIStrukturElement) data);
            }
            ByteArrayOutputStream out = null;
            ObjectOutputStream objectOut = null;
            try {
                out = new ByteArrayOutputStream();
                objectOut = new ObjectOutputStream(out);

                objectOut.writeObject(elements.toArray(new Object[elements.size()]));

                transfer.doJavaToNative(out.toByteArray(), transferData);
            } catch (IOException e) {
                LOG.error("Error while serializing object for dnd", e);
            } finally {
                if (out != null && objectOut != null) {
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

}
