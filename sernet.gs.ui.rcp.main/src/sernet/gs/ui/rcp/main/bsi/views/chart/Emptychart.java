package sernet.gs.ui.rcp.main.bsi.views.chart;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class Emptychart extends UmsetzungBarChart {

	@Override
	public JFreeChart createChart() {
		return createBarChart(createEmptyBarDataset());
	}
	
	private Object createEmptyBarDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		return dataset;
	}
	
	
}
