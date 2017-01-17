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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
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
public abstract class AbstractBsiChartsView implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String IMPLEMENTATION_STATUS_UNEDITED = "SingleSelectDummyValue";

    private PieChartModel pieModel;

    private HorizontalBarChartModel horizontalBarModel;



    @PostConstruct
    final public void init() {

        ChartModelFactory chartModelFactory = new ChartModelFactory(getStates());
        this.pieModel = chartModelFactory.getPieChartModel();
        this.horizontalBarModel = chartModelFactory.getHorizontalBarModel();
    }

    abstract protected SortedMap<String, Number> getStates();


    public PieChartModel getPieModel() {
        return pieModel;
    }


    public HorizontalBarChartModel getHorizontalBarModel() {
        return horizontalBarModel;
    }

    public void setHorizontalBarModel(HorizontalBarChartModel horizontalBarModel) {
        this.horizontalBarModel = horizontalBarModel;
    }
}
