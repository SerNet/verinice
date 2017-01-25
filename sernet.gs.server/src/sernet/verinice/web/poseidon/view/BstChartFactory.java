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
import java.util.Map.Entry;
import java.util.SortedMap;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.LegendPlacement;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class BstChartFactory extends AbstractChartModelFactory {

    private SortedMap<String, Map<String, Number>> data;

    private HorizontalBarChartModel horizontalBarChartModel;

    public BstChartFactory(SortedMap<String, Map<String, Number>> data) {
        this.data = data;
    }




    public HorizontalBarChartModel getHorizontalBarChartModel() {

        HorizontalBarChartModel horizontalBarModel = new HorizontalBarChartModel();

        setData(horizontalBarModel);
        horizontalBarModel.setLegendPlacement(LegendPlacement.OUTSIDE);

        Axis xAxis = horizontalBarModel.getAxis(AxisType.X);
        Axis yAxis = horizontalBarModel.getAxis(AxisType.Y);
        horizontalBarModel.setExtender("veriniceHorizontalBar");

        yAxis.setLabel("Status");

        return horizontalBarChartModel;
    }


    private void setData(HorizontalBarChartModel horizontalBarModel) {

        for (Map.Entry<String, Map<String, Number>> entry : data.entrySet()) {
            ChartSeries series = new ChartSeries();
            for (Entry<String, Number> dataPoint : entry.getValue().entrySet()) {
                series.set(dataPoint.getKey(), dataPoint.getValue());
            }

            horizontalBarModel.addSeries(series);
        }
    }

    public void setHorizontalBarChartModel(HorizontalBarChartModel horizontalBarChartModel) {
        this.horizontalBarChartModel = horizontalBarChartModel;
    }
}
