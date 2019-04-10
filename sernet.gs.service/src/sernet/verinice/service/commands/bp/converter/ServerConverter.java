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

import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bsi.Server;

public class ServerConverter extends ElementConverter<Server, ItSystem> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream.of(
            entry("server_kuerzel", "bp_itsystem_abbr"), entry("server_name", "bp_itsystem_name"),
            entry("server_tag", "bp_itsystem_tag"), entry("server_anzahl", "bp_itsystem_count"),
            entry("server_erlaeuterung", "bp_itsystem_description"),
            entry("server_dokument", "bp_itsystem_document"),
            entry("server_plattform", "bp_itsystem_platform"),
            entry("server_aufstellungsort", "bp_itsystem_site"),
            entry("server_netadr", "bp_itsystem_net_address"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CIA_VALUE_MAPPINGS = Stream.of(
            entry("server_vertraulichkeit_normal", "bp_itsystem_value_confidentiality_normal"),
            entry("server_vertraulichkeit_hoch", "bp_itsystem_value_confidentiality_high"),
            entry("server_vertraulichkeit_sehrhoch", "bp_itsystem_value_confidentiality_very_high"),

            entry("server_integritaet_normal", "bp_itsystem_value_integrity_normal"),
            entry("server_integritaet_hoch", "bp_itsystem_value_integrity_high"),
            entry("server_integritaet_sehrhoch", "bp_itsystem_value_integrity_very_high"),

            entry("server_verfuegbarkeit_normal", "bp_itsystem_value_availability_normal"),
            entry("server_verfuegbarkeit_hoch", "bp_itsystem_value_availability_high"),
            entry("server_verfuegbarkeit_sehrhoch", "bp_itsystem_value_availability_very_high")

    ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> SERVER_STATUS_MAPPINGS = Stream
            .of(entry("server_status_betrieb", "bp_itsystem_status_operation"),
                    entry("server_status_planung", "bp_itsystem_status_planning"),
                    entry("server_status_test", "bp_itsystem_status_test"),
                    entry("server_status_reserve", "bp_itsystem_status_reserve"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    ItSystem convert(Server sourceElement) {
        ItSystem converted = new ItSystem(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);

        migrateSelectValue(sourceElement, "server_vertraulichkeit", converted,
                "bp_itsystem_value_confidentiality", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "server_verfuegbarkeit", converted,
                "bp_itsystem_value_availability", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "server_integritaet", converted,
                "bp_itsystem_value_integrity", CIA_VALUE_MAPPINGS);

        convertCIAValues(sourceElement, "server_vertraulichkeit_begruendung", converted,
                "bp_itsystem_method_confidentiality", "bp_itsystem_value_comment_c",
                "bp_itsystem_value_method_confidentiality");
        convertCIAValues(sourceElement, "server_integritaet_begruendung", converted,
                "bp_itsystem_method_integrity", "bp_itsystem_value_comment_i",
                "bp_itsystem_value_method_integrity");
        convertCIAValues(sourceElement, "server_verfuegbarkeit_begruendung", converted,
                "bp_itsystem_method_availability", "bp_itsystem_value_comment_a",
                "bp_itsystem_value_method_availability");

        migrateSelectValue(sourceElement, "server_status", converted, "bp_itsystem_status",
                SERVER_STATUS_MAPPINGS);
        return converted;
    }

}
