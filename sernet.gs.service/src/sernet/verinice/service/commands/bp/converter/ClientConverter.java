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
import sernet.verinice.model.bsi.Client;

public class ClientConverter extends ElementConverter<Client, ItSystem> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream.of(
            entry("client_kuerzel", "bp_itsystem_abbr"), entry("client_name", "bp_itsystem_name"),
            entry("client_tag", "bp_itsystem_tag"), entry("client_anzahl", "bp_itsystem_count"),
            entry("client_erlaeuterung", "bp_itsystem_description"),
            entry("client_dokument", "bp_itsystem_document"),
            entry("client_plattform", "bp_itsystem_platform"),
            entry("client_aufstellungsort", "bp_itsystem_site"),
            entry("client_netadr", "bp_itsystem_net_address"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CIA_VALUE_MAPPINGS = Stream
            .of(entry("client_vertraulichkeit_normal", "bp_itsystem_value_confidentiality_normal"),
                    entry("client_vertraulichkeit_hoch", "bp_itsystem_value_confidentiality_high"),
                    entry("client_vertraulichkeit_sehrhoch",
                            "bp_itsystem_value_confidentiality_very_high"),

                    entry("client_integritaet_normal", "bp_itsystem_value_integrity_normal"),
                    entry("client_integritaet_hoch", "bp_itsystem_value_integrity_high"),
                    entry("client_integritaet_sehrhoch", "bp_itsystem_value_integrity_very_high"),

                    entry("client_verfuegbarkeit_normal", "bp_itsystem_value_availability_normal"),
                    entry("client_verfuegbarkeit_hoch", "bp_itsystem_value_availability_high"),
                    entry("client_verfuegbarkeit_sehrhoch",
                            "bp_itsystem_value_availability_very_high"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CLIENT_STATUS_MAPPINGS = Stream
            .of(entry("client_status_betrieb", "bp_itsystem_status_operation"),
                    entry("client_status_planung", "bp_itsystem_status_planning"),
                    entry("client_status_test", "bp_itsystem_status_test"),
                    entry("client_status_reserve", "bp_itsystem_status_reserve"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CLIENT_COMS_MAPPINGS = Stream
            .of(entry("client_coms_ether", "bp_itsystem_interfaces_ethernet"),
                    entry("client_coms_wlan", "bp_itsystem_interfaces_wlan"),
                    entry("client_coms_blue", "bp_itsystem_interfaces_bluetooth"),
                    entry("client_coms_irda", "bp_itsystem_interfaces_irda"),
                    entry("client_coms_modem", "bp_itsystem_interfaces_modem"),
                    entry("client_coms_usb", "bp_itsystem_interfaces_usb"),
                    entry("client_coms_ieee1394", "bp_itsystem_interfaces_ieee1394"),
                    entry("client_coms_seriell", "bp_itsystem_interfaces_serial"),
                    entry("client_coms_parallel", "bp_itsystem_interfaces_parallel"),
                    entry("client_coms_scsi", "bp_itsystem_interfaces_scsi"),
                    entry("client_coms_sata", "bp_itsystem_interfaces_sata"),
                    entry("client_coms_ide", "bp_itsystem_interfaces_ide"),
                    entry("client_coms_audio", "bp_itsystem_interfaces_audio"),
                    entry("client_coms_video", "bp_itsystem_interfaces_video"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    ItSystem convert(Client sourceElement) {
        ItSystem converted = new ItSystem(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);

        migrateSelectValue(sourceElement, "client_vertraulichkeit", converted,
                "bp_itsystem_value_confidentiality", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "client_verfuegbarkeit", converted,
                "bp_itsystem_value_availability", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "client_integritaet", converted,
                "bp_itsystem_value_integrity", CIA_VALUE_MAPPINGS);

        convertCIAValues(sourceElement, "client_vertraulichkeit_begruendung", converted,
                "bp_itsystem_method_confidentiality", "bp_itsystem_value_comment_c",
                "bp_itsystem_value_method_confidentiality");
        convertCIAValues(sourceElement, "client_integritaet_begruendung", converted,
                "bp_itsystem_method_integrity", "bp_itsystem_value_comment_i",
                "bp_itsystem_value_method_integrity");
        convertCIAValues(sourceElement, "client_verfuegbarkeit_begruendung", converted,
                "bp_itsystem_method_availability", "bp_itsystem_value_comment_a",
                "bp_itsystem_value_method_availability");

        migrateSelectValue(sourceElement, "client_status", converted, "bp_itsystem_status",
                CLIENT_STATUS_MAPPINGS);
        migrateSelectValue(sourceElement, "client_coms", converted, "bp_itsystem_interfaces",
                CLIENT_COMS_MAPPINGS);

        return converted;
    }

}
