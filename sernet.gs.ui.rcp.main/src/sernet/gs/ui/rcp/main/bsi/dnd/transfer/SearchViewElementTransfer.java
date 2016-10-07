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

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;

import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IMassnahmeUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 * Class to support drag and drop bbetween SearchView and
 * BSIModelView/ISMModelView
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class SearchViewElementTransfer extends VeriniceElementTransfer {

    private static final String TYPENAME_SEARCH_VIEW_ELEMENT = CnATreeElement.class
            .getCanonicalName();
    private static final int TYPEID_SEARCH_VIEW_ELEMENT = registerType(
            TYPENAME_SEARCH_VIEW_ELEMENT);
    private transient Logger log = Logger.getLogger(SearchViewElementTransfer.class);



    private static SearchViewElementTransfer instance = new SearchViewElementTransfer();

    private SearchViewElementTransfer() {
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SearchViewElementTransfer.class);
        }
        return log;
    }
    public static SearchViewElementTransfer getInstance() {
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPENAME_SEARCH_VIEW_ELEMENT };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPEID_SEARCH_VIEW_ELEMENT };
    }

    public void javaToNative(Object data, TransferData transferData) {
        if (data == null || !(validateData(data))) {
            return;
        }
        if (isSupportedType(transferData)) {
            if (data instanceof IISO27kElement[] || data instanceof IISO27kElement) {
                TransferUtil.iSO27KtoNative(getInstance(), data, transferData);
            } else if (data instanceof IBSIStrukturElement[]
                    || data instanceof IBSIStrukturElement) {
                TransferUtil.bSIStrukturElementToNative(getInstance(), data, transferData);
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
    public boolean validateData(Object data) {
        return data instanceof IBSIStrukturElement[] ||
                data instanceof IBSIStrukturElement ||
                data instanceof IISO27kElement[] ||
                data instanceof IISO27kElement || data instanceof IMassnahmeUmsetzung
                || data instanceof IMassnahmeUmsetzung[];
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer#
     * doJavaToNative(byte[], org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public void doJavaToNative(byte[] byteArray, TransferData transferData) {
        super.javaToNative(byteArray, transferData);

    }
}
