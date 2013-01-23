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

import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.snutils.AssertException;

/**
 * The HUI version of a dropdown box for numeric values.
 * 
 * @author koderman[at]sernet[dot]de
 */
public class NumericSelectionControl implements IHuiControl {

    private static final Logger LOG = Logger.getLogger(NumericSelectionControl.class);
    
	private Entity entity;

	private PropertyType fieldType;

	private Composite composite;

	private Combo combo;

	private boolean editable = false;

	private Property savedProp;

	private int min;

	private int max;

	private String[] numericItems;

    private boolean showValidationHint;
    
    private boolean useValidationGUIHints;
    
    private Label label;

	public Control getControl() {
            return combo;
	}

	private static final Color GREY = new Color(Display.getDefault(), 240, 240,
			240);

	/**
	 * Constructor for DropDownBox.
	 * 
	 * @param dyndoc
	 * @param type
	 * @param composite
	 */
	public NumericSelectionControl(Entity dyndoc, PropertyType type,
			Composite parent, boolean edit, boolean showValidationHint, boolean useValidationGuiHints) {
		this.entity = dyndoc;
		this.fieldType = type;
		this.composite = parent;
		this.editable = edit;
		this.min = type.getMinValue();
		this.max = type.getMaxValue();
		this.showValidationHint = showValidationHint;
		this.useValidationGUIHints = useValidationGuiHints;

	}

	/**
	 * @throws AssertException
	 * 
	 */
	public void create() {
		try {
			label = new Label(composite, SWT.NULL);
			String labelText = fieldType.getName();
			if(showValidationHint && useValidationGUIHints){
			    refontLabel(true);
			}
			label.setText(labelText);

			List<Property> savedProps = entity.getProperties(fieldType.getId()).getProperties();
			savedProp = savedProps != null && !savedProps.isEmpty() ? (Property) savedProps.get(0) : null;

		    createCombo();
			
		} catch (Exception e1) {
			LOG.error("Error while creating",e1);
		}

	}

    /**
     * 
     */
    private void createCombo() {
        String[] shownItems;
        combo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        this.numericItems = createNumericItems();
        shownItems = createNumericItemsWithDisplayString();
        combo.setItems(shownItems);
        if (savedProp == null) {
        	// create property in which to save entered value:
        	savedProp = entity.createNewProperty(fieldType, "");
        	combo.deselectAll();
        } else {
        	// use saved property:
        	combo.select(indexForOption(savedProp));
        }

        GridData comboLData = new GridData();
        comboLData.horizontalAlignment = GridData.BEGINNING;
        comboLData.grabExcessHorizontalSpace = false;
        combo.setLayoutData(comboLData);
        combo.setEnabled(editable);
        if (!editable){
        	combo.setBackground(GREY);
        }
        combo.setToolTipText(fieldType.getTooltiptext());

        combo.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent evt) {
        		savedProp.setPropertyValue(numericItems[combo.getSelectionIndex()], true, combo);
        		validate();
        	}
        });
        combo.pack(true);
    }

	/**
	 * @return
	 */
	private String[] createNumericItems() {
		String[] items = new String[max-min+1];
		int j=0;
		for(int i = min; i <= max; i++) {
			items[j] = Integer.toString(i);
			j++;
		}
		return items;
	}
	
	/**
     * @return
     */
    private String[] createNumericItemsWithDisplayString() {
        String[] items = new String[max-min+1];
        int j=0;
        for(int i = min; i <= max; i++) {
            items[j] = fieldType.getNameForValue(i); 
            j++;
        }
        return items;
    }


    public void setFocus() {
		this.combo.setFocus();
	}

	public boolean validate() {
		//FIXME bg colour not working in 3.4M4:
	       boolean valid = true;
           String propValue = savedProp != null ? savedProp.getPropertyValue() : null;
	        for(Entry<String, Boolean> entry : fieldType.validate(propValue, null).entrySet()){
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

	public void update() {
		PropertyList propList = entity.getProperties(fieldType.getId());
		Property entityProp;
			entityProp = propList != null ? propList.getProperty(0) : null;
		if (entityProp != null) {
			savedProp = entityProp;

			if (Display.getCurrent() != null) {
				combo.select(indexForOption(savedProp));
				validate();
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						combo.select(indexForOption(savedProp));
						validate();
					}
				});
			}
		}
	}

	/**
	 * @param savedProp2
	 * @return
	 */
	private int indexForOption(Property savedProp) {
		int i = 0;
		for (String item : numericItems) {
			if ( item.equals(savedProp.getPropertyValue()) ) {
				return i;
			}
			i++;
		}
		return -1;
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
}
