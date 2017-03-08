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
import java.util.Map;
import java.util.SortedMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.primefaces.component.remotecommand.RemoteCommand;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.PieChartModel;

import sernet.verinice.web.poseidon.services.ControlService;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "itNetworkView")
@ViewScoped
public class ItNetworkView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer scopeId;

    private String itNetwork;

    @ManagedProperty("#{controlService}")
    private ControlService controlService;

    private SortedMap<String, Number> states;

    private PieChartModel pieModel;

    private BarChartModel barModel;

    private boolean calculated = false;

    private RemoteCommand remoteCommand;

    @PostConstruct
    public void init() {
        readParameter();
        initRemoteCall();
    }

    private void readParameter() {
        Map<String, String> parameterMap = getParameterMap();
        this.scopeId = Integer.valueOf(parameterMap.get("scopeId"));
        this.itNetwork = parameterMap.get("itNetwork");
    }

    private Map<String, String> getParameterMap() {
        return (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    }

    private void initRemoteCall() {
        remoteCommand = new RemoteCommand();
        remoteCommand.addActionListener(new DataLoader(getScopeId()));
        remoteCommand.setAutoRun(true);
        remoteCommand.setUpdate("chartPanel");
        remoteCommand.setName("onload");
        remoteCommand.setDelay("2");
    }

    public void loadData(Integer scopeId) {
        setScopeId(scopeId);
        BsiControlChartsFactory chartModelFactory = new BsiControlChartsFactory(getStates());
        this.setPieModel(chartModelFactory.getPieChartModel());
        this.setBarModel(chartModelFactory.getBarChart());
        calculated = true;
    }

    private SortedMap<String, Number> getStates() {
        if (states == null) {
            states = controlService.aggregateMassnahmenUmsetzung(scopeId);
        }

        return states;
    }

    public ControlService getControlService() {
        return controlService;
    }

    public void setControlService(ControlService controlService) {
        this.controlService = controlService;
    }

    public Integer getScopeId() {
        return scopeId;
    }

    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }

    public String getItNetwork() {
        return itNetwork;
    }

    public void setItNetwork(String itNetwork) {
        this.itNetwork = itNetwork;
    }

    public PieChartModel getPieModel() {
        return pieModel;
    }

    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public BarChartModel getBarModel() {
        return barModel;
    }

    public void setBarModel(BarChartModel barModel) {
        this.barModel = barModel;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public RemoteCommand getRemoteCommand() {
        return remoteCommand;
    }

    public void setRemoteCommand(RemoteCommand remoteCommand) {
        this.remoteCommand = remoteCommand;
    }

    private final class DataLoader implements ActionListener {
        private Integer scopeId;

        public DataLoader(Integer scopeId) {
            this.scopeId = scopeId;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            loadData(scopeId);
        }
    }
}
