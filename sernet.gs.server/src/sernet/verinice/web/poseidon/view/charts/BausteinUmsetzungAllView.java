/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
package sernet.verinice.web.poseidon.view.charts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.HorizontalBarChartModel;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.service.DAOFactory;
import sernet.verinice.web.poseidon.services.ChartService;
import sernet.verinice.web.poseidon.services.strategy.GroupedByChapterStrategy;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "bausteinUmsetzungAllView")
@ViewScoped
public class BausteinUmsetzungAllView {

    @ManagedProperty("#{chartService}")
    private ChartService chartService;

    private List<VeriniceChartRow> charts;

    private HorizontalBarChartModel horizontalBarChart;

    private BarChartModel verticalBarChart;

    private boolean totalCalculated = false;

    private boolean allItNetworksCalculated = false;

    private GroupedByChapterStrategy strategy;

    private Map<String, Map<String, Number>> allStates;

    @PostConstruct
    public void init() {
        readParameter();
    }

    private void readParameter() {
        Map<String, String> parameterMap = getParameterMap();
        this.strategy = new GroupedByChapterStrategy(parameterMap.get("crunchStrategy"));
    }

    private Map<String, String> getParameterMap() {
        return (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    }

    /**
     * Loads data, does number crunching and sets the
     * {@link #isTotalCalculated()} to true, so the result can be fetched via
     * ajax.
     */
    public void loadTotalData() {
        initTotalCharts();
        totalCalculated = true;
    }

    private void initTotalCharts() {
        allStates = chartService.groupByMassnahmenStates("", strategy.getStrategy());
        ModulChartsFactory allChartModelFactory = new ModulChartsFactory(allStates);

        verticalBarChart = allChartModelFactory.getVerticalBarChartModel();
        horizontalBarChart = allChartModelFactory.getHorizontalBarChartModel();
    }

    public void loadDataForAllItNetworks() {
        initChartsForAllItNetworks();
        allItNetworksCalculated = true;
    }

    private void initChartsForAllItNetworks() {
        List<ITVerbund> itNetworks = loadItNetworks();
        itNetworks = sortItNetworks(itNetworks);
        this.charts = createCharts(itNetworks);
    }

    private List<ITVerbund> loadItNetworks() {
        DAOFactory daoFactory = (DAOFactory) VeriniceContext.get(VeriniceContext.DAO_FACTORY);
        IBaseDao<ITVerbund, Serializable> itNetworkDao = daoFactory.getDAO(ITVerbund.class);
        @SuppressWarnings("unchecked")
        List<ITVerbund> itNetworks = itNetworkDao.findAll(RetrieveInfo.getPropertyInstance());
        return itNetworks;
    }

    private ArrayList<VeriniceChartRow> createCharts(List<ITVerbund> itNetworks) {

        ArrayList<VeriniceChartRow> charts = new ArrayList<>();

        for (ITVerbund itNetwork : itNetworks) {

            Map<String, Map<String, Number>> states = chartService.groupByMassnahmenStates(itNetwork, strategy.getStrategy());

            if (states.isEmpty()) {
                continue;
            }

            VeriniceChartRow item = new VeriniceChartRow();
            ModulChartsFactory chartModelFactory = new ModulChartsFactory(states);

            item.setTitle(itNetwork.getTitle());
            item.setFirstChartModel(chartModelFactory.getVerticalBarChartModel());
            HorizontalBarChartModel horizontalBarChartModel = chartModelFactory.getHorizontalBarChartModel();
            item.setSecondChartModel(horizontalBarChartModel);
            Axis axis = horizontalBarChartModel.getAxis(AxisType.X);

            axis.setMax(horizontalBarChart.getAxis(AxisType.X).getMax());
            charts.add(item);
        }

        return charts;
    }

    private List<ITVerbund> sortItNetworks(List<ITVerbund> itNetworks) {
        Collections.sort(itNetworks, new Comparator<ITVerbund>() {
            @Override
            public int compare(ITVerbund o1, ITVerbund o2) {
                return new NumericStringComparator().compare(o1.getTitle(), o2.getTitle());
            }
        });

        return itNetworks;
    }

    public List<VeriniceChartRow> getCharts() {
        return charts;
    }

    public void setCharts(List<VeriniceChartRow> charts) {
        this.charts = charts;
    }

    public HorizontalBarChartModel getHorizontalBarChartModel() {
        return horizontalBarChart;
    }

    public void setHorizontalBarChartModel(HorizontalBarChartModel horizontalBarChartModel) {
        this.horizontalBarChart = horizontalBarChartModel;
    }

    public BarChartModel getVerticalBarChart() {
        return verticalBarChart;
    }

    public void setVerticalBarChart(BarChartModel verticalBarChart) {
        this.verticalBarChart = verticalBarChart;
    }

    public GroupedByChapterStrategy getStrategyBean() {
        return strategy;
    }

    public void setStrategyBean(GroupedByChapterStrategy strategyBean) {
        this.strategy = strategyBean;
    }

    public boolean isTotalCalculated() {
        return totalCalculated;
    }

    public void setTotalCalculated(boolean totalCalculated) {
        this.totalCalculated = totalCalculated;
    }

    public boolean isAllItNetworksCalculated() {
        return allItNetworksCalculated;
    }

    public void setAllItNetworksCalculated(boolean allItNetworksCalculated) {
        this.allItNetworksCalculated = allItNetworksCalculated;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

    public Map<String, Map<String, Number>> getAllStates() {
        return allStates;
    }

    public void setAllStates(Map<String, Map<String, Number>> allStates) {
        this.allStates = allStates;
    }
}
