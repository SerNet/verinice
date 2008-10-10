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

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IMassnahmenDAO;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenHibernateDAO;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

public class SchichtenBarChart implements IChartGenerator {

	public JFreeChart createChart() {
		// return createBarChart(createBarDataset());
		return createSpiderChart(createBarDataset());
	}

	protected JFreeChart createBarChart(Object dataset) {
		JFreeChart chart = ChartFactory.createBarChart3D(null,
				"Umsetzung nach Schichten", "Maßnahmen",
				(CategoryDataset) dataset, PlotOrientation.HORIZONTAL, false,
				true, false);
		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setForegroundAlpha(0.6f);
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		plot.getDomainAxis().setCategoryLabelPositions(
				CategoryLabelPositions.STANDARD);
		return chart;

	}

	protected JFreeChart createSpiderChart(Object dataset) {
		SpiderWebPlot plot = new SpiderWebPlot((CategoryDataset) dataset);
		//plot.setStartAngle(54);
		//plot.setInteriorGap(0.40);
		plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		
		JFreeChart chart = new JFreeChart("Umsetzung nach Schichten",
				TextTitle.DEFAULT_FONT, plot, false);

		LegendTitle legend = new LegendTitle(plot);
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legend);
		
		return chart;
	}

	protected Object createBarDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		IMassnahmenDAO dao = new MassnahmenHibernateDAO();

		Map<String, Integer> items1 = dao.getSchichtenSummary();
		Set<Entry<String, Integer>> entrySet = items1.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			dataset.addValue(entry.getValue(), "zugeordnet", getLabel(entry
					.getKey()));
		}

		Map<String, Integer> items2 = dao.getCompletedSchichtenSummary();
		Set<Entry<String, Integer>> entrySet2 = items2.entrySet();
		for (Entry<String, Integer> entry : entrySet2) {
			dataset.addValue(entry.getValue(), "umgesetzt", getLabel(entry
					.getKey()));
		}

		return dataset;
	}

	private String getLabel(String key) {
		return BausteinUmsetzung.getSchichtenBezeichnung(key);
	}

}
