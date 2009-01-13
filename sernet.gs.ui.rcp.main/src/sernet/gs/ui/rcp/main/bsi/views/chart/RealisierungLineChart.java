package sernet.gs.ui.rcp.main.bsi.views.chart;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.statscommands.CountMassnahmen;

public class RealisierungLineChart implements IChartGenerator {

	
	
	public JFreeChart createChart() {
		try {
			return createProgressChart(createProgressDataset());
		} catch (CommandException e) {
			ExceptionUtil.log(e, "Fehler beim Datenzugriff");
		}
		return null;
	}

	
	private JFreeChart createProgressChart(Object dataset) {
		 XYDataset data1 = (XYDataset) dataset;
	        XYItemRenderer renderer1 = new StandardXYItemRenderer();
	        NumberAxis rangeAxis1 = new NumberAxis("Anzahl Maßnahmen");
	        XYPlot subplot1 = new XYPlot(data1, null, rangeAxis1, renderer1);
	        subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
	        
	        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("fällig bis"));
	        plot.setGap(10.0);
	        
	        plot.add(subplot1, 1);
	        plot.setOrientation(PlotOrientation.VERTICAL);
	        
	        CountMassnahmen command = new CountMassnahmen();
	        try {
				command = ServiceFactory.lookupCommandService().executeCommand(command);
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler beim Datenzugriff");
			}
	        int totalNum = command.getTotalCount();
	        
	        NumberAxis axis = (NumberAxis) subplot1.getRangeAxis();
			axis.setUpperBound(totalNum + 50);
	        
	        ValueMarker bst = new ValueMarker(totalNum);
			bst.setPaint(Color.GREEN);
			bst.setLabel("    Maßnahmen insg.");
			bst.setLabelAnchor(RectangleAnchor.LEFT);
			bst.setLabelFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 10));
			bst.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
			subplot1.addRangeMarker(bst, Layer.BACKGROUND);

	        // return a new chart containing the overlaid plot...
	        JFreeChart chart = new JFreeChart(
	            "Realisierungsplan", JFreeChart.DEFAULT_TITLE_FONT, plot, true
	        );
	        chart.setBackgroundPaint(Color.white);
	        return chart;
	}

	private Object createProgressDataset() throws CommandException {
		TimeSeries ts1 =new TimeSeries("umgesetzt", Day.class);
		TimeSeries ts2 =new TimeSeries("alle", Day.class);
		
		LoadCnAElementByType<MassnahmenUmsetzung> command = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		List<MassnahmenUmsetzung> massnahmen = command.getElements();
		
		DateValues dateTotal1 = new DateValues();
		DateValues dateTotal2 = new DateValues();
		
		for (MassnahmenUmsetzung massnahme : massnahmen) {
			
			Date date = massnahme.getUmsetzungBis();
			//fixme umgesetzte sollten datum der umsetzung gesetzt haben! fix in bulk edit
			if (date == null)
				date = Calendar.getInstance().getTime();

			if (massnahme.isCompleted()) {
				dateTotal1.add(date);
				dateTotal2.add(date);
			}
			else  {
				dateTotal2.add(date);
			}
		}
		
		Map<Day, Integer> totals1 = dateTotal1.getDateTotals();
		Set<Entry<Day, Integer>> entrySet1 = totals1.entrySet();
		for (Entry<Day, Integer> entry : entrySet1) {
			ts1.add(entry.getKey(), entry.getValue());
		}

		Map<Day, Integer> totals2 = dateTotal2.getDateTotals();
		Set<Entry<Day, Integer>> entrySet2 = totals2.entrySet();
		for (Entry<Day, Integer> entry : entrySet2) {
			ts2.add(entry.getKey(), entry.getValue());
		}
		
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		tsc.addSeries(ts2);
		tsc.addSeries(ts1);
		return tsc;
	}
}
