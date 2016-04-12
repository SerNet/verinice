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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import sernet.verinice.rcp.linktable.composite.Messages;

/**
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableMultiSelectionDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
    private VeriniceLinkTableMultiSelectionList mList = null;
    private VeriniceLinkTableMultiSelectionControl ltrMultiSelectionControl;

    private static final Logger LOG = Logger.getLogger(VeriniceLinkTableMultiSelectionDialog.class);


    public VeriniceLinkTableMultiSelectionDialog(Shell parent, VeriniceLinkTableMultiSelectionControl ltrMultiSelectionControl,
            int style) {
		super(parent, style);
        this.ltrMultiSelectionControl = ltrMultiSelectionControl;
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			dialogShell.setLayout(new GridLayout(1, false));
			dialogShell.setSize(400, 300);
            dialogShell.setText(Messages.MultiSelectionDialog_0);
            mList = new VeriniceLinkTableMultiSelectionList(this);
            GridData scrolledComposite1LData = new GridData();
            scrolledComposite1LData.grabExcessVerticalSpace = true;
            scrolledComposite1LData.horizontalAlignment = GridData.FILL;
            scrolledComposite1LData.verticalAlignment = GridData.FILL;
            scrolledComposite1LData.grabExcessHorizontalSpace = true;
            mList.setLayoutData(scrolledComposite1LData);
			

			
			Composite buttons = new Composite(dialogShell, SWT.NULL);
			GridLayout contLayout = new GridLayout(2, false);
			contLayout.horizontalSpacing = 5;
			buttons.setLayout(contLayout);
			
			GridData containerLData = new GridData();
			containerLData.horizontalAlignment = GridData.END;
			buttons.setLayoutData(containerLData);
			
			Button okayBtn = new Button(buttons, SWT.PUSH);
			okayBtn.setText(Messages.MultiSelectionControl_1);
			okayBtn.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent arg0) {
					close();
				}
				
				public void widgetDefaultSelected(SelectionEvent arg0) {
					close();
				}
			});
			
			dialogShell.layout();
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch()){
					display.sleep();
				}
			}
		} catch (Exception e) {
			Logger.getLogger(VeriniceLinkTableMultiSelectionDialog.class).error("Exception while opening dialog", e);
		}
	}
	
	void close() {
        ltrMultiSelectionControl.getLtrParent().updateVeriniceContent();
        dialogShell.dispose();
        LOG.debug("shell closed");

	}

    public VeriniceLinkTableMultiSelectionList getLTRMultiSelectionList() {
        return mList;
    }

    public VeriniceLinkTableMultiSelectionControl getLTRMultiSelectionControl() {
        return ltrMultiSelectionControl;
    }

    public Shell getDialogShell(){
        return dialogShell;
    }



}
