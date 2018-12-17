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
import java.nio.charset.StandardCharsets;
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
 * This singleton contains the mapping from the old dataprotection to the new
 * one.
 *
 */
public final class TomMapper {
    private static final Logger LOG = Logger.getLogger(TomMapper.class);
    private static final String MAPPING_ISO_CONTROLS_CSV = "/MappingISO-Controls.csv";
    private static final String MAPPING_ITGS_CONTROLS_CSV = "/MappingITGS-Controls.csv";

    private static TomMapper instance;
    private static final int NUMBER_OF_TOMS = 8;
    private Map<String, Set<PropertyType>> isoMapping;
    private Map<String, Set<PropertyType>> itgsMapping;

    private TomMapper() {
        super();
        try {
            List<String[]> readAll = readMapping(MAPPING_ISO_CONTROLS_CSV);
            isoMapping = transformCsv(readAll);
            readAll = readMapping(MAPPING_ITGS_CONTROLS_CSV);
            itgsMapping = transformCsv(readAll);
        } catch (IOException e) {
            LOG.error("Error initalizing Mapping", e);
        }
    }

    /**
     * @param filename
     * @return
     * @throws IOException
     */
    private List<String[]> readMapping(String filename) throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(filename);
                InputStreamReader reader = new InputStreamReader(resourceAsStream,
                        StandardCharsets.UTF_8);
                CSVReader csvReader = new CSVReader(reader, ',', '"')) {
            return csvReader.readAll();
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
        if (line.length < NUMBER_OF_TOMS + 1) {
            return Collections.emptySet();
        }
        Set<PropertyType> hashSet = new HashSet<>(NUMBER_OF_TOMS);
        if (!line[1].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    Control.PROP_EUGDPR_PSEUDONYMIZATION));
        }
        if (!line[2].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    Control.PROP_EUGDPR_ENCRYPTION));
        }
        if (!line[3].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    Control.PROP_EUGDPR_CONFIDENTIALITY));
        }
        if (!line[4].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    Control.PROP_EUGDPR_INTEGRITY));
        }
        if (!line[5].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    Control.PROP_EUGDPR_AVAILABILITY));
        }
        if (!line[6].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    Control.PROP_EUGDPR_RESILIENCE));
        }
        if (!line[7].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    Control.PROP_EUGDPR_RECOVERABILITY));
        }
        if (!line[8].isEmpty()) {
            hashSet.add(HUITypeFactory.getInstance().getPropertyType(Control.TYPE_ID,
                    Control.PROP_EUGDPR_EFFECTIVENESS));
        }

        return hashSet;
    }

    public Set<PropertyType> getMapping(String controlTitle) {
        Set<PropertyType> set = isoMapping.get(controlTitle);
        if (set == null) {
            set = itgsMapping.get(controlTitle);
        }

        return set;
    }
}
