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

import org.apache.commons.lang.StringUtils;
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
import sernet.verinice.model.iso27k.Control;
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
    private static final String[] TYPE_IDS = new String[] { Process.TYPE_ID, Control.TYPE_ID };
    public static final String REL_PROCESS_CONTROL_OBJECTIVES = "rel_process_control_objectives";
    public static final List<String> RELATIONS = Lists.newArrayList(
            "rel_process_control_Zutrittskontrolle", "rel_process_control_Zugangskontrolle",
            "rel_process_control_Zugriffskontrolle", "rel_process_control_Weitergabekontrolle",
            "rel_process_control_Eingabekontrolle", "rel_process_control_Auftragskontrolle",
            "rel_process_control_Verfügbarkeitskontrolle",
            "rel_process_control_Trennungskontrolle");
    private Set<CnATreeElement> processes;
    private Set<CnATreeElement> transformedControls = new HashSet<>();
    private Set<CnATreeElement> affectedObjects = new HashSet<>();
    private Set<CnATreeElement> missedControls = new HashSet<>();

    public MigrateDataProtectionCommand(Integer... scopeIds) {
        initalizeCommand(scopeIds);
    }

    public MigrateDataProtectionCommand(Set<CnATreeElement> selectedElementSet) {
        Set<Integer> set = new HashSet<>();
        for (CnATreeElement cnATreeElement : selectedElementSet) {
            set.add(cnATreeElement.getDbId());
        }
        initalizeCommand(set.toArray(new Integer[selectedElementSet.size()]));
    }

    /**
     * @param scopeIds
     */
    protected void initalizeCommand(Integer... scopeIds) {
        GraphElementLoader loader = new GraphElementLoader();
        loader.setTypeIds(TYPE_IDS);
        if (scopeIds.length > 0) {
            loader.setScopeIds(scopeIds);
        }
        addLoader(loader);
        for (String relation : RELATIONS) {
            addRelationId(relation);
        }
    }

    @Override
    public void executeWithGraph() {
        VeriniceGraph processGraph = getGraph();
        processes = processGraph.getElements(Process.TYPE_ID);
        Set<CnATreeElement> controls = processGraph.getElements(Control.TYPE_ID);

        for (CnATreeElement control : controls) {
            Set<PropertyType> toms = getTOMS(control);
            if (toms != null && !toms.isEmpty()) {
                try {
                    updateControlWithToms(control, toms);
                    Set<CnATreeElement> affectedProcesses = removeLinks(control, processGraph);
                    createLinks(control, affectedProcesses);
                    affectedObjects.addAll(affectedProcesses);
                    transformedControls.add(control);
                } catch (CommandException e) {
                    LOG.error("Error while transforming data protection controls. Current Control: "
                            + control.getTitle(), e);
                }
            } else {
                missedControls.add(control);
            }
        }
    }

    /**
     * Create the new dataprotection link between the control and the processes.
     *
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
     * Remove the old dataprotection links between control and process.
     *
     * @return the affected processes
     * @throws CommandException
     */
    private Set<CnATreeElement> removeLinks(CnATreeElement control, VeriniceGraph processGraph)
            throws CommandException {
        Set<Edge> allRelations = processGraph.getEdgesByElementType(control, Process.TYPE_ID);

        String cUuid = control.getUuid();
        Set<CnATreeElement> affectedProcesses = new HashSet<>();
        RemoveLink<CnALink> removeLinkCmd = new RemoveLink<>();
        for (Edge edge : allRelations) {
            if (!RELATIONS.contains(edge.getType())) {
                continue;// ensure we only take the right link types (might be
                         // unnecessary)
            }

            if (edge.getSource().getUuid().equals(cUuid)) {
                affectedProcesses.add(edge.getTarget());
            } else {
                affectedProcesses.add(edge.getSource());
            }
            removeLinkCmd.addLinkData(edge.getSource().getDbId(),
                    edge.getTarget().getDbId(), edge.getType());
        }
        getCommandService().executeCommand(removeLinkCmd);
        return affectedProcesses;
    }

    /**
     * Write the state of the mapped control in the control properties.
     *
     * @throws CommandException
     */
    private void updateControlWithToms(CnATreeElement control, Set<PropertyType> toms)
            throws CommandException {
        for (PropertyType property : toms) {
            control.getEntity().setSimpleValue(property, "1");
        }
        UpdateElementEntity<CnATreeElement> updateCmd = new UpdateElementEntity<>(control,
                ChangeLogEntry.STATION_ID);
        UpdateElementEntity<CnATreeElement> command = getCommandService().executeCommand(updateCmd);
        affectedObjects.add(command.getElement());
    }

    /**
     * Get the TOMS set for a given control.
     */
    private Set<PropertyType> getTOMS(CnATreeElement control) {
        String title = control.getTitle();
        if (StringUtils.isEmpty(title)) {
            return null;
        }
        return TomMapper.getInstance().getMapping(title.trim());
    }

    public Set<CnATreeElement> getProcesses() {
        return processes;
    }

    public Set<CnATreeElement> getAffectedObjects() {
        return affectedObjects;
    }

    public Set<CnATreeElement> getMissedControls() {
        return missedControls;
    }

    public Set<CnATreeElement> getControls() {
        return transformedControls;
    }

}
