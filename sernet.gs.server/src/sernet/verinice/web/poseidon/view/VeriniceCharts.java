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
package sernet.verinice.web.poseidon.view;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;

import sernet.verinice.web.poseidon.services.ControlService;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "veriniceChartView")
public class VeriniceCharts implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManagedProperty("#{controlServiceDummy}")
    private ControlService controlService;

    private PieChartModel pieModel;
    private BarChartModel barModel;

    @PostConstruct()
    public void init() {
        createPieModel();
        createBarModel();
    }

    private void createPieModel() {

        pieModel = initPieModel();

        pieModel.setTitle("Umsetzungstatus Hamburg");
        pieModel.setLegendPosition("w");
        pieModel.setExtender("skinPie");

        initPieModel();
    }

    private PieChartModel initPieModel() {
        PieChartModel model = new PieChartModel();

        Map<String, Integer> states = controlService.getAccumulatedControlStatesForScope("");

        for (Map.Entry<String, Integer> entry : states.entrySet()) {
            model.set(entry.getKey(), entry.getValue());
        }

        return model;
    }

    private void createBarModel() {

        barModel = initBarModel();

        barModel.setTitle("Umsetzungsstatus Hamburg");
        barModel.setLegendPosition("ne");

        Axis xAxis = barModel.getAxis(AxisType.X);
        xAxis.setLabel("Status");

        Axis yAxis = barModel.getAxis(AxisType.Y);
        yAxis.setLabel("Anzahl");
        yAxis.setMin(0);
        yAxis.setMax(100);

        barModel.setExtender("skinBar");
    }

    private BarChartModel initBarModel() {
        BarChartModel model = new BarChartModel();

        ChartSeries status = new ChartSeries();
        status.setLabel("Status");

        Map<String, Integer> states = controlService.getAccumulatedControlStatesForScope("");

        for (Map.Entry<String, Integer> entry : states.entrySet()) {
            status.set(entry.getKey(), entry.getValue());
        }

        model.addSeries(status);

        return model;
    }

    public PieChartModel getPieModel() {
        return pieModel;
    }

    public BarChartModel getBarModel() {
        return barModel;
    }

    public ControlService getControlService() {
        return controlService;
    }

    public void setControlService(ControlService controlService) {
        this.controlService = controlService;
    }
}
