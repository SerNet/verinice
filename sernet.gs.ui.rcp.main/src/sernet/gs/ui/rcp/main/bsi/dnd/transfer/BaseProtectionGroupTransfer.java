/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm[a]sernet.de>.
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
 *     Daniel Murygin <dm[a]sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd.transfer;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;

import sernet.verinice.model.bp.IBpGroup;

/**
 * This class is part of the drag and drop support. It provides a method
 * to transfer a base protection groups objects to native data, see method:
 * javaToNative (Object, TransferData)
 * 
 * @author Daniel Murygin <dm[a]sernet.de>
 */
public final class BaseProtectionGroupTransfer extends VeriniceElementTransfer {
    
    private static final String TYPE_NAME_BASE_PROTECTION_GROUP = "baseProtectionGroup";
    private static final int TYPE_ID_BASE_PROTECTION_GROUP = registerType(TYPE_NAME_BASE_PROTECTION_GROUP);
    
    private static BaseProtectionGroupTransfer instance = new BaseProtectionGroupTransfer();
    
    public static BaseProtectionGroupTransfer getInstance(){
        return instance;
    }
    
    private BaseProtectionGroupTransfer(){}

    /**
     * Transfers a base protection object to native data.
     * 
     * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public void javaToNative (Object data, TransferData transferData){
        TransferUtil.baseProtectionGroupToNative(getInstance(), data, transferData);
    }
    
    @Override
    public boolean validateData(Object data) {
        return data instanceof IBpGroup[] ||
                data instanceof IBpGroup;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[]{TYPE_NAME_BASE_PROTECTION_GROUP};
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        if (getLog().isDebugEnabled()) {
            getLog().debug(TYPE_ID_BASE_PROTECTION_GROUP + "=" + TYPE_NAME_BASE_PROTECTION_GROUP);
        }
        return new int[]{TYPE_ID_BASE_PROTECTION_GROUP};
    }
    
    @Override
    protected Logger getLog() {
        if(log == null){
            log = Logger.getLogger(BaseProtectionGroupTransfer.class);
        }
        return log;
    }

}
