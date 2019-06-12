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
 *     Daniel Murygin <dm[at]sernet[dot]de> - Initial implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.IPerson;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.UsernameExistsException;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.interfaces.ldap.SizeLimitExceededException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Domain;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.service.ldap.LoadLdapUser;
import sernet.verinice.service.ldap.PersonInfo;
import sernet.verinice.service.ldap.SaveLdapUser;

/**
 * Dialog that allows importing users from LDAP or Active Directory
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LdapImportDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(LdapImportDialog.class);

    private CheckboxTableViewer viewer;

    private Text surname, givenName, title, department, company;

    private Button[] radioButtonTargetPerspective = new Button[3];

    private HashMap<PersonParameter, List<PersonInfo>> ldapQueryCache;

    public LdapImportDialog(Shell parent) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        ldapQueryCache = new HashMap<>();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        final int shellWidth = 700;
        final int shellHeight = 800;
        newShell.setText(Messages.LdapImportDialog_28);
        newShell.setSize(shellWidth, shellHeight);
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        final int gridDataHorizontalSpan = 5;

        setTitle(Messages.LdapImportDialog_30);

        final int defaultMarginHeight = 10;

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layoutRoot = (GridLayout) composite.getLayout();
        layoutRoot.marginWidth = defaultMarginHeight;
        layoutRoot.marginHeight = defaultMarginHeight;
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        composite.setLayoutData(gd);

        showInformation();

        final Composite container = new Composite(composite, SWT.NONE | SWT.BORDER);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = defaultMarginHeight;
        layout.marginHeight = defaultMarginHeight;
        GridData containerGd = new GridData(GridData.GRAB_HORIZONTAL);
        containerGd.grabExcessHorizontalSpace = true;
        containerGd.grabExcessVerticalSpace = true;
        containerGd.horizontalAlignment = GridData.FILL;
        containerGd.verticalAlignment = GridData.FILL;
        containerGd.horizontalSpan = 2;
        container.setLayout(layout);
        container.setLayoutData(containerGd);

        Label givenNameLabel = new Label(container, SWT.NONE);
        givenNameLabel.setText(Messages.LdapImportDialog_0);
        givenName = new Text(container, SWT.BORDER);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        givenName.setLayoutData(gridData);

        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText(Messages.LdapImportDialog_31);
        surname = new Text(container, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        surname.setLayoutData(gridData);

        Label titleLabel = new Label(container, SWT.NONE);
        titleLabel.setText(Messages.LdapImportDialog_2);
        title = new Text(container, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        title.setLayoutData(gridData);

        Label departmentLabel = new Label(container, SWT.NONE);
        departmentLabel.setText(Messages.LdapImportDialog_3);
        department = new Text(container, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        department.setLayoutData(gridData);

        Label companyLabel = new Label(container, SWT.NONE);
        companyLabel.setText(Messages.LdapImportDialog_4);
        company = new Text(container, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        company.setLayoutData(gridData);

        Composite targetComposite = new Composite(container, SWT.NONE);
        GridLayout targetLayout = new GridLayout(radioButtonTargetPerspective.length + 1, false);
        targetLayout.marginWidth = defaultMarginHeight;
        targetLayout.marginHeight = defaultMarginHeight;
        targetComposite.setLayout(targetLayout);
        GridData gdTarget = new GridData(GridData.FILL_HORIZONTAL);
        gdTarget.grabExcessHorizontalSpace = true;
        gdTarget.grabExcessVerticalSpace = false;
        gdTarget.horizontalAlignment = SWT.LEFT;
        gdTarget.verticalAlignment = SWT.TOP;
        gdTarget.horizontalSpan = gridDataHorizontalSpan;
        targetComposite.setLayoutData(gdTarget);

        Label targetLabel = new Label(targetComposite, SWT.LEFT);
        targetLabel.setText(Messages.LdapImportDialog_47);
        radioButtonTargetPerspective[0] = generateButton(targetComposite, SWT.RADIO,
                Messages.LdapImportDialog_48, false);
        radioButtonTargetPerspective[1] = generateButton(targetComposite, SWT.RADIO,
                Messages.LdapImportDialog_49, true);
        radioButtonTargetPerspective[2] = generateButton(targetComposite, SWT.RADIO,
                Messages.Modernized_IT_Baseline_Protection, false);

        Button buttonLoadAccounts = new Button(container, SWT.PUSH | SWT.BORDER);
        buttonLoadAccounts.setText(Messages.LdapImportDialog_35);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.horizontalSpan = 2;
        buttonLoadAccounts.setLayoutData(gridData);

        createViewer(container);
        Composite buttonRow = new Composite(container, SWT.NONE);
        GridData gdButtonRow = new GridData(SWT.FILL, SWT.TOP, true, false);
        gdButtonRow.horizontalSpan = 2;
        buttonRow.setLayoutData(gdButtonRow);
        buttonRow.setLayout(new GridLayout(3, false));

        Button selectAll = new Button(buttonRow, SWT.PUSH | SWT.BORDER);
        selectAll.setText(sernet.gs.ui.rcp.main.Messages.Select_All);
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(true);
            }
        });
        selectAll.setEnabled(false);
        Button selectNone = new Button(buttonRow, SWT.PUSH | SWT.BORDER);
        selectNone.setText(sernet.gs.ui.rcp.main.Messages.Select_None);
        selectNone.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(false);
            }
        });
        selectNone.setEnabled(false);
        buttonLoadAccounts.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ldapQueryCache.containsKey(getParameter())) {
                    viewer.setInput(getFromCache(getParameter()));
                } else {
                    loadLdapUser();
                }
                selectAll.setEnabled(viewer.getTable().getItemCount() > 0);
                selectNone.setEnabled(viewer.getTable().getItemCount() > 0);
            }

        });
        return container;
    }

    private Button generateButton(Composite composite, Integer style, String text,
            boolean selection) {
        Button button = new Button(composite, style);
        button.setText((text != null) ? text : button.getText());
        button.setSelection(selection);
        return button;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Messages.LdapImportDialog_37, false)
                .setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    protected void showInformation() {
        setMessage(Messages.LdapImportDialog_38);

    }

    private void loadLdapUser() {
        try {
            Domain domain;
            if (radioButtonTargetPerspective[0].getSelection()) {
                domain = Domain.BASE_PROTECTION_OLD;
            } else if (radioButtonTargetPerspective[1].getSelection()) {
                domain = Domain.ISM;
            } else {
                domain = Domain.BASE_PROTECTION;
            }
            LoadLdapUser loadLdapUser = new LoadLdapUser(getParameter(), domain);
            loadLdapUser = ServiceFactory.lookupCommandService().executeCommand(loadLdapUser);
            List<PersonInfo> personList = loadLdapUser.getPersonList();
            if (personList != null) {
                List<PersonInfo> accountList = new ArrayList<>(personList);
                ldapQueryCache.put(getParameter(), accountList);
            }

            viewer.setInput(personList);

        } catch (SizeLimitExceededException sizeLimitExceededException) {
            LOG.warn("Too many results when searching for LDAP users."); //$NON-NLS-1$
            if (LOG.isDebugEnabled()) {
                LOG.debug("stacktrace: ", sizeLimitExceededException); //$NON-NLS-1$
            }
            MessageDialog.openInformation(getShell(), Messages.LdapImportDialog_6,
                    Messages.LdapImportDialog_7);
        } catch (Exception t) {
            LOG.error("Error while setting table data", t); //$NON-NLS-1$
            MessageDialog.openError(getShell(), Messages.LdapImportDialog_45,
                    Messages.LdapImportDialog_1);
        }
    }

    private PersonParameter getParameter() {
        return new PersonParameter(surname.getText(), givenName.getText(), title.getText(),
                department.getText(), company.getText());
    }

    private void createViewer(Composite parent) {
        int style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
        style = style | SWT.FULL_SELECTION | SWT.BORDER;
        viewer = CheckboxTableViewer.newCheckList(parent, style);

        createColumns();
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        final NumericStringComparator numericStringComparator = new NumericStringComparator();
        Comparator<PersonInfo> personInfoComparator = (o1, o2) -> numericStringComparator
                .compare(o1.getLoginName(), o2.getLoginName());
        viewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                return personInfoComparator.compare((PersonInfo) e1, (PersonInfo) e2);
            }
        });
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.addCheckStateListener(event -> {
            Button button = getButton(IDialogConstants.OK_ID);
            button.setEnabled(viewer.getCheckedElements().length != 0);
        });

        // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(gridData);
    }

    private static String getPersonSurname(CnATreeElement person) {
        if (person instanceof IPerson) {
            return ((IPerson) person).getLastName();
        }
        return "";
    }

    private static String getPersonName(CnATreeElement person) {
        if (person instanceof IPerson) {
            return ((IPerson) person).getFirstName();
        }
        return "";
    }

    private void createColumns() {
        String[] titles = { Messages.LdapImportDialog_39, Messages.LdapImportDialog_40,
                Messages.LdapImportDialog_41, Messages.LdapImportDialog_2,
                Messages.LdapImportDialog_3, Messages.LdapImportDialog_4 };
        final int[] bounds = { 80, 90, 130, 100, 100, 120 };

        // First column: login name
        createTableViewerColumn(titles[0], bounds[0], PersonInfo::getLoginName);

        // 2. column: name
        createTableViewerColumn(titles[1], bounds[1],
                personInfo -> getPersonName(personInfo.getPerson()));

        // 3. column: surname
        createTableViewerColumn(titles[2], bounds[2],
                personInfo -> getPersonSurname(personInfo.getPerson()));

        final int constant3 = 3;
        final int constant4 = 4;
        final int constant5 = 5;

        // 4. column: tile
        createTableViewerColumn(titles[constant3], bounds[constant3], PersonInfo::getTitle);

        // 5. column: department
        createTableViewerColumn(titles[constant4], bounds[constant4], PersonInfo::getDepartment);

        // 6. column: company
        createTableViewerColumn(titles[constant5], bounds[constant5], PersonInfo::getCompany);
    }

    private TableViewerColumn createTableViewerColumn(String title, int bound,
            Function<PersonInfo, String> labelRetriever) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        viewerColumn.setLabelProvider(new PersonInfoLabelProvider(labelRetriever));
        return viewerColumn;

    }

    @Override
    protected void okPressed() {
        SaveLdapUser saveLdapUser = new SaveLdapUser(Arrays.stream(viewer.getCheckedElements())
                .map(PersonInfo.class::cast).collect(Collectors.toSet()));
        try {
            saveLdapUser = ServiceFactory.lookupCommandService().executeCommand(saveLdapUser);
        } catch (UsernameExistsException e) {
            LOG.error(e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stacktrace: ", e); //$NON-NLS-1$
            }
            MessageDialog.openError(this.getShell(), Messages.LdapImportDialog_45,
                    NLS.bind(Messages.LdapImportDialog_46, e.getUsername()));
            return;
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        updateModel(saveLdapUser.getImportRootObject(), saveLdapUser.getChangedElements());
        super.okPressed();
        InfoDialogWithShowToggle.openInformation(Messages.LdapImportDialog_42,
                NLS.bind(Messages.LdapImportDialog_43, saveLdapUser.getChangedElements().size()),
                Messages.LdapImportDialog_44, PreferenceConstants.INFO_IMPORT_LDAP);
    }

    private static void updateModel(CnATreeElement importRootObject,
            List<CnATreeElement> changedElement) {
        final int maxNrOfElements = 9;
        if (changedElement != null && changedElement.size() > maxNrOfElements) {
            // if more than 9 elements changed or added do a complete reload
            CnAElementFactory.getInstance().reloadAllModelsFromDatabase();
        } else {
            if (importRootObject != null) {
                fireAddedEvents(importRootObject);
                if (changedElement != null) {
                    changedElement.forEach(LdapImportDialog::fireAddedEvents);
                }
            }
            if (changedElement != null) {
                changedElement.forEach(LdapImportDialog::fireChangedEvents);
            }
        }
    }

    private static void fireChangedEvents(CnATreeElement element) {
        CnAElementFactory.getModel(element).childChanged(element);
        CnAElementFactory.getModel(element).databaseChildChanged(element);
    }

    private static void fireAddedEvents(CnATreeElement element) {
        CnAElementFactory.getModel(element).childAdded(element.getParent(), element);
        CnAElementFactory.getModel(element).databaseChildAdded(element);
    }

    private List<PersonInfo> getFromCache(PersonParameter parameter) {
        if (ldapQueryCache.containsKey(parameter)) {
            return ldapQueryCache.get(parameter);
        }
        return Collections.emptyList();
    }

    private static class PersonInfoLabelProvider extends ColumnLabelProvider {

        private final Function<PersonInfo, String> labelRetriever;

        private PersonInfoLabelProvider(Function<PersonInfo, String> labelRetriever) {
            this.labelRetriever = labelRetriever;
        }

        @Override
        public String getText(Object element) {
            return labelRetriever.apply(((PersonInfo) element));
        }

    }
}
