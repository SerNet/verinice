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

import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.snutils.AssertException;

/**
 * The HUI text control.
 * 
 * @author koderman[at]sernet[dot]de
 */
public class TextControl implements IHuiControl {

	private Entity entity;
	private PropertyType fieldType;
	private Composite composite;
	private boolean editable = false;
	private Property savedProp;
	private Text text;
	private int lines;
	private Color bgColor;
	private Color fgColor;
	private boolean useRule;
	private boolean showValidationHint;
	private boolean useValidationGUIHints;
	
	private Label label;
	
	// This limit is set in Property.hbm.xml / PropertyList.hbm.xml:
    private static final int HIBERNATE_MAPPED_STRING_LIMIT = 400000;

	public Control getControl() {
		return text;
	}

	public TextControl(Entity ent, PropertyType type, Composite parent, boolean edit, int lines, boolean rules, boolean showValidationHint, boolean useValidationGuiHints) {
		this.entity = ent;
		this.fieldType = type;
		this.composite = parent;
		this.editable = edit;
		this.lines = lines;
		this.useRule = rules;
		this.showValidationHint = showValidationHint;
		this.useValidationGUIHints = useValidationGuiHints;
	}

	/**
	 * @throws AssertException
	 * 
	 */
	public void create() {
		label = new Label(composite, SWT.NULL);
		String labelText = fieldType.getName();
		if(showValidationHint && useValidationGUIHints){ 
		    refontLabel(true);
		}
		label.setText(labelText);
		
		PropertyList propList = entity.getProperties(fieldType.getId());
		savedProp = propList != null ? propList.getProperty(0) : null;

		if (savedProp == null) {
			// create property in which to save entered value:
			String defaultValue = "" ; //$NON-NLS-1$
			if (useRule && fieldType.getDefaultRule() != null){
				defaultValue = fieldType.getDefaultRule().getValue();
			}
			savedProp = entity.createNewProperty(fieldType, defaultValue);
			text = createText();
		} else {
			text = createText();
			// use saved property:
			text.setText(notNull(savedProp.getPropertyValue()));
		}

		bgColor = text.getBackground();
		fgColor = text.getForeground();

		// change value when user enters text:
		text.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				Text field = (Text) e.widget;
				savedProp.setPropertyValue(field.getText(), true, text);
				validate();
			}
		});

		composite.layout();
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

	public boolean validate() {
        boolean valid = true;
        for(Entry<String, Boolean> entry : fieldType.validate(text.getText(), null).entrySet()){
            if(!entry.getValue().booleanValue()){
                valid = false;
                break;
            }
        }
		if (valid) {
			text.setForeground(fgColor);
			text.setBackground(bgColor);
			refontLabel(false);
			return true;
		}

		if(useValidationGUIHints){
		    text.setForeground(Colors.BLACK);
		    text.setBackground(Colors.YELLOW);
		    refontLabel(true);
		}
		return false;
	}

	private Text createText() {
		if (lines > 1) {
			Text text_ = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);

			GridData textLData = new GridData();
			textLData.verticalAlignment = GridData.FILL;
			textLData.horizontalAlignment = GridData.FILL;
			textLData.heightHint = lines * 20;
			textLData.grabExcessHorizontalSpace = true;
			text_.setLayoutData(textLData);
			text_.setEditable(editable);
			if (!editable){
				text_.setBackground(Colors.GREY);
			}
			text_.setToolTipText(fieldType.getTooltiptext());
			if(this.savedProp!=null && this.savedProp.getPropertyValue()!=null) {
			    text_.setText(this.savedProp.getPropertyValue());
			}
			text_.setTextLimit(HIBERNATE_MAPPED_STRING_LIMIT);
			return text_;
		}
		// single line field:
		Text text = new Text(composite, SWT.BORDER);
		GridData textLData = new GridData();
		textLData.horizontalAlignment = GridData.FILL;
		textLData.grabExcessHorizontalSpace = true;
		text.setLayoutData(textLData);
		text.setEditable(editable);
		if (!editable){
			text.setBackground(Colors.GREY);
		}
		text.setToolTipText(fieldType.getTooltiptext());
		text.setText(notNull(this.savedProp.getPropertyValue()));
		text.setTextLimit(HIBERNATE_MAPPED_STRING_LIMIT);
		return text;
	}

	private String notNull(String propertyValue) {
		return (propertyValue != null) ? propertyValue : "";
	}

	public void setFocus() {
		this.text.setFocus();
		// outcommented due to bug 497
		// does not matter how long input string is
		// cursor will always stay at idx=0 now
//		this.text.selectAll();
	}

	public void update() {
		PropertyList propList = entity.getProperties(fieldType.getId());
		Property entityProp;
		entityProp = propList != null ? propList.getProperty(0) : null;
		if (entityProp != null && !text.getText().equals(entityProp.getPropertyValue())) {
			savedProp = entityProp;
			if (Display.getCurrent() != null) {
				text.setText(savedProp.getPropertyValue());
				validate();
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						text.setText(savedProp.getPropertyValue());
						validate();
					}
				});
			}
		}
	}
	
	
}
