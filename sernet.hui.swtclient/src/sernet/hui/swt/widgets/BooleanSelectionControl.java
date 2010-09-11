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
import org.eclipse.swt.widgets.Button;
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
 * The HUI version of a chdeckbox.
 * 
 * @author koderman[at]sernet[dot]de
 */
public class BooleanSelectionControl implements IHuiControl {

    private static final Logger LOG = Logger.getLogger(BooleanSelectionControl.class);
    
	private Entity entity;

	private PropertyType fieldType;

	private Composite composite;

	private boolean editable = false;

	private Property savedProp;


    private Button button;

	public Control getControl() {
	        return button;
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
	public BooleanSelectionControl(Entity dyndoc, PropertyType type,
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
			savedProp = savedProps != null && !savedProps.isEmpty() ? (Property) savedProps.get(0) : null;

			    createCheckbox();
			
		} catch (Exception e1) {
			LOG.error("Error while creating",e1);
		}

	}


    /**
     * 
     */
    private void createCheckbox() {
        button = new Button(composite, SWT.CHECK);
        button.setEnabled(editable);
        
        if (savedProp == null) {
            // create property in which to save entered value:
            savedProp = entity.createNewProperty(fieldType, "");
        } else {
            // use saved property:
            button.setSelection(savedProp.getNumericPropertyValue()==1);
        }
        
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                savedProp.setPropertyValue(button.getSelection() ? "1" : "0");
            }

        });
    }

	
    public void setFocus() {
		this.button.setFocus();
	}

	public void update() {
		PropertyList propList = entity.getProperties(fieldType.getId());
		Property entityProp;
			entityProp = propList != null ? propList.getProperty(0) : null;
		if (entityProp != null) {
			savedProp = entityProp;

			if (Display.getCurrent() != null) {
				button.setSelection(savedProp.getNumericPropertyValue() == 1);
				validate();
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
					    button.setSelection(savedProp.getNumericPropertyValue() == 1);
						validate();
					}
				});
			}
		}
	}

    /* (non-Javadoc)
     * @see sernet.hui.swt.widgets.IHuiControl#validate()
     */
    @Override
    public boolean validate() {
        return true;
    }

	
}
