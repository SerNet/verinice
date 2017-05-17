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

import java.io.Serializable;
import java.util.UUID;

import org.primefaces.model.chart.ChartModel;

public class VeriniceChartRow implements Serializable {

    private static final long serialVersionUID = -4099634714286782260L;

    private String id;

    private String title;

    private ChartModel firstChartModel;

    private ChartModel secondChartModel;

    public VeriniceChartRow() {
        setId(UUID.randomUUID().toString());
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ChartModel getFirstChartModel() {
        return firstChartModel;
    }

    public void setFirstChartModel(ChartModel firstChartModel) {
        this.firstChartModel = firstChartModel;
    }

    public ChartModel getSecondChartModel() {
        return secondChartModel;
    }

    public void setSecondChartModel(ChartModel secondChartModel) {
        this.secondChartModel = secondChartModel;
    }
}