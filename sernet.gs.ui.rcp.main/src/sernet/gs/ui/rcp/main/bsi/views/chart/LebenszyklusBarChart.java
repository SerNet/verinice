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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.MassnahmenSummaryHome;
import sernet.verinice.interfaces.CommandException;

public class LebenszyklusBarChart implements IChartGenerator {

	public JFreeChart createChart() {
		try {
			return createBarChart(createBarDataset());
		} catch (CommandException e) {
			ExceptionUtil.log(e, Messages.LebenszyklusBarChart_0);
		}
		return null;
	}

	protected JFreeChart createSpiderChart(Object dataset) {
	    final int startAngle = 54;
	    final double interiorGap = 0.4d;
		SpiderWebPlot plot = new SpiderWebPlot((CategoryDataset) dataset);
		plot.setStartAngle(startAngle);
		plot.setInteriorGap(interiorGap);
		plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		JFreeChart chart = new JFreeChart(Messages.LebenszyklusBarChart_1, TextTitle.DEFAULT_FONT, plot, false);
		LegendTitle legend = new LegendTitle(plot);
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legend);
		return chart;
	}

	protected JFreeChart createBarChart(Object dataset) {
	    final float foregroundAlpha = 0.6f;
		JFreeChart chart = ChartFactory.createStackedBarChart3D(null, Messages.LebenszyklusBarChart_2, Messages.LebenszyklusBarChart_3, (CategoryDataset) dataset, PlotOrientation.HORIZONTAL, false, true, false);
		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setForegroundAlpha(foregroundAlpha);
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
		return chart;

	}

	protected Object createBarDataset() throws CommandException {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		MassnahmenSummaryHome dao = new MassnahmenSummaryHome();

		Map<String, Integer> items1 = dao.getNotCompletedZyklusSummary();
		Set<Entry<String, Integer>> entrySet = items1.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			dataset.addValue(entry.getValue(), Messages.LebenszyklusBarChart_4, entry.getKey());
		}

		Map<String, Integer> completedItems = dao.getCompletedZyklusSummary();
		Set<Entry<String, Integer>> entrySet2 = completedItems.entrySet();
		for (Entry<String, Integer> entry : entrySet2) {
			dataset.addValue(entry.getValue(), Messages.LebenszyklusBarChart_5, entry.getKey());
		}

		return dataset;
	}
}
