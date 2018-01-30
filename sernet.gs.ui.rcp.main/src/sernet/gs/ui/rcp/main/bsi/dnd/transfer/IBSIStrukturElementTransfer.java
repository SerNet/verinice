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

import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IMassnahmeUmsetzung;

public final class IBSIStrukturElementTransfer extends VeriniceElementTransfer {
    
    private static final String TYPENAME_IBSIELEMENT = "bsiElement";
    private static final int TYPEID_ISBSIELEMENT = registerType(TYPENAME_IBSIELEMENT);
    
    private static IBSIStrukturElementTransfer instance = new IBSIStrukturElementTransfer();
    
    public static IBSIStrukturElementTransfer getInstance(){
        return instance;
    }
    
    private IBSIStrukturElementTransfer(){}

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[]{TYPENAME_IBSIELEMENT};
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[]{TYPEID_ISBSIELEMENT};
    }
    
    public void javaToNative (Object data, TransferData transferData){
        TransferUtil.bSIStrukturElementToNative(getInstance(), data, transferData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer#
     * validateData(java.lang.Object)
     */
    @Override
    public boolean validateData(Object data) {
        return data instanceof IBSIStrukturElement || data instanceof IBSIStrukturElement[]
                || data instanceof IMassnahmeUmsetzung || data instanceof IMassnahmeUmsetzung[]
                || data instanceof BausteinUmsetzung || data instanceof BausteinUmsetzung[];
    }

}
