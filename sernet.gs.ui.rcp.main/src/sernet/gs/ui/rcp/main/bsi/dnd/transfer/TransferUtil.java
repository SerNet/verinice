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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;

import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IMassnahmeUmsetzung;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 * Class for BSI and ISM "javaToNative"-methods uused by multiple
 * ElementTransfer classes ({@link SearchViewElementTransfer},
 * {@link IBSIStrukturElementTransfer} and {@link ISO27kElementTransfer} )
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class TransferUtil {

    private static final Logger LOG = Logger.getLogger(TransferUtil.class);

    private TransferUtil() {

    }

    public static void iSO27KtoNative(VeriniceElementTransfer transfer, Object data,
            TransferData transferData) {
        if (data == null || !(transfer.validateData(data))) {
            return;
        }
        if (transfer.isSupportedType(transferData)) {
            ArrayList<IISO27kElement> elements = new ArrayList<>();
            if (data instanceof IISO27kElement[]) {
                elements.addAll(Arrays.asList((IISO27kElement[]) data));
            } else if (data instanceof IISO27kElement) {
                elements.add((IISO27kElement) data);
            }
            write(transfer, transferData, elements);
        }
    }
    
    public static void baseProtectionElementToNative(BaseProtectionElementTransfer transfer, Object data,
            TransferData transferData) {
        if (data == null || !(transfer.validateData(data))) {
            return;
        }
        if (transfer.isSupportedType(transferData)) {
            ArrayList<IBpElement> elements = new ArrayList<>();
            if (data instanceof IBpElement[]) {
                elements.addAll(Arrays.asList((IBpElement[]) data));
            } else if (data instanceof IBpElement) {
                elements.add((IBpElement) data);
            }
            write(transfer, transferData, elements);
        }     
    }
    
    public static void baseProtectionGroupToNative(BaseProtectionModelingTransfer transfer, Object data,
            TransferData transferData) {
        if (data == null || !(transfer.validateData(data))) {
            return;
        }
        if (transfer.isSupportedType(transferData)) {
            List<IBpGroup> elements = new LinkedList<>();
            if (data instanceof IBpGroup[]) {
                elements.addAll(Arrays.asList((IBpGroup[]) data));
            } else if (data instanceof Object[]) {
                elements = convertToBpGroupList(data);
            } else if (data instanceof IBpGroup) {
                elements.add((IBpGroup) data);
            }
            write(transfer, transferData, elements);
        }     
    }

    protected static List<IBpGroup> convertToBpGroupList(Object data) {
        List<IBpGroup> elements = new LinkedList<>();
        for (Object o : (Object[])data) {
            if(o instanceof IBpGroup) {
                elements.add((IBpGroup) o);
            } else {
                LOG.warn("Element in drag and drop transfer data is not IBpGroup: " + o);
            }
        }
        return elements;
    }

    public static void bSIStrukturElementToNative(VeriniceElementTransfer transfer, Object data,
            TransferData transferData) {
        if (data == null || !(transfer.validateData(data))) {
            return;
        }
        if (transfer.isSupportedType(transferData)) {
            ArrayList<IBSIStrukturElement> elements = new ArrayList<>();
            if (data instanceof IBSIStrukturElement[]) {
                elements.addAll(Arrays.asList((IBSIStrukturElement[]) data));
                write(transfer, transferData, elements);
            } else if (data instanceof IBSIStrukturElement) {
                elements.add((IBSIStrukturElement) data);
                write(transfer, transferData, elements);
            }
            ArrayList<IMassnahmeUmsetzung> massnahmen = new ArrayList<>();
            if (data instanceof IMassnahmeUmsetzung[]) {
                massnahmen.addAll(Arrays.asList((IMassnahmeUmsetzung[]) data));
                write(transfer, transferData, massnahmen);
            } else if (data instanceof IMassnahmeUmsetzung) {
                massnahmen.add((IMassnahmeUmsetzung) data);
                write(transfer, transferData, massnahmen);
            }

            ArrayList<BausteinUmsetzung> bausteine = new ArrayList<>();
            if(data instanceof BausteinUmsetzung[]){
                bausteine.addAll(Arrays.asList((BausteinUmsetzung[]) data));
                write(transfer, transferData, bausteine);
            }else if(data instanceof BausteinUmsetzung){
                bausteine.add((BausteinUmsetzung) data);
                write(transfer, transferData, bausteine);
            }

        }
    }

    private static void write(VeriniceElementTransfer transfer, TransferData transferData,
            List<?> elements) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);

            objectOut.writeObject(elements.toArray(new Object[elements.size()]));

            transfer.doJavaToNative(out.toByteArray(), transferData);
        } catch (IOException e) {
            LOG.error("Error while serializing object for dnd", e);
        }
    }

}
