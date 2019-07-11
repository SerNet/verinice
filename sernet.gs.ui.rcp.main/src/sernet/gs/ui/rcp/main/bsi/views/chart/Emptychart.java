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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import sernet.verinice.model.common.CnATreeElement;

public class Emptychart implements ISelectionChartGenerator {

    @Override
    public JFreeChart createChart() {
        return createBarChart(createEmptyBarDataset());
    }

    private Object createEmptyBarDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        return dataset;
    }

    @Override
    public JFreeChart createChart(CnATreeElement elmt) {
        return createBarChart(createEmptyBarDataset());
    }

    protected JFreeChart createBarChart(Object dataset) {
        final float plotForegroundAlpha = 0.6f;
        JFreeChart chart = ChartFactory.createStackedBarChart3D(null, Messages.UmsetzungBarChart_1,
                Messages.UmsetzungBarChart_2, (CategoryDataset) dataset, PlotOrientation.HORIZONTAL,
                false, true, false);
        chart.setBackgroundPaint(Color.white);
        chart.getPlot().setForegroundAlpha(plotForegroundAlpha);
        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        return chart;

    }

}
