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

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bsi.Gebaeude;

public class GebaeudeConverter extends ElementConverter<Gebaeude, Room> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream
            .of(entry("gebaeude_kuerzel", "bp_room_abbr"), entry("gebaeude_name", "bp_room_name"),
                    entry("gebaeude_tag", "bp_room_tag"), entry("gebaeude_anzahl", "bp_room_count"),
                    entry("gebaeude_erlaeuterung", "bp_room_description"),
                    entry("gebaeude_dokument", "bp_room_document"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CIA_VALUE_MAPPINGS = Stream
            .of(entry("gebaeude_vertraulichkeit_normal", "bp_room_value_confidentiality_normal"),
                    entry("gebaeude_vertraulichkeit_hoch", "bp_room_value_confidentiality_high"),
                    entry("gebaeude_vertraulichkeit_sehrhoch",
                            "bp_room_value_confidentiality_very_high"),

                    entry("gebaeude_integritaet_normal", "bp_room_value_integrity_low"),
                    entry("gebaeude_integritaet_hoch", "bp_room_value_integrity_high"),
                    entry("gebaeude_integritaet_sehrhoch", "bp_room_value_integrity_very_high"),

                    entry("gebaeude_verfuegbarkeit_normal", "bp_room_value_availability_normal"),
                    entry("gebaeude_verfuegbarkeit_hoch", "bp_room_value_availability_high"),
                    entry("gebaeude_verfuegbarkeit_sehrhoch",
                            "bp_room_value_availability_very_high"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    Room convert(Gebaeude sourceElement) {
        Room converted = new Room(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);

        migrateSelectValue(sourceElement, "gebaeude_vertraulichkeit", converted,
                "bp_room_value_confidentiality", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "gebaeude_verfuegbarkeit", converted,
                "bp_room_value_availability", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "gebaeude_integritaet", converted,
                "bp_room_value_integrity", CIA_VALUE_MAPPINGS);

        convertCIAValues(sourceElement, "gebaeude_vertraulichkeit_begruendung", converted,
                "bp_room_method_confidentiality", "bp_room_value_comment_c",
                "bp_room_value_method_confidentiality");
        convertCIAValues(sourceElement, "gebaeude_integritaet_begruendung", converted,
                "bp_room_method_integrity", "bp_room_value_comment_i",
                "bp_room_value_method_integrity");
        convertCIAValues(sourceElement, "gebaeude_verfuegbarkeit_begruendung", converted,
                "bp_room_method_availability", "bp_room_value_comment_a",
                "bp_room_value_method_availability");

        return converted;
    }

}
