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
package sernet.verinice.web.poseidon.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

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
import sernet.verinice.web.poseidon.services.ControlService;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategySum;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "bausteinUmsetzungAllView")
public class BausteinUmsetzungAllView {

    @ManagedProperty("#{controlService}")
    private ControlService controlService;

    private List<VeriniceChartRow> charts;

    private HorizontalBarChartModel horizontalBarChart;

    private BarChartModel verticalBarChart;

    @PostConstruct
    public void init() {

        Map<String, Map<String, Number>> allStates = controlService.groupByMassnahmenStates("", new GroupByStrategySum());
        BstChartFactory allChartModelFactory = new BstChartFactory(allStates);

        verticalBarChart = allChartModelFactory.getVerticalBarChartModel();
        horizontalBarChart = allChartModelFactory.getHorizontalBarChartModel();

        charts = new ArrayList<>();

        DAOFactory daoFactory = (DAOFactory) VeriniceContext.get(VeriniceContext.DAO_FACTORY);
        IBaseDao<ITVerbund, Serializable> itNetworkDao = daoFactory.getDAO(ITVerbund.class);
        @SuppressWarnings("unchecked")
        List<ITVerbund> itNetworks = itNetworkDao.findAll(RetrieveInfo.getPropertyInstance());
        itNetworks = sortItNetworks(itNetworks);

        for (ITVerbund itNetwork : itNetworks) {

            Map<String, Map<String, Number>> states = controlService.groupByMassnahmenStates(itNetwork, new GroupByStrategySum());

            if(states.isEmpty()) continue;

            VeriniceChartRow item = new VeriniceChartRow();
            BstChartFactory chartModelFactory = new BstChartFactory(states);

            item.setTitle(itNetwork.getTitle());
            item.setFirstChartModel(chartModelFactory.getVerticalBarChartModel());
            HorizontalBarChartModel horizontalBarChartModel = chartModelFactory.getHorizontalBarChartModel();
            item.setSecondChartModel(horizontalBarChartModel);
            Axis axis = horizontalBarChartModel.getAxis(AxisType.X);

            axis.setMax(horizontalBarChart.getAxis(AxisType.X).getMax());
            charts.add(item);
        }
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


    public ControlService getControlService() {
        return controlService;
    }

    public void setControlService(ControlService controlService) {
        this.controlService = controlService;
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
}
