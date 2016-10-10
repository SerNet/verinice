/*******************************************************************************
 * Copyright (c) 2016 Viktor Schmidt.
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common.multiselectionlist;

import java.util.ArrayList;
import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class OptionSelectionHelper {

    public static String loadOptionLabels(Entity entity, PropertyType type) {
        List<Property> properties = entity.getProperties(type.getId()).getProperties();
        if (properties == null) {
            return "";
        }

        List<String> optionIds = getPropertiesIds(properties);
        return loadOptionLabels(type, optionIds);
    }

    public static String loadOptionLabels(PropertyType type, List<String> optionIds) {
        StringBuilder referenceLabels = new StringBuilder();
        for (String optionId : optionIds) {

            IMLPropertyOption option = type.getOption(optionId);
            if (option == null) {
                continue;
            }
            String optionName = option.getName();
            if (referenceLabels.length() == 0) {
                referenceLabels.append(optionName);
            } else {
                referenceLabels.append(" / ");
                referenceLabels.append(optionName);
            }
        }
        return referenceLabels.toString();
    }

    public static String loadReferenceLabels(Entity entity, PropertyType type) {
        List<Property> properties = entity.getProperties(type.getId()).getProperties();
        if (properties == null) {
            return "";
        }

        List<String> referencedIds = getPropertiesIds(properties);
        return loadReferenceLabels(type, referencedIds);
    }

    public static String loadReferenceLabels(PropertyType type, List<String> referencedIds) {
        StringBuilder referenceLabels = new StringBuilder();
        for (String referencedId : referencedIds) {
            String referenceName = loadReferenceLabel(type, referencedId);
            if (referenceName == null) {
                continue;
            } else if (referenceLabels.length() == 0) {
                referenceLabels.append(referenceName);
            } else {
                referenceLabels.append(" / ");
                referenceLabels.append(referenceName);
            }
        }
        return referenceLabels.toString();
    }

    private static List<String> getPropertiesIds(List<Property> properties) {
        List<String> referencedIds = new ArrayList<>(properties.size());
        for (Property property : properties) {
            referencedIds.add(property.getPropertyValue());
        }
        return referencedIds;
    }

    private static String loadReferenceLabel(PropertyType type, String referencedId) {
        IMLPropertyOption ref = getEntity(type, referencedId);
        if (ref == null) {
            return null;
        }
        return ref.getName();
    }

    private static IMLPropertyOption getEntity(PropertyType type, String referencedId) {
        for (IMLPropertyOption entity : type.getReferencedEntities()) {
            if (entity.getId().equals(referencedId)) {
                return entity;
            }
        }
        return null;
    }
}
