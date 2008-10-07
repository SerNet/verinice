package sernet.gs.ui.rcp.main.bsi.views.chart;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.data.category.CategoryDataset;

public class LabelGenerator implements CategorySeriesLabelGenerator {

	public String generateLabel(CategoryDataset dataset, int series) {
		return dataset.getValue(0, series).toString();
	}


}
