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

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;

import sernet.verinice.model.iso27k.IISO27kElement;

/**
 *
 */
public final class ISO27kElementTransfer extends VeriniceElementTransfer {
    
    private static final Logger log = Logger.getLogger(ISO27kElementTransfer.class);
    
    private static final String TYPENAME_ISOELEMENT = "isoElement";
    private static final int TYPEID_ISOELEMENT = registerType(TYPENAME_ISOELEMENT);
    
    private static ISO27kElementTransfer instance = new ISO27kElementTransfer();
    
    public static ISO27kElementTransfer getInstance(){
        return instance;
    }
    
    private ISO27kElementTransfer(){}

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[]{TYPENAME_ISOELEMENT};
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        if (log.isDebugEnabled()) {
            log.debug(TYPEID_ISOELEMENT + "=" + TYPENAME_ISOELEMENT);
        }
        return new int[]{TYPEID_ISOELEMENT};
    }
    
    public void javaToNative (Object data, TransferData transferData){
        TransferUtil.iSO27KtoNative(getInstance(), data, transferData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer#
     * validateData(java.lang.Object)
     */
    @Override
    public boolean validateData(Object data) {
        return data instanceof IISO27kElement[] ||
                data instanceof IISO27kElement;
    }

}
