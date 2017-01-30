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
import java.util.UUID;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;

import sernet.verinice.web.poseidon.services.DataPoint;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class BstChartFactory extends AbstractChartModelFactory {

    private Map<String, Map<String, Number>> data;


    public BstChartFactory(Map<String, Map<String, Number>> data) {
        this.data = data;
    }

    public HorizontalBarChartModel getHorizontalBarChartModel() {

        HorizontalBarChartModel horizontalBarModel = new HorizontalBarChartModel();

//        setData(horizontalBarModel);

//        ChartSeries boys = new ChartSeries();
//        boys.setLabel("Boys");
//        boys.set("2004", 50);
//        boys.set("2005", 96);
//        boys.set("2006", 44);
//        boys.set("2007", 55);
//        boys.set("2008", 25);
//
//        ChartSeries girls = new ChartSeries();
//        girls.setLabel("Girls");
//        girls.set("2004", 52);
//        girls.set("2005", 60);
//        girls.set("2006", 82);
//        girls.set("2007", 35);
//        girls.set("2008", 120);
//
//        ChartSeries neutral = new ChartSeries();
//        neutral.setLabel("neutral");
//        neutral.set("2004", 50);
//        neutral.set("2005", 96);
//        neutral.set("2006", 44);
//        neutral.set("2007", 55);
//        neutral.set("2008", 25);
//
//
//        horizontalBarModel.addSeries(boys);
//        horizontalBarModel.addSeries(girls);
//        horizontalBarModel.addSeries(neutral);

        setData(horizontalBarModel);

        horizontalBarModel.setTitle("Horizontal and Stacked");
        horizontalBarModel.setLegendPosition("e");
        horizontalBarModel.setStacked(false);

        Axis xAxis = horizontalBarModel.getAxis(AxisType.X);
        Axis yAxis = horizontalBarModel.getAxis(AxisType.Y);

        xAxis.setLabel("Status");
        xAxis.setMin(0);
        xAxis.setMax(500);

        yAxis.setLabel("Massnahmen");

        return horizontalBarModel;
    }


    private void setData(HorizontalBarChartModel horizontalBarModel) {

        for(Entry<String, Map<String, Number>> dataPoint : data.entrySet()) {
            ChartSeries girls = new ChartSeries();
            girls.setLabel(dataPoint.getKey());
            for(Entry<String, Number> e : dataPoint.getValue().entrySet()) {
                girls.set(e.getKey(), e.getValue());
            }
            horizontalBarModel.addSeries(girls);
        }
    }
}
