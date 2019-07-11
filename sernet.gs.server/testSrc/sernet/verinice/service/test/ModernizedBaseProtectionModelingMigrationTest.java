package sernet.verinice.service.test;

import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.migration.ModelingMigrationService;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class ModernizedBaseProtectionModelingMigrationTest
        extends AbstractModernizedBaseProtection {

    private static final Logger log = Logger
            .getLogger(ModernizedBaseProtectionModelingMigrationTest.class);

    @Resource(name = "modelingMigrationServiceImpl")
    private ModelingMigrationService migrationService;

    private ItNetwork network;

    @Test
    @Transactional
    @Rollback(true)
    public void migrate_it_network_with_single_requirement_and_safeguard_with_identifiers()
            throws CommandException {
        network = createNewBPOrganization();

        BpRequirementGroup globalModules = createRequirementGroup(network, "global modules");
        BpRequirementGroup module1 = createRequirementGroup(globalModules, "M1", "module 1");
        BpRequirement requirement1 = createBpRequirement(module1, "M1R1", "requirement 1");
        createLink(requirement1, network, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        SafeguardGroup globalSafeguards = createSafeguardGroup(network, "global safeguards");
        SafeguardGroup safeguardGroup1 = createSafeguardGroup(globalSafeguards, "S1",
                "safeguards 1");
        Safeguard safeguard = createSafeguard(safeguardGroup1, "S1S1", "safeguard");
        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        flushAndClear();

        migrateModelingOfItNetwork(network.getDbId());

        network = reloadElement(network);

        CnATreeElement firstModule = reloadElement(findChildWithTitle(network, "module 1"));
        Assert.assertNotNull(firstModule);
        Assert.assertEquals("module 1", firstModule.getTitle());
        Set<CnATreeElement> childrenOfModule1 = firstModule.getChildren();
        Assert.assertEquals(1, childrenOfModule1.size());
        CnATreeElement firstRequirement = reloadElement(
                findChildWithTypeId(firstModule, BpRequirement.TYPE_ID));
        Assert.assertEquals("requirement 1", firstRequirement.getTitle());

        CnATreeElement firstSafeguardGroup = reloadElement(
                findChildWithTitle(network, "safeguards 1"));
        Assert.assertNotNull(firstSafeguardGroup);
        Assert.assertEquals("safeguards 1", firstSafeguardGroup.getTitle());
        Set<CnATreeElement> childrenOfFirstSafeguardGroup = firstSafeguardGroup.getChildren();
        Assert.assertEquals(1, childrenOfFirstSafeguardGroup.size());
        CnATreeElement firstSafeguard = reloadElement(
                findChildWithTypeId(firstSafeguardGroup, Safeguard.TYPE_ID));
        Assert.assertEquals("safeguard", firstSafeguard.getTitle());

        Set<CnALink> linksDownFromFirstRequirement = firstRequirement.getLinksDown();
        Assert.assertEquals(2, linksDownFromFirstRequirement.size());
        Set<CnATreeElement> dependencies = getDependenciesFromLinks(linksDownFromFirstRequirement);
        Assert.assertThat(dependencies,
                JUnitMatchers.<CnATreeElement> hasItems(network, firstSafeguard));

    }

    @Test
    @Transactional
    @Rollback(true)
    public void migrate_single_it_system_with_single_requirement() throws CommandException {
        network = createNewBPOrganization();
        ItSystemGroup itSystems = createGroup(network, ItSystemGroup.class, "ItSystemGroup");
        ItSystem server1 = createElement(itSystems, ItSystem.class, "server 1");
        BpRequirementGroup globalModules = createRequirementGroup(network, "global modules");
        BpRequirementGroup module1 = createRequirementGroup(globalModules, "module 1");
        BpRequirement requirement1 = createBpRequirement(module1, "requirement 1");
        createLink(requirement1, server1, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);

        flushAndClear();

        migrateModelingOfItNetwork(network.getDbId());

        server1 = reloadElement(server1);
        Set<CnATreeElement> childrenOfServer1 = server1.getChildren();
        Assert.assertEquals(1, childrenOfServer1.size());
        CnATreeElement firstModule = reloadElement(
                findChildWithTypeId(server1, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("module 1", firstModule.getTitle());
        Set<CnATreeElement> childrenOfModule1 = firstModule.getChildren();
        Assert.assertEquals(1, childrenOfModule1.size());
        CnATreeElement firstRequirement = reloadElement(
                findChildWithTypeId(firstModule, BpRequirement.TYPE_ID));
        Assert.assertEquals("requirement 1", firstRequirement.getTitle());
        Set<CnALink> linksDownFromFirstRequirement = firstRequirement.getLinksDown();
        Assert.assertEquals(1, linksDownFromFirstRequirement.size());
        CnALink firstLink = linksDownFromFirstRequirement.iterator().next();
        Assert.assertEquals(server1, firstLink.getDependency());

    }

    private void migrateModelingOfItNetwork(Integer itNetworkId) {
        migrationService.migrateModelingOfItNetwork(itNetworkId);
        elementDao.clear();
    }

    private void flushAndClear() {
        elementDao.flush();
        elementDao.clear();
    }

    @Test
    @Transactional
    @Rollback(true)
    public void migrate_single_it_system_with_single_threat() throws CommandException {
        network = createNewBPOrganization();
        ItSystemGroup itSystems = createGroup(network, ItSystemGroup.class);
        ItSystem server1 = createElement(itSystems, ItSystem.class, "server 1");
        BpThreatGroup globalThreats = createBpThreatGroup(network, "global threats");
        BpThreatGroup threatGroup1 = createBpThreatGroup(globalThreats, "threats 1");
        BpThreat threat1 = createBpThreat(threatGroup1, "threat 1");
        createLink(threat1, server1, BpThreat.REL_BP_THREAT_BP_ITSYSTEM);

        flushAndClear();

        migrateModelingOfItNetwork(network.getDbId());

        server1 = reloadElement(server1);
        Set<CnATreeElement> childrenOfServer1 = server1.getChildren();
        Assert.assertEquals(1, childrenOfServer1.size());
        CnATreeElement firstThreatGroup = reloadElement(
                findChildWithTypeId(server1, BpThreatGroup.TYPE_ID));
        Assert.assertEquals("threats 1", firstThreatGroup.getTitle());
        Set<CnATreeElement> childrenOfModule1 = firstThreatGroup.getChildren();
        Assert.assertEquals(1, childrenOfModule1.size());
        CnATreeElement firstThreat = reloadElement(
                findChildWithTypeId(firstThreatGroup, BpThreat.TYPE_ID));
        Assert.assertEquals("threat 1", firstThreat.getTitle());
        Set<CnALink> linksDownFromFirstThreat = firstThreat.getLinksDown();
        Assert.assertEquals(1, linksDownFromFirstThreat.size());
        CnALink firstLink = linksDownFromFirstThreat.iterator().next();
        Assert.assertEquals(server1, firstLink.getDependency());

    }

    @Test
    @Transactional
    @Rollback(true)
    public void migrate_two_servers_sharing_a_module() throws CommandException {
        network = createNewBPOrganization();
        ItSystemGroup itSystems = createGroup(network, ItSystemGroup.class);
        ItSystem server1 = createElement(itSystems, ItSystem.class, "server 1");
        ItSystem server2 = createElement(itSystems, ItSystem.class, "server 2");
        BpRequirementGroup globalModules = createRequirementGroup(network, "global modules");
        BpRequirementGroup module1 = createRequirementGroup(globalModules, "module 1");
        BpRequirement requirement1 = createBpRequirement(module1, "requirement 1");
        createLink(requirement1, server1, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);
        createLink(requirement1, server2, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);

        flushAndClear();

        migrateModelingOfItNetwork(network.getDbId());

        server1 = reloadElement(server1);
        Set<CnATreeElement> childrenOfServer1 = server1.getChildren();
        Assert.assertEquals(1, childrenOfServer1.size());
        CnATreeElement firstModule = reloadElement(
                findChildWithTypeId(server1, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("module 1", firstModule.getTitle());
        Set<CnATreeElement> childrenOfModule1 = firstModule.getChildren();
        Assert.assertEquals(1, childrenOfModule1.size());
        CnATreeElement firstRequirement = reloadElement(
                findChildWithTypeId(firstModule, BpRequirement.TYPE_ID));
        Assert.assertEquals("requirement 1", firstRequirement.getTitle());
        Set<CnALink> linksDownFromFirstRequirement = firstRequirement.getLinksDown();
        Assert.assertEquals(1, linksDownFromFirstRequirement.size());
        CnALink firstLink = linksDownFromFirstRequirement.iterator().next();
        Assert.assertEquals(server1, firstLink.getDependency());

        server2 = reloadElement(server2);
        Set<CnATreeElement> childrenOfServer2 = server2.getChildren();
        Assert.assertEquals(1, childrenOfServer2.size());
        firstModule = reloadElement(findChildWithTypeId(server2, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("module 1", firstModule.getTitle());
        childrenOfModule1 = firstModule.getChildren();
        Assert.assertEquals(1, childrenOfModule1.size());
        firstRequirement = reloadElement(findChildWithTypeId(firstModule, BpRequirement.TYPE_ID));
        Assert.assertEquals("requirement 1", firstRequirement.getTitle());
        linksDownFromFirstRequirement = firstRequirement.getLinksDown();
        Assert.assertEquals(1, linksDownFromFirstRequirement.size());
        firstLink = linksDownFromFirstRequirement.iterator().next();
        Assert.assertEquals(server2, firstLink.getDependency());

    }

    @Test
    @Transactional
    @Rollback(true)
    public void migrate_two_servers_sharing_a_threat() throws CommandException {
        network = createNewBPOrganization();
        ItSystemGroup itSystems = createGroup(network, ItSystemGroup.class);
        ItSystem server1 = createElement(itSystems, ItSystem.class, "server 1");
        ItSystem server2 = createElement(itSystems, ItSystem.class, "server 2");
        BpThreatGroup globalThreats = createBpThreatGroup(network, "global threats");
        BpThreatGroup threatGroup1 = createBpThreatGroup(globalThreats, "threats 1");
        BpThreat threat1 = createBpThreat(threatGroup1, "threat 1");
        createLink(threat1, server1, BpThreat.REL_BP_THREAT_BP_ITSYSTEM);
        createLink(threat1, server2, BpThreat.REL_BP_THREAT_BP_ITSYSTEM);

        flushAndClear();

        migrateModelingOfItNetwork(network.getDbId());

        server1 = reloadElement(server1);
        Set<CnATreeElement> childrenOfServer1 = server1.getChildren();
        Assert.assertEquals(1, childrenOfServer1.size());
        Assert.assertEquals(BpThreatGroup.TYPE_ID, childrenOfServer1.iterator().next().getTypeId());
        CnATreeElement firstThreatGroup = reloadElement(findChildWithTitle(server1, "threats 1"));
        Assert.assertEquals(BpThreatGroup.TYPE_ID, firstThreatGroup.getTypeId());
        Set<CnATreeElement> childrenOfThreatGroup1 = firstThreatGroup.getChildren();
        Assert.assertEquals(1, childrenOfThreatGroup1.size());
        CnATreeElement firstThreat = reloadElement(
                findChildWithTypeId(firstThreatGroup, BpThreat.TYPE_ID));
        Assert.assertEquals("threat 1", firstThreat.getTitle());
        Set<CnALink> linksDownFromFirstThreat = firstThreat.getLinksDown();
        Assert.assertEquals(1, linksDownFromFirstThreat.size());
        CnALink firstLink = linksDownFromFirstThreat.iterator().next();
        Assert.assertEquals(server1, firstLink.getDependency());

        server2 = reloadElement(server2);
        Set<CnATreeElement> childrenOfServer2 = server2.getChildren();
        Assert.assertEquals(1, childrenOfServer2.size());
        firstThreatGroup = reloadElement(findChildWithTitle(server2, "threats 1"));
        Assert.assertEquals(BpThreatGroup.TYPE_ID, firstThreatGroup.getTypeId());
        childrenOfThreatGroup1 = firstThreatGroup.getChildren();
        Assert.assertEquals(1, childrenOfThreatGroup1.size());
        firstThreat = reloadElement(findChildWithTypeId(firstThreatGroup, BpThreat.TYPE_ID));
        Assert.assertEquals("threat 1", firstThreat.getTitle());
        linksDownFromFirstThreat = firstThreat.getLinksDown();
        Assert.assertEquals(1, linksDownFromFirstThreat.size());
        firstLink = linksDownFromFirstThreat.iterator().next();
        Assert.assertEquals(server2, firstLink.getDependency());

    }

    @Test
    @Transactional
    @Rollback(true)
    public void migrate_two_servers_sharing_two_requirements_and_two_safeguard()
            throws CommandException {
        network = createNewBPOrganization();

        ItSystemGroup itSystems = createGroup(network, ItSystemGroup.class);
        ItSystem server1 = createElement(itSystems, ItSystem.class, "server 1");
        ItSystem server2 = createElement(itSystems, ItSystem.class, "server 2");

        BpRequirementGroup globalModules = createRequirementGroup(network, "global modules");
        BpRequirementGroup module1 = createRequirementGroup(globalModules, "M1", "module 1");
        BpRequirement requirement1 = createBpRequirement(module1, "M1R1", "requirement 1");
        BpRequirement requirement2 = createBpRequirement(module1, "M1R2", "requirement 2");

        SafeguardGroup globalSafeguards = createSafeguardGroup(network, "S", "global safeguards");
        SafeguardGroup safeguardGroup1 = createSafeguardGroup(globalSafeguards, "SG1",
                "safeguard group 1");
        Safeguard safeguard1 = createSafeguard(safeguardGroup1, "S1", "safeguard 1");
        Safeguard safeguard2 = createSafeguard(safeguardGroup1, "S2", "safeguard 2");

        createLink(requirement1, server1, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);
        createLink(requirement1, server2, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);
        createLink(requirement1, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        createLink(requirement2, server1, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);
        createLink(requirement2, server2, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);
        createLink(requirement2, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        flushAndClear();

        migrateModelingOfItNetwork(network.getDbId());

        server1 = reloadElement(server1);
        Set<CnATreeElement> childrenOfServer1 = server1.getChildren();

        Assert.assertEquals(2, childrenOfServer1.size());
        Assert.assertEquals(1l, childrenOfServer1.stream()
                .filter(child -> child.getTypeId().equals(BpRequirementGroup.TYPE_ID)).count());
        CnATreeElement firstModule = reloadElement(
                findChildWithTypeId(server1, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("module 1", firstModule.getTitle());
        Set<CnATreeElement> childrenOfModule1 = firstModule.getChildren();
        Assert.assertEquals(2, childrenOfModule1.size());
        CnATreeElement firstRequirement = reloadElement(
                findChildWithTitle(firstModule, "requirement 1"));

        CnATreeElement firstSafeguardGroupServer1 = reloadElement(
                findChildWithTypeId(server1, SafeguardGroup.TYPE_ID));
        Assert.assertNotNull(firstSafeguardGroupServer1);
        Assert.assertEquals("safeguard group 1", firstSafeguardGroupServer1.getTitle());
        Assert.assertEquals(2l, firstSafeguardGroupServer1.getChildren().size());

        CnATreeElement firstSafeguardServer1 = reloadElement(
                findChildWithTitle(firstSafeguardGroupServer1, "safeguard 1"));
        Assert.assertNotNull(firstSafeguardServer1);
        CnATreeElement secondSafeguardServer1 = reloadElement(
                findChildWithTitle(firstSafeguardGroupServer1, "safeguard 2"));
        Assert.assertNotNull(secondSafeguardServer1);

        Assert.assertNotNull(firstRequirement);
        Set<CnALink> linksDownFromFirstRequirement = firstRequirement.getLinksDown();

        Assert.assertEquals(2, linksDownFromFirstRequirement.size());

        Set<CnATreeElement> dependencies = getDependenciesFromLinks(linksDownFromFirstRequirement);
        Assert.assertThat(dependencies,
                JUnitMatchers.<CnATreeElement> hasItems(server1, firstSafeguardServer1));

        CnATreeElement secondRequirement = reloadElement(
                findChildWithTitle(firstModule, "requirement 2"));
        Assert.assertNotNull(secondRequirement);
        Set<CnALink> linksDownFromSecondRequirement = secondRequirement.getLinksDown();

        Assert.assertEquals(2, linksDownFromSecondRequirement.size());
        dependencies = getDependenciesFromLinks(linksDownFromSecondRequirement);
        Assert.assertThat(dependencies,
                JUnitMatchers.<CnATreeElement> hasItems(server1, secondSafeguardServer1));

        server2 = reloadElement(server2);
        Set<CnATreeElement> childrenOfServer2 = server2.getChildren();
        Assert.assertEquals(2, childrenOfServer2.size());
        firstModule = reloadElement(findChildWithTypeId(server2, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("module 1", firstModule.getTitle());
        childrenOfModule1 = firstModule.getChildren();
        Assert.assertEquals(2, childrenOfModule1.size());

        CnATreeElement firstSafeguardGroupServer2 = reloadElement(
                findChildWithTypeId(server2, SafeguardGroup.TYPE_ID));
        Assert.assertNotNull(firstSafeguardGroupServer2);
        Assert.assertEquals("safeguard group 1", firstSafeguardGroupServer2.getTitle());
        Assert.assertEquals(2, firstSafeguardGroupServer2.getChildren().size());

        CnATreeElement firstSafeguardServer2 = reloadElement(
                findChildWithTitle(firstSafeguardGroupServer2, "safeguard 1"));
        Assert.assertNotNull(firstSafeguardServer2);

        CnATreeElement secondSafeguardServer2 = reloadElement(
                findChildWithTitle(firstSafeguardGroupServer2, "safeguard 2"));
        Assert.assertNotNull(secondSafeguardServer2);

        firstRequirement = reloadElement(findChildWithTitle(firstModule, "requirement 1"));
        Assert.assertEquals(BpRequirement.TYPE_ID, firstRequirement.getTypeId());
        linksDownFromFirstRequirement = firstRequirement.getLinksDown();
        Assert.assertEquals(2, linksDownFromFirstRequirement.size());
        dependencies = getDependenciesFromLinks(linksDownFromFirstRequirement);
        Assert.assertThat(dependencies,
                JUnitMatchers.<CnATreeElement> hasItems(server2, firstSafeguardServer2));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void migrate_single_it_system_with_two_requirements_with_same_identifier()
            throws CommandException {
        network = createNewBPOrganization();
        ItSystemGroup itSystems = createGroup(network, ItSystemGroup.class);
        ItSystem server = createElement(itSystems, ItSystem.class, "server");

        BpRequirementGroup globalModule1 = createRequirementGroup(network, "global module 1");
        BpRequirement globalRequirement1 = createBpRequirement(globalModule1, "R1",
                "global requirement 1");
        createLink(globalRequirement1, server, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);

        BpRequirementGroup serverModule1 = createRequirementGroup(server, "server module 1");
        BpRequirement serverRequirement1 = createBpRequirement(serverModule1, "R1",
                "server requirement 1");
        createLink(serverRequirement1, server, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);

        flushAndClear();

        migrateModelingOfItNetwork(network.getDbId());

        server = reloadElement(server);
        Set<CnATreeElement> childrenOfServer1 = server.getChildren();
        Assert.assertEquals(1, childrenOfServer1.size());
        CnATreeElement firstServerModule = reloadElement(
                findChildWithTypeId(server, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("server module 1", firstServerModule.getTitle());
        Set<CnATreeElement> childrenOfModule1 = firstServerModule.getChildren();
        Assert.assertEquals(1, childrenOfModule1.size());
        CnATreeElement firstRequirement = reloadElement(
                findChildWithTypeId(firstServerModule, BpRequirement.TYPE_ID));
        Assert.assertEquals("server requirement 1", firstRequirement.getTitle());
        Set<CnALink> linksDownFromFirstRequirement = firstRequirement.getLinksDown();
        Assert.assertEquals(1, linksDownFromFirstRequirement.size());
        CnALink firstLink = linksDownFromFirstRequirement.iterator().next();
        Assert.assertEquals(server, firstLink.getDependency());

        Set<CnATreeElement> childrenOfNetwork = network.getChildren();
        Assert.assertEquals(1l, childrenOfNetwork.stream()
                .filter(item -> item.getTypeId().equals(BpRequirementGroup.TYPE_ID)).count());
        CnATreeElement firstGlobalModule = reloadElement(
                findChildWithTypeId(network, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("global module 1", firstGlobalModule.getTitle());
        Assert.assertEquals(1, firstGlobalModule.getChildren().size());
        CnATreeElement firstGlobalRequirement = reloadElement(
                findChildWithTypeId(firstGlobalModule, BpRequirement.TYPE_ID));
        Assert.assertEquals("global requirement 1", firstGlobalRequirement.getTitle());

    }

    @Test
    @Transactional
    @Rollback(true)
    public void migrate_two_servers_sharing_a_module_and_a_threat() throws CommandException {
        network = createNewBPOrganization();

        ItSystemGroup itSystems = createGroup(network, ItSystemGroup.class);
        ItSystem server1 = createElement(itSystems, ItSystem.class, "server 1");
        ItSystem server2 = createElement(itSystems, ItSystem.class, "server 2");

        BpRequirementGroup globalModules = createRequirementGroup(network, "global modules");
        BpRequirementGroup module1 = createRequirementGroup(globalModules, "M1", "module 1");
        BpRequirement requirement1 = createBpRequirement(module1, "M1R1", "requirement 1");
        createLink(requirement1, server1, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);
        createLink(requirement1, server2, BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);

        BpThreatGroup globalThreats = createBpThreatGroup(network, "global threats");
        BpThreatGroup threatGroup1 = createBpThreatGroup(globalThreats, "threats 1");
        BpThreat threat1 = createThreat(threatGroup1, "T1", "threat 1");
        createLink(requirement1, threat1, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        createLink(threat1, server1, BpThreat.REL_BP_THREAT_BP_ITSYSTEM);
        createLink(threat1, server2, BpThreat.REL_BP_THREAT_BP_ITSYSTEM);

        flushAndClear();

        migrateModelingOfItNetwork(network.getDbId());

        network = reloadElement(network);

        server1 = reloadElement(server1);
        Set<CnATreeElement> childrenOfServer1 = server1.getChildren();

        Assert.assertEquals(2, childrenOfServer1.size());

        CnATreeElement firstModule = reloadElement(
                findChildWithTypeId(server1, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("module 1", firstModule.getTitle());
        Set<CnATreeElement> childrenOfModule1 = firstModule.getChildren();
        Assert.assertEquals(1, childrenOfModule1.size());
        CnATreeElement firstRequirement = reloadElement(
                findChildWithTitle(firstModule, "requirement 1"));
        Assert.assertNotNull(firstRequirement);
        Set<CnALink> linksDownFromFirstRequirement = firstRequirement.getLinksDown();

        CnATreeElement firstThreatGroup = reloadElement(
                findChildWithTypeId(server1, BpThreatGroup.TYPE_ID));
        Assert.assertEquals("threats 1", firstThreatGroup.getTitle());
        Set<CnATreeElement> childrenOfThreatGroup = firstThreatGroup.getChildren();
        Assert.assertEquals(1, childrenOfThreatGroup.size());
        CnATreeElement firstThreat = reloadElement(
                findChildWithTitle(firstThreatGroup, "threat 1"));
        Assert.assertNotNull(firstThreat);
        Set<CnALink> linksDownFromFirstThreat = firstThreat.getLinksDown();

        Assert.assertEquals(2, linksDownFromFirstRequirement.size());
        Set<CnATreeElement> dependencies = getDependenciesFromLinks(linksDownFromFirstRequirement);
        Assert.assertThat(dependencies,
                JUnitMatchers.<CnATreeElement> hasItems(server1, firstThreat));

        Assert.assertEquals(1, linksDownFromFirstThreat.size());
        dependencies = getDependenciesFromLinks(linksDownFromFirstThreat);
        Assert.assertThat(dependencies, JUnitMatchers.<CnATreeElement> hasItems(server1));
        Set<CnALink> linksUpFromFirstThreat = firstThreat.getLinksUp();

        Assert.assertEquals(1, linksUpFromFirstThreat.size());
        dependencies = getDependantsFromLinks(linksUpFromFirstThreat);
        Assert.assertThat(dependencies, JUnitMatchers.<CnATreeElement> hasItems(firstRequirement));

        server2 = reloadElement(server2);
        Set<CnATreeElement> childrenOfServer2 = server2.getChildren();

        Assert.assertEquals(2, childrenOfServer2.size());

        firstModule = reloadElement(findChildWithTypeId(server2, BpRequirementGroup.TYPE_ID));
        Assert.assertEquals("module 1", firstModule.getTitle());
        childrenOfModule1 = firstModule.getChildren();
        Assert.assertEquals(1, childrenOfModule1.size());
        firstRequirement = reloadElement(findChildWithTitle(firstModule, "requirement 1"));
        Assert.assertNotNull(firstRequirement);
        linksDownFromFirstRequirement = firstRequirement.getLinksDown();

        firstThreatGroup = reloadElement(findChildWithTypeId(server2, BpThreatGroup.TYPE_ID));
        Assert.assertEquals("threats 1", firstThreatGroup.getTitle());
        childrenOfThreatGroup = firstThreatGroup.getChildren();
        Assert.assertEquals(1, childrenOfThreatGroup.size());
        firstThreat = reloadElement(findChildWithTitle(firstThreatGroup, "threat 1"));
        Assert.assertNotNull(firstThreat);
        linksDownFromFirstThreat = firstThreat.getLinksDown();

        Assert.assertEquals(2, linksDownFromFirstRequirement.size());
        dependencies = getDependenciesFromLinks(linksDownFromFirstRequirement);
        Assert.assertThat(dependencies,
                JUnitMatchers.<CnATreeElement> hasItems(server2, firstThreat));

        Assert.assertEquals(1, linksDownFromFirstThreat.size());
        dependencies = getDependenciesFromLinks(linksDownFromFirstThreat);
        Assert.assertThat(dependencies, JUnitMatchers.<CnATreeElement> hasItems(server2));
        linksUpFromFirstThreat = firstThreat.getLinksUp();

        Assert.assertEquals(1, linksUpFromFirstThreat.size());
        dependencies = getDependantsFromLinks(linksUpFromFirstThreat);
        Assert.assertThat(dependencies, JUnitMatchers.<CnATreeElement> hasItems(firstRequirement));

    }

    private long getNumberOfItemsInScope(String typeId, Integer scopeId) {
        return (Long) elementDao.executeCallback(session -> {
            return session.createQuery(
                    "select count(e) from CnATreeElement e where objectType = ? and scopeId = ?")
                    .setParameter(0, typeId).setParameter(1, scopeId).iterate().next();
        });
    }

}
