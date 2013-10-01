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
package sernet.hui.swt.widgets.multiselectionlist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;


public class MultiSelectionDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
	private Entity entity;
	private PropertyType propertyType;
	private boolean referencesEntities;

	public MultiSelectionDialog(Shell parent, int style, Entity ent, PropertyType type, 
	        boolean referencesEntities) {
		super(parent, style);
		this.entity = ent;
		this.propertyType = type;
		this.referencesEntities = referencesEntities;
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			dialogShell.setLayout(new GridLayout(1, false));
			dialogShell.setSize(400, 300);
			dialogShell.setText("Optionen für Feld: " + propertyType.getName());
			
			MultiSelectionList mList = new MultiSelectionList(entity, propertyType, dialogShell, referencesEntities);
			mList.create();
			GridData scrolledComposite1LData = new GridData();
			scrolledComposite1LData.grabExcessVerticalSpace = true;
			scrolledComposite1LData.horizontalAlignment = GridData.FILL;
			scrolledComposite1LData.verticalAlignment = GridData.FILL;
			scrolledComposite1LData.grabExcessHorizontalSpace = true;
			mList.setLayoutData(scrolledComposite1LData);
			
			
			// set selected options:
			List options = new ArrayList();
			
			if (referencesEntities) {
				List properties = entity.getProperties(propertyType.getId()).getProperties();
				List<IMLPropertyOption> referencedEntities = propertyType.getReferencedEntities();
				if (properties != null) {
					for (Object object : properties) {
						Property prop = (Property) object;
						Object option = findOptionForId(referencedEntities, prop.getPropertyValue());
						if (option != null){
							options.add(option);
						}
					}
					mList.setSelection(options, true);
				}				
			}
			else {
				// select options saved in properties:
				List properties = entity.getProperties(propertyType.getId()).getProperties();
				if (properties != null) {
					for (Iterator iter = properties.iterator(); iter.hasNext();) {
						Property prop = (Property) iter.next();
						String optionId = prop.getPropertyValue();
						options.add(propertyType.getOption(optionId));
					}
					mList.setSelection(options, true);
				}
			}

			
			Composite buttons = new Composite(dialogShell, SWT.NULL);
			GridLayout contLayout = new GridLayout(2, false);
			contLayout.horizontalSpacing = 5;
			buttons.setLayout(contLayout);
			
			GridData containerLData = new GridData();
			containerLData.horizontalAlignment = GridData.END;
			buttons.setLayoutData(containerLData);
			
			if (mList.isContextMenuPresent()) {
				Label label = new Label(buttons, SWT.NONE);
				label.setText("Rechtsklick für Kontextmenü");
			}
			
			Button okayBtn = new Button(buttons, SWT.PUSH);
			okayBtn.setText("Fertig");
			okayBtn.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent arg0) {
					close();
				}
				
				public void widgetDefaultSelected(SelectionEvent arg0) {
					close();
				}
			});
			
			dialogShell.layout();
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch()){
					display.sleep();
				}
			}
		} catch (Exception e) {
			Logger.getLogger(MultiSelectionDialog.class).error("Exception while opening dialog", e);
		}
	}
	
	private Object findOptionForId(List<IMLPropertyOption> referencedEntities,
			String propertyValue) {
		for (IMLPropertyOption option : referencedEntities) {
			if (option.getId().equals(propertyValue)){
				return option;
			}
		}
		return null;
	}

	void close() {
		dialogShell.dispose();
	}

}
