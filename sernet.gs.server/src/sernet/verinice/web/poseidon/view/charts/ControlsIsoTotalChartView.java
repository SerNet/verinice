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
import org.primefaces.model.chart.PieChartModel;

import sernet.verinice.web.poseidon.services.ChartService;
import sernet.verinice.web.poseidon.services.StateData;

/**
 * Provide backing beands for charts of ISO
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "controlsIsoTotalChartView")
@ViewScoped
public class ControlsIsoTotalChartView {

    @ManagedProperty("#{chartService}")
    private ChartService chartService;

    private StateData states;

    private PieChartModel pieModel;

    private BarChartModel barModel;

    private boolean calculated = false;

    private Integer scopeId;

    @PostConstruct
    public void init() {
        readParameter();
    }

    private void readParameter() {
        Map<String, String> parameterMap = getParameterMap();
        this.scopeId = Integer.valueOf(parameterMap.get("scopeId"));
    }

    private Map<String, String> getParameterMap() {
        return (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    }

    public void loadStates() {
        ControlChartsFactory chartModelFactory = new ControlChartsFactory(getStates());
        this.pieModel = chartModelFactory.getPieChartModel();
        this.barModel = chartModelFactory.getBarChart();
        this.calculated = true;
    }

    private StateData getStates() {
        if (states == null) {
            states = getChartService().aggregateControlStates(scopeId);
        }
        return states;
    }

    public boolean dataAvailable() {
        return states.dataAvailable();
    }

    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public PieChartModel getPieModel() {
        return pieModel;
    }

    public BarChartModel getBarModel() {
        return barModel;
    }

    public void setBarModel(BarChartModel barModel) {
        this.barModel = barModel;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }
}
