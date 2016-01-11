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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;

/**
 * Modal dialog to enter a new security measure ("Massnahme").
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class EditRisikoMassnahmenUmsetzungDialog extends RiskAnalysisDialog<MassnahmenUmsetzung> {

	private static final Logger LOG = Logger.getLogger(EditRisikoMassnahmenUmsetzungDialog.class);
	
	private RisikoMassnahmenUmsetzung risikoMassnahmenUmsetzung;

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            shell of parent Composite
	 * @param newRisikoMassnahmenUmsetzung
	 *            the RisikoMassnahmenUmsetzung to edit
	 */
    public EditRisikoMassnahmenUmsetzungDialog(Shell parentShell,
            RisikoMassnahmenUmsetzung newRisikoMassnahmenUmsetzung,
            RiskAnalysisDialogItems<MassnahmenUmsetzung> items) {
        super(parentShell, items);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		risikoMassnahmenUmsetzung = newRisikoMassnahmenUmsetzung;
	}


	/**
	 * Returns true if given String is not null.
	 * 
	 * @param string
	 *            the String to test
	 * @return true if given String is not null, false else
	 */
    private static String notNull(String string) {
		return string != null ? string : ""; //$NON-NLS-1$
	}

	
	public RisikoMassnahmenUmsetzung getRisikoMassnahmenUmsetzung() {
		return risikoMassnahmenUmsetzung;
	}

    @Override
    protected Object getItem() {
        return risikoMassnahmenUmsetzung;
    }

    @Override
    protected void okPressedAndApproved() {
        RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(risikoMassnahmenUmsetzung);
        risikoMassnahmenUmsetzung.getRisikoMassnahme().setNumber(textNumber.getText());
        risikoMassnahmenUmsetzung.setName(textName.getText());
        risikoMassnahmenUmsetzung.getRisikoMassnahme().setDescription(textDescription.getText());
        risikoMassnahmenUmsetzung.setNumber(textNumber.getText());
        risikoMassnahmenUmsetzung.getRisikoMassnahme().setName(textName.getText());

        try {
            RisikoMassnahme rm = RisikoMassnahmeHome.getInstance()
                    .save(risikoMassnahmenUmsetzung.getRisikoMassnahme());
            risikoMassnahmenUmsetzung.setMassnahme(rm);
        } catch (Exception e) {
            LOG.error("Error while saving massnahme", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.EditRisikoMassnahmenUmsetzungDialog_5);
        }

    }

    @Override
    protected String[] loadCategories() {
        return new String[0];
    }

    @Override
    protected void initContents() {
        RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(risikoMassnahmenUmsetzung);
        textNumber.setText(notNull(risikoMassnahmenUmsetzung.getNumber()));
        textName.setText(notNull(risikoMassnahmenUmsetzung.getTitle()));
        textDescription.setText(notNull(risikoMassnahmenUmsetzung.getDescription()));

    }

    @Override
    protected void addCategory(Composite parent) {
        /* no category needed */
    }
}
