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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.PieChartModel;

import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.web.poseidon.services.ChartService;
import sernet.verinice.web.poseidon.services.MenuService;

/**
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "controlsIsoAllChartView")
@ViewScoped
public class ControlsIsoAllChartView {

    @ManagedProperty("#{chartService}")
    private ChartService chartService;

    @ManagedProperty("#{menuService}")
    private MenuService menuService;

    private List<VeriniceChartRow> charts;

    private HorizontalBarChartModel horizontalBarChartModel;

    private PieChartModel pieModel;

    private boolean totalCalculated;

    private boolean allCatalogsCalculated;

    private Map<String, Number> states;

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

    public boolean dataAvailable() {
        return states != null && checkValue();
    }

    private boolean checkValue(){
        for(Number number : states.values()){
            if(number.intValue() > 0){
                return true;
            }
        }
        return false;
    }

    private void createTotalIsmsCatalogsChartModels() {
        ControlChartsFactory allChartModelFactory = new ControlChartsFactory(states);
        pieModel = allChartModelFactory.getPieChartModel();
        horizontalBarChartModel = allChartModelFactory.getHorizontalBarModel();
        totalCalculated = true;
    }

    public void loadTotalIsmsCatalogs() {
        charts = new ArrayList<>();
        states = chartService.getIsoControlsData(scopeId);
        createTotalIsmsCatalogsChartModels();
    }

    public void loadAllIsmsCatalogs() {

        charts = new ArrayList<>();

        List<ControlGroup> catalogs = menuService.getCatalogs();
        for (ControlGroup catalog : catalogs) {

            if (catalog.getScopeId().equals(scopeId)) {

                Map<String, Number> catalogStates = getChartService().getIsoControlsData(scopeId, catalog.getDbId());

                if (catalogStates.isEmpty())
                    continue;

                VeriniceChartRow item = new VeriniceChartRow();
                ControlChartsFactory chartModelFactory = new ControlChartsFactory(catalogStates);

                item.setTitle(catalog.getTitle());
                item.setFirstChartModel(chartModelFactory.getPieChartModel());
                HorizontalBarChartModel horizontalBarModel = chartModelFactory.getHorizontalBarModel();
                item.setSecondChartModel(horizontalBarModel);
                Axis axis = horizontalBarModel.getAxis(AxisType.X);
                axis.setMax(horizontalBarChartModel.getAxis(AxisType.X).getMax());
                charts.add(item);
            }
        }

        allCatalogsCalculated = true;
    }

    public List<VeriniceChartRow> getCharts() {
        return charts;
    }

    public void setCharts(List<VeriniceChartRow> charts) {
        this.charts = charts;
    }

    public HorizontalBarChartModel getHorizontalBarChartModel() {
        return horizontalBarChartModel;
    }

    public void setHorizontalBarChartModel(HorizontalBarChartModel horizontalBarChartModel) {
        this.horizontalBarChartModel = horizontalBarChartModel;
    }

    public PieChartModel getPieModel() {
        return pieModel;
    }

    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public boolean isTotalCalculated() {
        return totalCalculated;
    }

    public void setTotalCalculated(boolean totalCalculated) {
        this.totalCalculated = totalCalculated;
    }

    public boolean isAllCatalogsCalculated() {
        return allCatalogsCalculated;
    }

    public void setAllItNetworksCalculated(boolean allItNetworksCalculated) {
        this.allCatalogsCalculated = allItNetworksCalculated;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

    public MenuService getMenuService() {
        return menuService;
    }

    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }
}
