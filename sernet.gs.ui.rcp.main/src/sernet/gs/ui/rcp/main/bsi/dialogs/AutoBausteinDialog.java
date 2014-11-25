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
package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.text.Collator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadGenericElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveGenericElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BausteinVorschlag;
import sernet.verinice.service.commands.SaveElement;

/**
 * Dialog to choose from a list of standard module assignments to target
 * objects.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class AutoBausteinDialog extends Dialog {

    private BausteinVorschlag selectedSubtype;
    private ListViewer viewer;
    private List<BausteinVorschlag> elements;

    public AutoBausteinDialog(Shell shell) {
        super(shell);
        int style = SWT.CLOSE | SWT.TITLE | SWT.BORDER;
        style = style | SWT.APPLICATION_MODAL | SWT.RESIZE;
        setShellStyle(style);
    }

    protected void createListGroup(Composite parent) {
        final int scrolledCompWidth = 100;
        final int scrolledCompHeight = scrolledCompWidth;
        Group groupComposite = new Group(parent, SWT.BORDER);
        groupComposite.setText(Messages.AutoBausteinDialog_0);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1);
        groupComposite.setLayoutData(gridData);
        groupComposite.setLayout(new GridLayout(1, false));

        ScrolledComposite comp = new ScrolledComposite(groupComposite, SWT.V_SCROLL);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setExpandHorizontal(true);

        viewer = new ListViewer(comp, SWT.CHECK);
        comp.setContent(viewer.getControl());
        comp.setMinSize(scrolledCompWidth, scrolledCompHeight);

        LoadGenericElementByType<BausteinVorschlag> command = new LoadGenericElementByType<BausteinVorschlag>(BausteinVorschlag.class);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException e) {
            ExceptionUtil.log(e, Messages.AutoBausteinDialog_1);
        }
        elements = command.getElements();

        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                BausteinVorschlag vorschlag = (BausteinVorschlag) element;
                return vorschlag.getName();
            }
        });

        viewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                BausteinVorschlag bst1 = (BausteinVorschlag) e1;
                BausteinVorschlag bst2 = (BausteinVorschlag) e2;
                return Collator.getInstance().compare(bst1.getName(), bst2.getName());
            }
        });

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setInput(elements);
        viewer.getList().pack();

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection sel = ((IStructuredSelection) viewer.getSelection());
                if (sel.size() == 1) {
                    selectedSubtype = (BausteinVorschlag) sel.getFirstElement();
                }
            }
        });

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                // make sure that selection is set:
                IStructuredSelection sel = ((IStructuredSelection) viewer.getSelection());
                if (sel.size() == 1) {
                    selectedSubtype = (BausteinVorschlag) sel.getFirstElement();
                }

                close();
            }
        });

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);

        createListGroup(container);
        createButtonGroup(container);

        return container;
    }

    private void createButtonGroup(Composite parent) {
        Group container = new Group(parent, SWT.BORDER);
        container.setText(Messages.AutoBausteinDialog_2);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gridData);

        GridLayout layout2 = new GridLayout();
        layout2.numColumns = 1;
        container.setLayout(layout2);

        Button buttonNew = new Button(container, SWT.PUSH);
        buttonNew.setText(Messages.AutoBausteinDialog_3);
        GridData gridNew = new GridData(GridData.FILL_HORIZONTAL);
        buttonNew.setLayoutData(gridNew);
        buttonNew.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                BausteinVorschlag neuerVorschlag = new BausteinVorschlag(Messages.AutoBausteinDialog_4, "");
                neuerVorschlag = showEditDialog(neuerVorschlag);
                if (neuerVorschlag != null) {
                    elements.add(neuerVorschlag);
                    viewer.add(neuerVorschlag);
                }
            }
        });

        Button buttonEdit = new Button(container, SWT.PUSH);
        buttonEdit.setText(Messages.AutoBausteinDialog_6);
        GridData grid2 = new GridData(GridData.FILL_HORIZONTAL);
        buttonEdit.setLayoutData(grid2);
        buttonEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!viewer.getSelection().isEmpty()) {
                    Object selection = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                    BausteinVorschlag vorschlag = (BausteinVorschlag) selection;
                    vorschlag = showEditDialog(vorschlag);
                    if (vorschlag != null) {
                        viewer.refresh(vorschlag);
                    }
                }
            }
        });

        Button buttonDel = new Button(container, SWT.PUSH);
        buttonDel.setText(Messages.AutoBausteinDialog_7);
        GridData grid3 = new GridData(GridData.FILL_HORIZONTAL);
        buttonDel.setLayoutData(grid3);
        buttonDel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (MessageDialog.openConfirm(getParentShell(), Messages.AutoBausteinDialog_8, Messages.AutoBausteinDialog_9)) {
                    IStructuredSelection sel = ((IStructuredSelection) viewer.getSelection());
                    if (sel.size() == 1) {
                        BausteinVorschlag selection = (BausteinVorschlag) sel.getFirstElement();
                        RemoveGenericElement<BausteinVorschlag> command = new RemoveGenericElement<BausteinVorschlag>(selection);
                        try {
                            ServiceFactory.lookupCommandService().executeCommand(command);
                            selectedSubtype = null;
                            elements.remove(selection);
                            viewer.remove(selection);
                        } catch (CommandException e1) {
                            ExceptionUtil.log(e1, Messages.AutoBausteinDialog_10);
                        }
                    }
                }
            }
        });
    }

    protected BausteinVorschlag showEditDialog(BausteinVorschlag vorschlag) {
        EditBausteinVorgabeDialog dialog = new EditBausteinVorgabeDialog(getShell(), vorschlag);
        int okay = dialog.open();
        if (okay == Window.OK) {
            vorschlag.setName(dialog.getName());
            vorschlag.setBausteine(dialog.getBausteine());

            SaveElement<BausteinVorschlag> command = new SaveElement<BausteinVorschlag>(vorschlag);
            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                return command.getElement();
            } catch (CommandException e1) {
                ExceptionUtil.log(e1, Messages.AutoBausteinDialog_11);
            }
        }
        return null;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        final int shellWidth = 640;
        final int shellHeight = 480;
        newShell.setText(Messages.AutoBausteinDialog_12);
        newShell.setSize(shellWidth, shellHeight);
    }

    public BausteinVorschlag getSelectedSubtype() {
        return selectedSubtype;
    }

}
