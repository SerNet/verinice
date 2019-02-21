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

import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bsi.NetzKomponente;

public class NetzKomponenteConverter extends ElementConverter<NetzKomponente, Network> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream
            .of(entry("netzkomponente_kuerzel", "bp_network_abbr"),
                    entry("netzkomponente_name", "bp_network_name"),
                    entry("netzkomponente_tag", "bp_network_tag"),
                    entry("netzkomponente_dokument", "bp_network_document"),
                    entry("netzkomponente_kabel", "bp_network_cable"),
                    entry("netzkomponente_kapazitaet", "bp_network_capacity"),
                    entry("netzkomponente_protokolle", "bp_network_protocols"),
                    entry("netzkomponente_extnetz", "bp_network_external_network"),
                    entry("netzkomponente_erlaeuterung", "bp_network_explanation"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> CRITICALITY_MAPPINGS = Stream
            .of(entry("netzkomponente_kritikalitaet_0", "bp_network_criticality_0"),
                    entry("netzkomponente_kritikalitaet_1", "bp_network_criticality_1"),
                    entry("netzkomponente_kritikalitaet_2", "bp_network_criticality_2"),
                    entry("netzkomponente_kritikalitaet_3", "bp_network_criticality_3"),
                    entry("netzkomponente_kritikalitaet_4", "bp_network_criticality_4"),
                    entry("netzkomponente_kritikalitaet_5", "bp_network_criticality_5"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    Network convert(NetzKomponente sourceElement) {
        Network converted = new Network(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);
        migrateSelectValue(sourceElement, "netzkomponente_kritikalitaet", converted,
                "bp_network_criticality", CRITICALITY_MAPPINGS);
        return converted;
    }

}
