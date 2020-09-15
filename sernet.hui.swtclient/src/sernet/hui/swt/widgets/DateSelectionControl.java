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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;

/**
 * Custom widget to enter a date, either directly or using a date picker (SWT
 * calendar).
 * 
 * @author koderman[at]sernet[dot]de
 */
public class DateSelectionControl extends AbstractHuiControl {

    private Entity entity;

    private PropertyType fieldType;

    private boolean editable = false;

    private Property savedProp;

    private DateTime dateTime;

    private boolean useRule;

    private boolean showValidationHint;

    private boolean useValidationGUIHints;

    public Control getControl() {
        return dateTime;
    }

    public DateSelectionControl(Entity dyndoc, PropertyType type, Composite parent, boolean edit,
            boolean rules, boolean showValidationHint, boolean useValidationGuiHints) {
        super(parent);
        this.entity = dyndoc;
        this.fieldType = type;
        this.editable = edit;
        this.useRule = rules;
        this.showValidationHint = showValidationHint;
        this.useValidationGUIHints = useValidationGuiHints;

    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);
    }

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

        dateTime = new DateTime(container, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);

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
        if (showValidationHint && useValidationGUIHints) {
            refontLabel(true);
        }
        label.setText(labelText);

        GridData startWvTextLData = new GridData();
        startWvTextLData.horizontalAlignment = GridData.FILL;
        startWvTextLData.horizontalSpan = 1;
        startWvTextLData.grabExcessHorizontalSpace = true;
        dateTime.setLayoutData(startWvTextLData);
        dateTime.setEnabled(editable);
        if (!editable) {
            dateTime.setBackground(Colors.GREY);
        }
        dateTime.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Calendar calendar = getCalendar();
                if (!isSameDay(savedProp.getPropertyValue(), calendar.getTimeInMillis())) {
                    savedProp.setPropertyValue(calendar, true, dateTime);
                }
            }
        });

        dateTime.addListener(SWT.MouseVerticalWheel, event -> event.doit = false);

        List<Property> savedProps = entity.getProperties(fieldType.getId()).getProperties();
        savedProp = savedProps != null && !savedProps.isEmpty() ? savedProps.get(0) : null;

        String millis = "";
        if (savedProp != null) {
            millis = savedProp.getPropertyValue();
        } else {
            if (useRule) {
                millis = fieldType.getDefaultRule().getValue();
            }
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
                    && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));

        } catch (Exception e) {
            return false;
        }
    }

    private void setDisplayedTime(String millis) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTimeInMillis(Long.parseLong(millis));
        } catch (Exception e) {
            // do nothing, use today's date
        }
        dateTime.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    protected Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
        return calendar;
    }

    public void setFocus() {
    }

    public void update() {
        validate();
    }

    public boolean validate() {
        boolean valid = true;
        for (Entry<String, Boolean> entry : fieldType.validate(String.valueOf(
                dateTime.getDay() + "." + (dateTime.getMonth() + 1) + "." + dateTime.getYear()),
                null).entrySet()) {
            if (!entry.getValue().booleanValue()) {
                valid = false;
                break;
            }
        }
        if (valid) {
            refontLabel(false);
            return true;
        }

        if (useValidationGUIHints) {
            refontLabel(true);
        }
        return false;
    }
}
