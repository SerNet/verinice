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

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.PieChartModel;

import sernet.verinice.web.poseidon.services.ChartService;
import sernet.verinice.web.poseidon.services.StateData;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "controlsIsoChartView")
@ViewScoped
public class ControlsIsoChartView {

    @ManagedProperty("#{chartService}")
    private ChartService chartService;

    private String organization;

    private PieChartModel pieChart;

    private BarChartModel horizontalBarChartModel;

    private boolean dataCalculated = false;

    private int catalogId;

    private Integer scopeId;

    private StateData states;

    @PostConstruct
    public void init() {
        readParameter();
    }

    private void readParameter() {
        Map<String, String> parameterMap = getParameterMap();
        this.catalogId = Integer.valueOf(parameterMap.get("catalogId"));
        this.scopeId = Integer.valueOf(parameterMap.get("scopeId"));
        this.organization = parameterMap.get("organizationName");
    }


    private Map<String, String> getParameterMap() {
        return (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    }

    public void loadData() {

        states = chartService.aggregateControlStates(scopeId, catalogId);

        pieChart = new PieChartModel();
        pieChart.setData(states.getStates());
        pieChart.setSeriesColors(states.getColors());
        pieChart.setExtender("verinicePie");

        horizontalBarChartModel = new BarChartModel();
        ChartSeries series = new ChartSeries();
        for (Map.Entry<String, Number> entry : states.getStates().entrySet()) {
            series.set(entry.getKey(), entry.getValue());
        }

        horizontalBarChartModel.addSeries(series);
        horizontalBarChartModel.setSeriesColors(states.getColors()  );
        horizontalBarChartModel.setExtender("veriniceVerticalBar");

        dataCalculated = true;
    }



    public PieChartModel getPieChart() {
        return pieChart;
    }

    public void setPieChart(PieChartModel pieChart) {
        this.pieChart = pieChart;
    }

    public BarChartModel getHorizontalBarChartModel() {
        return horizontalBarChartModel;
    }

    public boolean dataAvailable() {
        return states.dataAvailable();
    }

    public void setHorizontalBarChartModel(HorizontalBarChartModel horizontalBarChartModel) {
        this.horizontalBarChartModel = horizontalBarChartModel;
    }

    public boolean isDataCalculated() {
        return dataCalculated;
    }

    public void setDataCalculated(boolean dataCalculated) {
        this.dataCalculated = dataCalculated;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

}
