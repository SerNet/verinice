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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.PieChartModel;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.service.DAOFactory;
import sernet.verinice.web.poseidon.services.ControlService;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "allBsiChartsView")
public class AllBsiChartsView {

    @ManagedProperty("#{controlService}")
    private ControlService controlService;

    private List<VeriniceAllChartItem> charts;


    @PostConstruct
    public void init() {

        charts = new ArrayList<>();

        DAOFactory daoFactory = (DAOFactory) VeriniceContext.get(VeriniceContext.DAO_FACTORY);
        IBaseDao<ITVerbund, Serializable> itNetworkDao = daoFactory.getDAO(ITVerbund.class);
        @SuppressWarnings("unchecked")
        List<ITVerbund> itNetworks = itNetworkDao.findAll(RetrieveInfo.getPropertyInstance());
        itNetworks = sortItNetworks(itNetworks);

        for (ITVerbund itNetwork : itNetworks) {

            SortedMap<String, Number> states = getControlService().aggregateMassnahmenUmsetzung(itNetwork);

            if(states.isEmpty()) continue;

            VeriniceAllChartItem item = new VeriniceAllChartItem();
            ChartModelFactory chartModelFactory = new ChartModelFactory(states);

            item.setTitle(itNetwork.getTitle());
            item.pieChartModel = chartModelFactory.getPieChartModel();
            item.horizontalBarChartModel = chartModelFactory.getHorizontalBarModel();

            charts.add(item);
        }
    }



    private List<ITVerbund> sortItNetworks(List<ITVerbund> itNetworks) {
        Collections.sort(itNetworks, new Comparator<ITVerbund>() {
            @Override
            public int compare(ITVerbund o1, ITVerbund o2) {
                return new NumericStringComparator().compare(o1.getTitle(), o2.getTitle());
            }
        });

        return itNetworks;
    }



    public List<VeriniceAllChartItem> getCharts() {
        return charts;
    }

    public void setCharts(List<VeriniceAllChartItem> charts) {
        this.charts = charts;
    }

    public static class VeriniceAllChartItem {

        private String id;

        private String title;

        private PieChartModel pieChartModel;
        private HorizontalBarChartModel horizontalBarChartModel;

        public VeriniceAllChartItem() {
            setId(UUID.randomUUID().toString());
        }

        public PieChartModel getPieChartModel() {
            return pieChartModel;
        }

        public void setPieChartModel(PieChartModel pieChartModel) {
            this.pieChartModel = pieChartModel;
        }

        public HorizontalBarChartModel getHorizontalBarChartModel() {
            return horizontalBarChartModel;
        }

        public void setHorizontalBarChartModel(HorizontalBarChartModel horizontalBarChartModel) {
            this.horizontalBarChartModel = horizontalBarChartModel;
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
    }

    public ControlService getControlService() {
        return controlService;
    }

    public void setControlService(ControlService controlService) {
        this.controlService = controlService;
    }

}
