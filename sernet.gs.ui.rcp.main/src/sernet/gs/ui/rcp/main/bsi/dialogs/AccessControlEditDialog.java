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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.rcp.ImageColumnProvider;
import sernet.verinice.service.account.AccountLoader;
import sernet.verinice.service.commands.crud.LoadPermissions;

/**
 * Dialog that allows changing the access options for elements.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AccessControlEditDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(AccessControlEditDialog.class);

    private static final int MARGIN_WIDTH_DEFAULT = 10;
    private static final int MARGIN_HEIGHT_DEFAULT = 10;
    private static final int COLSPAN_DEFAULT = 5;

    private List<CnATreeElement> elements = new ArrayList<CnATreeElement>();
    private Set<Permission> permissionSet;
    private Set<Permission> permissionSetAdd;
    private Set<Permission> permissionSetRemove;
    private String[] roleArray;

    private boolean isOverride;
    private boolean isUpdateChildren;

    private TableViewer viewer;
    private Text filter;
    private Combo comboRole;
    private Button buttonRead;
    private Button buttonWrite;
    private Button buttonInherit;
    private Button buttonAdd;
    private Button[] radioButtonMode = new Button[2];

    public AccessControlEditDialog(Shell parent, IStructuredSelection selection) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);

        Iterator<Object> iterator = selection.iterator();
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

        // open the window right under the mouse pointer:
        final int cursorLocationXSubtrahend = 300;
        final int cursorLocationYSubtrahend = 300;
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x - cursorLocationXSubtrahend, cursorLocation.y - cursorLocationYSubtrahend));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle(Messages.AccessControlEditDialog_6);
        Composite composite = createComposite(parent);
        createSettingsComposite(composite);
        createRolesComposite(composite);
        return composite;
    }

    private Composite createComposite(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layoutRoot = (GridLayout) composite.getLayout();
        layoutRoot.marginWidth = MARGIN_WIDTH_DEFAULT;
        layoutRoot.marginHeight = MARGIN_HEIGHT_DEFAULT;
        GridData gd = generateGridData(GridData.GRAB_HORIZONTAL, Boolean.TRUE, Boolean.TRUE, GridData.FILL, GridData.FILL, null);
        composite.setLayoutData(gd);
        return composite;
    }

    private Composite createSettingsComposite(final Composite composite) {
        final Composite containerSettings = new Composite(composite, SWT.NONE);
        GridLayout layoutSettings = generateGridLayout(2, false, MARGIN_WIDTH_DEFAULT, MARGIN_HEIGHT_DEFAULT);
        containerSettings.setLayout(layoutSettings);
        GridData gridData = generateGridData(null, null, null, null, null, Integer.valueOf(COLSPAN_DEFAULT));
        containerSettings.setLayoutData(gridData);

        radioButtonMode[0] = generateButton(containerSettings, Integer.valueOf(SWT.RADIO), Messages.AccessControlEditDialog_9, Boolean.TRUE, radioListener);
        radioButtonMode[1] = generateButton(containerSettings, Integer.valueOf(SWT.RADIO), Messages.AccessControlEditDialog_10, null, radioListener);

        showInformation();

        buttonInherit = generateButton(containerSettings, Integer.valueOf(SWT.CHECK), Messages.AccessControlEditDialog_11, Boolean.FALSE, null);
        gridData = generateGridData(null, Boolean.TRUE, null, Integer.valueOf(GridData.FILL), null, null);
        buttonInherit.setLayoutData(gridData);
        return containerSettings;
    }

    private Composite createRolesComposite(final Composite composite) {
        GridData gridData;
        final Composite containerRoles = new Composite(composite, SWT.NONE);
        GridLayout layout = generateGridLayout(COLSPAN_DEFAULT, false, MARGIN_WIDTH_DEFAULT, MARGIN_HEIGHT_DEFAULT);
        containerRoles.setLayout(layout);
        GridData gd2 = generateGridData(GridData.GRAB_HORIZONTAL, Boolean.TRUE, Boolean.TRUE, GridData.FILL, GridData.FILL, null);
        containerRoles.setLayoutData(gd2);

        Label labelFilter = new Label(containerRoles, SWT.NONE);
        labelFilter.setText("Filter");

        Label labelRole = new Label(containerRoles, SWT.NONE);
        labelRole.setText(Messages.AccessControlEditDialog_12);
        labelRole.setLayoutData(generateGridData(SWT.NONE, true, false, GridData.FILL, GridData.CENTER, 4));

        filter = new Text(containerRoles, SWT.BORDER);
        filter.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // do nothing
            }

            @Override
            public void keyReleased(KeyEvent e) {
                filterRoleCombo();
                switchButtons(comboRole.getText() != null && !comboRole.getText().isEmpty());
            }
        });
        
        filter.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent e) {
                String filterText = filter.getText();
                if(filterText == null || filterText.isEmpty()){
                    StructuredSelection selection = ((StructuredSelection)viewer.getSelection());
                    String tableText = "";
                    if(selection != null && selection.size() > 0){
                        Object firstElement = selection.getFirstElement();
                        if(firstElement != null){
                            tableText = ((Permission)firstElement).getRole();
                        }
                    }
                    comboRole.setItems(getRoles());
                    if(!"".equals(tableText) && ArrayUtils.contains(getRoles(), tableText)){
                        syncCombo(tableText);
                    }
                } 
                String comboText = comboRole.getText();
                switchButtons(comboText != null && !comboText.isEmpty());
            }
            
            @Override
            public void focusGained(FocusEvent e) {
                // do nothing
            }
        });

        comboRole = new Combo(containerRoles, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboRole.setItems(getRoles());
        comboRole.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String text = comboRole.getText();
                syncTable(comboRole.getText());
                switchButtons(text != null && !(text.trim().isEmpty()));
            }
        });

        buttonRead = generateButton(containerRoles, Integer.valueOf(SWT.CHECK), Messages.AccessControlEditDialog_13, Boolean.FALSE, null);
        buttonWrite = generateButton(containerRoles, Integer.valueOf(SWT.CHECK), Messages.AccessControlEditDialog_14, Boolean.FALSE, null);
        buttonRead.setEnabled(false);
        buttonWrite.setEnabled(false);
        SelectionListener addListener = new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addPermission();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };
        buttonAdd = generateButton(containerRoles, Integer.valueOf(SWT.PUSH), Messages.AccessControlEditDialog_15, null, addListener);
        buttonAdd.setEnabled(false);
        gridData = generateGridData(null, Boolean.TRUE, null, Integer.valueOf(SWT.RIGHT), null, null);
        buttonAdd.setLayoutData(gridData);

        createViewer(containerRoles);

        Button buttonRemove = generateButton(containerRoles, Integer.valueOf(SWT.PUSH), Messages.AccessControlEditDialog_16, null, removeListener);
        gridData = generateGridData(null, null, null, SWT.RIGHT, null, Integer.valueOf(COLSPAN_DEFAULT));
        buttonRemove.setLayoutData(gridData);

        return containerRoles;
    }
    
    private void switchButtons(boolean enabled){
        buttonRead.setEnabled(enabled);
        buttonWrite.setEnabled(enabled);
        buttonAdd.setEnabled(enabled);
        
    }


    private TableViewer createViewer(Composite parent) {
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

        loadPermission(firstElement);
        refreshTable();

        // Layout the viewer
        GridData gridData = generateGridData(SWT.NONE, true, true, GridData.FILL, GridData.FILL, gridDataHorizontalSpan);
        viewer.getControl().setLayoutData(gridData);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                Set<Permission> selectedPermissions = getSelectedPermissions();
                if (!selectedPermissions.isEmpty()) {
                    syncComboAndCheckboxes(selectedPermissions.iterator().next());
                }
            }
        });
        return viewer;
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
        col.setLabelProvider(new ImageColumnProvider() {
            @Override
            public Image getImage(Object element) {
                return getYesNoImage(((Permission) element).isReadAllowed(), true);

            }
        });
        // 3. column: write
        col = createTableViewerColumn(titles[2], bounds[2]);
        col.setLabelProvider(new ImageColumnProvider() {
            @Override
            public Image getImage(Object element) {
                return getYesNoImage(((Permission) element).isWriteAllowed(), true);
            }
        });
    }

    private void filterRoleCombo() {
        String filterText = filter.getText();
        String selected = comboRole.getText();
        String[] allRoles = getRoles();
        List<String> roles = new ArrayList<String>(allRoles.length);
        for (String role : allRoles) {
            if (role.toLowerCase().contains(filterText.toLowerCase())) {
                roles.add(role);
            }
        }
        comboRole.setItems(roles.toArray(new String[roles.size()]));
        selectCombo(selected);
        if (comboRole.getItems().length == 1) {
            comboRole.select(0);
        }
        selected = comboRole.getText();
        if (selected != null) {
            syncTable(selected);
        }
    }

    protected void syncTable(String role) {
        int i = 0;
        Permission p = (Permission) viewer.getElementAt(i);
        while (p != null) {
            if (p.getRole().equals(role)) {
                viewer.setSelection(new StructuredSelection(p), true);
            }
            i++;
            p = (Permission) viewer.getElementAt(i);
        }

    }
    
    private void syncCombo(String tableRole){
        int i = 0;
        try{
            String cRole = comboRole.getItem(i);
            while(cRole != null){
                if(cRole.equals(tableRole)){
                    comboRole.select(i);
                    break;
                }
                i++;
                cRole = comboRole.getItem(i);
            }
        }catch (IllegalArgumentException e){
            // no Element found, dont change selection
        }
        String selection = comboRole.getText();
        switchButtons(selection != null && !selection.isEmpty());
    }
    

    protected void syncComboAndCheckboxes(Permission permission) {
        setPermissionInCombo(permission);
        setPermissionInCheckboxes(permission);
    }

    protected void setPermissionInCombo(Permission permission) {
        String role = permission.getRole();
        selectCombo(role);
    }

    private void selectCombo(String role) {
        String[] items = comboRole.getItems();
        boolean set = false;
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(role)) {
                comboRole.select(i);
                set = true;
            }
        }
        // if role is not element of combobox, select empty entry
        if(!set){
            List<String> list = new ArrayList(0);
            list.add("");
            list.addAll(removeEmptyEntries(Arrays.asList(comboRole.getItems())));
            comboRole.setItems(list.toArray(new String[list.size()]));
            comboRole.select(0);
        }
        
    }
    
    private List<String> removeEmptyEntries(List<String> list){
        List<String> filteredList = new ArrayList(0);
        for(String s : list){
            if(!s.isEmpty()){
                filteredList.add(s);
            }
        }
        return filteredList;
    }

    protected void setPermissionInCheckboxes(Permission permission) {
        switchButtons(comboRole.getText() != null && !comboRole.getText().isEmpty());
        buttonRead.setSelection(permission.isReadAllowed());
        buttonWrite.setSelection(permission.isWriteAllowed());

    }

    private void refreshTable() {
        List<Permission> permissionList = new ArrayList<Permission>(this.permissionSet);
        Collections.sort(permissionList);
        // Get the content for the viewer, setInput will call getElements in the
        // contentProvider
        try {
            viewer.setInput(permissionList.toArray());
        } catch (Exception t) {
            LOG.error("Error while setting table data", t); //$NON-NLS-1$
        }
    }

    protected void showInformation() {
        if (isOverride()) {
            setMessage(Messages.AccessControlEditDialog_8);
        } else {
            setMessage(Messages.AccessControlEditDialog_7);
        }
    }

    protected Set<Permission> getSelectedPermissions() {
        Set<Permission> selectedPermission = new HashSet<Permission>();
        StructuredSelection selection = (StructuredSelection) viewer.getSelection();
        if (selection != null && !selection.isEmpty()) {
            for (Iterator<Permission> iterator = selection.iterator(); iterator.hasNext();) {
                selectedPermission.add(iterator.next());
            }
        }
        return selectedPermission;
    }

    private String[] getRoles() {
        if (roleArray == null) {
            List<String> accountsAndGroups = new ArrayList<String>();

            boolean isLocalAdmin = getAuthService().currentUserHasRole(new String[] { ApplicationRoles.ROLE_LOCAL_ADMIN });
            if (isLocalAdmin) {
                accountsAndGroups = AccountLoader.loadAccountsAndGroupNamesForLocalAdmin();
            } else {
                accountsAndGroups = AccountLoader.loadLoginAndGroupNames();
            }

            roleArray = accountsAndGroups.toArray(new String[accountsAndGroups.size()]);
        }
        return roleArray;
    }



    protected void addPermission() {
        CnATreeElement element = null;
        if (elements != null && !elements.isEmpty()) {
            element = elements.get(0);
        }
        if (element != null) {
            Permission p = Permission.createPermission(element, comboRole.getText(), buttonRead.getSelection(), buttonWrite.getSelection());
            // Remove permission first and add it again to replace the permission
            this.permissionSet.remove(p);
            this.permissionSet.add(p);
            this.permissionSetAdd.remove(p);
            this.permissionSetAdd.add(p);
            refreshTable();
        }
    }

    protected void removePermission() {
        int[] selectionIndices = viewer.getTable().getSelectionIndices();
        for (int i : selectionIndices) {
            Permission p = (Permission) viewer.getElementAt(i);
            viewer.getTable().getItem(i).setBackground(new Color(Display.getCurrent(), 230, 230, 230));
            viewer.getTable().getItem(i).setForeground(new Color(Display.getCurrent(), 180, 180, 180));
            viewer.getTable().getItem(i).setImage(1, getYesNoImage(p.isReadAllowed(), false));
            viewer.getTable().getItem(i).setImage(2, getYesNoImage(p.isWriteAllowed(), false));
        }

        this.permissionSetAdd.removeAll(getSelectedPermissions());
        this.permissionSetRemove.addAll(getSelectedPermissions());
    }

    private Set<Permission> loadPermission(CnATreeElement firstElement) {
        LoadPermissions lp = new LoadPermissions(firstElement);
        try {
            lp = ServiceFactory.lookupCommandService().executeCommand(lp);
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        // clone the permissions because of hashcode trouble in set with
        // instances created by hibernate
        this.permissionSet = Permission.clonePermissionSet(firstElement, lp.getPermissions());
        this.permissionSetAdd = Permission.clonePermissionSet(firstElement, lp.getPermissions());
        this.permissionSetRemove = new HashSet<Permission>();
        return this.permissionSet;
    }

    protected Image getYesNoImage(boolean value, boolean enabled) {
        if (value) {
            if (enabled) {
                return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_JA);
            } else {
                return ImageCache.getInstance().getImage(ImageCache.OK_DISABLED);
            }
        } else {
            if (enabled) {
                return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
            } else {
                return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
            }
        }
    }

    private GridLayout generateGridLayout(int columns, boolean makeColumnsEqualWidth, int marginWidth, int marginHeight) {
        GridLayout layout = new GridLayout(columns, makeColumnsEqualWidth);
        layout.marginWidth = marginWidth;
        layout.marginHeight = marginHeight;
        return layout;
    }

    private GridData generateGridData(Integer style, Boolean grabExcessHorizontalSpace, Boolean grabExcessVerticalSpace, Integer horizontalAlignment, Integer verticalAlignment, Integer horizontalSpan) {
        GridData data = (style != null) ? new GridData(style.intValue()) : new GridData();
        data.grabExcessHorizontalSpace = (grabExcessHorizontalSpace != null) ? grabExcessHorizontalSpace.booleanValue() : data.grabExcessHorizontalSpace;
        data.grabExcessVerticalSpace = (grabExcessVerticalSpace != null) ? grabExcessVerticalSpace.booleanValue() : data.grabExcessVerticalSpace;
        data.horizontalAlignment = (horizontalAlignment != null) ? horizontalAlignment.intValue() : data.horizontalAlignment;
        data.verticalAlignment = (verticalAlignment != null) ? verticalAlignment.intValue() : data.verticalAlignment;
        data.horizontalSpan = (horizontalSpan != null) ? horizontalSpan.intValue() : data.horizontalSpan;
        return data;
    }

    private Button generateButton(Composite composite, Integer style, String text, Boolean selection, SelectionListener listener) {
        Button button = new Button(composite, style);
        button.setText((text != null) ? text : button.getText());
        button.setSelection((selection != null) ? selection.booleanValue() : button.getSelection());
        if (listener != null) {
            button.addSelectionListener(listener);
        }
        return button;
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
        isOverride = radioButtonMode[1].getSelection();
        isUpdateChildren = buttonInherit.getSelection();
        super.okPressed();
    }

    public List<CnATreeElement> getElements() {
        return elements;
    }

    public Set<Permission> getPermissionSetAdd() {
        return permissionSetAdd;
    }

    public Set<Permission> getPermissionSetRemove() {
        return permissionSetRemove;
    }

    public boolean isOverride() {
        return isOverride;
    }

    public boolean isUpdateChildren() {
        return isUpdateChildren;
    }

    SelectionListener removeListener = new SelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                removePermission();
            } catch (Exception e1) {
                LOG.error("Error while removing permission.", e1);
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    };

    SelectionListener radioListener = new SelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            isOverride = radioButtonMode[1].getSelection();
            showInformation();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    };

    private IAuthService getAuthService() {
        return ServiceFactory.lookupAuthService();
    }
}
