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
package sernet.hui.swt.widgets;

import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.snutils.AssertException;

/**
 * Custom widget to enter a date, either directly or using a date picker (SWT
 * calendar).
 * 
 * @author koderman[at]sernet[dot]de
 */
public class DateSelectionControl implements IHuiControl {

	private Entity entity;

	private PropertyType fieldType;

	private Composite composite;

	private boolean editable = false;

	private Property savedProp;

	private Label label;

	private DateTime dateTime;

	private boolean useRule;
	
	private boolean showValidationHint;

	private boolean useValidationGUIHints;

	
	public Control getControl() {
		return dateTime;
	}

	/**
	 * @param dyndoc
	 * @param type
	 * @param composite
	 */
	public DateSelectionControl(Entity dyndoc, PropertyType type,
			Composite parent, boolean edit, boolean rules, boolean showValidationHint, boolean useValidationGuiHints) {
		this.entity = dyndoc;
		this.fieldType = type;
		this.composite = parent;
		this.editable = edit;
		this.useRule = rules;
		this.showValidationHint = showValidationHint;
		this.useValidationGUIHints = useValidationGuiHints;

	}
	
	public static boolean isWindows(){
        String os = System.getProperty("os.name").toLowerCase();
        //windows
        return (os.indexOf( "win" ) >= 0); 
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

		if (isWindows()) {
		    dateTime = new DateTime(container, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);
		} else {
		    dateTime = new DateTime(container, SWT.CALENDAR | SWT.MEDIUM | SWT.DROP_DOWN);
		}

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
		String labelText = fieldType.getName();
		if(showValidationHint && useValidationGUIHints){
		    refontLabel(true);
		}
		label.setText(labelText);

		GridData startWvTextLData = new GridData();
		startWvTextLData.horizontalAlignment = GridData.FILL;
		startWvTextLData.horizontalSpan = 1;
		startWvTextLData.grabExcessHorizontalSpace = true;
		dateTime.setLayoutData(startWvTextLData);
		dateTime.setEnabled(editable);
		if (!editable){
			dateTime.setBackground(Colors.GREY);
		}
		dateTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!isSameDay(savedProp.getPropertyValue(), getDateInMillis())){				
					savedProp.setPropertyValue(Long.toString(getDateInMillis()), true, dateTime);
				}
			}
		});

		List savedProps = entity.getProperties(fieldType.getId()).getProperties();
		savedProp = savedProps!=null &&  !savedProps.isEmpty() ? (Property) savedProps.get(0) : null;

		String millis = "";
		if (savedProp != null) {
			millis = savedProp.getPropertyValue();
		} else {
			if (useRule){
				millis = fieldType.getDefaultRule().getValue();
			}
			savedProp = entity.createNewProperty(fieldType, millis);
		}

		setDisplayedTime(millis);
		dateTime.setToolTipText(fieldType.getTooltiptext());
	}

    private void refontLabel(boolean dye) {
        FontData fontData = label.getFont().getFontData()[0];
        Font font;
        int color;
        if(dye){
            font= new Font(composite.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(),
                    SWT.BOLD));
            color = SWT.COLOR_RED;
        } else {
            font = new Font(composite.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.NONE));
            color = SWT.COLOR_WIDGET_FOREGROUND;
        }
        label.setForeground(composite.getDisplay().getSystemColor(color));
        label.setFont(font);
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
	      validate();
	}

	public boolean validate() {
        boolean valid = true;
        for(Entry<String, Boolean> entry : fieldType.validate(
                String.valueOf(dateTime.getDay() +
                "." + (dateTime.getMonth() + 1) +
                "." + dateTime.getYear()),
                null).entrySet()){
            if(!entry.getValue().booleanValue()){
                valid = false;
                break;
            }
        }
        if (valid) {
            refontLabel(false);
            return true;
        }

        if(useValidationGUIHints){
            refontLabel(true);
        }
        return false;
	}

}
