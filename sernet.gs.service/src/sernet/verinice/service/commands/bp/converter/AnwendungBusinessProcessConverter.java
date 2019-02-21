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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bsi.Anwendung;

public class AnwendungBusinessProcessConverter
        extends ElementConverter<Anwendung, BusinessProcess> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream
            .of(entry("anwendung_kuerzel", "bp_businessprocess_abbr"),
                    entry("anwendung_name", "bp_businessprocess_name"),
                    entry("anwendung_dokument", "bp_businessprocess_document"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> PERSBEZ_VALUE_MAPPINGS = Stream
            .of(entry("anwendung_persbez_1", "bp_businessprocess_stellungnahmedsb_pbdaten_1"),
                    entry("anwendung_persbez_2", "bp_businessprocess_stellungnahmedsb_pbdaten_2"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CIA_VALUE_MAPPINGS = Stream.of(
            entry("anwendung_vertraulichkeit_normal",
                    "bp_businessprocess_value_confidentiality_normal"),
            entry("anwendung_vertraulichkeit_hoch",
                    "bp_businessprocess_value_confidentiality_high"),
            entry("anwendung_vertraulichkeit_sehrhoch",
                    "bp_businessprocess_value_confidentiality_very_high"),

            entry("anwendung_integritaet_normal", "bp_businessprocess_value_integrity_normal"),
            entry("anwendung_integritaet_hoch", "bp_businessprocess_value_integrity_high"),
            entry("anwendung_integritaet_sehrhoch", "bp_businessprocess_value_integrity_very_high"),

            entry("anwendung_verfuegbarkeit_normal",
                    "bp_businessprocess_value_availability_normal"),
            entry("anwendung_verfuegbarkeit_hoch", "bp_businessprocess_value_availability_high"),
            entry("anwendung_verfuegbarkeit_sehrhoch",
                    "bp_businessprocess_value_availability_very_high"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    BusinessProcess convert(Anwendung sourceElement) {
        BusinessProcess converted = new BusinessProcess(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);
        List<? extends String> tags = sourceElement.getTags().stream()
                .filter(tag -> !MasterConverter.TAG_MOGS_PROZESS.equals(tag))
                .collect(Collectors.toList());
        if (!tags.isEmpty()) {
            converted.getEntity().setSimpleValue(
                    converted.getEntityType().getPropertyType("bp_businessprocess_tag"),
                    String.join(", ", tags.toArray(new String[tags.size()])));
        }

        String processDescription = Stream
                .of("anwendung_erlaeuterung", "anwendung_prozess",
                        "anwendung_prozess_informationen", "anwendung_prozessbezug",
                        "anwendung_prozessbezug_begruendung")
                .map(sourceElement.getEntity()::getRawPropertyValue).filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
        converted.setSimpleProperty("bp_businessprocess_description", processDescription);

        migrateSelectValue(sourceElement, "anwendung_persbez", converted,
                "bp_businessprocess_stellungnahmedsb_pbdaten", PERSBEZ_VALUE_MAPPINGS);

        migrateSelectValue(sourceElement, "anwendung_vertraulichkeit", converted,
                "bp_businessprocess_value_confidentiality", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "anwendung_integritaet", converted,
                "bp_businessprocess_value_integrity", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "anwendung_verfuegbarkeit", converted,
                "bp_businessprocess_value_availability", CIA_VALUE_MAPPINGS);

        convertCIAValues(sourceElement, "anwendung_vertraulichkeit_begruendung", converted,
                "bp_businessprocess_method_confidentiality", "bp_businessprocess_value_comment_c",
                "bp_businessprocess_value_method_confidentiality");
        convertCIAValues(sourceElement, "anwendung_integritaet_begruendung", converted,
                "bp_businessprocess_method_integrity", "bp_businessprocess_value_comment_i",
                "bp_businessprocess_value_method_integrity");
        convertCIAValues(sourceElement, "anwendung_verfuegbarkeit_begruendung", converted,
                "bp_businessprocess_method_availability", "bp_businessprocess_value_comment_a",
                "bp_businessprocess_value_method_availability");

        return converted;
    }

}
