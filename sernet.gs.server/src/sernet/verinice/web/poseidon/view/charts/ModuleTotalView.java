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

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.primefaces.model.chart.BarChartModel;

import sernet.verinice.web.poseidon.services.ChartService;
import sernet.verinice.web.poseidon.services.ModuleStateData;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategySum;

/**
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "bstUmsetzungTotalView")
public class ModuleTotalView {

    private BarChartModel horizontalChartModel;

    private BarChartModel verticalChartModel;

    @ManagedProperty("#{chartService}")
    private ChartService chartService;

    @PostConstruct
    public void init(){
        ModuleStateData moduleStatedata = getChartService().groupByModuleChapterSafeguardStates("", new GroupByStrategySum());
        ModuleChartsFactory chartModelFactory = new ModuleChartsFactory(moduleStatedata.getData());
        horizontalChartModel = chartModelFactory.getHorizontalBarChartModel();
        verticalChartModel = chartModelFactory.getVerticalBarChartModel();
    }

    public BarChartModel getVerticalChartModel() {
        return verticalChartModel;
    }

    public void setVerticalChartModel(BarChartModel verticalChartModel) {
        this.verticalChartModel = verticalChartModel;
    }

    public BarChartModel getHorizontalChartModel() {
        return horizontalChartModel;
    }

    public void setHorizontalChartModel(BarChartModel horizontalChartModel) {
        this.horizontalChartModel = horizontalChartModel;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

}
