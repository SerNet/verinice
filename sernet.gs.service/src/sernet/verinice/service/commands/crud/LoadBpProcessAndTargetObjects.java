/*******************************************************************************
 * Copyright (c) 2019 Alexander Koderman
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
 *     Alexander Koderman - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.crud;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sernet.hui.common.connect.ITargetObject;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.interfaces.oda.IChainableFilter;
import sernet.verinice.interfaces.oda.IFilteringCommand;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;


/**
 * Recursively load all business processes and linked <code>IBPElements</code>.
 * Supports filtering <i>all</i> elements with the given filter criteria.
 * <p>
 * If a filter is set it will be applied as follows:
 * </p>
 * <bl>
 * <li>All processes that match will be removed from the result table with all
 * loaded elements for that process. No linked elements for those processes will
 * be loaded at all.</li>
 * <li>The filter for all other elements is applied only to those elements. This
 * means that elements linked to and from a deleted element will still be 
 * present in the result - if they match the filter themselves.</li>
 * </bl>
 * <p/>

     * <h2>Notice regarding performance:</h2>
     * <p>
     * Adding the relation-ID filter does not affect the runtime by much.
     * Measurements taken in local dev environment:
     * <bl>
     * <li>With relationID filter:    126.3s </li>
     * <li> Without relationID filter: 125.6s</li>
     * </bl>
     * </p>
     * <p>
     * Main reason for the long runtime is the removal of the imported test VNA data:
     * <br/><br/>
     * <table border="1">
     * <tr><td> From start to beginning of test method (loading of VNA):</td><td align="right">     19s </td></tr>
     * <tr><td>From start of test method to initGraph(): </td><td align="right">   2s</td></tr>
     * <tr><td>initGraph():                   </td><td align="right">             17s</td></tr>
     * <tr><td>executeWithGraph():            </td><td align="right">              2s</td></tr>
     * <tr><td>resultAdd() (recursive) and all asserts:  </td><td align="right">   1s</td></tr>
     * <tr><td>Time until positive Junit feedback:   </td><td align="right">     100s</td></tr>
     * </table>
     * </p>
     * <p>
     * The 100s are spent in the method:
     * <pre>
     * VNAImportHelper.removeAllElementsByType(ImportGroup.TYPE_ID, elementDao)
     * </pre>
     * </p>
 */
public class LoadBpProcessAndTargetObjects extends GraphCommand implements IFilteringCommand {
    
    private static final long serialVersionUID = -1162742559987555778L;
    
    /*
     */
    
    private static final String[] RELATION_IDS = {
           // bp process to target objects:
           "rel_bp_businessprocess_bp_businessprocess",
           "rel_bp_businessprocess_bp_application",
           "rel_bp_businessprocess_bp_itsystem" ,
           "rel_bp_businessprocess_bp_icssystem",
           "rel_bp_businessprocess_bp_device" ,
           "rel_bp_businessprocess_bp_network",
           //bp application to target objects:
           "rel_bp_application_bp_application",
           "rel_bp_application_bp_itsystem",
           "rel_bp_application_bp_icssystem",
           "rel_bp_application_bp_device",
           "rel_bp_application_bp_network",
           "rel_bp_application_bp_room",
           // bp itsystem to target objects:
           "rel_bp_itsystem_bp_itsystem",
           "rel_bp_itsystem_bp_itsystem_virtualized",
           "rel_bp_itsystem_bp_icssystem",
           "rel_bp_itsystem_bp_device",
           "rel_bp_itsystem_bp_network",
           "rel_bp_itsystem_bp_room",
           // bp icssystem to target objects:
           "rel_bp_icssystem_bp_icssystem",
           "rel_bp_icssystem_bp_device",
           "rel_bp_icssystem_bp_network",
           "rel_bp_icssystem_bp_room",
           // bp device to target objects:
           "rel_bp_device_bp_device",
           "rel_bp_device_bp_network",
           "rel_bp_device_bp_room",
           // bp network to target objects:
           "rel_bp_network_bp_network",
           "rel_bp_network_bp_room",
           // bp room to target objects:
           "rel_bp_room_bp_room"
    };
    
    private List<List<String>> commandResult;
    private IChainableFilter resultsFilter;
    private boolean isFilterActive;

    public LoadBpProcessAndTargetObjects(int rootId) {
        super();

        GraphElementLoader processLoader = new GraphElementLoader();
        processLoader.setScopeId(rootId);
        processLoader.setTypeIds(new String[] {
                BusinessProcess.TYPE_ID,
                Application.TYPE_ID,
                Device.TYPE_ID,
                IcsSystem.TYPE_ID,
                ItNetwork.TYPE_ID,
                ItSystem.TYPE_ID,
                Network.TYPE_ID,
                Room.TYPE_ID
        });
        addLoader(processLoader);
        
        Stream.of(RELATION_IDS).forEach(this::addRelationId);
        
        this.isFilterActive = false;
    }

    @Override
    public void executeWithGraph() {
        VeriniceGraph processGraph = getGraph();
        Set<CnATreeElement> processes = processGraph.getElements(BusinessProcess.TYPE_ID);

        List<List<String>> result = new LinkedList<>();
        for (CnATreeElement process : processes) {
            result.addAll(addProcessWithLinkedElements(processGraph, process));
        }
        commandResult = result;
    }

    private List<List<String>> addProcessWithLinkedElements(VeriniceGraph processGraph,
            CnATreeElement process) {
        // skip this process if a filter is active and it doesn't match:
        if (this.isFilterActive && !this.resultsFilter.matches(process.getEntity()))
            return Collections.emptyList();

        Set<CnATreeElement> linkTargets = processGraph.getLinkTargets(process);
        Set<CnATreeElement> linkedElementsPerProcessCollector = new HashSet<>();
        for (CnATreeElement element : linkTargets) {
            addAllRequiredTargetObjects(processGraph, element, linkedElementsPerProcessCollector);
        }
        // add elements to result set, skip elements that do not match the
        // filter:
        return linkedElementsPerProcessCollector.stream().filter(
                element -> this.isFilterActive && this.resultsFilter.matches(element.getEntity()))
                .map(elmt -> Arrays.<String> asList(process.getDbId().toString(),
                            elmt.getDbId().toString()))
                .collect(Collectors.toList());
    }
    

    /**
     * Recursively load all linked elements for one process.
     * 
     * @param processGraph - the graph is passed along with each recursive call.
     * @param element - the element for which the links are loaded in this particular method call.
     * @param elementsPerProcess - the collection of loaded elements for one process. This is an output argument: loaded elements
     * will be added to this collection.
     */
    private void addAllRequiredTargetObjects(VeriniceGraph processGraph, CnATreeElement element,
            Set<CnATreeElement> elementsPerProcess) {
        if ( !(element instanceof ITargetObject)
                || elementsPerProcess.contains(element)) {
            return;
        }
        elementsPerProcess.add(element);
        Set<CnALink> linksDown = element.getLinksDown();
        Set<CnATreeElement> linkTargets = linksDown.stream().map(CnALink::getDependency).collect(Collectors.toSet());
        for (CnATreeElement target : linkTargets) {
            addAllRequiredTargetObjects(processGraph, target, elementsPerProcess);
        }
    }

    public List<List<String>> getElements() {
        return commandResult;
    }

 
    @Override
    public void setFilterCriteria(IChainableFilter filter) {
        this.resultsFilter = filter;
        this.isFilterActive = Optional.ofNullable(filter).isPresent();
    }

    @Override
    public boolean isFilterActive() {
        return this.isFilterActive;
    }
}
