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
 * Dialog to edit an exsisting OwnGefaehrdung.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class EditGefaehrdungDialog extends RiskAnalysisDialog<Gefaehrdung> {

    private OwnGefaehrdung ownGefaehrdung;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            shell of parent Composite
     * @param newOwnGefaehrdung
     *            the OwnGefaehrdung to edit
     */
    public EditGefaehrdungDialog(Shell parentShell, OwnGefaehrdung newOwnGefaehrdung, RiskAnalysisDialogItems<Gefaehrdung> items) {
        super(parentShell, items);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        ownGefaehrdung = newOwnGefaehrdung;
    }


    /**
     * Loads all categories for OwnGefaehrdungen from database.
     * 
     * @return an array of all categories as Strings
     */
    @Override
    protected String[] loadCategories() {
        ArrayList<String> allCategories = new ArrayList<>();
        allCategories.add(Messages.EditGefaehrdungDialog_4);
        allCategories.addAll(Gefaehrdung.getAllCategories());

        List<OwnGefaehrdung> allOwnGefaehrdungen = new ArrayList<>(0);
        try {
            allOwnGefaehrdungen = OwnGefaehrdungHome.getInstance().loadAll();
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.EditGefaehrdungDialog_5);
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
     * Saves the OwnGefaehrung in the database, if okay button is pressed.
     */
    @Override
    protected void okPressedAndApproved() {
            ownGefaehrdung.setId(textNumber.getText());
            ownGefaehrdung.setTitel(textName.getText());
            ownGefaehrdung.setBeschreibung(textDescription.getText());
            ownGefaehrdung.setOwnkategorie(textCategory.getText());

            try {

                ownGefaehrdung = OwnGefaehrdungHome.getInstance().save(ownGefaehrdung);

            } catch (Exception e) {
                ExceptionUtil.log(e, Messages.EditGefaehrdungDialog_6);
            }
    }


    @Override
    protected void initContents() {
        
        textNumber.setText(ownGefaehrdung.getId());
        textName.setText(ownGefaehrdung.getTitel());
        textDescription.setText(ownGefaehrdung.getBeschreibung());
        textCategory.setText(ownGefaehrdung.getOwnkategorie());

    }

    @Override
    protected Object getItem() {
        return ownGefaehrdung;
    }
}
