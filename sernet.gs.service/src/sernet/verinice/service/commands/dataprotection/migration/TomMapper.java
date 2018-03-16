/*******************************************************************************
 * Copyright (c) 2018 Urs Zeidler.
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
package sernet.verinice.service.commands.dataprotection.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.opencsv.CSVReader;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.iso27k.Control;

/**
 *
 */
public final class TomMapper {
    private static final Logger LOG = Logger.getLogger(TomMapper.class);

    private static TomMapper instance;
    private static final int NUMBER_OF_TOMS = 8;
    private Map<String, Set<PropertyType>> isoMapping;

    public TomMapper() {
        super();
        try {
            InputStream resourceAsStream = getClass()
                    .getResourceAsStream("/MappingISO-Controls.csv");
            InputStreamReader reader = new InputStreamReader(resourceAsStream);
            CSVReader csvReader = new CSVReader(reader, ',', '"');
            List<String[]> readAll = csvReader.readAll();
            csvReader.close();
            reader.close();
            isoMapping = transformCsv(readAll);
        } catch (IOException e) {
            LOG.error("Error initalizing ISO Mapping", e);
        }
    }

    public static TomMapper getInstance() {
        if (instance == null) {
            synchronized (TomMapper.class) {// we cloud also use a lock
                if (instance != null) {// prevent double instantiation
                    return instance;
                }
                instance = new TomMapper();
            }
        }

        return instance;
    }

    private Map<String, Set<PropertyType>> transformCsv(List<String[]> readAll) {
        Map<String, Set<PropertyType>> hashMap = new HashMap<>(readAll.size());
        for (String[] line : readAll) {
            Set<PropertyType> propertyTypeSet = toPropertyTypeSet(line);
            if (!propertyTypeSet.isEmpty()) {
                hashMap.put(line[0], propertyTypeSet);
            }
        }
        return hashMap;
    }

    private Set<PropertyType> toPropertyTypeSet(String[] line) {
        Set<PropertyType> hashSet = new HashSet<>(NUMBER_OF_TOMS);
        if (line.length < NUMBER_OF_TOMS + 1) {
            return Collections.emptySet();
        }
        if (!line[1].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    "control_data_protection_objectives_eugdpr_pseudonymization"));
        }
        if (!line[2].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    "control_data_protection_objectives_eugdpr_encryption"));
        }
        if (!line[3].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    "control_data_protection_objectives_eugdpr_confidentiality"));
        }
        if (!line[4].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    "control_data_protection_objectives_eugdpr_integrity"));
        }
        if (!line[5].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    "control_data_protection_objectives_eugdpr_availability"));
        }
        if (!line[6].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    "control_data_protection_objectives_eugdpr_resilience"));
        }
        if (!line[7].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    "control_data_protection_objectives_eugdpr_recoverability"));
        }
        if (!line[8].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    "control_data_protection_objectives_eugdpr_effectiveness"));
        }

        return hashSet;
    }

    public Map<String, Set<PropertyType>> getIsoMapping() {
        return isoMapping;
    }
}
