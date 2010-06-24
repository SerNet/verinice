/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah[at]sernet[dot]de>
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
 *     Anne Hanekop <ah[at]sernet[dot]de> 	- initial API and implementation
 *     ak[at]sernet[dot]de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;

/**
 * Dialog to enter a new Gefaehrdung.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class NewGefaehrdungDialog extends Dialog {

    private Text textNumber;
    private Text textName;
    private Text textDescription;
    private Combo textCategory;
    private List<OwnGefaehrdung> ownGefaehrdungen;
    private OwnGefaehrdung ownGefaehrdung = new OwnGefaehrdung();

    /**
     * Constructor initializes the new Gefaehrdung.
     * 
     * @param parentShell
     *            shell of parent (WizardPage)
     * @param newOwnGefaehrdungen
     *            List of all currently existing OwnGefaehrdungen
     */
    public NewGefaehrdungDialog(Shell parentShell, List<OwnGefaehrdung> newOwnGefaehrdungen) {
        /*
         * note: you need to hand the ArrayList over differently, if you don't
         * use this this dialog modally!
         */
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        ownGefaehrdungen = newOwnGefaehrdungen;
    }

    /**
     * Creates the content area of the Dialog.
     * 
     * @param parent
     *            the parent Composite
     * @return the dialog area control
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        /* label number */
        final Label labelNumber = new Label(composite, SWT.NONE);
        GridData gridLabelNumber = new GridData();
        gridLabelNumber.horizontalAlignment = SWT.LEFT;
        gridLabelNumber.verticalAlignment = SWT.CENTER;
        labelNumber.setText(Messages.NewGefaehrdungDialog_0);
        labelNumber.setLayoutData(gridLabelNumber);

        /* text number */
        textNumber = new Text(composite, SWT.BORDER);
        GridData gridTextNumber = new GridData();
        gridTextNumber.horizontalAlignment = SWT.FILL;
        gridTextNumber.verticalAlignment = SWT.CENTER;
        gridTextNumber.grabExcessHorizontalSpace = true;
        textNumber.setLayoutData(gridTextNumber);

        /* label name */
        final Label labelName = new Label(composite, SWT.NONE);
        GridData gridLabelName = new GridData();
        gridLabelName.horizontalAlignment = SWT.LEFT;
        gridLabelName.verticalAlignment = SWT.CENTER;
        labelName.setText(Messages.NewGefaehrdungDialog_1);
        labelName.setLayoutData(gridLabelName);

        /* text name */
        textName = new Text(composite, SWT.BORDER);
        GridData gridTextName = new GridData();
        gridTextName.horizontalAlignment = SWT.FILL;
        gridTextName.verticalAlignment = SWT.CENTER;
        gridTextName.grabExcessHorizontalSpace = true;
        textName.setLayoutData(gridTextName);

        /* label description */
        final Label labelDescription = new Label(composite, SWT.NONE);
        GridData gridLabelDescription = new GridData();
        gridLabelDescription.horizontalAlignment = SWT.LEFT;
        gridLabelDescription.verticalAlignment = SWT.TOP;
        labelDescription.setText(Messages.NewGefaehrdungDialog_2);
        labelDescription.setLayoutData(gridLabelDescription);

        /* text description */
        textDescription = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
        GridData gridTextDescription = new GridData();
        gridTextDescription.horizontalAlignment = SWT.FILL;
        gridTextDescription.verticalAlignment = SWT.FILL;
        gridTextDescription.grabExcessHorizontalSpace = true;
        gridTextDescription.grabExcessVerticalSpace = true;
        gridTextDescription.widthHint = 400;
        gridTextDescription.heightHint = 200;
        textDescription.setLayoutData(gridTextDescription);

        /* label category */
        final Label labelCategory = new Label(composite, SWT.NONE);
        GridData gridLabelCategory = new GridData();
        gridLabelCategory.horizontalAlignment = SWT.LEFT;
        gridLabelCategory.verticalAlignment = SWT.TOP;
        labelCategory.setText(Messages.NewGefaehrdungDialog_3);
        labelCategory.setLayoutData(gridLabelCategory);

        /* text category */
        textCategory = new Combo(composite, SWT.DROP_DOWN);
        GridData gridTextCategory = new GridData();
        gridTextCategory.horizontalAlignment = SWT.FILL;
        gridTextCategory.verticalAlignment = SWT.CENTER;
        gridTextCategory.grabExcessHorizontalSpace = true;
        textCategory.setLayoutData(gridTextCategory);
        textCategory.setItems(loadCategories());
        textCategory.setText(Messages.NewGefaehrdungDialog_4);

        return composite;
    }

    /**
     * Loads all categories of OwnGefaehrdungen from database.
     * 
     * @return an array of all categories as Strings
     */
    private String[] loadCategories() {
        ArrayList<String> allCategories = new ArrayList<String>();
        allCategories.add(Messages.NewGefaehrdungDialog_5);
        allCategories.add(Gefaehrdung.KAT_STRING_HOEHERE_GEWALT);
        allCategories.add(Gefaehrdung.KAT_STRING_ORG_MANGEL);
        allCategories.add(Gefaehrdung.KAT_STRING_MENSCH);
        allCategories.add(Gefaehrdung.KAT_STRING_TECHNIK);
        allCategories.add(Gefaehrdung.KAT_STRING_VORSATZ);

        List<OwnGefaehrdung> allOwnGefaehrdungen = new ArrayList<OwnGefaehrdung>(0);
        try {
            allOwnGefaehrdungen = OwnGefaehrdungHome.getInstance().loadAll();
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.NewGefaehrdungDialog_6);
        }
        Boolean contains = false;

        for (OwnGefaehrdung gefaehrdung : allOwnGefaehrdungen) {
            for (String category : allCategories) {
                if (category.equalsIgnoreCase(gefaehrdung.getKategorieAsString())) {
                    /* category already in List */
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                allCategories.add(gefaehrdung.getKategorieAsString());
            } else {
                contains = false;
            }
        }
        return allCategories.toArray(new String[allCategories.size()]);
    }

    /**
     * Saves the new OwnGefaehrung in the database, if okay button is pressed.
     */
    @Override
    protected void okPressed() {

        ownGefaehrdung.setId(textNumber.getText());
        ownGefaehrdung.setTitel(textName.getText());
        ownGefaehrdung.setBeschreibung(textDescription.getText());
        ownGefaehrdung.setOwnkategorie(textCategory.getText());
        ownGefaehrdungen.add(ownGefaehrdung);

        try {
            OwnGefaehrdungHome.getInstance().save(ownGefaehrdung);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.NewGefaehrdungDialog_7);
        }

        super.okPressed();
    }
}
