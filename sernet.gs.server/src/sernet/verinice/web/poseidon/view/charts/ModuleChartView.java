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

import sernet.verinice.web.poseidon.services.ChartService;
import sernet.verinice.web.poseidon.services.ModuleStateData;
import sernet.verinice.web.poseidon.services.strategy.GroupedByChapterStrategy;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "moduleChartView")
@ViewScoped
public class ModuleChartView {

    private BarChartModel horizontalChartModel;

    private BarChartModel verticalChartModel;

    private String itNetwork;

    private String scopeId;

    private GroupedByChapterStrategy strategyBean;

    private boolean calculated = false;

    @ManagedProperty("#{chartService}")
    private ChartService chartService;

    private ModuleStateData moduleStateData;

    @PostConstruct
    public void init() {
        readParameter();
    }

    private void readParameter() {
        Map<String, String> parameterMap = getParameterMap();
        this.scopeId = parameterMap.get("scopeId");
        this.strategyBean = new GroupedByChapterStrategy(parameterMap.get("strategy"));
    }

    private Map<String, String> getParameterMap() {
        return (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    }

    /**
     * Calculates data for the charts and set {@link #setCalculated(boolean)} to
     * true, so the client that data are available.
     */
    public void loadData() {
        createCharts();
        calculated = true;
    }

    private void createCharts() {
        moduleStateData = chartService.groupByModuleChapterSafeguardStates(scopeId, strategyBean.getStrategy());
        ModuleChartsFactory chartModelFactory = new ModuleChartsFactory(moduleStateData.getData());
        horizontalChartModel = chartModelFactory.getHorizontalBarChartModel();
        verticalChartModel = chartModelFactory.getVerticalBarChartModel();
    }

    public boolean dataAvailable() {
        return moduleStateData.dataAvailable();
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

    public String getItNetwork() {
        return itNetwork;
    }

    public void setItNetwork(String itNetwork) {
        this.itNetwork = itNetwork;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public GroupedByChapterStrategy getStrategyBean() {
        return strategyBean;
    }

    public void setStrategyBean(GroupedByChapterStrategy strategyBean) {
        this.strategyBean = strategyBean;
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
