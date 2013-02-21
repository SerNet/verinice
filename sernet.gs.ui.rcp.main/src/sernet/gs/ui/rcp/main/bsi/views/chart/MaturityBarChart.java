/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.chart;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.MassnahmenSummaryHome;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MaturityBarChart extends MaturitySpiderChart {
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.bsi.views.chart.ISelectionChartGenerator#createChart
     * (sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public JFreeChart createChart(CnATreeElement elmt) {
        if (!(elmt instanceof ControlGroup)) {
            return null;
        }

        setElmt((ControlGroup) elmt);
        try {
            return createBarChart(createBarDataset());
        } catch (CommandException e) {
            ExceptionUtil.log(e, Messages.MaturityBarChart_0);
            return null;
        }

    }
    
    protected JFreeChart createBarChart(Object dataset) {
        final float plotForegroundAlpha = 0.6f;
        final int axisUpperBound = 9;
        JFreeChart chart = ChartFactory.createBarChart3D(null, 
                Messages.MaturityBarChart_1, Messages.MaturityBarChart_2,
                (CategoryDataset) dataset, PlotOrientation.HORIZONTAL, false,
                true, false);
        chart.setBackgroundPaint(Color.white);
        chart.getPlot().setForegroundAlpha(plotForegroundAlpha);
        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        
        plot.getRenderer().setSeriesPaint(0, ChartColor.LIGHT_BLUE);

        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setUpperBound(axisUpperBound);  
        
        return chart;
    }
    
    protected Object createBarDataset() throws CommandException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MassnahmenSummaryHome dao = new MassnahmenSummaryHome();

        Map<String, Double> items1 = dao.getControlGroups(getElmt());
        Set<Entry<String, Double>> entrySet = items1.entrySet();
        for (Entry<String, Double> entry : entrySet) {
            dataset.addValue(entry.getValue(), Messages.MaturityBarChart_3, entry.getKey());
        }
        return dataset;
    }
}


