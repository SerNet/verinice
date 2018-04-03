package sernet.verinice.dataprotection.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Sets;
import org.junit.After;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.dataprotection.migration.MigrateDataProtectionCommand;
import sernet.verinice.service.test.CommandServiceProvider;


public class MigrateDataProtectionCommandTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(MigrateDataProtectionCommandTest.class);
    private static final Set<String> DP_PROPERTIES = Sets.newHashSet(
            "control_data_protection_objectives_eugdpr_pseudonymization",
            "control_data_protection_objectives_eugdpr_encryption",
            "control_data_protection_objectives_eugdpr_confidentiality",
            "control_data_protection_objectives_eugdpr_integrity",
            "control_data_protection_objectives_eugdpr_availability",
            "control_data_protection_objectives_eugdpr_resilience",
            "control_data_protection_objectives_eugdpr_recoverability",
            "control_data_protection_objectives_eugdpr_effectiveness");

    private static Set<CnATreeElement> createdElements = new HashSet<>();

    @After
    public void tearDown() throws CommandException {
        RemoveElement<CnATreeElement> removeElementCmd = new RemoveElement<>(createdElements);
        commandService.executeCommand(removeElementCmd);
        createdElements.clear();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOneProcess() throws CommandException {
        Organization organization = createOrganization("test org");
        createdElements.add(organization);

        CnATreeElement pgroup = organization.getGroup(ProcessGroup.TYPE_ID);
        CnATreeElement process = createNewElement((Group<CnATreeElement>) pgroup, Process.class);

        CnATreeElement cgroup = organization.getGroup(ControlGroup.TYPE_ID);
        CnATreeElement control = createNewElement((Group<CnATreeElement>) cgroup, Control.class);
        control.setTitel("5.1.1 Informationssicherheitsrichtlinie");
        control = updateElement(control);

        createdElements.add(process);
        createdElements.add(control);

        createLink(process, control, "rel_process_control_Zutrittskontrolle");
        createLink(process, control, "rel_process_control_Zugangskontrolle");

        MigrateDataProtectionCommand selectAffectedProcesses = new MigrateDataProtectionCommand(
                organization.getDbId());

        commandService.executeCommand(selectAffectedProcesses);
        Set<CnATreeElement> processes = selectAffectedProcesses.getProcesses();

        assertEquals(1, processes.size());
        CnATreeElement p = processes.iterator().next();
        assertEquals(process.getUuid(), p.getUuid());

        p = loadAllDataFromElement(p);

        Set<CnALink> linksDown = p.getLinksDown();
        assertEquals(1, linksDown.size());
        CnALink next = linksDown.iterator().next();

        assertEquals(MigrateDataProtectionCommand.REL_PROCESS_CONTROL_OBJECTIVES,
                next.getId().getTypeId());

        control = loadAllDataFromElement(control);

        for (String pname : DP_PROPERTIES) {
            assertEquals("1", control.getPropertyValue(pname));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOneProcessTwoControls() throws CommandException {
        Organization organization = createOrganization("test org");
        createdElements.add(organization);

        CnATreeElement pgroup = organization.getGroup(ProcessGroup.TYPE_ID);
        CnATreeElement process = createNewElement((Group<CnATreeElement>) pgroup, Process.class);

        CnATreeElement cgroup = organization.getGroup(ControlGroup.TYPE_ID);
        CnATreeElement control = createNewElement((Group<CnATreeElement>) cgroup, Control.class);
        control.setTitel("5.1.1 Informationssicherheitsrichtlinie");
        control = updateElement(control);

        createLink(process, control, "rel_process_control_Zutrittskontrolle");
        createLink(process, control, "rel_process_control_Zugangskontrolle");

        CnATreeElement control1 = createNewElement((Group<CnATreeElement>) cgroup, Control.class);
        control1.setTitel(
                "14.1.1 Analyse und Spezifikation von Informationssicherheitsanforderungen");
        control1 = updateElement(control1);
        createdElements.add(process);
        createdElements.add(control);
        createdElements.add(control1);

        createLink(process, control1, "rel_process_control_Zutrittskontrolle");
        createLink(process, control1, "rel_process_control_Zugangskontrolle");

        MigrateDataProtectionCommand selectAffectedProcesses = new MigrateDataProtectionCommand(
                organization.getDbId());

        commandService.executeCommand(selectAffectedProcesses);
        Set<CnATreeElement> processes = selectAffectedProcesses.getProcesses();

        assertEquals(1, processes.size());
        CnATreeElement p = processes.iterator().next();
        assertEquals(process.getUuid(), p.getUuid());

        p = loadAllDataFromElement(p);

        Set<CnALink> linksDown = p.getLinksDown();
        assertEquals(2, linksDown.size());

        for (CnALink cnALink : linksDown) {
            assertEquals(MigrateDataProtectionCommand.REL_PROCESS_CONTROL_OBJECTIVES,
                    cnALink.getId().getTypeId());
        }
        control = loadAllDataFromElement(control);

        for (String pname : DP_PROPERTIES) {
            assertEquals("1", control.getPropertyValue(pname));
        }

        control1 = loadAllDataFromElement(control1);

        for (String pname : DP_PROPERTIES) {
            assertEquals("1", control1.getPropertyValue(pname));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOneProcessUnrelatedControl() throws CommandException {
        Organization organization = createOrganization("test org");
        createdElements.add(organization);

        CnATreeElement pgroup = organization.getGroup(ProcessGroup.TYPE_ID);
        CnATreeElement process = createNewElement((Group<CnATreeElement>) pgroup, Process.class);

        CnATreeElement cgroup = organization.getGroup(ControlGroup.TYPE_ID);
        CnATreeElement control = createNewElement((Group<CnATreeElement>) cgroup, Control.class);
        control.setTitel("5.1.1 Informationssicherheitsrichtlinie");
        control = updateElement(control);

        createLink(process, control, "rel_process_control_Zutrittskontrolle");
        createLink(process, control, "rel_process_control_Zugangskontrolle");

        CnATreeElement control1 = createNewElement((Group<CnATreeElement>) cgroup, Control.class);
        control1.setTitel("Unrelated control");
        control1 = updateElement(control1);
        createdElements.add(process);
        createdElements.add(control);
        createdElements.add(control1);

        createLink(process, control1, "rel_process_control_Zutrittskontrolle");
        createLink(process, control1, "rel_process_control_Zugangskontrolle");

        MigrateDataProtectionCommand selectAffectedProcesses = new MigrateDataProtectionCommand(
                organization.getDbId());

        commandService.executeCommand(selectAffectedProcesses);
        Set<CnATreeElement> processes = selectAffectedProcesses.getProcesses();

        assertEquals(1, processes.size());
        CnATreeElement p = processes.iterator().next();
        assertEquals(process.getUuid(), p.getUuid());

        p = loadAllDataFromElement(p);

        Set<CnALink> linksDown = p.getLinksDown();
        assertEquals(3, linksDown.size());

        control = loadAllDataFromElement(control);

        for (String pname : DP_PROPERTIES) {
            assertEquals("1", control.getPropertyValue(pname));
        }

        control1 = loadAllDataFromElement(control1);

        for (String pname : DP_PROPERTIES) {
            assertEquals("0", control1.getPropertyValue(pname));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAllRelations() throws CommandException {
        Organization organization = createOrganization("test org");
        createdElements.add(organization);

        CnATreeElement pgroup = organization.getGroup(ProcessGroup.TYPE_ID);
        CnATreeElement cgroup = organization.getGroup(ControlGroup.TYPE_ID);

        Map<String, CnATreeElement> pmap = new HashMap<>();
        for (String relationType : MigrateDataProtectionCommand.RELATIONS) {
            CnATreeElement process = createNewElement((Group<CnATreeElement>) pgroup,
                    Process.class);
            CnATreeElement control = createNewElement((Group<CnATreeElement>) cgroup,
                    Control.class);
            control.setTitel("5.1.1 Informationssicherheitsrichtlinie");
            updateElement(control);
            createdElements.add(process);
            createdElements.add(control);

            createLink(process, control, relationType);
            pmap.put(relationType, process);
        }

        MigrateDataProtectionCommand selectAffectedProcesses = new MigrateDataProtectionCommand(
                organization.getDbId());

        commandService.executeCommand(selectAffectedProcesses);
        Set<CnATreeElement> processes = selectAffectedProcesses.getProcesses();

        assertEquals(MigrateDataProtectionCommand.RELATIONS.size(), processes.size());
        for (String relationType : MigrateDataProtectionCommand.RELATIONS) {
            CnATreeElement cnATreeElement = pmap.get(relationType);
            CnATreeElement p = null;
            for (CnATreeElement p1 : processes) {
                if (p1.getUuid().equals(cnATreeElement.getUuid()))
                    p = p1;
            }
            assertNotNull(p);
            p = loadAllDataFromElement(p);
            assertEquals(1, p.getLinksDown().size());

            CnALink cnALink = p.getLinksDown().iterator().next();

            assertEquals(MigrateDataProtectionCommand.REL_PROCESS_CONTROL_OBJECTIVES,
                    cnALink.getId().getTypeId());
            CnATreeElement control = cnALink.getDependency();

            control = loadAllDataFromElement(control);
            for (String pname : DP_PROPERTIES) {
                assertEquals("1", control.getPropertyValue(pname));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTwoOrganizations() throws Exception {
        Organization organization = createOrganization("test org");
        createdElements.add(organization);

        CnATreeElement pgroup = organization.getGroup(ProcessGroup.TYPE_ID);
        CnATreeElement process = createNewElement((Group<CnATreeElement>) pgroup, Process.class);

        CnATreeElement cgroup = organization.getGroup(ControlGroup.TYPE_ID);
        CnATreeElement control = createNewElement((Group<CnATreeElement>) cgroup, Control.class);
        control.setTitel("5.1.1 Informationssicherheitsrichtlinie");
        control = updateElement(control);
        createdElements.add(process);
        createdElements.add(control);

        createLink(process, control, "rel_process_control_Zutrittskontrolle");
        createLink(process, control, "rel_process_control_Zugangskontrolle");

        Organization organization1 = createOrganization("test org1");
        createdElements.add(organization1);

        CnATreeElement pgroup1 = organization.getGroup(ProcessGroup.TYPE_ID);
        CnATreeElement process1 = createNewElement((Group<CnATreeElement>) pgroup1, Process.class);

        CnATreeElement cgroup1 = organization.getGroup(ControlGroup.TYPE_ID);
        CnATreeElement control1 = createNewElement((Group<CnATreeElement>) cgroup1, Control.class);
        control1.setTitel("5.1.1 Informationssicherheitsrichtlinie");
        control1 = updateElement(control1);
        createdElements.add(process1);
        createdElements.add(control1);

        createLink(process1, control1, "rel_process_control_Zutrittskontrolle");
        createLink(process1, control1, "rel_process_control_Zugangskontrolle");

        MigrateDataProtectionCommand selectAffectedProcesses = new MigrateDataProtectionCommand(
                organization.getDbId(), organization1.getDbId());

        commandService.executeCommand(selectAffectedProcesses);
        Set<CnATreeElement> processes = selectAffectedProcesses.getProcesses();

        assertEquals(2, processes.size());

        process = loadAllDataFromElement(process);
        Set<CnALink> linksDown = process.getLinksDown();
        assertEquals(1, linksDown.size());
        CnALink next = linksDown.iterator().next();

        assertEquals(MigrateDataProtectionCommand.REL_PROCESS_CONTROL_OBJECTIVES,
                next.getId().getTypeId());

        control = loadAllDataFromElement(control);

        for (String pname : DP_PROPERTIES) {
            assertEquals("1", control.getPropertyValue(pname));
        }

        process1 = loadAllDataFromElement(process1);
        Set<CnALink> linksDown1 = process.getLinksDown();
        assertEquals(1, linksDown1.size());
        CnALink next1 = linksDown1.iterator().next();

        assertEquals(MigrateDataProtectionCommand.REL_PROCESS_CONTROL_OBJECTIVES,
                next1.getId().getTypeId());

        control1 = loadAllDataFromElement(control1);

        for (String pname : DP_PROPERTIES) {
            assertEquals("1", control1.getPropertyValue(pname));
        }

    }

    /**
     * @param control
     * @return
     * @throws CommandException
     */
    private CnATreeElement loadAllDataFromElement(CnATreeElement control) throws CommandException {
        RetrieveInfo ri = new RetrieveInfo().setProperties(true).setLinksUp(true)
                .setLinksDown(true);
        LoadElementByUuid<CnATreeElement> loadElementByUuid = new LoadElementByUuid<CnATreeElement>(
                control.getUuid(), ri);
        LoadElementByUuid<CnATreeElement> executeCommand = commandService
                .executeCommand(loadElementByUuid);
        control = executeCommand.getElement();
        return control;
    }

}
