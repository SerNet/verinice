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

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.rcp.linktable.ui.Messages;
import sernet.verinice.rcp.linktable.ui.UpdateLinkTable;

/**
 * @see LinkTableMultiSelectionControl
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTableMultiSelectionDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
    private LinkTableMultiSelectionList mList = null;
    private LinkTableMultiSelectionControl ltrMultiSelectionControl;

    private static final Logger LOG = Logger.getLogger(LinkTableMultiSelectionDialog.class);

    public LinkTableMultiSelectionDialog(Shell parent, LinkTableMultiSelectionControl ltrMultiSelectionControl,
            int style) {
		super(parent, style);
        this.ltrMultiSelectionControl = ltrMultiSelectionControl;
	}

	public void open() {
		try {

			dialogShell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

            GridLayoutFactory.swtDefaults().margins(20, 20).generateLayout(dialogShell);
            dialogShell.setText(Messages.MultiSelectionDialog_0);
            mList = new LinkTableMultiSelectionList(this);
			
			Composite buttons = new Composite(dialogShell, SWT.NULL);
            GridLayoutFactory.swtDefaults().spacing(5, 5).numColumns(2).equalWidth(false)
                    .generateLayout(buttons);
			
            GridDataFactory.swtDefaults().align(GridData.END, GridData.CENTER).applyTo(buttons);
			
			Button okayBtn = new Button(buttons, SWT.PUSH);
			okayBtn.setText(Messages.MultiSelectionControl_1);
			okayBtn.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent event) {
                    if (mList.getSelectedItems().isEmpty()
                            && !ltrMultiSelectionControl.useAllRelationIds()) {
                        MessageDialog.openError(dialogShell, Messages.LinkTableMultiSelectionDialog_0,
                                Messages.LinkTableMultiSelectionDialog_1);
                    } else {
                        close();
                    }
				}
				
                public void widgetDefaultSelected(SelectionEvent event) {
                    widgetSelected(event);
				}
			});

            Point computeSize = dialogShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            int width = computeSize.x + 10;
            int height = Math.min(computeSize.y, 300);
            dialogShell.setSize(width, height);
            dialogShell.layout(true);
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch()){
					display.sleep();
				}
			}
		} catch (Exception e) {
            LOG.error("Exception while opening dialog", e);
		}
	}
	
	void close() {

        Set<String> selectedItems = mList.getSelectedItems();
        ltrMultiSelectionControl.setSelectedItems(selectedItems);

        ltrMultiSelectionControl.getVltParent()
                .updateAndValidateVeriniceContent(UpdateLinkTable.RELATION_IDS);
        ltrMultiSelectionControl.getVltParent().fireValidationEvent();
        dialogShell.dispose();
	}

    public LinkTableMultiSelectionList getLTRMultiSelectionList() {
        return mList;
    }

    public LinkTableMultiSelectionControl getLTRMultiSelectionControl() {
        return ltrMultiSelectionControl;
    }

    public Shell getDialogShell(){
        return dialogShell;
    }
}
