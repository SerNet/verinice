/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.gsimport;

import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import sernet.hui.common.connect.HUITypeFactory;

/**
 * Dialog is shown if unknown GSTOOL types are found during
 * GSTOOL-Import.
 *
 * Special confirm dialog with a button "Add types". Klick on this button
 * add properties to {@link GstoolTypeMapper}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class UnknownTypeDialog extends MessageDialog {

    private Set<String> unknownTypes;

    public static boolean open(Shell parent, Set<String> unknownTypes) {
        MessageDialog dialog = new UnknownTypeDialog(parent, unknownTypes);
        return dialog.open() == 0;
    }

    public UnknownTypeDialog(Shell parentShell, Set<String> unknownTypes) {
        super(parentShell,
              Messages.GstoolTypeValidator_0,
              null,
              createMessage(unknownTypes),
              CONFIRM,
              new String[]{ Messages.UnknownTypeDialog_0, IDialogConstants.CANCEL_LABEL },
              0);
        this.unknownTypes = unknownTypes;
    }

    private void addUnknownTypes() {
        for (String type : unknownTypes) {
            GstoolImportMappingElement mappingEntry = new GstoolImportMappingElement(type, GstoolImportMappingElement.UNKNOWN);
            GstoolTypeMapper.addGstoolSubtypeToPropertyFile(mappingEntry);
        }
    }


    protected void createButtonsForButtonBar(Composite parent) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Messages.UnknownTypeDialog_1);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                addUnknownTypes();
                cancelPressed();
            }
        });
        ((GridLayout) parent.getLayout()).numColumns++;
        setButtonLayoutData(button);
        super.createButtonsForButtonBar(parent);
    }

    private static String createMessage(Set<String> unknownTypes) {
        StringBuilder sb = new StringBuilder();
        final String defaultTypeTitle = HUITypeFactory.getInstance().getMessage(GstoolTypeMapper.DEFAULT_TYPE_ID);
        sb.append(NLS.bind(Messages.GstoolTypeValidator_1, defaultTypeTitle ));
        sb.append("\n\n"); //$NON-NLS-1$
        for (String typeLabel : unknownTypes) {
            sb.append(typeLabel).append("\n"); //$NON-NLS-1$
        }
        return sb.toString();
    }

}