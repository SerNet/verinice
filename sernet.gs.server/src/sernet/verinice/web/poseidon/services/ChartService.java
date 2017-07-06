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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.chart.ChartModel;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.web.poseidon.services.strategy.AggregateIsmsControlsStrategy;
import sernet.verinice.web.poseidon.services.strategy.AggregateIsmsControlsStrategyImpl;
import sernet.verinice.web.poseidon.services.strategy.CalculateSafeguardImplementationStrategy;
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

    private static final Logger log = Logger.getLogger(ChartService.class);

    @ManagedProperty("#{menuService}")
    private MenuService menuService = new MenuService();

    /**
     * Returns states of all {@link MassnahmenUmsetzung} in verinice.
     *
     * @return The keys of the map is {@link MassnahmenUmsetzung#getUmsetzung()}
     *
     */
    public StateData aggregateAllSafeguardStates() {
        return aggregateSafeguardStates(null);
    }

    /**
     * Returns states of all {@link MassnahmenUmsetzung} of a it network.
     *
     * @param scopeId
     *            The IT-Network for which the {@link MassnahmenUmsetzung} are
     *            aggregated.
     * @return If no {@link MassnahmenUmsetzung} is defined for the IT network.
     *         it could be empty.
     * @exception IllegalArgumentException
     *                If no it network is given.
     */
    public StateData aggregateSafeguardStates(Integer scopeId) {
        VeriniceGraph g = loadSafeguards(scopeId, new String[] { ITVerbund.TYPE_ID, MassnahmenUmsetzung.TYPE_ID });
        CalculateSafeguardImplementationStrategy strategy = new SimpleSumOfStates();
        return new StateData(getItNetworkTitle(g), strategy.aggregateData(g.getElements(MassnahmenUmsetzung.class)));
    }

    private String getItNetworkTitle(VeriniceGraph g) {
        return g.getElements(ITVerbund.class).iterator().next().getTitle();
    }

    /**
     * Returns states of all {@link MassnahmenUmsetzung} of all it networks. All
     * {@link ITVerbund} which are readable by the user are taken into account.
     *
     * @return If no {@link MassnahmenUmsetzung} is defined for the IT network.
     *         it could be empty.
     */
    public List<StateData> aggregateSafeguardStates() {
        List<StateData> controlsItgsDatas = new ArrayList<>();
        for (ITVerbund itVerbund : menuService.getVisibleItNetworks()) {
            controlsItgsDatas.add(aggregateSafeguardStates(itVerbund.getScopeId()));
        }

        Collections.sort(controlsItgsDatas, new Comparator<StateData>() {
            @Override
            public int compare(StateData o1, StateData o2) {
                return new NumericStringComparator().compare(o1.getScopeName(), o2.getScopeName());
            }
        });

        return controlsItgsDatas;
    }

    /**
     * Returns all module objects {@link BausteinUmsetzung} and aggregate the
     * {@link MassnahmenUmsetzung} states. All {@link ITVerbund} which are
     * readable by the user are taken into account.
     *
     * This method normalizes the data in a way, that the number of a specific
     * state is divided through the number of instances of a specific
     * {@link BausteinUmsetzung}. With instances is meant several
     * {@link BausteinUmsetzung} object with the same chapter value.
     *
     *
     * @param groupByStrategy
     *            Decides which algorithm is used for the number crunching.
     *
     * @return List of module data, which contains meta inforamtion and a result
     *         set for being able processed by {@link ChartModel}.
     */
    public List<ModuleStateData> groupByModuleChapterSafeguardStates(GroupByStrategy groupByStrategy) {

        List<ModuleStateData> modulDataResult = new ArrayList<>();
        for (ITVerbund itVerbund : menuService.getVisibleItNetworks()) {
            String scopeId = String.valueOf(itVerbund.getScopeId());
            ModuleStateData moduleStateData= groupByModuleChapterSafeguardStates(scopeId, groupByStrategy);
            modulDataResult.add(moduleStateData);
        }

        return modulDataResult;
    }

    /**
     * Returns all {@link BausteinUmsetzung} objects and aggregate the
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
    public ModuleStateData groupByModuleChapterSafeguardStates(String scopeId, GroupByStrategy groupByStrategie) {
        Integer scope = checkScopeId(scopeId);
        VeriniceGraph g = loadSafeguards(scope, new String[] {ITVerbund.TYPE_ID,
                BausteinUmsetzung.HIBERNATE_TYPE_ID, MassnahmenUmsetzung.HIBERNATE_TYPE_ID });

        if(g.getElements(MassnahmenUmsetzung.class).isEmpty()){
            return new ModuleStateData(getItNetworkTitle(g));
        }else {
            return new ModuleStateData(getItNetworkTitle(g), groupByStrategie.aggregateMassnahmen(g));
        }
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
    public StateData aggregateControlStates(int scopeId, int catalogId) {

        IGraphService graphService = getGraphService();
        graphService.setLoadLinks(false);
        IGraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setTypeIds(new String[] {Organization.TYPE_ID, ControlGroup.TYPE_ID, Control.TYPE_ID });
        graphElementLoader.setScopeId(scopeId);
        graphService.setLoader(graphElementLoader);
        VeriniceGraph veriniceGraph = graphService.createDirectedGraph();
        ControlGroup controlGroup = (ControlGroup) veriniceGraph.getElement(catalogId);

        AggregateIsmsControlsStrategy strategy = new AggregateIsmsControlsStrategyImpl(veriniceGraph, controlGroup);
        return new StateData(getOrganizationTitle(veriniceGraph), strategy.getData());
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
    public StateData aggregateControlStates(int scopeId) {

        IGraphService graphService = getGraphService();
        graphService.setLoadLinks(false);
        IGraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setTypeIds(new String[] { Organization.TYPE_ID, ControlGroup.TYPE_ID, Control.TYPE_ID });
        graphService.setLoader(graphElementLoader);

        VeriniceGraph veriniceGraph = graphService.createDirectedGraph();

        List<ControlGroup> catalogs = menuService.getCatalogs();
        List<ControlGroup> catalogsOfScopeId = new ArrayList<>();
        for (ControlGroup catalog : catalogs) {
            if (catalog.getScopeId().equals(scopeId)) {
                catalogsOfScopeId.add(catalog);
            }
        }

        AggregateIsmsControlsStrategy strategy = new AggregateIsmsControlsStrategyImpl(veriniceGraph, catalogsOfScopeId);
        return new StateData(getOrganizationTitle(veriniceGraph), strategy.getData());
    }

    private String getOrganizationTitle(VeriniceGraph veriniceGraph) {
        return veriniceGraph.getElements(Organization.class).iterator().next().getTitle();
    }

    private VeriniceGraph loadSafeguards(Integer scopeId, String... typeIds) {
        IGraphService graphService = getGraphService();
        graphService.setLoadLinks(false);
        IGraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setTypeIds(typeIds);
        if (scopeId != null) {
            graphElementLoader.setScopeId(scopeId);
        }
        graphService.setLoader(graphElementLoader);
        return graphService.create();
    }

    private Integer checkScopeId(String scopeId) {

        Integer scope = null;

        if (scopeId == null || StringUtils.EMPTY.equals(scopeId)) {
            return scope;
        }

        try {

            scope = Integer.parseInt(scopeId);
        } catch (NumberFormatException ex) {
            log.warn("scope id not valid", ex);

        }

        return scope;
    }


    public MenuService getMenuService() {
        return menuService;
    }

    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }



}
