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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.model.bsi.BausteinVorschlag;

/**
 * Dialog to edit an exsisting OwnGefaehrdung.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class EditBausteinVorgabeDialog extends Dialog {

    private Text textName;
    private Text textBausteine;
    private BausteinVorschlag vorschlag;
    private String bausteine;
    private String name;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            shell of parent Composite
     * @param newOwnGefaehrdung
     *            the OwnGefaehrdung to edit
     */
    public EditBausteinVorgabeDialog(Shell parentShell, BausteinVorschlag vorschlag) {
        super(parentShell);
        this.vorschlag = vorschlag;
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.TITLE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.EditBausteinVorgabeDialog_0);
    }

    /**
     * Creates the content area of the Dialog.
     * 
     * @param parent
     *            the parent Composite
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        final Label labelTitle = new Label(composite, SWT.NONE);
        GridData gridLabelTitle = new GridData();
        gridLabelTitle.horizontalAlignment = SWT.FILL;
        gridLabelTitle.horizontalSpan = 2;
        labelTitle.setText(Messages.EditBausteinVorgabeDialog_1);
        labelTitle.setLayoutData(gridLabelTitle);

        final Label labelNumber = new Label(composite, SWT.NONE);
        GridData gridLabelNumber = new GridData();
        gridLabelNumber.horizontalAlignment = SWT.LEFT;
        gridLabelNumber.verticalAlignment = SWT.CENTER;
        labelNumber.setText(Messages.EditBausteinVorgabeDialog_2);
        labelNumber.setLayoutData(gridLabelNumber);

        textName = new Text(composite, SWT.BORDER);
        GridData gridTextDescription = new GridData();
        gridTextDescription.horizontalAlignment = SWT.FILL;
        gridTextDescription.grabExcessHorizontalSpace = true;
        gridTextDescription.widthHint = 400;
        textName.setLayoutData(gridTextDescription);
        textName.setText(vorschlag.getName());

        final Label labelName = new Label(composite, SWT.NONE);
        GridData gridLabelName = new GridData();
        gridLabelName.horizontalAlignment = SWT.LEFT;
        gridLabelName.verticalAlignment = SWT.CENTER;
        labelName.setText(Messages.EditBausteinVorgabeDialog_3);
        labelName.setLayoutData(gridLabelName);

        textBausteine = new Text(composite, SWT.BORDER);
        GridData gridTextDescription2 = new GridData();
        gridTextDescription2.horizontalAlignment = SWT.FILL;
        gridTextDescription2.grabExcessHorizontalSpace = true;
        gridTextDescription2.widthHint = 400;
        textBausteine.setLayoutData(gridTextDescription2);
        textBausteine.setText(vorschlag.getBausteine());

        return composite;
    }

    @Override
    protected void okPressed() {
        name = textName.getText();
        bausteine = textBausteine.getText();
        super.okPressed();
    }

    public String getName() {
        return name;
    }

    public String getBausteine() {
        return bausteine;
    }

}
