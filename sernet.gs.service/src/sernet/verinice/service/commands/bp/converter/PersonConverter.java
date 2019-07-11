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

import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.Person;

public class PersonConverter extends ElementConverter<Person, BpPerson> {

    private static final Map<String, String> IDENTITY_MAPPINGS = Stream
            .of(entry("person_kuerzel", "bp_person_abbr"), entry("vorname", "bp_person_first_name"),
                    entry("nachname", "bp_person_last_name"), entry("person_tag", "bp_person_tag"),
                    entry("person_anzahl", "bp_person_count"),
                    entry("person_telefon", "bp_person_phone"),
                    entry("person_email", "bp_person_email"),
                    entry("person_orgeinheit", "bp_person_org_unit"),
                    entry("person_erlaeuterung", "bp_person_explanation"),
                    entry("person_dokument", "bp_person_document"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> PERSON_ANREDE_MAPPINGS = Stream
            .of(entry("person_anrede_none", "bp_person_title_none"),
                    entry("person_anrede_herr", "bp_person_titel_mr"),
                    entry("person_anrede_frau", "bp_person_titel_mrs"),
                    entry("person_anrede_herrdr", "bp_person_titel_mrdr"),
                    entry("person_anrede_fraudr", "bp_person_titel_mrsdr"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> PERSON_ROLLEN_MAPPINGS = Stream.of(
            entry("person_0", "bp_person_role_0"), entry("person_1", "bp_person_role_1"),
            entry("person_2", "bp_person_role_2"), entry("person_3", "bp_person_role_3"),
            entry("person_4", "bp_person_role_4"), entry("person_5", "bp_person_role_5"),
            entry("person_6", "bp_person_role_6"), entry("person_7", "bp_person_role_7"),
            entry("person_8", "bp_person_role_8"), entry("person_9", "bp_person_role_9"),
            entry("person_10", "bp_person_role_10"), entry("person_11", "bp_person_role_11"),
            entry("person_12", "bp_person_role_12"), entry("person_13", "bp_person_role_13"),
            entry("person_14", "bp_person_role_14"), entry("person_15", "bp_person_role_15"),
            entry("person_16", "bp_person_role_16"), entry("person_17", "bp_person_role_17"),
            entry("person_18", "bp_person_role_18"), entry("person_19", "bp_person_role_19"),
            entry("person_20", "bp_person_role_20"), entry("person_21", "bp_person_role_21"),
            entry("person_22", "bp_person_role_22"), entry("person_23", "bp_person_role_23"),
            entry("person_24", "bp_person_role_24"), entry("person_25", "bp_person_role_25"),
            entry("person_26", "bp_person_role_26"), entry("person_27", "bp_person_role_27"),
            entry("person_28", "bp_person_role_28"), entry("person_29", "bp_person_role_29"),
            entry("person_30", "bp_person_role_30"), entry("person_31", "bp_person_role_31"),
            entry("person_32", "bp_person_role_32"), entry("person_33", "bp_person_role_33"),
            entry("person_34", "bp_person_role_34"), entry("person_35", "bp_person_role_35"),
            entry("person_36", "bp_person_role_36"), entry("person_37", "bp_person_role_37"),
            entry("person_38", "bp_person_role_38"), entry("person_39", "bp_person_role_39"),
            entry("person_40", "bp_person_role_40"), entry("person_41", "bp_person_role_41"),
            entry("person_42", "bp_person_role_42"), entry("person_43", "bp_person_role_43"),
            entry("person_44", "bp_person_role_44"), entry("person_45", "bp_person_role_45"),
            entry("person_46", "bp_person_role_46"), entry("person_47", "bp_person_role_47"),
            entry("person_48", "bp_person_role_48"), entry("person_49", "bp_person_role_49"),
            entry("person_50", "bp_person_role_50"), entry("person_51", "bp_person_role_51"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Override
    BpPerson convert(Person sourceElement) {
        BpPerson converted = new BpPerson(null);
        copyProperties(sourceElement, converted, IDENTITY_MAPPINGS);
        migrateSelectValue(sourceElement, "person_anrede", converted, "bp_person_title",
                PERSON_ANREDE_MAPPINGS);
        migrateSelectValue(sourceElement, "person_rollen", converted, "bp_person_roles",
                PERSON_ROLLEN_MAPPINGS);

        return converted;
    }

}
