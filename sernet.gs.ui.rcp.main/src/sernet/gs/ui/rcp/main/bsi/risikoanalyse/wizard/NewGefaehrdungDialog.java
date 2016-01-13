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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;

/**
 * Dialog to enter a new Gefaehrdung.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class NewGefaehrdungDialog extends RiskAnalysisDialog<Gefaehrdung> {

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
    public NewGefaehrdungDialog(Shell parentShell, List<OwnGefaehrdung> newOwnGefaehrdungen,
            RiskAnalysisDialogItems<Gefaehrdung> items) {
        /*
         * note: you need to hand the ArrayList over differently, if you don't
         * use this this dialog modally!
         */
        super(parentShell, items);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        ownGefaehrdungen = newOwnGefaehrdungen;
    }


    /**
     * Loads all categories of OwnGefaehrdungen from database.
     * 
     * @return an array of all categories as Strings
     */
    protected String[] loadCategories() {
        ArrayList<String> allCategories = new ArrayList<>();
        allCategories.add(Messages.NewGefaehrdungDialog_5);
        allCategories.addAll(Gefaehrdung.getAllCategories());

        List<OwnGefaehrdung> allOwnGefaehrdungen = new ArrayList<>(0);
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
    protected void okPressedAndApproved() {

        ownGefaehrdung.setId(textNumber.getText());
        ownGefaehrdung.setTitel(textName.getText());
        ownGefaehrdung.setBeschreibung(textDescription.getText());
        ownGefaehrdung.setOwnkategorie(textCategory.getText());

        try {

            ownGefaehrdung = OwnGefaehrdungHome.getInstance().save(ownGefaehrdung);

        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.NewGefaehrdungDialog_7);
        }
        ownGefaehrdungen.add(ownGefaehrdung);


    }


    @Override
    protected void initContents() {

        textNumber.setText("");
        textName.setText("");
        textDescription.setText("");
        textCategory.setText(Messages.NewGefaehrdungDialog_4);

    }

    @Override
    protected Object getItem() {
        return new OwnGefaehrdung();
    }
}
