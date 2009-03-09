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
package sernet.gs.ui.rcp.main.bsi.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.StrukturanalyseReport;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.ReportGetItemsCommand;
import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.IEntityElement;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;


/**
 * Dialog that lets the user choose which properties from all
 * given HUI Entities should be included in a report output.
 * 
 * 
 * @author koderman@sernet.de
 *
 * @see Hitro-UI framework documentation for a description of
 * 		Entity and Property classes
 */
public class ChoosePropertiesPage extends WizardPage {
	private HashSet<EntityType> shownEntityTypes;
	
	
	private CheckboxTreeViewer viewer;
	
	protected ChoosePropertiesPage() {
		super("Spalten auswählen");
		setTitle("Spalten auswählen");
		setDescription("Wählen Sie die Felder der angezeigten Elemente aus, " +
				"die in dem Report enthalten sein sollen.");
	}
	
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		container.setLayout(gridLayout);
		setControl(container);

		viewer = new CheckboxTreeViewer(container, SWT.BORDER);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ChoosePropertiesContentProvider(viewer));
		viewer.setLabelProvider(new ChoosePropertiesLabelProvider());
		
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);

		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkChildren(event.getElement(), null, event.getChecked());
				updatePageComplete();
			}
		});
		
		setPageComplete(false);
	}
	
	private void updatePropertiesToExport() {
		// iterate over all entity's properties, keeping their order when
		// adding them to the report:
		getExportWizard().resetShownPropertyTypes();
		for (EntityType type : shownEntityTypes) {
			for (IEntityElement child : type.getElements()) {
				if (child instanceof PropertyType
						&& viewer.getChecked(child)) {
					getExportWizard().addShownProperty(type.getId(), child.getId());
				}
				
				if (child instanceof PropertyGroup) {
					for (PropertyType propType : 
						((PropertyGroup)child).getPropertyTypes()) {
						if (viewer.getChecked(propType)) {
							getExportWizard()
								.addShownProperty(type.getId(), propType.getId());
						}
					}
				}
			}
		}
	}

	protected void checkChildren(Object element, EntityType parent, boolean check) {
		
		if (element instanceof EntityType) {
			EntityType type = (EntityType) element;
			for (PropertyType child : type.getPropertyTypes()) {
				viewer.setChecked(child, check);
			}
			
			for (PropertyGroup child : type.getPropertyGroups()) {
				viewer.setChecked(child, check);
				checkChildren(child, type, check);
			}
		}
		
		if (element instanceof PropertyGroup) {
			PropertyGroup group = (PropertyGroup) element;
			for (PropertyType child : group.getPropertyTypes()) {
				viewer.setChecked(child, check);
			}				
		}
		
		updatePropertiesToExport();
	}

	public ExportWizard getExportWizard() {
		return (ExportWizard)getWizard();
	}
	
	private void initContents() {
		shownEntityTypes = new HashSet<EntityType>();
		getExportWizard().resetShownPropertyTypes();
		
		// iterate over shown items and add each found type of item to the list:
		IBSIReport report = getExportWizard().getReport();
		ReportGetItemsCommand command = new ReportGetItemsCommand(report);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			ArrayList<CnATreeElement> items = command.getItems(); 
			
			
			for (CnATreeElement item : items) {
				if (item.getEntity() == null)
					continue;
				
				EntityType entityType = HUITypeFactory
				.getInstance()
				.getEntityType(item.getEntity()
						.getEntityType());
				
				if (! shownEntityTypes.contains(entityType))
					shownEntityTypes.add(entityType);
			}
			viewer.setInput(shownEntityTypes);
			viewer.refresh();
			checkDefaults();
		} catch (CommandException e) {
			ExceptionUtil.log(e, "Fehler beim Vorbereiten des Reports.");
		}
	}
	
	private void checkDefaults() {
		for (Object element : shownEntityTypes) {

			if (element instanceof EntityType) {
				EntityType type = (EntityType) element;
				for (PropertyType child : type.getPropertyTypes()) {
					if (getExportWizard().getReport().isDefaultColumn(child.getId()))
						viewer.setChecked(child, true);
					else 
						viewer.setChecked(child, false);
				}
				
				for (PropertyGroup group : type.getPropertyGroups()) {
					checkGroupDefaults(group);
				}
			}
			
			if (element instanceof PropertyGroup) {
				PropertyGroup group = (PropertyGroup) element;
				checkGroupDefaults(group);
			}
			updatePropertiesToExport();
		}
	}
	
	private void checkGroupDefaults(PropertyGroup group) {
		for (PropertyType child : group.getPropertyTypes()) {
			if (getExportWizard().getReport().isDefaultColumn(child.getId()))
				viewer.setChecked(child, true);
			else 
				viewer.setChecked(child, false);
		}				
	
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			initContents();
			updatePageComplete();
		}
		else {
			setPageComplete(false);
		}
	}

	private void updatePageComplete() {
		boolean checked = (viewer.getCheckedElements().length > 0);
		setPageComplete(checked);		
	}
}
