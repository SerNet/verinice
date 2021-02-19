/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.RemoveLink;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.VNAImportHelper;

/**
 * Test of Schutzbedarfsvererbung. See
 * https://wiki.sernet.private/wiki/Verinice/Business_Impact_Inheritence/de for
 * a description of the test cases.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@Transactional
@TransactionConfiguration(transactionManager = "txManager")
public class SchutzbedarfsvererbungsTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(SchutzbedarfsvererbungsTest.class);

    private static final String VNA_FILENAME = "SchutzbedarfsvererbungsTest.vna";

    private static final String NORMAL = "Normal";
    private static final String HOCH = "Hoch";
    private static final String SEHR_HOCH = "Sehr Hoch";

    private static final String SOURCE_ID = "Unit-Test-GS";
    private static final String EXT_ID_GEBAEUDE_1 = "ENTITY_1135838";
    private static final String EXT_ID_RAUM_1 = "ENTITY_1135872";
    private static final String EXT_ID_RAUM_2 = "ENTITY_1135906";
    private static final String EXT_ID_ANWENDUNG_2 = "ENTITY_1136120";
    private static final String EXT_ID_ANWENDUNG_1 = "ENTITY_1136066";
    private static final String EXT_ID_ANWENDUNG_3 = "ENTITY_1136175";
    private static final String EXT_ID_SERVER_1 = "ENTITY_1135940";

    @Before
    public void importData() throws IOException, CommandException, SyncParameterException {
        VNAImportHelper.importFile(VNA_FILENAME);
    }

    @Test
    public void testRemoveElement() throws Exception {

        Gebaeude gebaeude = (Gebaeude) loadElement(SOURCE_ID, EXT_ID_GEBAEUDE_1);
        checkSchutzbedarf(gebaeude, SEHR_HOCH, SEHR_HOCH, SEHR_HOCH);

        Anwendung anwendung = (Anwendung) loadElement(SOURCE_ID, EXT_ID_ANWENDUNG_3);
        RemoveElement removeCommand = new RemoveElement(anwendung);
        commandService.executeCommand(removeCommand);

        gebaeude = (Gebaeude) loadElement(SOURCE_ID, EXT_ID_GEBAEUDE_1);
        checkSchutzbedarf(gebaeude, HOCH, NORMAL, HOCH);
    }

    @Test
    public void testAddAndRemoveLink() throws Exception {

        Gebaeude gebaeude = (Gebaeude) loadElement(SOURCE_ID, EXT_ID_GEBAEUDE_1);
        checkSchutzbedarf(gebaeude, SEHR_HOCH, SEHR_HOCH, SEHR_HOCH);

        // remove link
        Raum raum = (Raum) loadElement(SOURCE_ID, EXT_ID_RAUM_1);
        checkSchutzbedarf(raum, SEHR_HOCH, SEHR_HOCH, SEHR_HOCH);
        removeLinksToServer(raum);
        gebaeude = (Gebaeude) loadElement(SOURCE_ID, EXT_ID_GEBAEUDE_1);
        checkSchutzbedarf(gebaeude, HOCH, NORMAL, HOCH);
        raum = (Raum) loadElement(SOURCE_ID, EXT_ID_RAUM_1);
        checkSchutzbedarf(raum, "", "", "");

        // add link
        Server server = (Server) loadElement(SOURCE_ID, EXT_ID_SERVER_1);
        CreateLink<Server, Raum> command = new CreateLink<Server, Raum>(server, raum,
                Server.REL_SERVER_RAUM, "SchutzbedarfsvererbungsTest");
        command = commandService.executeCommand(command);
        gebaeude = (Gebaeude) loadElement(SOURCE_ID, EXT_ID_GEBAEUDE_1);
        checkSchutzbedarf(gebaeude, SEHR_HOCH, SEHR_HOCH, SEHR_HOCH);
        raum = (Raum) loadElement(SOURCE_ID, EXT_ID_RAUM_1);
        checkSchutzbedarf(raum, SEHR_HOCH, SEHR_HOCH, SEHR_HOCH);

    }

    @Test
    public void testChangeSchutzbedarf() throws Exception {

        Raum raum = (Raum) loadElement(SOURCE_ID, EXT_ID_RAUM_2);
        checkSchutzbedarf(raum, HOCH, NORMAL, HOCH);

        Anwendung anwendung = (Anwendung) loadElement(SOURCE_ID, EXT_ID_ANWENDUNG_2);
        checkSchutzbedarf(anwendung, NORMAL, NORMAL, NORMAL);
        setSchutzbedarf(anwendung, Anwendung.PROP_VERFUEGBARKEIT_HOCH,
                Anwendung.PROP_VERTRAULICHKEIT_HOCH, Anwendung.PROP_INTEGRITAET_HOCH);
        anwendung = (Anwendung) loadElement(SOURCE_ID, EXT_ID_ANWENDUNG_2);
        checkSchutzbedarf(anwendung, HOCH, HOCH, HOCH);

        raum = (Raum) loadElement(SOURCE_ID, EXT_ID_RAUM_2);
        checkSchutzbedarf(raum, HOCH, HOCH, HOCH);
    }

    @Test
    public void testChangeInheritence() throws Exception {

        Raum raum = (Raum) loadElement(SOURCE_ID, EXT_ID_RAUM_1);
        checkSchutzbedarf(raum, SEHR_HOCH, SEHR_HOCH, SEHR_HOCH);

        Server server = (Server) loadElement(SOURCE_ID, EXT_ID_SERVER_1);
        setInheritence(server, false);
        Anwendung anwendung = (Anwendung) loadElement(SOURCE_ID, EXT_ID_ANWENDUNG_3);
        setSchutzbedarf(anwendung, Anwendung.PROP_VERFUEGBARKEIT_NORMAL,
                Anwendung.PROP_VERTRAULICHKEIT_NORMAL, Anwendung.PROP_INTEGRITAET_NORMAL);

        raum = (Raum) loadElement(SOURCE_ID, EXT_ID_RAUM_1);
        checkSchutzbedarf(raum, SEHR_HOCH, SEHR_HOCH, SEHR_HOCH);
    }

    private void removeLinksToServer(Raum raum) throws CommandException {
        Set<CnALink> links = raum.getLinksUp();
        for (CnALink link : links) {
            if (Server.REL_SERVER_RAUM.equals(link.getRelationId())) {
                RemoveLink removeLink = new RemoveLink(link);
                removeLink = commandService.executeCommand(removeLink);
            }
        }
    }

    private void setInheritence(Server server, boolean inheritence) throws CommandException {
        String value = (inheritence) ? Schutzbedarf.MAXIMUM : "foo";
        server.setSimpleProperty(Server.PROP_INTEGRITAET_BEGRUENDUNG, value);
        server.setSimpleProperty(Server.PROP_VERFUEGBARKEIT_BEGRUENDUNG, value);
        server.setSimpleProperty(Server.PROP_VERTRAULICHKEIT_BEGRUENDUNG, value);
        updateElement(server);
    }

    private void setSchutzbedarf(Anwendung anwendung, String verfuegbarkeit, String vertraulichkeit,
            String integritaet) throws CommandException {
        anwendung.setSimpleProperty(Anwendung.PROP_INTEGRITAET, integritaet);
        anwendung.setSimpleProperty(Anwendung.PROP_VERFUEGBARKEIT, verfuegbarkeit);
        anwendung.setSimpleProperty(Anwendung.PROP_VERTRAULICHKEIT, vertraulichkeit);
        updateElement(anwendung);
    }

    private void checkSchutzbedarf(Anwendung element, String verfuegbarkeit, String vertraulichkeit,
            String integritaet) {
        assertEquals("Integritaet of element is not " + integritaet, integritaet,
                element.getEntity().getPropertyValue(Anwendung.PROP_INTEGRITAET));
        assertEquals("Verfuegbarkeit of element is not " + verfuegbarkeit, verfuegbarkeit,
                element.getEntity().getPropertyValue(Anwendung.PROP_VERFUEGBARKEIT));
        assertEquals("Vertraulichkeit of element is not " + vertraulichkeit, vertraulichkeit,
                element.getEntity().getPropertyValue(Anwendung.PROP_VERTRAULICHKEIT));
    }

    private void checkSchutzbedarf(Gebaeude element, String verfuegbarkeit, String vertraulichkeit,
            String integritaet) {
        assertEquals("Integritaet of element is not " + integritaet, integritaet,
                element.getEntity().getPropertyValue(Gebaeude.PROP_INTEGRITAET));
        assertEquals("Verfuegbarkeit of element is not " + verfuegbarkeit, verfuegbarkeit,
                element.getEntity().getPropertyValue(Gebaeude.PROP_VERFUEGBARKEIT));
        assertEquals("Vertraulichkeit of element is not " + vertraulichkeit, vertraulichkeit,
                element.getEntity().getPropertyValue(Gebaeude.PROP_VERTRAULICHKEIT));
    }

    private void checkSchutzbedarf(Raum element, String verfuegbarkeit, String vertraulichkeit,
            String integritaet) {
        assertEquals("Integritaet of element is not " + integritaet, integritaet,
                element.getEntity().getPropertyValue(Raum.PROP_INTEGRITAET));
        assertEquals("Verfuegbarkeit of element is not " + verfuegbarkeit, verfuegbarkeit,
                element.getEntity().getPropertyValue(Raum.PROP_VERFUEGBARKEIT));
        assertEquals("Vertraulichkeit of element is not " + vertraulichkeit, vertraulichkeit,
                element.getEntity().getPropertyValue(Raum.PROP_VERTRAULICHKEIT));
    }
}
