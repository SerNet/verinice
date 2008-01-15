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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.swt.dialogs.SWTCalendarDialog;
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

	private Text dateText;

	private Button button;

	private Color oldBgColor;

	private Color oldFgColor;

	private boolean useRule;
	
	public Control getControl() {
		return dateText;
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
		GridLayout contLayout = new GridLayout(2, false);
		contLayout.horizontalSpacing = 0;
		contLayout.marginHeight = 0;
		contLayout.marginLeft = 0;
		contLayout.marginWidth = 0;
		container.setLayout(contLayout);

		GridData containerLData = new GridData();
		containerLData.horizontalAlignment = GridData.FILL;
		containerLData.grabExcessHorizontalSpace = true;
		container.setLayoutData(containerLData);

		dateText = new Text(container, SWT.BORDER);
		button = new Button(container, SWT.PUSH | SWT.CENTER);

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
		dateText.setLayoutData(startWvTextLData);
		dateText.setEditable(editable);
		if (!editable)
			dateText.setBackground(Colors.GREY);
		oldBgColor = dateText.getBackground();
		oldFgColor = dateText.getForeground();
		dateText.setDoubleClickEnabled(editable);
		dateText.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent evt) {
				showCalendarDialog();
			}
		});
		dateText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				validate();
			}
		});
		dateText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					// try to fix user input on exit:
					java.sql.Date userDate = FormInputParser
							.stringToDate(dateText.getText());
					dateText.setText(sernet.snutils.FormInputParser
							.dateToString(userDate));
					java.sql.Date fixedDate = FormInputParser
							.stringToDate(dateText.getText());
					savedProp.setPropertyValue(Long.toString(fixedDate
							.getTime()));
					dateText.setBackground(oldBgColor);
					dateText.setForeground(oldFgColor);
				} catch (AssertException e) {
					dateText.setBackground(Colors.YELLOW);
					dateText.setForeground(Colors.BLACK);
				}
			}
		});

		GridData button4LData = new GridData();
		button4LData.verticalAlignment = GridData.CENTER;
		button4LData.horizontalAlignment = GridData.BEGINNING;
		button4LData.widthHint = -1;
		button4LData.heightHint = -1;
		button4LData.horizontalIndent = 0;
		button4LData.horizontalSpan = 1;
		button4LData.verticalSpan = 1;
		button4LData.grabExcessHorizontalSpace = false;
		button4LData.grabExcessVerticalSpace = false;
		button.setLayoutData(button4LData);
		button.setText("Kalender...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				showCalendarDialog();
			}
		});
		button.setEnabled(editable);

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

		String displayDate = "";
		try {
			if (millis != null && millis.length() > 0)
				displayDate = FormInputParser.dateToString(new java.sql.Date(
						Long.parseLong(millis)));
		} catch (AssertException e) {
			Logger.getLogger(DateSelectionControl.class).error(e);
		}

		dateText.setText(displayDate);
		dateText.setToolTipText(fieldType.getTooltiptext());
	}

	/**
	 * Show date picker dialog - date set to value entered by the user in the
	 * text field if possible.
	 * 
	 */
	protected void showCalendarDialog() {
		final SWTCalendarDialog cal = new SWTCalendarDialog(Display
				.getCurrent());

		try {
			cal.setDate(FormInputParser.stringToDate(dateText.getText()));
		} catch (AssertException e) {
			cal.setDate(new Date(Calendar.getInstance().getTimeInMillis()));
		}

		cal.addDateChangedListener(new SWTCalendarListener() {
			public void dateChanged(SWTCalendarEvent event) {
				try {
					savedProp.setPropertyValue(Long.toString(event
							.getCalendar().getTimeInMillis()), true, dateText);
					java.sql.Date newDate = new java.sql.Date(event
							.getCalendar().getTimeInMillis());
					dateText.setText(sernet.snutils.FormInputParser
							.dateToString(newDate));
				} catch (AssertException e) {
					ExceptionHandlerFactory.getDefaultHandler()
							.handleException(e);
				}
			}
		});
		cal.open();
	}

	public void setFocus() {
		this.dateText.setFocus();
	}

	public void update() {
		// not implemented
	}
	
	public boolean validate() {
		// check general rules:
		if (!fieldType.validate(dateText.getText(), null)) {
			dateText.setBackground(Colors.YELLOW);
			dateText.setForeground(Colors.BLACK);
			return false;
		}

		// check date:
		try {
			java.sql.Date userDate;
			userDate = FormInputParser.stringToDate(dateText.getText());
			sernet.snutils.FormInputParser.dateToString(userDate);
			dateText.setBackground(oldBgColor);
			dateText.setForeground(oldFgColor);
			return true;
		} catch (AssertException e) {
			dateText.setBackground(Colors.YELLOW);
			dateText.setForeground(Colors.BLACK);
			return false;
		}
	}

}
