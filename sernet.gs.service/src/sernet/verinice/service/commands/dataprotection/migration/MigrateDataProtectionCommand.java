/*******************************************************************************
 * Copyright (c) 2018 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.dataprotection.migration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Lists;

import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveLink;
import sernet.verinice.service.commands.UpdateElementEntity;

/**
 *
 */
public class MigrateDataProtectionCommand extends GraphCommand {

    private static final Logger LOG = Logger.getLogger(MigrateDataProtectionCommand.class);

    private static final long serialVersionUID = 1L;
    private static final String[] TYPE_IDS = new String[] { "process", "control" };
    public static final String REL_PROCESS_CONTROL_OBJECTIVES = "rel_process_control_objectives";
    public static final List<String> RELATIONS = Lists.newArrayList(
            "rel_process_control_Zutrittskontrolle", "rel_process_control_Zugangskontrolle",
            "rel_process_control_Zugriffskontrolle", "rel_process_control_Weitergabekontrolle",
            "rel_process_control_Eingabekontrolle", "rel_process_control_Auftragskontrolle",
            "rel_process_control_Verfügbarkeitskontrolle",
            "rel_process_control_Trennungskontrolle");
    private Set<CnATreeElement> processes;

    public MigrateDataProtectionCommand(Integer... scopeIds) {
        GraphElementLoader loader = new GraphElementLoader();
        loader.setTypeIds(TYPE_IDS);
        if (scopeIds.length > 0) {
            loader.setScopeIds(scopeIds);
        }
        addLoader(loader);
    }

    @Override
    public List<String> getRelationIds() {
        return RELATIONS;
    }

    @Override
    public void addRelationId(String id) {
        // override so no relation can be added later.
    }

    @Override
    public void executeWithGraph() {
        VeriniceGraph processGraph = getGraph();
        processes = processGraph.getElements("process");
        Set<CnATreeElement> controls = processGraph.getElements("control");

        for (CnATreeElement control : controls) {
            Set<PropertyType> toms = getTOMS(control);
            if (toms != null && !toms.isEmpty()) {
                try {
                    updateControlWithToms(control, toms);
                    Set<CnATreeElement> affectedProcesses = removeLinks(control, processGraph);
                    createLinks(control, affectedProcesses);
                } catch (CommandException e) {
                    LOG.error("Error while transforming data protection", e);
                }

            }
        }
    }

    /**
     * @param control
     * @param affectedProcesses
     * @throws CommandException
     */
    private void createLinks(CnATreeElement control, Set<CnATreeElement> affectedProcesses)
            throws CommandException {
        for (CnATreeElement process : affectedProcesses) {
            CreateLink<CnALink, CnATreeElement, CnATreeElement> createLinkCmd = new CreateLink<>(
                    process, control, REL_PROCESS_CONTROL_OBJECTIVES);
            getCommandService().executeCommand(createLinkCmd);
        }
    }

    /**
     * @param control
     * @param processGraph
     * @return the affected processes
     * @throws CommandException
     */
    private Set<CnATreeElement> removeLinks(CnATreeElement control, VeriniceGraph processGraph)
            throws CommandException {
        Set<Edge> allReslations = processGraph.getEdgesByElementType(control, Process.TYPE_ID);

        String cUuid = control.getUuid();
        Set<CnATreeElement> affectedProcesses = new HashSet<>();
        for (Edge edge : allReslations) {
            if (!RELATIONS.contains(edge.getType())) {
                continue;// ensure we only take the right link types (might be
                         // unnecessary)
            }

            if (edge.getSource().getUuid().equals(cUuid)) {
                affectedProcesses.add(edge.getTarget());
            } else {
                affectedProcesses.add(edge.getSource());
            }
            RemoveLink<CnALink> removeLinkCmd = new RemoveLink<>(edge.getSource().getDbId(),
                    edge.getTarget().getDbId(), edge.getType());
            getCommandService().executeCommand(removeLinkCmd);
        }
        return affectedProcesses;
    }

    private void updateControlWithToms(CnATreeElement control, Set<PropertyType> toms)
            throws CommandException {
        for (PropertyType property : toms) {
            control.getEntity().setSimpleValue(property, "1");
        }
        UpdateElementEntity<CnATreeElement> updateCmd = new UpdateElementEntity<>(control,
                ChangeLogEntry.STATION_ID);
        getCommandService().executeCommand(updateCmd);
    }

    private Set<PropertyType> getTOMS(CnATreeElement control) {
        return TomMapper.getInstance().getIsoMapping().get(control.getTitle());
    }

    public Set<CnATreeElement> getProcesses() {
        return processes;
    }

}
