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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.model.bsi.BausteinUmsetzung;

/**
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class KonsolidatorDialog extends Dialog {

    private List<BausteinUmsetzung> selection;

    private BausteinUmsetzung source = null;

    public KonsolidatorDialog(Shell shell, List<BausteinUmsetzung> selectedElements) {
        super(shell);
        this.selection = selectedElements;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);

        Label intro = new Label(container, SWT.NONE);
        intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        intro.setText(Messages.KonsolidatorDialog_0);

        final ListViewer viewer = new ListViewer(container, SWT.CHECK | SWT.BORDER);
        viewer.getList().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));

        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                BausteinUmsetzung bst = (BausteinUmsetzung) element;
                return bst.getKapitel() + ": " + bst.getParent().getTitle(); //$NON-NLS-1$
            }
        });

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setInput(selection);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection sel = ((IStructuredSelection) viewer.getSelection());
                if (sel.size() == 1) {
                    source = (BausteinUmsetzung) sel.getFirstElement();
                }
            }
        });

        return container;
    }

    public static boolean askConsolidate(Shell shell) {
        if (!MessageDialog.openQuestion(shell, Messages.KonsolidatorDialog_2, Messages.KonsolidatorDialog_3)) {
            return false;
        }
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.KonsolidatorDialog_4);
    }

    public BausteinUmsetzung getSource() {
        return source;
    }

}
