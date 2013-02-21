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

	private static final Logger LOG = Logger.getLogger(AccessControlEditDialog.class);

	private static final String INFORMATION_ADD_MODE = Messages.AccessControlEditDialog_7;

	private static final String INFORMATION_OVERRIDE_MODE = Messages.AccessControlEditDialog_8;
	
	private List<CnATreeElement> elements = new ArrayList<CnATreeElement>();

	private TableViewer viewer;

	private Set<Permission> permissionSet;

	private Combo comboRole;

	private Button buttonRead;

	private Button buttonWrite;

	private Button buttonInherit;
	
	private Button[] radioButtonMode = new Button[2];
	
	private static final int MARGIN_WIDTH_DEFAULT = 10;
	
	private static final int MARGIN_HEIGHT_DEFAULT = 10;

	private static final int COLSPAN_DEFAULT = 5;
	
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
		newShell.setSize(650, 610);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle(Messages.AccessControlEditDialog_6);

		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layoutRoot = (GridLayout) composite.getLayout();
		layoutRoot.marginWidth = MARGIN_WIDTH_DEFAULT;
		layoutRoot.marginHeight = MARGIN_HEIGHT_DEFAULT;
		GridData gd = generateGridData(Integer.valueOf(GridData.GRAB_HORIZONTAL), Boolean.TRUE, Boolean.TRUE, Integer.valueOf(GridData.FILL), Integer.valueOf(GridData.FILL), null);
		composite.setLayoutData(gd);

		final Composite containerSettings = new Composite(composite, SWT.NONE);
		GridLayout layoutSettings = generateGridLayout(2, false, MARGIN_WIDTH_DEFAULT, MARGIN_HEIGHT_DEFAULT);
		containerSettings.setLayout(layoutSettings);
		GridData gridData = generateGridData(null, null, null, null, null, Integer.valueOf(COLSPAN_DEFAULT));
		containerSettings.setLayoutData(gridData);	

		SelectionListener radio0Listener = new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                showInformation();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };
		
		radioButtonMode[0] = generateButton(containerSettings, Integer.valueOf(SWT.RADIO), Messages.AccessControlEditDialog_9, Boolean.TRUE, radio0Listener);
		radioButtonMode[1] = generateButton(containerSettings, Integer.valueOf(SWT.RADIO), Messages.AccessControlEditDialog_10, null, null);

		showInformation();

		buttonInherit = generateButton(containerSettings, Integer.valueOf(SWT.CHECK), Messages.AccessControlEditDialog_11, Boolean.FALSE, null);
		gridData = generateGridData(null, Boolean.TRUE, null, Integer.valueOf(GridData.FILL), null, null);
		buttonInherit.setLayoutData(gridData);
		
		final Composite containerRoles = new Composite(composite, SWT.NONE);
		GridLayout layout = generateGridLayout(COLSPAN_DEFAULT, false, MARGIN_WIDTH_DEFAULT, MARGIN_HEIGHT_DEFAULT);
		containerRoles.setLayout(layout);
		containerRoles.setLayoutData(gd);
		
		Label labelRole = new Label(containerRoles, SWT.NONE);
		labelRole.setText(Messages.AccessControlEditDialog_12);
		comboRole = new Combo(containerRoles, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboRole.setItems(getRoles());
		buttonRead = generateButton(containerRoles, Integer.valueOf(SWT.CHECK), Messages.AccessControlEditDialog_13, Boolean.FALSE, null);
		buttonWrite = generateButton(containerRoles, Integer.valueOf(SWT.CHECK), Messages.AccessControlEditDialog_14, Boolean.FALSE, null);
		SelectionListener addListener = new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                addPermission();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };
        Button buttonAdd = generateButton(containerRoles, Integer.valueOf(SWT.PUSH), Messages.AccessControlEditDialog_15, null, addListener);

		gridData = generateGridData(null, Boolean.TRUE, null, Integer.valueOf(SWT.RIGHT), null, null);
		buttonAdd.setLayoutData(gridData);

		createViewer(containerRoles);
		
		SelectionListener removeListener = new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                removePermission();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };
		
        
        Button buttonRemove = generateButton(containerRoles, Integer.valueOf(SWT.PUSH), Messages.AccessControlEditDialog_16, null, removeListener);
		gridData = generateGridData(null, null, null, SWT.RIGHT, null, Integer.valueOf(COLSPAN_DEFAULT));
		buttonRemove.setLayoutData(gridData);

		return containerRoles;
	}
	
	private GridLayout generateGridLayout(int columns, boolean makeColumnsEqualWidth, int marginWidth, int marginHeight){
	    GridLayout layout = new GridLayout(columns, makeColumnsEqualWidth);
	    layout.marginWidth = marginWidth;
	    layout.marginHeight = marginHeight;
	    return layout;
	}
	
	private GridData generateGridData(Integer style, Boolean grabExcessHorizontalSpace, Boolean grabExcessVerticalSpace, 
	        Integer horizontalAlignment, Integer verticalAlignment, Integer horizontalSpan){
	    GridData data = (style != null) ? new GridData(style.intValue()) : new GridData();
	    data.grabExcessHorizontalSpace = (grabExcessHorizontalSpace != null) ? grabExcessHorizontalSpace.booleanValue() : data.grabExcessHorizontalSpace;
	    data.grabExcessVerticalSpace = (grabExcessVerticalSpace != null) ? grabExcessVerticalSpace.booleanValue() : data.grabExcessVerticalSpace;
	    data.horizontalAlignment = (horizontalAlignment != null) ? horizontalAlignment.intValue() : data.horizontalAlignment;
	    data.verticalAlignment = (verticalAlignment != null) ? verticalAlignment.intValue() : data.verticalAlignment;
	    data.horizontalSpan = (horizontalSpan != null) ? horizontalSpan.intValue() : data.horizontalSpan;
	    return data;
	}
	
	private Button generateButton(Composite composite, Integer style, String text, Boolean selection, SelectionListener listener){
	    Button button = new Button(composite, style);
	    button.setText((text != null) ? text : button.getText());
	    button.setSelection((selection != null) ? selection.booleanValue() : button.getSelection());
	    if(listener != null){
	        button.addSelectionListener(listener);
	    }
	    return button;
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
			} catch (Exception t) {
				LOG.error("Error while setting table data", t); //$NON-NLS-1$
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
			} catch (Exception t) {
				LOG.error("Error while setting table data", t); //$NON-NLS-1$
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
	    final int gridDataHorizontalSpan = 5;
	    int style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
	    style = style | SWT.FULL_SELECTION | SWT.BORDER;
		viewer = new TableViewer(parent, style);

		createColumns();
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
		} catch (Exception t) {
			LOG.error("Error while setting table data", t); //$NON-NLS-1$
		}

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = gridDataHorizontalSpan;
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
		Set<Permission> pSet = Permission.clonePermissionSet(firstElement, lp.getPermissions());
		this.permissionSet = pSet;
		return pSet;
	}

	private void createColumns() {
		String[] titles = { Messages.AccessControlEditDialog_20, Messages.AccessControlEditDialog_21, Messages.AccessControlEditDialog_22 };
		int[] bounds = { 170, 50, 50 };

		// First column: title of the role
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Permission) element).getRole();
			}
		});

		// 2. column: read
		col = createTableViewerColumn(titles[1], bounds[1]);
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
		col = createTableViewerColumn(titles[2], bounds[2]);
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

	private TableViewerColumn createTableViewerColumn(String title, int bound) {
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
				LOG.error("Error while updating permissions", e); //$NON-NLS-1$
				throw new RuntimeException(e);
			}
		}
		super.okPressed();
	}

	private boolean isOverride() {
		return radioButtonMode[1].getSelection();
	}

}
