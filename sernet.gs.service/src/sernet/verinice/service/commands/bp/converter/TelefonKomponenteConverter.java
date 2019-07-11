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
import sernet.verinice.model.bsi.TelefonKomponente;

public class TelefonKomponenteConverter extends ElementConverter<TelefonKomponente, ItSystem> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream
            .of(entry("tkkomponente_kuerzel", "bp_itsystem_abbr"),
                    entry("tkkomponente_name", "bp_itsystem_name"),
                    entry("tkkomponente_tag", "bp_itsystem_tag"),
                    entry("tkkomponente_anzahl", "bp_itsystem_count"),
                    entry("tkkomponente_erlaeuterung", "bp_itsystem_description"),
                    entry("tkkomponente_dokument", "bp_itsystem_document"),
                    entry("tkkomponente_plattform", "bp_itsystem_platform"),
                    entry("tkkomponente_aufstellungsort", "bp_itsystem_site"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CIA_VALUE_MAPPINGS = Stream.of(
            entry("tkkomponente_vertraulichkeit_normal",
                    "bp_itsystem_value_confidentiality_normal"),
            entry("tkkomponente_vertraulichkeit_hoch", "bp_itsystem_value_confidentiality_high"),
            entry("tkkomponente_vertraulichkeit_sehrhoch",
                    "bp_itsystem_value_confidentiality_very_high"),

            entry("tkkomponente_integritaet_normal", "bp_itsystem_value_integrity_normal"),
            entry("tkkomponente_integritaet_hoch", "bp_itsystem_value_integrity_high"),
            entry("tkkomponente_integritaet_sehrhoch", "bp_itsystem_value_integrity_very_high"),

            entry("tkkomponente_verfuegbarkeit_normal", "bp_itsystem_value_availability_normal"),
            entry("tkkomponente_verfuegbarkeit_hoch", "bp_itsystem_value_availability_high"),
            entry("tkkomponente_verfuegbarkeit_sehrhoch",
                    "bp_itsystem_value_availability_very_high"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> TKKOMPONENTE_STATUS_MAPPINGS = Stream
            .of(entry("tkkomponente_status_betrieb", "bp_itsystem_status_operation"),
                    entry("tkkomponente_status_planung", "bp_itsystem_status_planning"),
                    entry("tkkomponente_status_test", "bp_itsystem_status_test"),
                    entry("tkkomponente_status_reserve", "bp_itsystem_status_reserve"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    ItSystem convert(TelefonKomponente sourceElement) {
        ItSystem converted = new ItSystem(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);

        migrateSelectValue(sourceElement, "tkkomponente_vertraulichkeit", converted,
                "bp_itsystem_value_confidentiality", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "tkkomponente_verfuegbarkeit", converted,
                "bp_itsystem_value_availability", CIA_VALUE_MAPPINGS);
        migrateSelectValue(sourceElement, "tkkomponente_integritaet", converted,
                "bp_itsystem_value_integrity", CIA_VALUE_MAPPINGS);

        convertCIAValues(sourceElement, "tkkomponente_vertraulichkeit_begruendung", converted,
                "bp_itsystem_method_confidentiality", "bp_itsystem_value_comment_c",
                "bp_itsystem_value_method_confidentiality");
        convertCIAValues(sourceElement, "tkkomponente_integritaet_begruendung", converted,
                "bp_itsystem_method_integrity", "bp_itsystem_value_comment_i",
                "bp_itsystem_value_method_integrity");
        convertCIAValues(sourceElement, "tkkomponente_verfuegbarkeit_begruendung", converted,
                "bp_itsystem_method_availability", "bp_itsystem_value_comment_a",
                "bp_itsystem_value_method_availability");

        migrateSelectValue(sourceElement, "tkkomponente_status", converted, "bp_itsystem_status",
                TKKOMPONENTE_STATUS_MAPPINGS);
        return converted;
    }

}
