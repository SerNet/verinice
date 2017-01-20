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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.PieChartModel;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.service.model.IObjectModelService;
import sernet.verinice.web.Messages;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ChartModelFactory {

    private static final String IMPLEMENTATION_STATUS_UNEDITED = "SingleSelectDummyValue";

    private SortedMap<String, Number> data;

    private enum DiagramColors {

        NO("FF4747"), NOT_APPLICABLE("BFBFBF"), PARTIALLY("FFE47A"), UNEDITED("4a93de"), YES("5fcd79");

        private String color;

        private DiagramColors(String color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return color;
        }
    }

    static final Map<String, DiagramColors> states2Colors;

    static {
        states2Colors = new HashMap<>();
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_JA, DiagramColors.YES);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_NEIN, DiagramColors.NO);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, DiagramColors.PARTIALLY);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET, DiagramColors.UNEDITED);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, DiagramColors.NOT_APPLICABLE);
    }

    public ChartModelFactory(SortedMap<String, Number> data){
        this.data = data;
    }

    public PieChartModel getPieChartModel(){
        PieChartModel model = new PieChartModel();
        model.setData(setLabel(data));
        model.setExtender("verinicePie");
        model.setSeriesColors(getColors());
        return model;
    }

    public BarChartModel getBarChart(){

        BarChartModel barChartModel = new BarChartModel();

        ChartSeries series = new ChartSeries();
        for (Map.Entry<String, Number> entry : setLabel(data).entrySet()) {
            series.set(entry.getKey(), entry.getValue());
        }

        barChartModel.addSeries(series);
        barChartModel.setLegendPlacement(LegendPlacement.OUTSIDE);

        Axis yAxis = barChartModel.getAxis(AxisType.Y);
        yAxis.setMax(getMax(data.values()));

        barChartModel.setExtender("veriniceBar");
        barChartModel.setSeriesColors(getColors());

        return barChartModel;
    }


    public HorizontalBarChartModel getHorizontalBarModel() {

        HorizontalBarChartModel horizontalBarModel = new HorizontalBarChartModel();

        ChartSeries series = new ChartSeries();
        for (Map.Entry<String, Number> entry : setLabel(data).entrySet()) {
            series.set(entry.getKey(), entry.getValue());
        }

        horizontalBarModel.addSeries(series);
        horizontalBarModel.setLegendPlacement(LegendPlacement.OUTSIDE);

        Axis xAxis = horizontalBarModel.getAxis(AxisType.X);
        xAxis.setMin(0);
        xAxis.setMax(getMax(data.values()));

        Axis yAxis = horizontalBarModel.getAxis(AxisType.Y);
        horizontalBarModel.setExtender("veriniceHorizontalBar");
        horizontalBarModel.setSeriesColors(getColors());

        yAxis.setLabel("Status");

        return horizontalBarModel;
    }

    private String getColors() {

        java.util.List<String> colors = new ArrayList<>();
        for (String state : data.keySet()) {
            colors.add(states2Colors.get(state).toString());
        }

        return StringUtils.join(colors, ",");
    }

    private Map<String, Number> setLabel(Map<String, Number> states) {
        Map<String, Number> humanReadableLabels = new TreeMap<>(new NumericStringComparator());
        for (Entry<String, Number> e : states.entrySet()) {
            humanReadableLabels.put(getLabel(e), e.getValue());
        }

        return humanReadableLabels;
    }

    private String getLabel(Map.Entry<String, Number> entry) {

        if (MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET.equals(entry.getKey())) {
            return Messages.getString(IMPLEMENTATION_STATUS_UNEDITED);
        }

        return getObjectModelService().getLabel(entry.getKey());
    }

    private Integer getMax(Collection<Number> values) {
        Collection<Integer> buffer = new ArrayList<>();
        for (Iterator<Number> iterator = values.iterator(); iterator.hasNext();) {
            Number number = iterator.next();
            buffer.add((Integer) number);
        }

        return Collections.max(buffer);
    }

    private IObjectModelService getObjectModelService() {
        return (IObjectModelService) VeriniceContext.get(VeriniceContext.OBJECT_MODEL_SERVICE);
    }
}
