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
import sernet.verinice.model.bsi.SonstIT;

public class SonstITConverter extends ElementConverter<SonstIT, ItSystem> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream.of(
            entry("sonstit_kuerzel", "bp_itsystem_abbr"), entry("sonstit_name", "bp_itsystem_name"),
            entry("sonstit_tag", "bp_itsystem_tag"), entry("sonstit_anzahl", "bp_itsystem_count"),
            entry("sonstit_erlaeuterung", "bp_itsystem_description"),
            entry("sonstit_dokument", "bp_itsystem_document"),
            entry("sonstit_plattform", "bp_itsystem_platform"),
            entry("sonstit_aufstellungsort", "bp_itsystem_site"),
            entry("sonstit_netadr", "bp_itsystem_net_address"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CIA_VALUE_MAPPINGS = Stream
            .of(entry("sonstit_vertraulichkeit_normal", "bp_itsystem_value_confidentiality_normal"),
                    entry("sonstit_vertraulichkeit_hoch", "bp_itsystem_value_confidentiality_high"),
                    entry("sonstit_vertraulichkeit_sehrhoch",
                            "bp_itsystem_value_confidentiality_very_high"),

                    entry("sonstit_integritaet_normal", "bp_itsystem_value_integrity_normal"),
                    entry("sonstit_integritaet_hoch", "bp_itsystem_value_integrity_high"),
                    entry("sonstit_integritaet_sehrhoch", "bp_itsystem_value_integrity_very_high"),

                    entry("sonstit_verfuegbarkeit_normal", "bp_itsystem_value_availability_normal"),
                    entry("sonstit_verfuegbarkeit_hoch", "bp_itsystem_value_availability_high"),
                    entry("sonstit_verfuegbarkeit_sehrhoch",
                            "bp_itsystem_value_availability_very_high"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> SONSTIT_STATUS_MAPPINGS = Stream
            .of(entry("sonstit_status_betrieb", "bp_itsystem_status_operation"),
                    entry("sonstit_status_planung", "bp_itsystem_status_planning"),
                    entry("sonstit_status_test", "bp_itsystem_status_test"),
                    entry("sonstit_status_reserve", "bp_itsystem_status_reserve"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> SONSTIT_COMS_MAPPINGS = Stream
            .of(entry("sonstit_coms_ether", "bp_itsystem_interfaces_ethernet"),
                    entry("sonstit_coms_wlan", "bp_itsystem_interfaces_wlan"),
                    entry("sonstit_coms_blue", "bp_itsystem_interfaces_bluetooth"),
                    entry("sonstit_coms_irda", "bp_itsystem_interfaces_irda"),
                    entry("sonstit_coms_modem", "bp_itsystem_interfaces_modem"),
                    entry("sonstit_coms_usb", "bp_itsystem_interfaces_usb"),
                    entry("sonstit_coms_ieee1394", "bp_itsystem_interfaces_ieee1394"),
                    entry("sonstit_coms_seriell", "bp_itsystem_interfaces_serial"),
                    entry("sonstit_coms_parallel", "bp_itsystem_interfaces_parallel"),
                    entry("sonstit_coms_scsi", "bp_itsystem_interfaces_scsi"),
                    entry("sonstit_coms_sata", "bp_itsystem_interfaces_sata"),
                    entry("sonstit_coms_ide", "bp_itsystem_interfaces_ide"),
                    entry("sonstit_coms_audio", "bp_itsystem_interfaces_audio"),
                    entry("sonstit_coms_video", "bp_itsystem_interfaces_video"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    ItSystem convert(SonstIT sourceElement) {
        ItSystem converted = new ItSystem(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);

        migrateSelectValue(sourceElement, "sonstit_vertraulichkeit", converted,
                "bp_itsystem_value_confidentiality", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "sonstit_verfuegbarkeit", converted,
                "bp_itsystem_value_availability", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "sonstit_integritaet", converted,
                "bp_itsystem_value_integrity", CIA_VALUE_MAPPINGS);

        convertCIAValues(sourceElement, "sonstit_vertraulichkeit_begruendung", converted,
                "bp_itsystem_method_confidentiality", "bp_itsystem_value_comment_c",
                "bp_itsystem_value_method_confidentiality");
        convertCIAValues(sourceElement, "sonstit_integritaet_begruendung", converted,
                "bp_itsystem_method_integrity", "bp_itsystem_value_comment_i",
                "bp_itsystem_value_method_integrity");
        convertCIAValues(sourceElement, "sonstit_verfuegbarkeit_begruendung", converted,
                "bp_itsystem_method_availability", "bp_itsystem_value_comment_a",
                "bp_itsystem_value_method_availability");

        migrateSelectValue(sourceElement, "sonstit_status", converted, "bp_itsystem_status",
                SONSTIT_STATUS_MAPPINGS);
        migrateSelectValue(sourceElement, "sonstit_coms", converted, "bp_itsystem_interfaces",
                SONSTIT_COMS_MAPPINGS);

        return converted;
    }

}
