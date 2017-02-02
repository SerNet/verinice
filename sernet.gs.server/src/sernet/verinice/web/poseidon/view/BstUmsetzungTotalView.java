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

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.primefaces.model.chart.BarChartModel;

import sernet.verinice.web.poseidon.services.ControlService;
import sernet.verinice.web.poseidon.services.strategy.GroupByStrategySum;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "bstUmsetzungTotalView")
public class BstUmsetzungTotalView {

    private BarChartModel horizontalChartModel;

    private BarChartModel verticalChartModel;

    @ManagedProperty("#{controlService}")
    private ControlService controlService;

    @PostConstruct
    public void init(){
        Map<String, Map<String, Number>> data = controlService.groupByMassnahmenStates("", new GroupByStrategySum());
        BstChartFactory chartModelFactory = new BstChartFactory(data);
        horizontalChartModel = chartModelFactory.getHorizontalBarChartModel();
        verticalChartModel = chartModelFactory.getVerticalBarChartModel();
    }

    public ControlService getControlService() {
        return controlService;
    }

    public void setControlService(ControlService controlService) {
        this.controlService = controlService;
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

}
