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
import java.util.Set;
import java.util.Map.Entry;

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
import sernet.verinice.model.bsi.BausteinUmsetzung;

public class SchichtenBarChart implements IChartGenerator {

	public JFreeChart createChart() {
		// return createBarChart(createBarDataset());
		try {
			return createSpiderChart(createBarDataset());
		} catch (CommandException e) {
			ExceptionUtil.log(e, Messages.SchichtenBarChart_0);
			return null;
		}
	}

	protected JFreeChart createBarChart(Object dataset) {
		JFreeChart chart = ChartFactory.createBarChart3D(null, Messages.SchichtenBarChart_1, Messages.SchichtenBarChart_2, (CategoryDataset) dataset, PlotOrientation.HORIZONTAL, false, true, false);
		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setForegroundAlpha(0.6f);
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
		return chart;

	}

	protected JFreeChart createSpiderChart(Object dataset) {
		SpiderWebPlot plot = new SpiderWebPlot((CategoryDataset) dataset);
		// plot.setStartAngle(54);
		// plot.setInteriorGap(0.40);
		plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());

		JFreeChart chart = new JFreeChart(Messages.SchichtenBarChart_3, TextTitle.DEFAULT_FONT, plot, false);

		LegendTitle legend = new LegendTitle(plot);
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legend);

		return chart;
	}

	protected Object createBarDataset() throws CommandException {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		MassnahmenSummaryHome dao = new MassnahmenSummaryHome();

		Map<String, Integer> items1 = dao.getSchichtenSummary();
		Set<Entry<String, Integer>> entrySet = items1.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			dataset.addValue(entry.getValue(), Messages.SchichtenBarChart_4, getLabel(entry.getKey()));
		}

		Map<String, Integer> items2 = dao.getCompletedSchichtenSummary();
		Set<Entry<String, Integer>> entrySet2 = items2.entrySet();
		for (Entry<String, Integer> entry : entrySet2) {
			dataset.addValue(entry.getValue(), Messages.SchichtenBarChart_5, getLabel(entry.getKey()));
		}

		return dataset;
	}

	private String getLabel(String key) {
		return BausteinUmsetzung.getSchichtenBezeichnung(key);
	}

}
