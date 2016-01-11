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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;

/**
 * Dialog to enter a new MassnahmenUmsetzung.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class NewRisikoMassnahmeDialog extends RiskAnalysisDialog<MassnahmenUmsetzung> {

    private RisikoMassnahme newRisikoMassnahme;

    /**
     * Constructor of NewMassnahmenDialog. The dialog creates a new
     * RiskoMassnahmen and adds it to the given list.
     * 
     * @param parentShell
     *            shell of the viewer in which the Dialog is called
     */
    public NewRisikoMassnahmeDialog(Shell parentShell, RiskAnalysisDialogItems<MassnahmenUmsetzung> items) {
        super(parentShell, items);
        newRisikoMassnahme = new RisikoMassnahme();
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    public RisikoMassnahme getNewRisikoMassnahme() {
        return newRisikoMassnahme;
    }

    @Override
    protected Object getItem() {
        return new RisikoMassnahme();
    }

    @Override
    protected void okPressedAndApproved() {
        newRisikoMassnahme.setNumber(textNumber.getText());
        newRisikoMassnahme.setName(textName.getText());
        newRisikoMassnahme.setDescription(textDescription.getText());

        try {
            newRisikoMassnahme = RisikoMassnahmeHome.getInstance().save(newRisikoMassnahme);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.NewRisikoMassnahmeDialog_3);
        }
    }

    @Override
    protected String[] loadCategories() {
        return new String[0];
    }

    @Override
    protected void initContents() {
        textNumber.setText("");
        textName.setText("");
        textDescription.setText("");

    }

    @Override
    protected void addCategory(Composite parent) {
        /* no category needed */
    }
}
