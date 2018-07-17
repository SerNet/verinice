package sernet.verinice.model.bp;

/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;

/**
 * The implementation status of a safeguard or requirement
 */
public enum ImplementationStatus {
    YES, NO, PARTIALLY, NOT_APPLICABLE;

    static {
        Map<String, ImplementationStatus> m = new HashMap<>();
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_YES, YES);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_YES, YES);

        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_NO, NO);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_NO, NO);

        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_PARTIALLY, PARTIALLY);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_PARTIALLY, PARTIALLY);

        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE, NOT_APPLICABLE);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE, NOT_APPLICABLE);

        m.put(null, null);
        m.put("", null);

        implementationStatusByPropertyValue = Collections.unmodifiableMap(m);
    }

    private static final Map<String, ImplementationStatus> implementationStatusByPropertyValue;

    private final String label;

    private ImplementationStatus() {
        label = Messages.getString(getClass().getSimpleName() + "." + this.name());
    }

    public String getLabel() {
        return label;
    }

    /**
     * Find the implementation status of an element. If the given element does
     * not support an implementation status or if the implementation status is
     * an unknown/custom value, the method will return <code>null</code>. If the
     * element supports but does not have an implementation status, the method
     * will return {@link ImplementationStatus#PRISTINE}.
     * 
     * @param element
     *            the element
     * @return the appropriate implementation status or <code>null</code> for
     *         unsupported element types
     */
    public static ImplementationStatus findValue(CnATreeElement element) {

        String implementationStatus;
        if (element instanceof Safeguard) {
            implementationStatus = ((Safeguard) element).getEntity()
                    .getRawPropertyValue(Safeguard.PROP_IMPLEMENTATION_STATUS);
        } else if (element instanceof BpRequirement) {
            implementationStatus = ((BpRequirement) element).getEntity()
                    .getRawPropertyValue(BpRequirement.PROP_IMPLEMENTATION_STATUS);
        } else {
            return null;
        }

        return implementationStatusByPropertyValue.get(implementationStatus);
    }
}
