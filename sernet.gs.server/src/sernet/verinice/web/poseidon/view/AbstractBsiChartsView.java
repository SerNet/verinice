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
import java.util.SortedMap;

import javax.annotation.PostConstruct;

import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.PieChartModel;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public abstract class AbstractBsiChartsView implements Serializable {

    private static final long serialVersionUID = 1L;

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
