/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
package sernet.verinice.service.commands.bp.converter;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A base class to convert elements from the old ITBP to elements from the
 * modernized ITBP
 */
public abstract class ElementConverter<S extends CnATreeElement & IBSIStrukturElement, T extends CnATreeElement & IBpElement> {

    protected static final Logger logger = Logger.getLogger(ElementConverter.class);

    protected static final String MAXIMUMPRINZIP = "Maximumprinzip";
    protected static final String VERTEILUNGSEFFEKT = "Verteilungseffekt";
    protected static final String KUMULATIONSEFFEKT = "Kumulationseffekt";

    abstract T convert(S sourceElement);

    protected static void copyPropertyRaw(CnATreeElement sourceElement,
            String sourceElementPropertyId, CnATreeElement converted,
            String targetElementPropertyid) {

        logger.info("Copying " + sourceElement + "." + sourceElementPropertyId + " to " + converted
                + "." + targetElementPropertyid);
        PropertyType sourceType = sourceElement.getEntityType()
                .getPropertyType(sourceElementPropertyId);
        PropertyType targetType = converted.getEntityType()
                .getPropertyType(targetElementPropertyid);
        logger.debug("sourceType: " + sourceType + ", targetType: " + targetType);

        if (sourceType.getInputName().equals(targetType.getInputName())) {
            Optional.ofNullable(
                    sourceElement.getEntity().getRawPropertyValue(sourceElementPropertyId))
                    .ifPresent(value -> {
                        logger.debug("Found value: " + value);
                        converted.getEntity().setSimpleValue(targetType, value);
                    });
        } else {
            logger.warn("Not converting " + sourceType + " to " + targetType
                    + ", different types, add special handling");
        }
    }

    protected static void copyProperties(CnATreeElement sourceElement, CnATreeElement converted,
            Map<String, String> identityMappings) {
        identityMappings.forEach((sourceElementPropertyId, targetElementPropertyid) -> {

            PropertyType sourceType = sourceElement.getEntityType()
                    .getPropertyType(sourceElementPropertyId);
            PropertyType targetType = converted.getEntityType()
                    .getPropertyType(targetElementPropertyid);
            if (sourceType == null) {
                logger.warn("Unknown source property " + sourceElementPropertyId + " in "
                        + sourceElement.getEntityType());
                return;
            }
            if (targetType == null) {
                logger.warn("Unknown target property " + targetElementPropertyid + " in "
                        + converted.getEntityType());
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("sourceType: " + sourceType + ", targetType: " + targetType);
            }
            if (sourceType.getInputName().equals(targetType.getInputName())) {
                if (sourceType.isLine() || sourceType.isText()) {
                    copyPropertyRaw(sourceElement, sourceElementPropertyId, converted,
                            targetElementPropertyid);
                } else {
                    throw new IllegalArgumentException(
                            "Illegal entry for " + sourceElementPropertyId + ", unsupported type "
                                    + sourceType.getInputName());
                }
            } else {
                throw new IllegalArgumentException("Illegal entry for " + sourceElementPropertyId
                        + ", source and target types do not match");
            }

        });
    }

    protected void migrateSelectValue(CnATreeElement sourceElement, String sourcePropertyId,
            CnATreeElement converted, String targetPropertyId, Map<String, String> valueMappings) {
        PropertyList values = sourceElement.getEntity().getTypedPropertyLists()
                .get(sourcePropertyId);
        if (values == null || values.isEmpty() || values.getProperties().stream()
                .map(Property::getPropertyValue).allMatch(StringUtils::isEmpty)) {
            logger.debug("No value found for " + sourcePropertyId);
            return;
        }
        PropertyType targetType = converted.getEntityType().getPropertyType(targetPropertyId);
        boolean targeTypeIsMultiSelect = targetType.isMultiselect();
        if (!targeTypeIsMultiSelect && values.getProperties().size() > 1) {
            logger.warn("Not converting " + sourcePropertyId + " from " + sourceElement
                    + " multiple values found for single select option");
        }
        boolean targetTypeIsNumericSelect = targetType.isNumericSelect();
        for (Property property : values.getProperties()) {
            String sourceValue = property.getPropertyValue();
            if (valueMappings.containsKey(sourceValue)) {
                String targetId = valueMappings.get(sourceValue);
                PropertyOption targetOption = targetType.getOption(targetId);
                if (targetOption != null) {
                    String targetValue = targetTypeIsNumericSelect
                            ? String.valueOf(targetOption.getValue())
                            : targetOption.getId();
                    logger.info("Converting " + sourceValue + " to " + targetValue);
                    if (targeTypeIsMultiSelect) {
                        converted.getEntity().createNewProperty(targetType, targetValue);
                    } else {
                        converted.setSimpleProperty(targetPropertyId, targetValue);
                    }
                } else {
                    logger.warn("Not converting " + sourcePropertyId + " from " + sourceElement
                            + " target id " + targetId + " not found in SNCA");
                }
            } else {
                logger.warn("Not converting " + sourcePropertyId + " from " + sourceElement
                        + ", value " + sourceValue + " not found in mapping " + valueMappings);
            }
        }

    }

    protected void convertCIAValues(CnATreeElement sourceElement, String sourcePropertyId,
            CnATreeElement converted, String methodPropertyId, String commentPropertyId,
            String valueMethodPropertyId) {
        String vertraulichkeitBegruendung = sourceElement.getEntity()
                .getRawPropertyValue(sourcePropertyId);

        boolean isMaximumPrinzip = MAXIMUMPRINZIP.equals(vertraulichkeitBegruendung);
        boolean isVerteilungseffekt = VERTEILUNGSEFFEKT.equals(vertraulichkeitBegruendung);
        boolean isKumulationseffekt = KUMULATIONSEFFEKT.equals(vertraulichkeitBegruendung);

        boolean method = isMaximumPrinzip;
        String valueComment = isMaximumPrinzip || isVerteilungseffekt || isKumulationseffekt ? null
                : vertraulichkeitBegruendung;
        int valueMethod = 0;
        if (isVerteilungseffekt) {
            valueMethod = 1;
        }
        if (isKumulationseffekt) {
            valueMethod = 2;
        }

        Entity targetEntity = converted.getEntity();

        targetEntity.setFlag(valueMethodPropertyId, method);
        if (valueComment != null) {
            converted.setSimpleProperty(commentPropertyId, valueComment);
        }
        converted.getEntity().setNumericValue(
                converted.getEntityType().getPropertyType(methodPropertyId), valueMethod);
    }

    protected static Entry<String, String> entry(String key, String value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}
