/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 *     Daniel Murygin <dm[at]sernet[dot]de> - New Layout with table, add mode
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadPermissions;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdatePermissions;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllRoles;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;

/**
 * Simple dialog that allows defining the access options for an element.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class AccessControlEditDialog extends TitleAreaDialog {

	private static final Logger log = Logger.getLogger(AccessControlEditDialog.class);

	private static final String INFORMATION_ADD_MODE = Messages.AccessControlEditDialog_7;

	private static final String INFORMATION_OVERRIDE_MODE = Messages.AccessControlEditDialog_8;
	
	private List<CnATreeElement> elements = new ArrayList<CnATreeElement>();

	private TableViewer viewer;

	private Set<Permission> permissionSet;

	private Combo comboRole;

	private Button buttonRead;

	private Button buttonWrite;

	private Button buttonInherit;
	
	Button[] radioButtonMode = new Button[2];
	
	Button buttonRemove;

	@SuppressWarnings("unchecked")
	public AccessControlEditDialog(Shell parent, IStructuredSelection selection) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);

		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			if (next instanceof CnATreeElement) {
				CnATreeElement nextElement = (CnATreeElement) next;
				elements.add(nextElement);
			}
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.AccessControlEditDialog_6);
		newShell.setSize(560, 600);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle(Messages.AccessControlEditDialog_6);

		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layoutRoot = (GridLayout) composite.getLayout();
		layoutRoot.marginWidth = 10;
		layoutRoot.marginHeight = 10;
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		composite.setLayoutData(gd);

		final Composite containerSettings = new Composite(composite, SWT.NONE);
		GridLayout layoutSettings = new GridLayout(2, false);
		layoutSettings.marginWidth = 10;
		layoutSettings.marginHeight = 10;
		containerSettings.setLayout(layoutSettings);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 5;
		containerSettings.setLayoutData(gridData);	

		radioButtonMode[0] = new Button(containerSettings, SWT.RADIO);
		radioButtonMode[0].setSelection(true);
		radioButtonMode[0].setText(Messages.AccessControlEditDialog_9);
		radioButtonMode[0].addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showInformation();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		radioButtonMode[1] = new Button(containerSettings, SWT.RADIO);
		radioButtonMode[1].setText(Messages.AccessControlEditDialog_10);

		showInformation();

		buttonInherit = new Button(containerSettings, SWT.CHECK | SWT.BORDER);
		buttonInherit.setText(Messages.AccessControlEditDialog_11);
		buttonInherit.setSelection(false);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		buttonInherit.setLayoutData(gridData);
		
		final Composite containerRoles = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(5, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		containerRoles.setLayout(layout);
		containerRoles.setLayoutData(gd);
		
		Label labelRole = new Label(containerRoles, SWT.NONE);
		labelRole.setText(Messages.AccessControlEditDialog_12);
		comboRole = new Combo(containerRoles, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboRole.setItems(getRoles());
		buttonRead = new Button(containerRoles, SWT.CHECK | SWT.BORDER);
		buttonRead.setText(Messages.AccessControlEditDialog_13);
		buttonRead.setSelection(false);
		buttonWrite = new Button(containerRoles, SWT.CHECK | SWT.BORDER);
		buttonWrite.setText(Messages.AccessControlEditDialog_14);
		buttonWrite.setSelection(false);
		Button buttonAdd = new Button(containerRoles, SWT.PUSH | SWT.BORDER);
		buttonAdd.setText(Messages.AccessControlEditDialog_15);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalAlignment = SWT.RIGHT;
		buttonAdd.setLayoutData(gridData);
		buttonAdd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPermission();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		createViewer(containerRoles);
		
		buttonRemove = new Button(containerRoles, SWT.PUSH | SWT.BORDER);
		buttonRemove.setText(Messages.AccessControlEditDialog_16);
		buttonRemove.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removePermission();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = SWT.RIGHT;
		buttonRemove.setLayoutData(gridData);

		return containerRoles;
	}

	protected void showInformation() {
		if(isOverride()) {
			setMessage(INFORMATION_OVERRIDE_MODE);
		} else {
			setMessage(INFORMATION_ADD_MODE);
		}
		
	}

	protected void addPermission() {
		CnATreeElement element = null;
		if(elements!=null && !elements.isEmpty()) {
			element=elements.get(0);
		}
		if(element!=null) {
			this.permissionSet.add(Permission.createPermission(element, comboRole.getText(), buttonRead.getSelection(), buttonWrite.getSelection()));
			try {
				viewer.setInput(this.permissionSet.toArray());
			} catch (Throwable t) {
				log.error("Error while setting table data", t); //$NON-NLS-1$
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void removePermission() {
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		if (selection != null && !selection.isEmpty()) {
			for (Iterator<Permission> iterator = selection.iterator(); iterator.hasNext();) {
				Permission permission = iterator.next();
				this.permissionSet.remove(permission);
			}
			try {
				viewer.setInput(this.permissionSet.toArray());
			} catch (Throwable t) {
				log.error("Error while setting table data", t); //$NON-NLS-1$
			}
		}
	}

	private String[] getRoles() {
		FindAllRoles findAllRoles = new FindAllRoles(true);
		try {
			findAllRoles = ServiceFactory.lookupCommandService().executeCommand(findAllRoles);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		List<String> roleList  = new ArrayList<String>(findAllRoles.getRoles());
		Collections.sort(roleList);
		return roleList.toArray(new String[roleList.size()]);
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());

		// TODO dm: for now, only the permissions of the first element
		// are displayed, changes will be written to all selected elements
		CnATreeElement firstElement = elements.get(0);

		this.permissionSet = loadPermission(firstElement);
		List<Permission> permissionList = new ArrayList<Permission>(this.permissionSet);
		Collections.sort(permissionList);
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		try {
			viewer.setInput(permissionList.toArray());
		} catch (Throwable t) {
			log.error("Error while setting table data", t); //$NON-NLS-1$
		}

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
	}

	private Set<Permission> loadPermission(CnATreeElement firstElement) {
		LoadPermissions lp = new LoadPermissions(firstElement);
		try {
			lp = ServiceFactory.lookupCommandService().executeCommand(lp);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		// clone the permissions because of hashcode trouble in set with instances created by hibernate
		return this.permissionSet = Permission.clonePermissionSet(firstElement, lp.getPermissions());
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { Messages.AccessControlEditDialog_20, Messages.AccessControlEditDialog_21, Messages.AccessControlEditDialog_22 };
		int[] bounds = { 170, 50, 50 };

		// First column: title of the role
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Permission) element).getRole();
			}
		});

		// 2. column: read
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (((Permission) element).isReadAllowed()) {
					return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_JA);
				} else {
					return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
				}
			}
		});

		// 3. column: write
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (((Permission) element).isWriteAllowed()) {
					return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_JA);
				} else {
					return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
				}
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;

	}

	@Override
	protected void okPressed() {
		if (this.buttonInherit.getSelection()) {
			boolean openConfirm = MessageDialog.openConfirm(getParentShell(), Messages.AccessControlEditDialog_0, Messages.AccessControlEditDialog_2);
			if (!openConfirm) {
				return;
			}
		}
		for (CnATreeElement element : elements) {
			UpdatePermissions up = new UpdatePermissions(element, permissionSet, buttonInherit.getSelection(), isOverride());
			try {
				ServiceFactory.lookupCommandService().executeCommand(up);
			} catch (CommandException e) {
				log.error("Error while updating permissions", e); //$NON-NLS-1$
				throw new RuntimeException(e);
			}
		}
		super.okPressed();
	}

	private boolean isOverride() {
		return radioButtonMode[1].getSelection();
	}

}
