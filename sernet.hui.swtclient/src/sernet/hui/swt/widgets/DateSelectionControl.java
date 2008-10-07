/*
 * This file is part of the SerNet Customer Database Application (SNKDB).
 * Copyright Alexander Prack, 2004.
 * 
 *  SNKDB is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   SNKDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SNKDB; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package sernet.hui.swt.widgets;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.snutils.AssertException;
import sernet.snutils.ExceptionHandlerFactory;
import sernet.snutils.FormInputParser;

/**
 * Custom widget to enter a date, either directly or using a date picker (SWT
 * calendar).
 * 
 * @author koderman@sernet.de
 */
public class DateSelectionControl implements IHuiControl {

	private Entity entity;

	private PropertyType fieldType;

	private Composite composite;

	private boolean editable = false;

	private Property savedProp;

	private Label label;

	private DateTime dateTime;

	private Color oldBgColor;

	private Color oldFgColor;

	private boolean useRule;

	public Control getControl() {
		return dateTime;
	}

	/**
	 * @param dyndoc
	 * @param type
	 * @param composite
	 */
	public DateSelectionControl(Entity dyndoc, PropertyType type,
			Composite parent, boolean edit, boolean rules) {
		this.entity = dyndoc;
		this.fieldType = type;
		this.composite = parent;
		this.editable = edit;
		this.useRule = rules;
	}

	/**
	 * @throws AssertException
	 * 
	 */
	public void create() {
		label = new Label(composite, SWT.NULL);

		Composite container = new Composite(composite, SWT.NULL);
		GridLayout contLayout = new GridLayout(1, false);
		contLayout.horizontalSpacing = 0;
		contLayout.marginHeight = 0;
		contLayout.marginLeft = 0;
		contLayout.marginWidth = 0;
		container.setLayout(contLayout);

		GridData containerLData = new GridData();
		containerLData.horizontalAlignment = GridData.FILL;
		containerLData.grabExcessHorizontalSpace = true;
		container.setLayoutData(containerLData);

		
		dateTime = new DateTime(container, SWT.CALENDAR);

		GridData label36LData = new GridData();
		label36LData.verticalAlignment = GridData.CENTER;
		label36LData.horizontalAlignment = GridData.BEGINNING;
		label36LData.widthHint = -1;
		label36LData.heightHint = -1;
		label36LData.horizontalIndent = 0;
		label36LData.horizontalSpan = 1;
		label36LData.verticalSpan = 1;
		label36LData.grabExcessHorizontalSpace = false;
		label36LData.grabExcessVerticalSpace = false;
		label.setLayoutData(label36LData);
		label.setText(fieldType.getName());

		GridData startWvTextLData = new GridData();
		startWvTextLData.horizontalAlignment = GridData.FILL;
		startWvTextLData.horizontalSpan = 1;
		startWvTextLData.grabExcessHorizontalSpace = true;
		dateTime.setLayoutData(startWvTextLData);
		dateTime.setEnabled(editable);
		if (!editable)
			dateTime.setBackground(Colors.GREY);
		oldBgColor = dateTime.getBackground();
		oldFgColor = dateTime.getForeground();
		
		dateTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!isSameDay(savedProp.getPropertyValue(), getDateInMillis()))				
					savedProp.setPropertyValue(Long.toString(getDateInMillis()), true, dateTime);
			}
		});

		List savedProps = entity.getProperties(fieldType.getId())
				.getProperties();
		savedProp = savedProps != null ? (Property) savedProps.get(0) : null;

		String millis = "";
		if (savedProp != null) {
			millis = savedProp.getPropertyValue();
		} else {
			if (useRule)
				millis = fieldType.getDefaultRule().getValue();
			savedProp = entity.createNewProperty(fieldType, millis);
		}

		setDisplayedTime(millis);
		dateTime.setToolTipText(fieldType.getTooltiptext());
	}

	protected boolean isSameDay(String propertyValue, long dateInMillis2) {
		try {
			long dateInMillis1 = Long.parseLong(propertyValue);
			Calendar cal1 = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			cal1.setTimeInMillis(dateInMillis1);
			cal2.setTimeInMillis(dateInMillis2);
			return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
					&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
					&& cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
					);
			
		} catch (Exception e) {
			return false;
		}
	}

	private void setDisplayedTime(String millis) {
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTimeInMillis(Long.parseLong(millis));
		} catch (Exception e) {
			// do nothing, use todays date
		}
		dateTime.setDate(calendar.get(Calendar.YEAR), calendar
				.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}

	protected long getDateInMillis() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(dateTime.getYear(), dateTime.getMonth(),
				dateTime.getDay());
		return calendar.getTimeInMillis();
	}

	public void setFocus() {
	}

	public void update() {
		// not implemented
	}

	public boolean validate() {
		return true;
	}

}
