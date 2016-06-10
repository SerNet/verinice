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
 *     Ruth Motza <rm[at]sernet[dot]de> - adapion of new class
 ******************************************************************************/
package sernet.verinice.rcp.linktable.ui.multiselectiondialog;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import sernet.hui.swt.widgets.multiselectionlist.MultiSelectionControl;
import sernet.verinice.rcp.linktable.ui.LinkTableComposite;
import sernet.verinice.rcp.linktable.ui.Messages;

/**
 * 
 * Multi selection control to select the RelationIDs to be used in the LTR.
 * 
 * 
 * @see MultiSelectionControl
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTableMultiSelectionControl {
	
	
	private Composite parent;
	private Text text;
    private LinkTableMultiSelectionDialog dialog;
    private LinkTableComposite vltComposite;

    private Set<String> selectedItems = new HashSet<>();
    private boolean useAllRelations = false;

    public LinkTableMultiSelectionControl(Composite parent,
            LinkTableComposite vltParent) {
        this.parent = parent;
        this.vltComposite = vltParent;
        this.useAllRelations = vltParent.getVeriniceLinkTable() == null
                || vltParent.getVeriniceLinkTable().getRelationIds().isEmpty();
        create();
    }

    private void create() {
        Label header = new Label(parent, SWT.NULL);
        header.setText(Messages.MultiSelectionControl_5);
		
        Composite container = new Composite(parent, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(container);
        GridDataFactory.swtDefaults().align(GridData.FILL, GridData.CENTER).grab(true, false)
                .applyTo(container);

		text = new Text(container, SWT.BORDER);
		text.setEditable(false);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.getBackground();
        text.getForeground();
		
		Button editBtn = new Button(container, SWT.PUSH);
        editBtn.setText(Messages.MultiSelectionControl_0);
        editBtn.setEnabled(true);
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

        if (useAllRelations) {
            text.setText(Messages.MultiSelectionControl_3);
        } else {
            text.setText(Messages.MultiSelectionControl_4);

        }
        refresh();
	}

    private void refresh() {
        text.pack();
        parent.pack(true);
        parent.layout(true);
    }

    public void showSelectionDialog() {
        Display display = Display.getDefault();
        Shell shell = new Shell(display);
        if (dialog == null) {
            dialog = new LinkTableMultiSelectionDialog(
                    shell, this, SWT.NULL);
        }
        dialog.open();
	}

	public void setFocus() {
		this.text.setFocus();
	}

    public LinkTableComposite getVltParent() {
        return vltComposite;
    }

    public Set<String> getSelectedRelationIDs() {
        if (useAllRelations) {
            return new HashSet<>();
        }
        return selectedItems;
    }

    public boolean useAllRelationIds() {
        return useAllRelations;
    }

    public void setUseAllRelationIds(boolean useAllRelationIds) {
        useAllRelations = useAllRelationIds;
    }

    public void setSelectedItems(Set<String> selectedItems) {
        this.selectedItems = selectedItems;
    }
}
