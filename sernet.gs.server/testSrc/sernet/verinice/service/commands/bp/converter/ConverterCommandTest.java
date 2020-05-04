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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bsi.Addition;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Note;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.service.DAOFactory;
import sernet.verinice.service.commands.AttachmentFileCreationFactory;
import sernet.verinice.service.commands.LoadAttachmentFile;
import sernet.verinice.service.test.CommandServiceProvider;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
@Transactional
public class ConverterCommandTest extends CommandServiceProvider {

    @Resource(name = "additionDAO")
    private IBaseDao<Addition, Integer> additionDao;

    @Resource(name = "daoFactory")
    private DAOFactory daoFactory;

    @Resource(name = "cnaLinkDao")
    private IBaseDao<CnALink, Serializable> cnaLinkDao;

    private BSIModel bsiModel;

    @Before
    public void setupModels() {
        if (elementDao.findByCriteria(DetachedCriteria.forClass(BpModel.class)).isEmpty()) {
            elementDao.merge(new BpModel());
        }
        bsiModel = (BSIModel) elementDao.findByCriteria(DetachedCriteria.forClass(BSIModel.class))
                .get(0);
    }

    @Test
    public void convert_simple_itverbund() throws CommandException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);
        itVerbund.createNewCategories();

        CnATreeElement serverKategorie = itVerbund.getCategory(ServerKategorie.TYPE_ID);
        Server server = new Server(serverKategorie);
        server.setTitel("Mail server");
        serverKategorie.addChild(server);

        CnATreeElement personenKategorie = itVerbund.getCategory(PersonenKategorie.TYPE_ID);
        Person person = new Person(personenKategorie);
        person.setSimpleProperty(Person.P_VORNAME, "John");
        person.setSimpleProperty(Person.P_NAME, "Doe");
        person.setAnzahl(23);
        personenKategorie.addChild(person);

        CnATreeElement anwendungenKategorie = itVerbund.getCategory(AnwendungenKategorie.TYPE_ID);
        Anwendung anwendung1 = new Anwendung(anwendungenKategorie);
        anwendung1.setTitel("App 1");
        anwendung1.setSimpleProperty(Anwendung.PROP_TAG, MasterConverter.TAG_MOGS_ANWENDUNG);

        anwendungenKategorie.addChild(anwendung1);
        Anwendung prozess1 = new Anwendung(anwendungenKategorie);
        prozess1.setTitel("Prozess 1");
        prozess1.setProzessBeschreibung("Das ist der erste Prozess");
        prozess1.setSimpleProperty(Anwendung.PROP_TAG, MasterConverter.TAG_MOGS_PROZESS);

        anwendungenKategorie.addChild(prozess1);

        CnATreeElement netzKomponentenKategorie = itVerbund.getCategory(NKKategorie.TYPE_ID);
        NetzKomponente netzKomponente = new NetzKomponente(netzKomponentenKategorie);
        netzKomponente.setTitel("Netzkomponente 1");
        netzKomponentenKategorie.addChild(netzKomponente);

        CnATreeElement telekommunikationsKomponentenKategorie = itVerbund
                .getCategory(TKKategorie.TYPE_ID);
        TelefonKomponente tkKomponente = new TelefonKomponente(netzKomponentenKategorie);
        tkKomponente.setTitel("TK-Komponente 1");
        telekommunikationsKomponentenKategorie.addChild(tkKomponente);

        CnATreeElement sonstigeITKategorie = itVerbund.getCategory(SonstigeITKategorie.TYPE_ID);
        SonstIT sonstIT = new SonstIT(netzKomponentenKategorie);
        sonstIT.setTitel("Sonstige IT 1");
        sonstigeITKategorie.addChild(sonstIT);
        elementDao.saveOrUpdate(itVerbund);
        server.setSimpleProperty("server_anwender_link", Integer.toString(person.getDbId()));
        anwendung1.setSimpleProperty("anwendung_benutzer_link", Integer.toString(person.getDbId()));
        // should be ignored because anwendung1 becomes an Application
        anwendung1.setSimpleProperty("anwendung_eigentümer_link",
                Integer.toString(person.getDbId()));
        // should become a link because prozess1 becomes a BusinessProcess
        prozess1.setSimpleProperty("anwendung_eigentümer_link", Integer.toString(person.getDbId()));
        elementDao.saveOrUpdate(server);
        elementDao.saveOrUpdate(anwendung1);
        elementDao.saveOrUpdate(prozess1);
        CnALink linkAnwendungPersonResponsible = new CnALink(anwendung1, person,
                "anwendung_person_responsible", "");
        cnaLinkDao.saveOrUpdate(linkAnwendungPersonResponsible);
        CnALink linkProzessPersonInformed = new CnALink(prozess1, person,
                "anwendung_person_informed", "");
        cnaLinkDao.saveOrUpdate(linkProzessPersonInformed);

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        Integer itNetworkDId = itNetwork.getDbId();
        assertEquals(itNetworkDId, itNetwork.getScopeId());

        CnATreeElement itSystemGroup = findChildWithTypeId(itNetwork, ItSystemGroup.TYPE_ID);
        assertNotNull(itSystemGroup);
        assertEquals(itNetworkDId, itSystemGroup.getScopeId());
        assertEquals("IT-Systeme", itSystemGroup.getTitle());
        assertEquals(3, itSystemGroup.getChildren().size());

        CnATreeElement itSystemGroupServers = findChildWithTitle(itSystemGroup, "Server");
        assertNotNull(itSystemGroupServers);
        assertEquals(itNetworkDId, itSystemGroupServers.getScopeId());

        CnATreeElement itSystem = findChildWithTypeId(itSystemGroupServers, ItSystem.TYPE_ID);
        assertNotNull(itSystem);
        assertEquals(itNetworkDId, itSystem.getScopeId());
        assertEquals("Mail server", itSystem.getTitle());

        CnATreeElement personGroup = findChildWithTypeId(itNetwork, BpPersonGroup.TYPE_ID);
        assertNotNull(personGroup);
        assertEquals(itNetworkDId, personGroup.getScopeId());

        CnATreeElement bpPerson = findChildWithTypeId(personGroup, BpPerson.TYPE_ID);
        assertNotNull(bpPerson);
        assertEquals(itNetworkDId, bpPerson.getScopeId());

        assertEquals("Doe, John", bpPerson.getTitle());

        Set<CnALink> linksFromItSystem = itSystem.getLinksUp();
        assertEquals(1, linksFromItSystem.size());
        CnALink linkFromItSystem = linksFromItSystem.iterator().next();
        assertEquals(bpPerson, linkFromItSystem.getDependant());
        assertEquals("rel_bp_person_bp_itsystem_user", linkFromItSystem.getRelationId());

        CnATreeElement applicationGroup = findChildWithTypeId(itNetwork, ApplicationGroup.TYPE_ID);
        assertNotNull(applicationGroup);
        assertEquals(itNetworkDId, applicationGroup.getScopeId());

        CnATreeElement application = findChildWithTypeId(applicationGroup, Application.TYPE_ID);
        assertNotNull(application);
        assertEquals(itNetworkDId, application.getScopeId());

        assertEquals("App 1", application.getTitle());
        Set<CnALink> linksFromApplication = application.getLinksUp();
        assertEquals(2, linksFromApplication.size());

        Set<CnALink> linksApplicationUser = getLinksWithType(application,
                "rel_bp_person_bp_application_user");
        assertEquals(1, linksApplicationUser.size());
        CnALink linkApplicationUser = linksApplicationUser.iterator().next();
        assertEquals(bpPerson, linkApplicationUser.getDependant());

        Set<CnALink> linksApplicationResponsible = getLinksWithType(application,
                "rel_bp_person_bp_application");
        assertEquals(1, linksApplicationResponsible.size());
        CnALink linkApplicationResponsible = linksApplicationResponsible.iterator().next();
        assertEquals(bpPerson, linkApplicationResponsible.getDependant());

        CnATreeElement businessProcessGroup = findChildWithTypeId(itNetwork,
                BusinessProcessGroup.TYPE_ID);
        assertNotNull(businessProcessGroup);
        assertEquals(itNetworkDId, businessProcessGroup.getScopeId());

        CnATreeElement businessProcess = findChildWithTypeId(businessProcessGroup,
                BusinessProcess.TYPE_ID);
        assertNotNull(businessProcess);
        assertEquals(itNetworkDId, businessProcess.getScopeId());

        assertEquals("Prozess 1", businessProcess.getTitle());
        Set<CnALink> linksUpFromBusinessProcess = businessProcess.getLinksUp();
        assertEquals(1, linksUpFromBusinessProcess.size());
        CnALink linkUpFromBusinessProcess = linksUpFromBusinessProcess.iterator().next();
        assertEquals(bpPerson, linkUpFromBusinessProcess.getDependant());
        assertEquals("rel_bp_person_bp_businessprocess_owner",
                linkUpFromBusinessProcess.getRelationId());
        Set<CnALink> linksDownFromBusinessProcess = businessProcess.getLinksDown();
        assertEquals(1, linksDownFromBusinessProcess.size());
        CnALink linkDownFromBusinessProcess = linksDownFromBusinessProcess.iterator().next();
        assertEquals(bpPerson, linkDownFromBusinessProcess.getDependency());
        assertEquals("rel_bp_businessprocess_bp_person_informed",
                linkDownFromBusinessProcess.getRelationId());

    }

    @Test
    public void convert_two_itverbunds_with_link_between_them() throws CommandException {
        ITVerbund itVerbund1 = new ITVerbund(bsiModel);
        itVerbund1.setTitel("Verbund 1");
        itVerbund1.createNewCategories();
        CnATreeElement serverKategorie = itVerbund1.getCategory(ServerKategorie.TYPE_ID);
        Server server = new Server(serverKategorie);
        server.setTitel("Mail server");
        serverKategorie.addChild(server);
        elementDao.saveOrUpdate(itVerbund1);

        ITVerbund itVerbund2 = new ITVerbund(bsiModel);
        itVerbund2.setTitel("Verbund 2");
        itVerbund2.createNewCategories();
        CnATreeElement raeumeKategorie = itVerbund2.getCategory(RaeumeKategorie.TYPE_ID);
        Raum raum = new Raum(raeumeKategorie);
        raum.setTitel("R1");
        raeumeKategorie.addChild(raum);
        elementDao.saveOrUpdate(itVerbund2);

        CnALink linkServerRaum = new CnALink(server, raum, "server_raum_located", "");
        cnaLinkDao.saveOrUpdate(linkServerRaum);

        ConverterCommand command = new ConverterCommand(
                Arrays.asList(itVerbund1.getUuid(), itVerbund2.getUuid()));
        commandService.executeCommand(command);
        List<ItNetwork> itNetworks = elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class));
        assertEquals(2, itNetworks.size());

        ItNetwork network1 = itNetworks.stream()
                .filter(item -> item.getTitle().startsWith("Verbund 1")).findFirst().get();
        ItNetwork network2 = itNetworks.stream()
                .filter(item -> item.getTitle().startsWith("Verbund 2")).findFirst().get();

        CnATreeElement itSystemGroup = findChildWithTypeId(network1, ItSystemGroup.TYPE_ID);
        CnATreeElement itSystemGroupServers = findChildWithTitle(itSystemGroup, "Server");
        CnATreeElement itSystem = findChildWithTypeId(itSystemGroupServers, ItSystem.TYPE_ID);

        CnATreeElement roomGroup = findChildWithTypeId(network2, RoomGroup.TYPE_ID);
        CnATreeElement roomGroupRooms = findChildWithTypeId(roomGroup, RoomGroup.TYPE_ID);
        CnATreeElement room = findChildWithTypeId(roomGroupRooms, Room.TYPE_ID);
        assertNotNull(room);

        Set<CnALink> linksFromItSystem = itSystem.getLinksDown();
        assertEquals(1, linksFromItSystem.size());
        CnALink link = linksFromItSystem.iterator().next();
        assertEquals(room, link.getDependency());
        assertEquals("rel_bp_itsystem_bp_room", link.getRelationId());

    }

    @Test
    public void convert_server_with_note() throws CommandException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);
        itVerbund.createNewCategories();
        CnATreeElement serverKategorie = itVerbund.getCategory(ServerKategorie.TYPE_ID);
        Server server = new Server(serverKategorie);
        server.setTitel("Server 1");
        serverKategorie.addChild(server);

        elementDao.saveOrUpdate(itVerbund);

        Note noteForServer = new Note();
        noteForServer.setCnATreeElement(server);
        noteForServer.setText("Hello World!");
        additionDao.saveOrUpdate(noteForServer);

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        CnATreeElement itSystemGroup = findChildWithTypeId(itNetwork, ItSystemGroup.TYPE_ID);
        assertNotNull(itSystemGroup);
        assertEquals("IT-Systeme", itSystemGroup.getTitle());

        CnATreeElement itSystemGroupServers = findChildWithTitle(itSystemGroup, "Server");
        assertNotNull(itSystemGroupServers);

        CnATreeElement itSystem = findChildWithTypeId(itSystemGroupServers, ItSystem.TYPE_ID);
        assertNotNull(itSystem);

        List notesForServer = additionDao.findByCriteria(DetachedCriteria.forClass(Note.class)
                .add(Restrictions.eq("cnATreeElement.dbId", itSystem.getDbId())));

        assertEquals(1, notesForServer.size());
        Note note = (Note) notesForServer.get(0);
        assertEquals("Hello World!", note.getText());
    }

    @Test
    public void convert_room_with_attachment() throws CommandException, IOException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);
        itVerbund.createNewCategories();
        CnATreeElement raeumeKategorie = itVerbund.getCategory(RaeumeKategorie.TYPE_ID);
        Raum raum = new Raum(raeumeKategorie);
        raum.setTitel("Raum 1");
        raeumeKategorie.addChild(raum);

        elementDao.saveOrUpdate(itVerbund);

        Attachment attachmentForRaum = new Attachment();
        attachmentForRaum.setCnATreeElement(raum);
        attachmentForRaum.setTitel("example.txt");
        attachmentForRaum.setText("An example attachment");
        attachmentForRaum.setDate(new Date());

        additionDao.saveOrUpdate(attachmentForRaum);
        additionDao.flush();

        AttachmentFileCreationFactory.createAttachmentFile(attachmentForRaum,
                "Hello World!".getBytes(StandardCharsets.UTF_8));

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        CnATreeElement roomGroup = findChildWithTypeId(itNetwork, RoomGroup.TYPE_ID);
        CnATreeElement roomGroupRooms = findChildWithTypeId(roomGroup, RoomGroup.TYPE_ID);
        CnATreeElement room = findChildWithTypeId(roomGroupRooms, Room.TYPE_ID);

        List attachmentsForRoom = additionDao
                .findByCriteria(DetachedCriteria.forClass(Attachment.class)
                        .add(Restrictions.eq("cnATreeElement.dbId", room.getDbId())));

        assertEquals(1, attachmentsForRoom.size());
        Attachment attachment = (Attachment) attachmentsForRoom.get(0);
        assertEquals("example.txt", attachment.getTitel());
        LoadAttachmentFile loadAttachmentFile = new LoadAttachmentFile(attachment.getDbId());
        loadAttachmentFile = commandService.executeCommand(loadAttachmentFile);
        AttachmentFile attachmentFile = loadAttachmentFile.getAttachmentFile();
        assertEquals("Hello World!",
                new String(attachmentFile.getFileData(), StandardCharsets.UTF_8));
    }

    @Test
    public void link_from_person_to_anwendung() throws CommandException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);
        itVerbund.createNewCategories();

        CnATreeElement personenKategorie = itVerbund.getCategory(PersonenKategorie.TYPE_ID);
        Person person = new Person(personenKategorie);
        person.setSimpleProperty(Person.P_VORNAME, "John");
        person.setSimpleProperty(Person.P_NAME, "Doe");
        personenKategorie.addChild(person);

        CnATreeElement anwendungenKategorie = itVerbund.getCategory(AnwendungenKategorie.TYPE_ID);
        Anwendung anwendung = new Anwendung(anwendungenKategorie);
        anwendung.setTitel("App 1");
        anwendung.setSimpleProperty(Anwendung.PROP_TAG, MasterConverter.TAG_MOGS_ANWENDUNG);

        anwendungenKategorie.addChild(anwendung);

        elementDao.saveOrUpdate(itVerbund);

        CnALink linkAnwendungPersonResponsible = new CnALink(person, anwendung,
                "rel_person_anwendung", "");
        cnaLinkDao.saveOrUpdate(linkAnwendungPersonResponsible);

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        CnATreeElement personGroup = findChildWithTypeId(itNetwork, BpPersonGroup.TYPE_ID);
        assertNotNull(personGroup);

        CnATreeElement bpPerson = findChildWithTypeId(personGroup, BpPerson.TYPE_ID);
        assertNotNull(bpPerson);
        assertEquals("Doe, John", bpPerson.getTitle());

        CnATreeElement applicationGroup = findChildWithTypeId(itNetwork, ApplicationGroup.TYPE_ID);
        assertNotNull(applicationGroup);
        CnATreeElement application = findChildWithTypeId(applicationGroup, Application.TYPE_ID);
        assertNotNull(application);
        assertEquals("App 1", application.getTitle());
        Set<CnALink> linksFromApplication = application.getLinksUp();
        assertEquals(1, linksFromApplication.size());

        Set<CnALink> linksApplicationResponsible = getLinksWithType(application,
                "rel_bp_person_bp_application");
        assertEquals(1, linksApplicationResponsible.size());
        CnALink linkApplicationResponsible = linksApplicationResponsible.iterator().next();
        assertEquals(bpPerson, linkApplicationResponsible.getDependant());

    }

    @Test
    public void convert_itverbund_with_generic_links() throws CommandException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);
        itVerbund.createNewCategories();

        CnATreeElement serverKategorie = itVerbund.getCategory(ServerKategorie.TYPE_ID);
        Server server1 = new Server(serverKategorie);
        server1.setTitel("Server 1");
        serverKategorie.addChild(server1);

        Server server2 = new Server(serverKategorie);
        server2.setTitel("Server 2");
        serverKategorie.addChild(server2);

        CnATreeElement anwendungenKategorie = itVerbund.getCategory(AnwendungenKategorie.TYPE_ID);
        Anwendung anwendung1 = new Anwendung(anwendungenKategorie);
        anwendung1.setTitel("App 1");
        anwendung1.setSimpleProperty(Anwendung.PROP_TAG, MasterConverter.TAG_MOGS_ANWENDUNG);

        anwendungenKategorie.addChild(anwendung1);
        Anwendung prozess1 = new Anwendung(anwendungenKategorie);
        prozess1.setTitel("Prozess 1");
        prozess1.setSimpleProperty(Anwendung.PROP_TAG, MasterConverter.TAG_MOGS_PROZESS);

        anwendungenKategorie.addChild(prozess1);

        CnATreeElement netzKomponentenKategorie = itVerbund.getCategory(NKKategorie.TYPE_ID);
        NetzKomponente netzKomponente = new NetzKomponente(netzKomponentenKategorie);
        netzKomponente.setTitel("Netzkomponente 1");
        netzKomponentenKategorie.addChild(netzKomponente);

        elementDao.saveOrUpdate(itVerbund);
        CnALink linkAnwendungProzess = new CnALink(prozess1, anwendung1, null, "");
        cnaLinkDao.saveOrUpdate(linkAnwendungProzess);
        CnALink linkServerAnwendung = new CnALink(server1, anwendung1, null, "foo");
        cnaLinkDao.saveOrUpdate(linkServerAnwendung);
        CnALink linkAnwendungNetzKomponente = new CnALink(anwendung1, netzKomponente, null, null);
        cnaLinkDao.saveOrUpdate(linkAnwendungNetzKomponente);
        CnALink linkServer1Server2 = new CnALink(server1, server2, null, "bar");
        cnaLinkDao.saveOrUpdate(linkServer1Server2);

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        CnATreeElement itSystemGroup = findChildWithTypeId(itNetwork, ItSystemGroup.TYPE_ID);
        assertNotNull(itSystemGroup);
        assertEquals("IT-Systeme", itSystemGroup.getTitle());

        CnATreeElement itSystemGroupServers = findChildWithTitle(itSystemGroup, "Server");
        assertNotNull(itSystemGroupServers);

        CnATreeElement itSystem1 = findChildWithTitle(itSystemGroupServers, "Server 1");
        assertNotNull(itSystem1);

        CnATreeElement itSystem2 = findChildWithTitle(itSystemGroupServers, "Server 2");
        assertNotNull(itSystem2);

        CnATreeElement networkGroup = findChildWithTypeId(itNetwork, NetworkGroup.TYPE_ID);
        assertNotNull(networkGroup);

        CnATreeElement network = findChildWithTypeId(networkGroup, Network.TYPE_ID);
        assertNotNull(network);

        CnATreeElement applicationGroup = findChildWithTypeId(itNetwork, ApplicationGroup.TYPE_ID);
        assertNotNull(applicationGroup);

        CnATreeElement application = findChildWithTypeId(applicationGroup, Application.TYPE_ID);
        assertNotNull(application);

        CnATreeElement businessProcessGroup = findChildWithTypeId(itNetwork,
                BusinessProcessGroup.TYPE_ID);
        assertNotNull(businessProcessGroup);

        CnATreeElement businessProcess = findChildWithTypeId(businessProcessGroup,
                BusinessProcess.TYPE_ID);
        assertNotNull(businessProcess);

        Set<CnALink> linksUpFromItSystem = itSystem1.getLinksUp();
        assertEquals(1, linksUpFromItSystem.size());
        CnALink linkUpFromItSystem = linksUpFromItSystem.iterator().next();
        assertEquals(application, linkUpFromItSystem.getDependant());
        assertEquals("rel_bp_application_bp_itsystem", linkUpFromItSystem.getRelationId());
        assertEquals("foo", linkUpFromItSystem.getComment());

        Set<CnALink> linksUpFromApplication = application.getLinksUp();
        assertEquals(1, linksUpFromApplication.size());
        Set<CnALink> linksApplicationBusinessProcess = getLinksWithType(application,
                "rel_bp_businessprocess_bp_application");
        assertEquals(1, linksApplicationBusinessProcess.size());
        CnALink linkApplicationBusinessProcess = linksApplicationBusinessProcess.iterator().next();
        assertEquals(businessProcess, linkApplicationBusinessProcess.getDependant());
        assertEquals("", linkApplicationBusinessProcess.getComment());

        Set<CnALink> linksUpFromNetwork = network.getLinksUp();
        assertEquals(1, linksUpFromNetwork.size());
        Set<CnALink> linksNetworkApplication = getLinksWithType(network,
                "rel_bp_application_bp_network");
        assertEquals(1, linksNetworkApplication.size());
        CnALink linkNetworkApplication = linksNetworkApplication.iterator().next();
        assertEquals(application, linkNetworkApplication.getDependant());
        assertEquals(null, linkNetworkApplication.getComment());

        Set<CnALink> linksDownFromItSystem = itSystem1.getLinksDown();
        assertEquals(1, linksDownFromItSystem.size());
        CnALink linkDownFromItSystem = linksDownFromItSystem.iterator().next();
        assertEquals(itSystem2, linkDownFromItSystem.getDependency());
        assertEquals("rel_bp_itsystem_bp_itsystem", linkDownFromItSystem.getRelationId());
        assertEquals("[PRÜFEN:Verknüpfungstyp] bar", linkDownFromItSystem.getComment());

    }

    @Test
    public void convert_itverbund_with_generic_link_duplicating_specific_link()
            throws CommandException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);
        itVerbund.createNewCategories();

        CnATreeElement gebaeudeKategorie = itVerbund.getCategory(GebaeudeKategorie.TYPE_ID);
        Gebaeude gebaeude = new Gebaeude(gebaeudeKategorie);
        gebaeudeKategorie.addChild(gebaeude);

        CnATreeElement raeumeKategorie = itVerbund.getCategory(RaeumeKategorie.TYPE_ID);
        Raum raum = new Raum(raeumeKategorie);
        raeumeKategorie.addChild(raum);

        elementDao.saveOrUpdate(itVerbund);

        CnALink linkRaumGebaeudeSpecific = new CnALink(raum, gebaeude, null, "");
        cnaLinkDao.saveOrUpdate(linkRaumGebaeudeSpecific);
        CnALink linkRaumGebaeudeGeneric = new CnALink(raum, gebaeude, "raum_gebaeude", "");
        cnaLinkDao.saveOrUpdate(linkRaumGebaeudeGeneric);

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        CnATreeElement roomGroup = findChildWithTypeId(itNetwork, RoomGroup.TYPE_ID);
        assertNotNull(roomGroup);

        CnATreeElement roomGroupRooms = findChildWithTitle(roomGroup, "Räume");
        CnATreeElement room = findChildWithTypeId(roomGroupRooms, Room.TYPE_ID);

        CnATreeElement roomGroupBuildings = findChildWithTitle(roomGroup, "Gebäude");
        CnATreeElement building = findChildWithTypeId(roomGroupBuildings, Room.TYPE_ID);

        Set<CnALink> linksDownFromRoom = room.getLinksDown();
        assertEquals(1, linksDownFromRoom.size());
        CnALink linkDownFromRoom = linksDownFromRoom.iterator().next();
        assertEquals(building, linkDownFromRoom.getDependency());
        assertEquals("rel_bp_room_bp_room", linkDownFromRoom.getRelationId());
        assertEquals("", linkDownFromRoom.getComment());

    }

    @Test
    public void convert_itverbund_with_missing_categories() throws CommandException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);
        RaeumeKategorie raeumeKategorie = new RaeumeKategorie(itVerbund);
        itVerbund.addChild(raeumeKategorie);

        Raum raum = new Raum(raeumeKategorie);
        raum.setTitel("Raum 1");
        raeumeKategorie.addChild(raum);

        elementDao.saveOrUpdate(itVerbund);

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        CnATreeElement roomGroup = findChildWithTypeId(itNetwork, RoomGroup.TYPE_ID);
        CnATreeElement roomGroupRooms = findChildWithTypeId(roomGroup, RoomGroup.TYPE_ID);
        CnATreeElement room = findChildWithTypeId(roomGroupRooms, Room.TYPE_ID);
        assertEquals("Raum 1", room.getTitle());
    }

    @Test
    public void container_groups_get_merged_access_permissions() throws CommandException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);

        RaeumeKategorie raeumeKategorie = new RaeumeKategorie(itVerbund);
        itVerbund.addChild(raeumeKategorie);
        Raum raum = new Raum(raeumeKategorie);
        raeumeKategorie.addChild(raum);

        CnATreeElement gebaeudeKategorie = new GebaeudeKategorie(itVerbund);
        itVerbund.addChild(gebaeudeKategorie);
        Gebaeude gebaeude = new Gebaeude(gebaeudeKategorie);
        gebaeudeKategorie.addChild(gebaeude);

        gebaeudeKategorie
                .addPermission(Permission.createPermission(gebaeudeKategorie, "foo", true, false));
        raeumeKategorie
                .addPermission(Permission.createPermission(raeumeKategorie, "bar", true, false));

        elementDao.saveOrUpdate(itVerbund);

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        CnATreeElement roomGroup = findChildWithTypeId(itNetwork, RoomGroup.TYPE_ID);
        CnATreeElement roomGroupRooms = findChildWithTitle(roomGroup, "Räume");

        CnATreeElement roomGroupBuildings = findChildWithTitle(roomGroup, "Gebäude");
        Assert.assertEquals(2, roomGroup.getPermissions().size());
        Assert.assertThat(roomGroup.getPermissions().stream().map(Permission::getRole)
                .collect(Collectors.toSet()), JUnitMatchers.hasItems("foo", "bar"));

        Assert.assertEquals(1, roomGroupRooms.getPermissions().size());
        Assert.assertEquals(1, roomGroupBuildings.getPermissions().size());
    }

    @Test
    public void only_read_permissions_are_merged_for_container_groups() throws CommandException {
        ITVerbund itVerbund = new ITVerbund(bsiModel);

        RaeumeKategorie raeumeKategorie = new RaeumeKategorie(itVerbund);
        itVerbund.addChild(raeumeKategorie);
        Raum raum = new Raum(raeumeKategorie);
        raeumeKategorie.addChild(raum);

        CnATreeElement gebaeudeKategorie = new GebaeudeKategorie(itVerbund);
        itVerbund.addChild(gebaeudeKategorie);
        Gebaeude gebaeude = new Gebaeude(gebaeudeKategorie);
        gebaeudeKategorie.addChild(gebaeude);

        gebaeudeKategorie
                .addPermission(Permission.createPermission(gebaeudeKategorie, "foo", true, false));
        raeumeKategorie
                .addPermission(Permission.createPermission(raeumeKategorie, "foo", false, true));

        elementDao.saveOrUpdate(itVerbund);

        ConverterCommand command = new ConverterCommand(Collections.singleton(itVerbund.getUuid()));
        commandService.executeCommand(command);

        ItNetwork itNetwork = (ItNetwork) elementDao
                .findByCriteria(DetachedCriteria.forClass(ItNetwork.class)).get(0);

        CnATreeElement roomGroup = findChildWithTypeId(itNetwork, RoomGroup.TYPE_ID);
        Assert.assertEquals(1, roomGroup.getPermissions().size());
        Permission permission = roomGroup.getPermissions().iterator().next();
        Assert.assertTrue(permission.isReadAllowed());
        Assert.assertFalse(permission.isWriteAllowed());

    }

}
