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

import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.common.CnATreeElement;

public final class BCMUtils {

    private static final Map<String, BCMProperties> PROPERTIES_BY_TYPE_ID = new ConcurrentHashMap<>();

    private static final int NO_MTPD = 0;
    private static final int MTPD_UNEDITED = -1;
    private static final int DAMAGE_POTENTIAL_VALUE_UNEDITED = 0;
    public static final String DAMAGE_POTENTIAL_VALUE_UNEDITED_RAW = Integer
            .toString(DAMAGE_POTENTIAL_VALUE_UNEDITED);

    private static final String IMPACT_VALUE_UNEDITED_RAW = "0";

    private BCMUtils() {
    }

    public static BCMProperties getPropertiesForElement(CnATreeElement element) {
        String typeId = element.getTypeId();
        return PROPERTIES_BY_TYPE_ID.computeIfAbsent(typeId, BCMProperties::new);
    }

    public static boolean isMtpdCalculationEnabled(CnATreeElement element) {
        return element.getEntity()
                .isFlagged(getPropertiesForElement(element).deductionFlagProperty);
    }

    public static void updateMtpd(CnATreeElement element, Integer damagePotentialValue) {
        if (!isMtpdCalculationEnabled(element)) {
            return;
        }
        DamagePotentialAssessment damagePotentialAssessment = performDamageAssessment(element,
                damagePotentialValue);
        BCMProperties properties = getPropertiesForElement(element);

        if (damagePotentialAssessment == DamagePotentialAssessment.NOT_REACHED) {
            element.setNumericProperty(properties.propertyMtpd, NO_MTPD);
        } else if (damagePotentialAssessment == DamagePotentialAssessment.UNKNOWN) {
            element.setNumericProperty(properties.propertyMtpd, MTPD_UNEDITED);
        } else {
            element.setNumericProperty(properties.propertyMtpd,
                    damagePotentialAssessment.ordinal());
        }

    }

    private static DamagePotentialAssessment performDamageAssessment(CnATreeElement element,
            Integer damagePotentialValue) {
        if (damagePotentialValue.intValue() == DAMAGE_POTENTIAL_VALUE_UNEDITED) {
            return DamagePotentialAssessment.UNKNOWN;
        }
        BCMProperties properties = getPropertiesForElement(element);

        String[] impactProperties = new String[] { properties.propertyImpact24h,
                properties.propertyImpact3d, properties.propertyImpact7d,
                properties.propertyImpact14d, properties.propertyImpact30d };

        boolean allValuesPresent = true;

        for (int i = 0; i < impactProperties.length; i++) {
            String impactProperty = impactProperties[i];

            String sourceValueRaw = element.getEntity().getRawPropertyValue(impactProperty);
            if (sourceValueRaw == null || sourceValueRaw.isEmpty()
                    || IMPACT_VALUE_UNEDITED_RAW.equals(sourceValueRaw)) {
                allValuesPresent = false;
                continue;
            }
            Integer sourceValue = Integer.valueOf(sourceValueRaw);
            if (sourceValue >= damagePotentialValue) {
                return DamagePotentialAssessment.values()[i + 1];
            }
        }
        return allValuesPresent ? DamagePotentialAssessment.NOT_REACHED
                : DamagePotentialAssessment.UNKNOWN;

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

    public static boolean updateProcessZeitkritisch(CnATreeElement element,
            String damagePotentialValueRaw) {
        if (!(element.getTypeId().equals(BusinessProcess.TYPE_ID))) {
            throw new IllegalArgumentException("Cannot handle " + element);
        }
        if (!element.getEntity().isFlagged(BusinessProcess.PROP_DEDUCE_PROCESS_ZEITKRITISCH)) {
            return false;
        }
        String value = BusinessProcess.PROP_PROCESS_ZEITKRITISCH_MISSING_DATA;
        if (damagePotentialValueRaw != null && !damagePotentialValueRaw.isEmpty()) {

            Integer damagePotentialValue = Integer.valueOf(damagePotentialValueRaw);
            DamagePotentialAssessment damagePotentialAssessment = performDamageAssessment(element,
                    damagePotentialValue);
            switch (damagePotentialAssessment) {
            case UNKNOWN:
                // missing data, nothing to do;
                break;
            case NOT_REACHED:
                value = BusinessProcess.PROP_PROCESS_ZEITKRITISCH_NO;
                break;
            default:
                value = BusinessProcess.PROP_PROCESS_ZEITKRITISCH_YES;
            }
        }
        element.getEntity().setSimpleValue(
                element.getEntityType().getPropertyType(BusinessProcess.PROP_PROCESS_ZEITKRITISCH),
                value);
        return !value.equals(BusinessProcess.PROP_PROCESS_ZEITKRITISCH_MISSING_DATA);

    }

    public static class BCMProperties {

        public final String propertyMtpd;
        public final String deductionFlagProperty;
        public final String propertyImpact24h;
        public final String propertyImpact3d;
        public final String propertyImpact7d;
        public final String propertyImpact14d;
        public final String propertyImpact30d;
        public final String propertyMtpdOverride;
        public final String propertyMtpdMin;

        BCMProperties(String typeId) {
            this.propertyMtpd = typeId + "_bcm_mtpd1";
            this.propertyMtpdOverride = typeId + "_bcm_mtpd2";
            this.propertyMtpdMin = typeId + "_bcm_mtpdMIN";
            this.deductionFlagProperty = typeId + "_value_deduce_mtpd1";
            this.propertyImpact24h = typeId + "_bcm_tim24h";
            this.propertyImpact3d = typeId + "_bcm_time3d";
            this.propertyImpact7d = typeId + "_bcm_time7d";
            this.propertyImpact14d = typeId + "_bcm_time14d";
            this.propertyImpact30d = typeId + "_bcm_time30d";

        }

    }

    private enum DamagePotentialAssessment {
        NOT_REACHED, REACHED_AFTER_24H, REACHED_AFTER_3D, REACHED_AFTER_7D, REACHED_AFTER_14D, REACHED_AFTER_30D, UNKNOWN,
    }

}