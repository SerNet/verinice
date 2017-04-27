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
import java.util.SortedMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.PieChartModel;

import sernet.verinice.web.poseidon.services.ChartService;
import sernet.verinice.web.poseidon.services.SafeguardData;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "controlsAllChartView")
@ViewScoped
public class ControlsAllChartView {

    @ManagedProperty("#{chartService}")
    private ChartService chartService;

    private List<VeriniceChartRow> charts;

    private HorizontalBarChartModel horizontalBarChartModel;

    private PieChartModel pieModel;

    private boolean totalCalculated;

    private boolean allItNetworksCalculated;

    private SortedMap<String, Number> allStates;

    public void loadTotalData() {
        allStates = calculateTotal();
        ControlChartsFactory allChartModelFactory = new ControlChartsFactory(allStates);
        pieModel = allChartModelFactory.getPieChartModel();
        horizontalBarChartModel = allChartModelFactory.getHorizontalBarModel();
        totalCalculated = true;
    }

    public void loadDataForAllItNetworks(){

        charts = new ArrayList<>();
        List<SafeguardData> itNetworks = chartService.aggregateSafeguardStates();

        for (SafeguardData controlsItgsData : itNetworks) {

            if (controlsItgsData.noData()){
                continue;
            }

            VeriniceChartRow item = new VeriniceChartRow();
            ControlChartsFactory chartModelFactory = new ControlChartsFactory(controlsItgsData.getStates());

            item.setTitle(controlsItgsData.getItNetworkName());
            item.setFirstChartModel(chartModelFactory.getPieChartModel());
            HorizontalBarChartModel horizontalBarModel = chartModelFactory.getHorizontalBarModel();
            item.setSecondChartModel(horizontalBarModel);
            Axis axis = horizontalBarModel.getAxis(AxisType.X);
            axis.setMax(horizontalBarChartModel.getAxis(AxisType.X).getMax());
            charts.add(item);
        }

        allItNetworksCalculated = true;
    }



    private SortedMap<String, Number> calculateTotal() {
        SortedMap<String, Number> allStates = chartService.aggregateAllSafeguardStates();
        return allStates;
    }


    /**
     * Checks if the total diagram contains data.
     *
     */
    public boolean dataAvailable(){
       if(allStates != null){
           for(Number number  : allStates.values()){
               if(number.intValue() > 0){
                   return true;
               }
           }
           return false;
       } else {
           return false;
       }
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

    public SortedMap<String, Number> getAllStates() {
        return allStates;
    }

    public void setAllStates(SortedMap<String, Number> allStates) {
        this.allStates = allStates;
    }

}
