/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import sernet.verinice.web.poseidon.services.ControlService;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "totalBsiChartView")
public class TotalBsiChartsView extends AbstractBsiChartsView implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManagedProperty("#{controlService}")
    private ControlService controlService;

    private SortedMap<String, Number> states;

    @Override
    protected SortedMap<String, Number> getStates() {
        if (states == null){
            states = getControlService().aggregateMassnahmenUmsetzungStatus();
        }
        return states;
    }

    public ControlService getControlService() {
        return controlService;
    }

    public void setControlService(ControlService controlService) {
        this.controlService = controlService;
    }
}
