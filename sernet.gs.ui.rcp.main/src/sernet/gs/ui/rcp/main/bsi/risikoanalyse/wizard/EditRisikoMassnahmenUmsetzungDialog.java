/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah@sernet.de>
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Anne Hanekop <ah@sernet.de> 	- initial API and implementation
 *     ak@sernet.de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

/**
 * Modal dialog to enter a new security measure ("Massnahme").
 * 
 * @author ahanekop@sernet.de
 */
public class EditRisikoMassnahmenUmsetzungDialog extends Dialog {

	private Text textNumber;
	private Text textName;
	private Text textDescription;
	private RisikoMassnahmenUmsetzung risikoMassnahmenUmsetzung;

	/**
	 * Constructor.
	 * 
	 * @param parentShell shell of parent Composite
	 * @param newRisikoMassnahmenUmsetzung the RisikoMassnahmenUmsetzung to edit
	 */
	public EditRisikoMassnahmenUmsetzungDialog(Shell parentShell,
			RisikoMassnahmenUmsetzung newRisikoMassnahmenUmsetzung) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		risikoMassnahmenUmsetzung = newRisikoMassnahmenUmsetzung;
	}

	/**
	 * Creates the content area of the Dialog.
	 * 
	 * @param parent the parent Composite
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		/* label number */
		final Label labelNumber = new Label(container, SWT.NONE);
		GridData gridLabelNumber = new GridData();
		gridLabelNumber.horizontalAlignment = SWT.LEFT;
		gridLabelNumber.verticalAlignment = SWT.CENTER;
		labelNumber.setText("Nummer:");
		labelNumber.setLayoutData(gridLabelNumber);

		/* text number */
		textNumber = new Text(container, SWT.BORDER);
		GridData gridTextNumber = new GridData();
		gridTextNumber.horizontalAlignment = SWT.FILL;
		gridTextNumber.verticalAlignment = SWT.CENTER;
		gridTextNumber.grabExcessHorizontalSpace = true;
		textNumber.setLayoutData(gridTextNumber);
		textNumber.setText(notNull(risikoMassnahmenUmsetzung.getNumber()));

		/* label name */
		final Label labelName = new Label(container, SWT.NONE);
		GridData gridLabelName = new GridData();
		gridLabelName.horizontalAlignment = SWT.LEFT;
		gridLabelName.verticalAlignment = SWT.CENTER;
		labelName.setText("Name:");
		labelName.setLayoutData(gridLabelName);

		/* text name */
		textName = new Text(container, SWT.BORDER);
		GridData gridTextName = new GridData();
		gridTextName.horizontalAlignment = SWT.FILL;
		gridTextName.verticalAlignment = SWT.CENTER;
		gridTextName.grabExcessHorizontalSpace = true;
		textName.setLayoutData(gridTextName);
		textName.setText(notNull(risikoMassnahmenUmsetzung.getTitel()));

		/* label description */
		final Label labelDescription = new Label(container, SWT.NONE);
		GridData gridLabelDescription = new GridData();
		gridLabelDescription.horizontalAlignment = SWT.LEFT;
		gridLabelDescription.verticalAlignment = SWT.CENTER;
		labelDescription.setText("Beschreibung:");
		labelDescription.setLayoutData(gridLabelDescription);

		/* text description */
		textDescription = new Text(container, SWT.BORDER | SWT.V_SCROLL
				| SWT.MULTI | SWT.WRAP);
		GridData gridTextDescription = new GridData();
		gridTextDescription.horizontalAlignment = SWT.FILL;
		gridTextDescription.verticalAlignment = SWT.FILL;
		gridTextDescription.grabExcessHorizontalSpace = true;
		gridTextDescription.grabExcessVerticalSpace = true;
		gridTextDescription.widthHint = 400;
		gridTextDescription.heightHint = 200;
		textDescription.setLayoutData(gridTextDescription);
		textDescription.setText(notNull(risikoMassnahmenUmsetzung
				.getDescription()));

		return container;
	}

	/**
	 * Returns true if given String is not null.
	 * 
	 * @param string the String to test
	 * @return true if given String is not null, false else
	 */
	private String notNull(String string) {
		return string != null ? string : "";
	}

	/**
	 * Saves the RisikoMassnahmenUmsetzung in the database, if okay button
	 * is pressed.
	 */
	@Override
	protected void okPressed() {
		
		risikoMassnahmenUmsetzung.getRisikoMassnahme()
				.setNumber(textNumber.getText());
		risikoMassnahmenUmsetzung.getRisikoMassnahme().setName(textName.getText());
		risikoMassnahmenUmsetzung.getRisikoMassnahme().setDescription(
				textDescription.getText());

		try {
			RisikoMassnahmeHome.getInstance().save(
					risikoMassnahmenUmsetzung.getRisikoMassnahme());
		} catch (Exception e) {
			ExceptionUtil.log(e, "Änderung konnte nicht gespeichert werden.");
		}

		super.okPressed();
	}
}
