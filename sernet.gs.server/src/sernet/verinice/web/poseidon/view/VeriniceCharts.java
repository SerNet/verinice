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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.PieChartModel;

import sernet.gs.ui.rcp.main.actions.GreenboneIntroAction;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.service.model.IObjectModelService;
import sernet.verinice.web.Messages;
import sernet.verinice.web.poseidon.services.ControlService;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "veriniceChartView")
public class VeriniceCharts implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Does not work at the moment */
    private enum DiagramColor {

        GREEN("66CC00"),
        RED("FF4747"),
        YELLOW("FFE47A"),
        GREY("BFBFBF"),
        BLUE("0000FF");

        private String hexColor;

        DiagramColor(String hexColor) {
            this.hexColor = hexColor;

        }

        String getColor() {
            return hexColor;
        }

        public String toString() {
            return getColor();
        }
    };

    @ManagedProperty("#{controlService}")
    private ControlService controlService;

    private PieChartModel pieModel;

    private HorizontalBarChartModel horizontalBarModel;

    @PostConstruct()
    public void init() {
        createPieModel();
        initBarModel();
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

        model.setData(controlService.aggregateMassnahmenUmsetzungStatus());
        model.setSeriesColors(StringUtils.join(DiagramColor.values(), ","));
        return model;
    }



    private HorizontalBarChartModel initBarModel() {

        setHorizontalBarModel(new HorizontalBarChartModel());
        Map<String, Number> states = controlService.aggregateMassnahmenUmsetzungStatus();

        ChartSeries series = new ChartSeries();
        for (Map.Entry<String, Number> entry : states.entrySet()) {
            series.set(entry.getKey(), entry.getValue());
        }

        getHorizontalBarModel().addSeries(series);
        getHorizontalBarModel().setLegendPosition("e");

        Axis xAxis = getHorizontalBarModel().getAxis(AxisType.X);
        xAxis.setLabel("Anzahl");
        xAxis.setMin(0);
        xAxis.setMax(200);

        Axis yAxis = getHorizontalBarModel().getAxis(AxisType.Y);
        yAxis.setLabel("Status");
        return getHorizontalBarModel();
    }



    public PieChartModel getPieModel() {
        return pieModel;
    }

    public ControlService getControlService() {
        return controlService;
    }

    public void setControlService(ControlService controlService) {
        this.controlService = controlService;
    }

    public HorizontalBarChartModel getHorizontalBarModel() {
        return horizontalBarModel;
    }

    public void setHorizontalBarModel(HorizontalBarChartModel horizontalBarModel) {
        this.horizontalBarModel = horizontalBarModel;
    }
}
