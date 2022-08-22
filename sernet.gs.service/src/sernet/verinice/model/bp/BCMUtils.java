/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.model.bp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sernet.verinice.model.common.CnATreeElement;

public final class BCMUtils {

    private static final Map<String, BCMProperties> PROPERTIES_BY_TYPE_ID = new ConcurrentHashMap<>();

    private BCMUtils() {
    }

    public static BCMProperties getPropertiesForElement(CnATreeElement element) {
        String typeId = element.getTypeId();
        return PROPERTIES_BY_TYPE_ID.computeIfAbsent(typeId, BCMProperties::new);
    }

    public static void updateMinMtpd(CnATreeElement element) {
        BCMProperties properties = getPropertiesForElement(element);
        String sourceValueRaw = element.getEntity().getRawPropertyValue(properties.propertyMtpd);
        String overrideValueRaw = element.getEntity()
                .getRawPropertyValue(properties.propertyMtpdOverride);

        Integer sourceValue = sourceValueRaw.isEmpty() ? -1 : Integer.valueOf(sourceValueRaw);
        Integer overrideValue = overrideValueRaw.isEmpty() ? -1 : Integer.valueOf(overrideValueRaw);
        Integer targetValue = Math.min(sourceValue, overrideValue);

        element.setNumericProperty(properties.propertyMtpdMin, targetValue);
    }

    public static class BCMProperties {

        public final String propertyMtpd;
        public final String propertyMtpdOverride;
        public final String propertyMtpdMin;

        BCMProperties(String typeId) {
            this.propertyMtpd = typeId + "_bcm_mtpd1";
            this.propertyMtpdOverride = typeId + "_bcm_mtpd2";
            this.propertyMtpdMin = typeId + "_bcm_mtpdMIN";
        }
    }
}