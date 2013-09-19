/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.MassnahmenSummaryHome;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;

public class MaturitySpiderChart implements ISelectionChartGenerator {

    private ControlGroup elmt;

    protected JFreeChart createSpiderChart(Object dataset) {
        SpiderWebPlot plot = new SpiderWebPlot((CategoryDataset) dataset);
        plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());

        plot.setSeriesPaint(0, new Color(0.0f, 1f, 0f, 1f)); // green
        plot.setSeriesPaint(1, new Color(1f, 1f, 0f, 1f)); // yellow
        plot.setSeriesPaint(2, new Color(1f, 0f, 0f, 1f)); // red
        plot.setSeriesPaint(3, new Color(0f, 0f, 0f, 1f)); // grey

        plot.setWebFilled(true);
        JFreeChart chart = new JFreeChart(Messages.MaturitySpiderChart_0, TextTitle.DEFAULT_FONT, plot, false);

        LegendTitle legend = new LegendTitle(plot);
        legend.setPosition(RectangleEdge.BOTTOM);
        chart.addSubtitle(legend);

        return chart;
    }
    
    protected Object createSpiderDataset() throws CommandException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MassnahmenSummaryHome dao = new MassnahmenSummaryHome();

        Map<String, Double> items2 = dao.getControlMaxGroups(elmt);
        Set<Entry<String, Double>> entrySet2 = items2.entrySet();
        for (Entry<String, Double> entry : sort(entrySet2)) {
            dataset.addValue(entry.getValue(), Messages.MaturitySpiderChart_1, entry.getKey());
        }

        Map<String, Double> items4 = dao.getControlGoal2Groups(elmt);
        Set<Entry<String, Double>> entrySet4 = items4.entrySet();
        for (Entry<String, Double> entry : sort(entrySet4)) {
            dataset.addValue(entry.getValue(), Messages.MaturitySpiderChart_2, entry.getKey());
        }

        Map<String, Double> items3 = dao.getControlGoal1Groups(elmt);
        Set<Entry<String, Double>> entrySet3 = items3.entrySet();
        for (Entry<String, Double> entry : sort(entrySet3)) {
            dataset.addValue(entry.getValue(), Messages.MaturitySpiderChart_3, entry.getKey());
        }

        Map<String, Double> items1 = dao.getControlGroups(elmt);
        Set<Entry<String, Double>> entrySet = items1.entrySet();
       
        for (Entry<String, Double> entry : sort(entrySet)) {
            dataset.addValue(entry.getValue(), Messages.MaturitySpiderChart_4, entry.getKey());
        }
        
      

        return dataset;
    }

    /**
     * @param entrySet
     * @return
     */
    private List<Entry<String, Double>> sort(Set<Entry<String, Double>> entrySet) {
        ArrayList<Entry<String, Double>> list = new ArrayList<Entry<String,Double>>();
        list.addAll(entrySet);
        Collections.sort(list, new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.bsi.views.chart.ISelectionChartGenerator#createChart
     * (sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    public JFreeChart createChart(CnATreeElement elmt) {
        if (!(elmt instanceof ControlGroup)){
            return null;
        }
        this.elmt = (ControlGroup) elmt;
        try {
            return createSpiderChart(createSpiderDataset());
        } catch (CommandException e) {
            ExceptionUtil.log(e, Messages.MaturitySpiderChart_5);
            return null;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.views.chart.IChartGenerator#createChart()
     */
    public JFreeChart createChart() {
        return null;
    }

    public ControlGroup getElmt() {
        return elmt;
    }

    public void setElmt(ControlGroup elmt) {
        this.elmt = elmt;
    }

}
