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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.test.ContextConfiguration;

public class ElementConverterTest extends ContextConfiguration {

    @Test
    public void convert_server() {
        Server server = new Server(null);
        server.setKuerzel("S1");
        server.setTitel("Server 1");
        server.setAnzahl(23);
        server.setSimpleProperty("server_verfuegbarkeit", "server_verfuegbarkeit_sehrhoch");
        server.setSimpleProperty("server_verfuegbarkeit_begruendung", "Maximumprinzip");
        server.setSimpleProperty("server_status", "server_status_betrieb");
        ServerConverter serverConverter = new ServerConverter();
        ItSystem converted = serverConverter.convert(server);

        assertEquals("S1", converted.getAbbreviation());
        assertEquals("Server 1", converted.getTitle());
        assertEquals(Integer.valueOf(23),
                converted.getEntity().getNumericValue("bp_itsystem_count"));

        assertEquals("Sehr hoch",
                getSelectedOptionName(converted, "bp_itsystem_value_availability"));
        assertTrue(converted.getEntity().isFlagged("bp_itsystem_value_method_availability"));
        assertEquals("Unbearbeitet",
                getSelectedOptionName(converted, "bp_itsystem_method_availability"));
        assertNull(converted.getEntity().getRawPropertyValue("bp_itsystem_value_comment_a"));
        assertEquals("Betrieb", converted.getPropertyValue("bp_itsystem_status"));

    }

    private Object getSelectedOptionName(CnATreeElement element, String propertyId) {
        PropertyType propertyType = element.getEntityType().getPropertyType(propertyId);
        if (propertyType.isNumericSelect()) {
            int numericValue = element.getNumericProperty(propertyId);
            return propertyType.getNameForValue(numericValue);
        }
        return null;
    }

    @Test
    public void convert_client() {
        Client client = new Client(null);
        client.setKuerzel("C1");
        client.setTitel("Client 1");
        client.setSimpleProperty("client_vertraulichkeit", "client_vertraulichkeit_normal");
        client.setSimpleProperty("client_vertraulichkeit_begruendung", "This is a custom value");
        ClientConverter clientConverter = new ClientConverter();
        ItSystem converted = clientConverter.convert(client);

        assertEquals("C1", converted.getAbbreviation());
        assertEquals("Client 1", converted.getTitle());
        assertEquals("Normal",
                getSelectedOptionName(converted, "bp_itsystem_value_confidentiality"));
        assertFalse(converted.getEntity().isFlagged("bp_itsystem_value_method_confidentiality"));
        assertEquals("Unbearbeitet",
                getSelectedOptionName(converted, "bp_itsystem_method_confidentiality"));
        assertEquals("This is a custom value",
                converted.getEntity().getRawPropertyValue("bp_itsystem_value_comment_c"));

    }

    @Test
    public void convert_client_with_empty_select_values() {
        Client client = new Client(null);
        client.setKuerzel("C1");
        client.setTitel("Client 1");
        client.setSimpleProperty("client_vertraulichkeit", "");
        client.setSimpleProperty("client_verfuegbarkeit", null);
        ClientConverter clientConverter = new ClientConverter();
        ItSystem converted = clientConverter.convert(client);

        assertEquals("C1", converted.getAbbreviation());
        assertEquals("Client 1", converted.getTitle());
        assertEquals("Unbearbeitet",
                getSelectedOptionName(converted, "bp_itsystem_value_confidentiality"));
        assertEquals("Unbearbeitet",
                getSelectedOptionName(converted, "bp_itsystem_value_availability"));

    }

    @Test
    public void convert_raum() {
        Raum raum = new Raum(null);
        raum.setKuerzel("R1");
        raum.setTitel("Raum 1");
        raum.setSimpleProperty("raum_integritaet", "raum_integritaet_hoch");
        raum.setSimpleProperty("raum_integritaet_begruendung", "Verteilungseffekt");
        RaumConverter clientConverter = new RaumConverter();
        Room converted = clientConverter.convert(raum);

        assertEquals("R1", converted.getAbbreviation());
        assertEquals("Raum 1", converted.getTitle());
        assertEquals("Hoch", getSelectedOptionName(converted, "bp_room_value_integrity"));
        assertFalse(converted.getEntity().isFlagged("bp_room_value_method_integrity"));
        assertEquals("Verteilungseffekt",
                getSelectedOptionName(converted, "bp_room_method_integrity"));
        assertNull(converted.getEntity().getRawPropertyValue("bp_itsystem_value_comment_i"));

    }

    @Test
    public void convert_gebaeude() {
        Gebaeude gebaeude = new Gebaeude(null);
        gebaeude.setKuerzel("G1");
        gebaeude.setTitel("Gebäude 1");
        gebaeude.setSimpleProperty("gebaeude_vertraulichkeit", "gebaeude_vertraulichkeit_normal");
        gebaeude.setSimpleProperty("gebaeude_vertraulichkeit_begruendung", "Kumulationseffekt");
        gebaeude.setSimpleProperty(Gebaeude.PROP_TAG, "foo, bar");
        GebaeudeConverter converter = new GebaeudeConverter();
        Room converted = converter.convert(gebaeude);

        assertEquals("G1", converted.getAbbreviation());
        assertEquals("Gebäude 1", converted.getTitle());
        assertThat(converted.getTags(), JUnitMatchers.hasItems("foo", "bar"));
        assertEquals("Normal", getSelectedOptionName(converted, "bp_room_value_confidentiality"));
        assertFalse(converted.getEntity().isFlagged("bp_room_value_method_confidentiality"));
        assertEquals("Kumulationseffekt",
                getSelectedOptionName(converted, "bp_room_method_confidentiality"));
        assertNull(converted.getEntity().getRawPropertyValue("bp_room_value_comment_c"));

    }

    @Test
    public void convert_person() {
        Person source = new Person(null);
        source.setKuerzel("P1");
        source.setSimpleProperty(Person.P_VORNAME, "John");
        source.setSimpleProperty(Person.P_NAME, "Doe");
        source.setSimpleProperty(Person.P_ANREDE, "person_anrede_herrdr");
        source.setAnzahl(5);
        source.addRole("Benutzer");
        source.addRole("IT-Sicherheitsmanagement");
        source.addRole("Telearbeiter");
        PersonConverter converter = new PersonConverter();
        BpPerson converted = converter.convert(source);

        assertEquals("P1", converted.getAbbreviation());
        assertEquals("Doe, John", converted.getTitle());
        assertEquals("Herr Dr.", converted.getPropertyValue("bp_person_title"));
        assertEquals("5", converted.getPropertyValue("bp_person_count"));
        String roles = converted.getEntity().getRawPropertyValue("bp_person_roles");
        assertThat(Arrays.asList(roles.split(",")), JUnitMatchers.hasItems("bp_person_role_6",
                "bp_person_role_19", "bp_person_role_29"));

    }

    @Test
    public void convert_anwendung_to_application() {
        Anwendung source = new Anwendung(null);
        source.setKuerzel("A1");
        source.setSimpleProperty(Anwendung.PROP_TAG, MasterConverter.TAG_MOGS_ANWENDUNG);
        source.setErlaeuterung("Das ist die Erläuterung");
        source.setVerarbeiteteInformationen("Daten");
        source.setProzessWichtigkeit("Sehr wichtig");
        source.setProzessWichtigkeitBegruendung("Total wichtig!");
        AnwendungApplicationConverter converter = new AnwendungApplicationConverter();
        Application converted = converter.convert(source);

        assertEquals("A1", converted.getAbbreviation());
        assertEquals("Das ist die Erläuterung\nSehr wichtig\nTotal wichtig!",
                converted.getPropertyValue("bp_application_description"));
        assertTrue(converted.getTags().isEmpty());
    }

    @Test
    public void convert_anwendung_to_business_process() {
        Anwendung source = new Anwendung(null);
        source.setKuerzel("P1");
        source.setSimpleProperty(Anwendung.PROP_TAG,
                String.join(", ", "foo", MasterConverter.TAG_MOGS_PROZESS));
        source.setProzessBeschreibung("Rechnungsdaten verarbeiten");
        source.setVerarbeiteteInformationen("Rechnungsdaten");

        source.setErlaeuterung("Da bekommen wir Geld");
        source.setPersonenbezogen(true);
        source.setProzessWichtigkeit("Mittel");
        AnwendungBusinessProcessConverter converter = new AnwendungBusinessProcessConverter();
        BusinessProcess converted = converter.convert(source);

        assertEquals("P1", converted.getAbbreviation());
        assertEquals("Da bekommen wir Geld\nRechnungsdaten verarbeiten\nRechnungsdaten\nMittel",
                converted.getPropertyValue("bp_businessprocess_description"));
        assertEquals("Ja",
                converted.getPropertyValue("bp_businessprocess_stellungnahmedsb_pbdaten"));
        assertEquals(1, converted.getTags().size());
        assertEquals("foo", converted.getTags().iterator().next());

    }

}
