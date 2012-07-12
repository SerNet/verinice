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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.snutils.AssertException;

/**
 * The HUI version of a dropdown box.
 * 
 * @author koderman[at]sernet[dot]de
 */
public class SingleSelectionControl implements IHuiControl {

	private Entity entity;

	private PropertyType fieldType;

	private Composite composite;

	private Combo combo;

	private boolean editable = false;

	private String[] labels;

	private ArrayList<IMLPropertyOption> options;

	private Property savedProp;

	private Color fgColor;

	private Color bgColor;

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
	public SingleSelectionControl(Entity dyndoc, PropertyType type,
			Composite parent, boolean edit) {
		this.entity = dyndoc;
		this.fieldType = type;
		this.composite = parent;
		this.editable = edit;
	}

	/**
	 * @throws AssertException
	 * 
	 */
	public void create() {
		try {
			Label label = new Label(composite, SWT.NULL);
			label.setText(fieldType.getName());

			List<Property> savedProps = entity.getProperties(fieldType.getId()).getProperties();
			savedProp = savedProps!=null && !savedProps.isEmpty() ? (Property) savedProps.get(0) : null;

			combo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
			fgColor = combo.getForeground();
			bgColor = combo.getBackground();
			options = fieldType.getOptions();
			labels = new String[options.size()];
			int i = 0;
			for (Iterator<IMLPropertyOption> iter = options.iterator(); iter.hasNext(); i++) {
				labels[i] = iter.next().getName();
			}
			combo.setItems(labels);
			if (savedProp == null) {
				// create property in which to save entered value:
				savedProp = entity.createNewProperty(fieldType, "");
				combo.deselectAll();
			} else {
				// use saved property:
				int index = indexForOption(savedProp.getPropertyValue());
				combo.select(index);
			}

			GridData comboLData = new GridData();
			comboLData.horizontalAlignment = GridData.BEGINNING;
			comboLData.grabExcessHorizontalSpace = false;
			combo.setLayoutData(comboLData);
			combo.setEnabled(editable);
			if (!editable)
				combo.setBackground(GREY);
			combo.setToolTipText(fieldType.getTooltiptext());

			combo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					PropertyOption selection = (PropertyOption) options
							.get(combo.getSelectionIndex());
					savedProp.setPropertyValue(selection.getId(), true, combo);
					validate();
				}
			});
			combo.pack(true);
		} catch (Exception e1) {
			Logger.getLogger(SingleSelectionControl.class).error(e1);
		}

	}

	private int indexForOption(String propertyValue) {
		int i = 0;
		for (Iterator iter = options.iterator(); iter.hasNext(); ++i) {
			PropertyOption opt = (PropertyOption) iter.next();
			if (opt.getId().equals(propertyValue))
				return i;
		}
		return -1;
	}

	public void setFocus() {
		this.combo.setFocus();
	}

	public boolean validate() {
		//FIXME bg colour not working in 3.4M4:
		if (fieldType.validate(combo.getText(), null)) {
//			combo.setForeground(fgColor);
//			combo.setBackground(bgColor);
			return true;
		}

//		combo.setForeground(Colors.BLACK);
//		combo.setBackground(Colors.YELLOW);
		return false;
	}

	public void update() {
		PropertyList propList = entity.getProperties(fieldType.getId());
		Property entityProp;
			entityProp = propList != null ? propList.getProperty(0) : null;
		if (entityProp != null) {
			savedProp = entityProp;
			final int index = indexForOption(savedProp.getPropertyValue());

			if (Display.getCurrent() != null) {
				combo.select(index);
				validate();
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						combo.select(index);
						validate();
					}
				});
			}

		}
	}

}
