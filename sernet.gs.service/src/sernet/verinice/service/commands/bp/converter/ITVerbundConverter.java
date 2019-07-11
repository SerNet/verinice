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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bsi.ITVerbund;

public class ITVerbundConverter extends ElementConverter<ITVerbund, ItNetwork> {

    private static final DateTimeFormatter FORMATTER_CONVERSION_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream
            .of(entry("itverbund_tag", "bp_itnetwork_tag"),
                    entry("itverbund_organisation", "bp_itnetwork_organization"),
                    entry("itverbund_mitarbeiter", "bp_itnetwork_employee_number"),
                    entry("itverbund_geltungsbereich", "bp_itnetwork_description"),
                    entry("itverbund_dokument", "bp_itnetwork_document"),
                    entry("itverbund_autor", "bp_itnetwork_author"),
                    entry("itverbund_version", "bp_itnetwork_version"),
                    entry("itverbund_freigabe", "bp_itnetwork_approved"),
                    entry("itverbund_normal_gesetze", "bp_itnetwork_pl_normal_laws"),
                    entry("itverbund_normal_datenschutz", "bp_itnetwork_pl_normal_privacy"),
                    entry("itverbund_normal_leib", "bp_itnetwork_pl_normal_injury"),
                    entry("itverbund_normal_aufgaben", "bp_itnetwork_pl_normal_service"),
                    entry("itverbund_normal_wirkung", "bp_itnetwork_pl_normal_publicity"),
                    entry("itverbund_normal_finanzen", "bp_itnetwork_pl_normal_financial"),
                    entry("itverbund_hoch_gesetze", "bp_itnetwork_pl_high_laws"),
                    entry("itverbund_hoch_datenschutz", "bp_itnetwork_pl_high_privacy"),
                    entry("itverbund_hoch_leib", "bp_itnetwork_pl_high_injury"),
                    entry("itverbund_hoch_aufgaben", "bp_itnetwork_pl_high_service"),
                    entry("itverbund_hoch_wirkung", "bp_itnetwork_pl_high_publicity"),
                    entry("itverbund_hoch_finanzen", "bp_itnetwork_pl_high_financial"),
                    entry("itverbund_sehrhoch_gesetze", "bp_itnetwork_pl_very_high_laws"),
                    entry("itverbund_sehrhoch_datenschutz", "bp_itnetwork_pl_very_high_privacy"),
                    entry("itverbund_sehrhoch_leib", "bp_itnetwork_pl_very_high_injury"),
                    entry("itverbund_sehrhoch_aufgaben", "bp_itnetwork_pl_very_high_service"),
                    entry("itverbund_sehrhoch_wirkung", "bp_itnetwork_pl_very_high_publicity"),
                    entry("itverbund_sehrhoch_finanzen", "bp_itnetwork_pl_very_high_financial"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    public ItNetwork convert(ITVerbund itVerbund) {

        ItNetwork itNetwork = new ItNetwork(null);
        copyProperties(itVerbund, itNetwork, IDENTITY_MAPPINGS);
        String oldTitle = itVerbund.getTitle();
        String newTitle = String.join("", oldTitle, " [konvertiert ",
                LocalDateTime.now().format(FORMATTER_CONVERSION_TIME), "]");
        itNetwork.setTitel(newTitle);

        return itNetwork;
    }

}
