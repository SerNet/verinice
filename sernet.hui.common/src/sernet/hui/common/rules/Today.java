package sernet.hui.common.rules;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class Today implements IFillRule {

	private int changeField = 0;
	
	private int timeDifference = 0;
	
	private final HashMap<String, Integer>  calendarFields = new HashMap<String, Integer>(10);
	
	public String getValue() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.add(changeField, timeDifference);
		return String.valueOf(calendar.getTimeInMillis());
	}

	public void init(String[] params) {
		initFieldValues();
		try {
			if (params != null && params.length == 2) {
				changeField = calendarFields.get(params[0]);
				timeDifference = Integer.parseInt(params[1]);
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug(e);
		}
		
	}

	private void initFieldValues() {
		calendarFields.put("YEAR", Calendar.YEAR);
		calendarFields.put("HOUR", Calendar.HOUR);
		calendarFields.put("MINUTE", Calendar.MINUTE);
		calendarFields.put("SECOND", Calendar.SECOND);
		calendarFields.put("MONTH", Calendar.MONTH);
		calendarFields.put("DAY", Calendar.DAY_OF_MONTH);
	}


}
