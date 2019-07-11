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
import sernet.verinice.model.bsi.Raum;

public class RaumConverter extends ElementConverter<Raum, Room> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream
            .of(entry("raum_kuerzel", "bp_room_abbr"), entry("raum_name", "bp_room_name"),
                    entry("raum_tag", "bp_room_tag"), entry("raum_anzahl", "bp_room_count"),
                    entry("raum_erlaeuterung", "bp_room_description"),
                    entry("raum_dokument", "bp_room_document"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CIA_VALUE_MAPPINGS = Stream
            .of(entry("raum_vertraulichkeit_normal", "bp_room_value_confidentiality_normal"),
                    entry("raum_vertraulichkeit_hoch", "bp_room_value_confidentiality_high"),
                    entry("raum_vertraulichkeit_sehrhoch",
                            "bp_room_value_confidentiality_very_high"),

                    entry("raum_integritaet_normal", "bp_room_value_integrity_low"),
                    entry("raum_integritaet_hoch", "bp_room_value_integrity_high"),
                    entry("raum_integritaet_sehrhoch", "bp_room_value_integrity_very_high"),

                    entry("raum_verfuegbarkeit_normal", "bp_room_value_availability_normal"),
                    entry("raum_verfuegbarkeit_hoch", "bp_room_value_availability_high"),
                    entry("raum_verfuegbarkeit_sehrhoch", "bp_room_value_availability_very_high"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    Room convert(Raum sourceElement) {
        Room converted = new Room(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);

        migrateSelectValue(sourceElement, "raum_vertraulichkeit", converted,
                "bp_room_value_confidentiality", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "raum_verfuegbarkeit", converted,
                "bp_room_value_availability", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "raum_integritaet", converted, "bp_room_value_integrity",
                CIA_VALUE_MAPPINGS);

        convertCIAValues(sourceElement, "raum_vertraulichkeit_begruendung", converted,
                "bp_room_method_confidentiality", "bp_room_value_comment_c",
                "bp_room_value_method_confidentiality");
        convertCIAValues(sourceElement, "raum_integritaet_begruendung", converted,
                "bp_room_method_integrity", "bp_room_value_comment_i",
                "bp_room_value_method_integrity");
        convertCIAValues(sourceElement, "raum_verfuegbarkeit_begruendung", converted,
                "bp_room_method_availability", "bp_room_value_comment_a",
                "bp_room_value_method_availability");

        return converted;
    }

}
