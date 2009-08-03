/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
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
package sernet.hui.swt.widgets.multiselectionlist;


import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.Colors;
import sernet.hui.swt.widgets.IHuiControl;
import sernet.snutils.AssertException;

/**
 * @author prack
 */
public class MultiSelectionControl implements IHuiControl {
	
	
	private Entity entity;
	private PropertyType type;
	private Composite parent;
	Text text;
	private boolean editable = false;
	private Color bgColor;
	private Color fgColor;
	private boolean referencesEntities;
	
	
	public Control getControl() {
		return text;
	}
	
	/**
	 * @param entity
	 * @param type
	 * @param reference 
	 * @param composite
	 */
	public MultiSelectionControl(Entity entity, PropertyType type, Composite parent, boolean edit, boolean reference) {
		this.entity = entity;
		this.type = type;
		this.parent = parent;
		this.editable = edit;
		this.referencesEntities = reference;
	}
	
	/**
	 * 
	 */
	public void create() {
		Label label = new Label(parent, SWT.NULL);
		label.setText(type.getName());
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout contLayout = new GridLayout(2, false);
		contLayout.horizontalSpacing = 5;
		contLayout.marginLeft=0;
		contLayout.marginWidth=0;
		contLayout.marginHeight=0;
		container.setLayout(contLayout);
		
		GridData containerLData = new GridData();
		containerLData.horizontalAlignment = GridData.FILL;
		containerLData.grabExcessHorizontalSpace = true;
		container.setLayoutData(containerLData);
		
		
		text = new Text(container, SWT.BORDER);
		text.setEditable(false);
		text.setToolTipText(this.type.getTooltiptext());
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bgColor = text.getBackground();
		fgColor = text.getForeground();
		
		Button editBtn = new Button(container, SWT.PUSH);
		editBtn.setText("�ndern...");
		editBtn.setToolTipText(this.type.getTooltiptext());
		editBtn.setEnabled(editable);
		editBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				showSelectionDialog();
			}
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
				showSelectionDialog();
			}
		});
		
		writeToTextField();
	}

	public void writeToTextField() {
		if (referencesEntities)
			writeEntitiesToTextField();
		else
			writeOptionsToTextField();
	}

	private void writeEntitiesToTextField() {
		StringBuffer names = new StringBuffer();
		List properties = entity.getProperties(type.getId()).getProperties();
		if (properties == null)
			return;
		
		for (Iterator iter = properties.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			String referencedId = prop.getPropertyValue();
			IMLPropertyOption ref = getEntity(referencedId);
			if (ref==null)
				continue;
			String optionName = ref.getName();
			if (names.length()==0)
				names.append(optionName);
			else
				names.append(" / " + optionName);
				
		}
		text.setText(names.toString());
	}

	private IMLPropertyOption getEntity(String referencedId) {
		for (IMLPropertyOption entity : type.getReferencedEntities()) {
			if (entity.getId().equals(referencedId))
				return entity;
		}
		return null;
	}

	/**
	 * Writes the names of all selected options in the text field.
	 * 
	 * @param propertyValue
	 * @param b
	 * @throws AssertException 
	 */
	private void writeOptionsToTextField() {
		StringBuffer names = new StringBuffer();
		List properties = entity.getProperties(type.getId()).getProperties();
		if (properties == null)
			return;
		
		for (Iterator iter = properties.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			String optionId = prop.getPropertyValue();
			IMLPropertyOption opt = type.getOption(optionId);
			if (opt==null)
				continue;
			String optionName = opt.getName();
			if (names.length()==0)
				names.append(optionName);
			else
				names.append(" / " + optionName);
				
		}
		text.setText(names.toString());
		
	}
	
	void showSelectionDialog() {
		Display display = Display.getDefault();
        Shell shell = new Shell(display);
		MultiSelectionDialog dialog = new MultiSelectionDialog(shell, SWT.NULL, 
				this.entity, this.type, this.referencesEntities);
		dialog.open();
	}

	public void select(IMLPropertyType type, IMLPropertyOption option) throws AssertException {
		writeOptionsToTextField();
	}

	public void unselect(IMLPropertyType type, IMLPropertyOption option) throws AssertException {
		writeOptionsToTextField();
	}

	public void setFocus() {
		this.text.setFocus();
	}
	
	public boolean validate() {
		if (type.validate(text.getText(), null)) {
			text.setForeground(fgColor);
			text.setBackground(bgColor);
			return true;
		}
		
		text.setForeground(Colors.BLACK);
		text.setBackground(Colors.YELLOW);
		return false;
	
	}
	
	public void update() {
		// TODO Auto-generated method stub
	}

}
