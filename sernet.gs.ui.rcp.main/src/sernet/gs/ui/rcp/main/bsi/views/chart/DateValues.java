package sernet.gs.ui.rcp.main.bsi.views.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.time.Day;

public class DateValues {
	
	Map<Day, Integer>  ts;
	
	public DateValues() {
		ts = new HashMap<Day, Integer>();
	}

	public void add(Date date) {
		Day day = new Day(date);
		if (ts.containsKey(day)) {
			Integer totalForDate = ts.get(day);
			ts.put(day, totalForDate + 1);
		}
		else {
			ts.put(day, 1);
		}
	}
	
	public Map<Day, Integer> getDateTotals() {
		Map<Day, Integer> result = new HashMap<Day, Integer>();
		List<Day> days = new ArrayList<Day>();
		days.addAll(ts.keySet());
		Collections.sort(days);
		int total = 0;
		for (Day day : days) {
			Integer value = ts.get(day);
			total = total + value;
			result.put(day, total);
		}
		return result;
	}
	
}
