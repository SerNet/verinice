/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bp;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author uz[at]sernet.de
 *
 */
public class DeductionImplementationUtil {

    public static final String IMPLEMENTATION_STATUS = "_implementation_status";
    public static final String IMPLEMENTATION_DEDUCE = "_implementation_deduce";

    public static final String IMPLEMENTATION_STATUS_CODE_NO = "_implementation_status_no";
    public static final String IMPLEMENTATION_STATUS_CODE_YES = "_implementation_status_yes";
    public static final String IMPLEMENTATION_STATUS_CODE_PARTIALLY = "_implementation_status_partially";
    public static final String IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE = "_implementation_status_na";

    private DeductionImplementationUtil() {
        super();
    }

    public static String getImplementationStatus(CnATreeElement element) {
        Entity entity = element.getEntity();
        return entity.getOptionValue(getImplementationStatusId(element));
    }

    public static String getImplementationStatusId(CnATreeElement element) {
        return element.getTypeId() + IMPLEMENTATION_STATUS;
    }

    public static boolean isDeduciveImplementationEnabled(CnATreeElement element) {
        String value = element.getPropertyValue(element.getTypeId() + IMPLEMENTATION_DEDUCE);
        return isSelected(value);
    }

    /**
     * Is the property selected
     *
     * @param value
     * @return
     */
    private static boolean isSelected(String value) {
        return "1".equals(value);
    }

}
