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
package sernet.verinice.rcp.linktable.composite.multiselectiondialog;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import sernet.verinice.rcp.linktable.composite.Messages;
import sernet.verinice.rcp.linktable.composite.VeriniceLinkTableComposite;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableMultiSelectionControl {
	
	
	private Composite parent;
	private Text text;
    private VeriniceLinkTableMultiSelectionDialog dialog;
    private VeriniceLinkTableComposite ltrParent;
    private Set<String> selectedItems = new HashSet<>();
    private boolean useAllRelations = true;
	

    /**
     * @param multiControlContainer
     * @param veriniceLinkTableComposite
     */
    public VeriniceLinkTableMultiSelectionControl(Composite parent,
            VeriniceLinkTableComposite ltrParent) {
        this.parent = parent;
        this.ltrParent = ltrParent;
        create();
    }

    private void create() {
        Label label = new Label(parent, SWT.NULL);

        label.setText(Messages.MultiSelectionControl_5);
		
        Composite container = new Composite(parent, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(container);
		
		GridData containerLData = new GridData();
		containerLData.horizontalAlignment = GridData.FILL;
		containerLData.grabExcessHorizontalSpace = true;
		container.setLayoutData(containerLData);
		
		
		text = new Text(container, SWT.BORDER);
		text.setEditable(false);
        text.setToolTipText("Relations");
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.getBackground();
        text.setSize(1000, SWT.DEFAULT);
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
	}

    public void showSelectionDialog() {
		Display display = Display.getDefault();
        Shell shell = new Shell(display);
        if (dialog == null) {
            dialog = new VeriniceLinkTableMultiSelectionDialog(shell, this, SWT.NULL);
        }
        dialog.open();
	}

	public void setFocus() {
		this.text.setFocus();
	}

    public VeriniceLinkTableComposite getLtrParent() {
        return ltrParent;
    }

    public Set<String> getSelectedItems() {
        return selectedItems;
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
}
