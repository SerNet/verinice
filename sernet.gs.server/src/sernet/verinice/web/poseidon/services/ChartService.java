/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang.StringUtils;

import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.web.poseidon.services.strategy.AggregateIsmsControlsStrategy;
import sernet.verinice.web.poseidon.services.strategy.AggregateIsmsControlsStrategyImpl;
import sernet.verinice.web.poseidon.services.strategy.AggregateMassnahmenUmsetzungStrategy;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategy;
import sernet.verinice.web.poseidon.services.strategy.SimpleSumOfStates;

/**
 * Provides several methods which provide data for the charts.
 *
 * The user must have at least read access to the verinice object, otherwise it
 * is not used for the data aggregation.
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "chartService")
@ViewScoped
public class ChartService extends GenericChartService {

    @ManagedProperty("#{menuService}")
    private MenuService menuService = new MenuService();

    /**
     * Returns aggregate status of all {@link MassnahmenUmsetzung} in verinice.
     *
     * @return The keys of the map is {@link MassnahmenUmsetzung#getUmsetzung()}
     *
     */
    public SortedMap<String, Number> aggregateMassnahmenUmsetzungStatus() {
        return aggregateMassnahmenUmsetzung(-1);
    }

    /**
     * Returns aggregated status of all {@link MassnahmenUmsetzung} of a it
     * network.
     *
     * @param itNetwork
     *            The IT-Network for which the {@link MassnahmenUmsetzung} are
     *            aggregated.
     * @return If no {@link MassnahmenUmsetzung} is defined for the IT network.
     *         it could be empty.
     * @exception IllegalArgumentException
     *                If no it network is given.
     */
    public SortedMap<String, Number> aggregateMassnahmenUmsetzung(ITVerbund itNetwork) {

        if (itNetwork == null) {
            throw new IllegalArgumentException("param itNetwork may not be null");
        }

        return aggregateMassnahmenUmsetzung(itNetwork.getScopeId());
    }

    /**
     * Returns aggregated status of all {@link MassnahmenUmsetzung} of a it
     * network.
     *
     * @param scopeId
     *            The IT-Network for which the {@link MassnahmenUmsetzung} are
     *            aggregated.
     * @return If no {@link MassnahmenUmsetzung} is defined for the IT network.
     *         it could be empty.
     * @exception IllegalArgumentException
     *                If no it network is given.
     */
    public SortedMap<String, Number> aggregateMassnahmenUmsetzung(Integer scopeId) {
        VeriniceGraph g = loadAllBsiControls(scopeId);
        AggregateMassnahmenUmsetzungStrategy strategy = new SimpleSumOfStates();
        return strategy.aggregateData(g.getElements(MassnahmenUmsetzung.class));
    }

    /**
     * Aggregate over all {@link BausteinUmsetzung} objects and aggregate the
     * {@link MassnahmenUmsetzung} states.
     *
     * This method normalizes the data in a way, that the number of a specific
     * state is divided through the number of instances of a specific
     * {@link BausteinUmsetzung}. With instances is meant several
     * {@link BausteinUmsetzung} object with the same chapter value.
     *
     * @param itNetwork
     *            Only {@link BausteinUmsetzung} under this it network are taken
     *            into account.
     *
     * @param groupByStrategie
     *            Decides which algorithm is used for the number crunshing.
     *
     * @return Key is the chapter of a
     *         {@link MassnahmenUmsetzung#getUmsetzung()}. The value is a map
     *         with the key {@link BausteinUmsetzung#getKapitel()}. This allows
     *         the result to be displayed as a stacked chart.
     */
    public Map<String, Map<String, Number>> groupByMassnahmenStates(ITVerbund itNetwork, GroupByStrategy g) {
        return groupByMassnahmenStates(String.valueOf(itNetwork.getScopeId()), g);
    }

    /**
     * Aggregate over all {@link BausteinUmsetzung} objects and aggregate the
     * {@link MassnahmenUmsetzung} states.
     *
     * This method normalizes the data in a way, that the number of a specific
     * state is divided through the number of instances of a specific
     * {@link BausteinUmsetzung}. With instances is meant several
     * {@link BausteinUmsetzung} object with the same chapter value.
     *
     * @param scopeId
     *            Only {@link BausteinUmsetzung} under this it network are taken
     *            into account.
     * @param groupByStrategie
     *            Decides which algorithm is used for the number crunching.
     *
     * @return Key is the chapter of a
     *         {@link MassnahmenUmsetzung#getUmsetzung()}. The value is a map
     *         with the key {@link BausteinUmsetzung#getKapitel()}. This allows
     *         the result to be displayed as a stacked chart.
     */
    public Map<String, Map<String, Number>> groupByMassnahmenStates(String scopeId, GroupByStrategy groupByStrategie) {
        VeriniceGraph g = loadData(scopeId);
        return groupByStrategie.aggregateMassnahmen(g);
    }

    /**
     * Provides a map which contains the total number of every implementation
     * states of all {@link Control} which belongs to a specific catalog.
     *
     * @param scopeId
     *            Only catalog in this scope are loaded.
     * @param catalogId
     *            The database id of the catalog root element (it is a
     *            {@link ControlGroup}}
     * @return A map which keys are the implementation status property ids. (Can
     *         be looked up in the SNCA.xml)
     */
    public Map<String, Number> getIsoControlsData(int scopeId, int catalogId) {

        IGraphService graphService = getGraphService();
        IGraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setTypeIds(new String[] { ControlGroup.TYPE_ID, Control.TYPE_ID });
        graphElementLoader.setScopeId(scopeId);
        graphService.setLoader(graphElementLoader);
        VeriniceGraph veriniceGraph = graphService.createDirectedGraph();
        ControlGroup controlGroup = (ControlGroup) veriniceGraph.getElement(catalogId);

        AggregateIsmsControlsStrategy strategy = new AggregateIsmsControlsStrategyImpl(veriniceGraph, controlGroup);
        return strategy.getData();
    }

    /**
     * Provides a map which contains the total number of every implementation
     * states of all {@link Control} which are defined under a organization.
     *
     * Every catalog which is readable by the user is taken into account.
     *
     * @param scopeId
     *            Only catalog in this scope are loaded.
     * @return A map which keys are the implementation status property ids. (Can
     *         be looked up in the SNCA.xml)
     */
    public Map<String, Number> getIsoControlsData(int scopeId) {

        IGraphService graphService = getGraphService();
        IGraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setTypeIds(new String[] { ControlGroup.TYPE_ID, Control.TYPE_ID });
        graphService.setLoader(graphElementLoader);

        VeriniceGraph veriniceGraph = graphService.createDirectedGraph();

        List<ControlGroup> catalogs = menuService.getCatalogs();
        List<ControlGroup> catalogsOfScopeId = new ArrayList<>();
        for(ControlGroup catalog : catalogs){
            if(catalog.getScopeId().equals(scopeId)){
                catalogsOfScopeId.add(catalog);
            }
        }

        AggregateIsmsControlsStrategy strategy = new AggregateIsmsControlsStrategyImpl(veriniceGraph, catalogsOfScopeId);
        return strategy.getData();
    }

    private VeriniceGraph loadData(String scopeId) {
        IGraphService graphService = getGraphService();
        IGraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setTypeIds(new String[] { BausteinUmsetzung.HIBERNATE_TYPE_ID, MassnahmenUmsetzung.HIBERNATE_TYPE_ID });

        if (scopeId != null && !StringUtils.EMPTY.equals(scopeId)) {
            graphElementLoader.setScopeId(Integer.valueOf(scopeId));
        }

        graphService.setLoader(graphElementLoader);
        return graphService.create();
    }

    private VeriniceGraph loadAllBsiControls(int scopeId) {
        IGraphService graphService = getGraphService();
        IGraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setTypeIds(new String[] { MassnahmenUmsetzung.HIBERNATE_TYPE_ID });
        if (scopeId != -1) {
            graphElementLoader.setScopeId(scopeId);
        }
        graphService.setLoader(graphElementLoader);
        return graphService.create();
    }

    public MenuService getMenuService() {
        return menuService;
    }

    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

}
