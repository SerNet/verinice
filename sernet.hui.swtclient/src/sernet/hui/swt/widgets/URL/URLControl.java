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
package sernet.hui.swt.widgets.URL;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.swt.widgets.IHuiControl;

public class URLControl implements IHuiControl {

	private Entity entity;
	private PropertyType type;
	private Composite parent;
	private boolean editable;
	private Link link;
	private Property savedProp;
	private boolean showValidationHint;
	private boolean useValidationGUIHints;
	private Label label;
	
	private Pattern pattern = Pattern.compile("<a href=\"(.*)\">(.*)</a>"); //$NON-NLS-1$

	public URLControl(Entity entity, PropertyType type, Composite parent,
			boolean editable, boolean showValidationHint, boolean useValidationGuiHints) {
		this.entity = entity;
		this.type = type;
		this.parent = parent;
		this.editable = editable;
		this.showValidationHint = showValidationHint;
        this.useValidationGUIHints = useValidationGuiHints;
	}

	public void create() {
		label = new Label(parent, SWT.NULL);
		if(showValidationHint && useValidationGUIHints){
		   refontLabel(true);
		}
		label.setText(type.getName());
		

		Composite container = new Composite(parent, SWT.NULL);
		GridLayout contLayout = new GridLayout(3, false);
		contLayout.horizontalSpacing = 5;
		contLayout.marginLeft = 0;
		contLayout.marginWidth = 0;
		contLayout.marginHeight = 0;
		container.setLayout(contLayout);

		GridData containerLData = new GridData();
		containerLData.horizontalAlignment = GridData.FILL;
		containerLData.grabExcessHorizontalSpace = true;
		container.setLayoutData(containerLData);

		link = new Link(container, SWT.NONE);
		link.setToolTipText(this.type.getTooltiptext());
		link.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,		
				true, false, 1, 1));
		link.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				if (getHref() != null && getHref().length()>0){
					Program.launch(getHref());
				}
			}

		});

		Button editBtn = new Button(container, SWT.PUSH);
		editBtn.setText(Messages.getString("URLControl.1")); //$NON-NLS-1$
		editBtn.setToolTipText(this.type.getTooltiptext());
		editBtn.setEnabled(editable);
		editBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				showLinkEditDialog();
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				showLinkEditDialog();
			}
		});

		setLinkText();
	}

	protected String getHref() {
		Matcher matcher = pattern.matcher(savedProp.getPropertyValue());
		if (matcher.find()) {
			return matcher.group(1);

		}
		return ""; //$NON-NLS-1$
	}

	private void setLinkText() {
		PropertyList propList = entity.getProperties(type.getId());
		savedProp = propList != null ? propList.getProperty(0) : null;

		if (savedProp == null) {
			savedProp = entity.createNewProperty(type, ""); //$NON-NLS-1$
		}
		if(savedProp.getPropertyValue()!=null) {
			link.setText(savedProp.getPropertyValue());
		}
		link.pack();
	}

	protected void showLinkEditDialog() {
		String href = ""; //$NON-NLS-1$
		String name = ""; //$NON-NLS-1$
		if(savedProp!=null && savedProp.getPropertyValue()!=null) {
			Matcher matcher = pattern.matcher(savedProp.getPropertyValue());
			if (matcher.find()) {
				href = matcher.group(1);
				name = matcher.group(2);
	
			}
		}
		URLControlDialog dialog = new URLControlDialog(Display.getCurrent()
				.getActiveShell(), name, href, this.type);
		if (dialog.open() == InputDialog.OK) {
			savedProp.setPropertyValue("<a href=\"" + dialog.getHref() + "\">"  //$NON-NLS-1$ //$NON-NLS-2$
					+ dialog.getName()
					+ "</a>"); //$NON-NLS-1$
			update();
		}
	}

	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void update() {
		PropertyList propList = entity.getProperties(type.getId());
		Property entityProp;
		entityProp = propList != null ? propList.getProperty(0) : null;
		if (entityProp != null
				&& !link.getText().equals(entityProp.getPropertyValue())) {
			savedProp = entityProp;
			if (Display.getCurrent() != null) {
				setLinkText();
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setLinkText();
					}
				});
			}
		}
	}

	public boolean validate() {
        boolean valid = true;
        for(Entry<String, Boolean> entry : type.validate(
                link.getText(), null).entrySet()){
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
	
    private void refontLabel(boolean dye) {
        FontData fontData = label.getFont().getFontData()[0];
        Font font;
        int color;
        if(dye){
            font= new Font(label.getParent().getDisplay(), new FontData(fontData.getName(), fontData.getHeight(),
                    SWT.BOLD));
            color = SWT.COLOR_RED;
        } else {
            font = new Font(label.getParent().getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.NONE));
            color = SWT.COLOR_WIDGET_FOREGROUND;
        }
        label.setForeground(label.getParent().getDisplay().getSystemColor(color));
        label.setFont(font);
    }

}
