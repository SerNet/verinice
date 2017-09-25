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

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;
import org.elasticsearch.common.inject.Module;

import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;

/**
 * This class is part of the drag and drop support for the modeling in IT base
 * protection It provides a method to transfer a base protection groups objects
 * to native data, see method: javaToNative (Object, TransferData)
 * 
 * @author Daniel Murygin <dm[a]sernet.de>
 */
public final class BaseProtectionModelingTransfer extends VeriniceElementTransfer {

    private static final Logger log = Logger.getLogger(BaseProtectionModelingTransfer.class);
    
    private static final String TYPE_NAME_BASE_PROTECTION_MODELING = "baseProtectionModeling";
    private static final int TYPE_ID_BASE_PROTECTION_MODELING = registerType(
            TYPE_NAME_BASE_PROTECTION_MODELING);

    private static final List<String> CLASS_NAMES = new LinkedList<>();
    static {
        CLASS_NAMES.add(BpRequirementGroup.class.getName());
    }
    
    private static BaseProtectionModelingTransfer instance = new BaseProtectionModelingTransfer();

    public static BaseProtectionModelingTransfer getInstance() {
        return instance;
    }

    private BaseProtectionModelingTransfer() {
    }

    /**
     * Transfers a base protection object to native data.
     * 
     * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object,
     *      org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public void javaToNative(Object data, TransferData transferData) {
        TransferUtil.baseProtectionGroupToNative(getInstance(), data, transferData);
    }

    @Override
    public boolean validateData(Object data) {
        return BaseProtectionModelingTransfer.isDraggedDataValid(data);
    }


    public static boolean isSupportedClass(Object arrayElement) {   
        return CLASS_NAMES.contains(arrayElement.getClass().getName());
    }
    
    public static boolean isDraggedDataValid(Object data) {
        boolean valid = true;
        Object[] dataArray = (Object[]) data;
        for (Object arrayElement : dataArray) {
            if (log.isDebugEnabled()) {
                log.debug("Validating dragged element: " + arrayElement + "...");
            }
            if (!(isSupportedClass(arrayElement))) {
                valid = false;
                break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Validation state: " + valid);
        }
        return valid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME_BASE_PROTECTION_MODELING };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        if (log.isDebugEnabled()) {
            log.debug(
                    TYPE_ID_BASE_PROTECTION_MODELING + "=" + TYPE_NAME_BASE_PROTECTION_MODELING);
        }
        return new int[] { TYPE_ID_BASE_PROTECTION_MODELING };
    }


}
