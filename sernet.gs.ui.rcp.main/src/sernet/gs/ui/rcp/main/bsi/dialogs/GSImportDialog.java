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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.GSImportPreferencePage;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.preferences.ShowPreferencesAction;

/**
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class GSImportDialog extends Dialog {

	protected boolean massnahmenPersonen = true;
	protected boolean zielObjekteZielobjekte = true;
	protected boolean schutzbedarf = true;
	protected boolean bausteine = true;
	protected boolean rollen = true;
	protected boolean kosten = true;
	protected boolean umsetzung = true;
	protected boolean bausteinPersonen = true;

	public GSImportDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);
		
		
			String importDb = Activator.getDefault().getPluginPreferences()
			.getString(PreferenceConstants.GS_DB_URL);
			
			Label intro = new Label(container, SWT.NONE);
			intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
					false, false, 1, 1));
			intro
			.setText("Der Import wird aus folgender GSTOOL-Datenbank durchgeführt: \n"
					+ importDb);
			
		Link prefLink = new Link(container, SWT.NONE);
		prefLink.setText("<a>Ändern...</a>");
		prefLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
				ShowPreferencesAction action = new ShowPreferencesAction(GSImportPreferencePage.ID);
				action.run();
			}
		});
		
		Label intro2 = new Label(container, SWT.NONE);
		intro2.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 1, 1));
		intro2
		.setText("\n\nEs wird dafür ein neuer IT-Verbund angelegt. "
				+ "Es werden keine Daten überschrieben. \n\n" +
						"Importiere: ");
		
		final Button button4 = new Button(parent, SWT.CHECK);
		button4.setText("Zielobjekte");
		button4.setEnabled(false);
		button4.setSelection(true);
		button4.pack();

		final Button button5 = new Button(parent, SWT.CHECK);
		button5.setSelection(true);
		button5.setText("Zugeordnete Standard-Bausteine und -Maßnahmen");
		button5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				bausteine = button5.getSelection();
			}
		});
		button5.pack();
		
		final Button button6 = new Button(parent, SWT.CHECK);
		button6.setSelection(true);
		button6.setText("Zugeordnete Rollen der Mitarbeiter");
		button6.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				rollen = button6.getSelection();
			}
		});
		button6.pack();
		
		final Button button9 = new Button(parent, SWT.CHECK);
		button9.setSelection(true);
		button9.setText("Interviewer und befragte Personen für Bausteine");
		button9.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				bausteinPersonen = button9.getSelection();
			}
		});
		button9.pack();

		
		final Button button = new Button(parent, SWT.CHECK);
		button.setSelection(true);
		button.setText("Verantwortliche Personen für Maßnahmenumsetzung");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				massnahmenPersonen = button.getSelection();
			}
		});
		button.pack();
		
		final Button button7 = new Button(parent, SWT.CHECK);
		button7.setSelection(true);
		button7.setText("Kosten für Maßnahmenumsetzung");
		button7.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				kosten = button7.getSelection();
			}
		});
		button7.pack();
		
		final Button button8 = new Button(parent, SWT.CHECK);
		button8.setSelection(true);
		button8.setText("Umsetzungsstatus und -erläuterung der Maßnahmen");
		button8.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				umsetzung = button8.getSelection();
			}
		});
		button8.pack();

		final Button button2 = new Button(parent, SWT.CHECK);
		button2.setSelection(true);
		button2.setText("Verknüpfungen von Zielobjekten untereinander");
		button2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				zielObjekteZielobjekte = button2.getSelection();
			}
		});
		button2.pack();
		
		final Button button3 = new Button(parent, SWT.CHECK);
		button3.setSelection(true);
		button3.setText("Schutzbedarfszuordnung von Zielobjekten");
		button3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				schutzbedarf = button3.getSelection();
			}
		});
		button3.pack();

		
		
		return container;
						
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("GSTOOL™ Import");
	}

	public boolean isMassnahmenPersonen() {
		return massnahmenPersonen;
	}

	public boolean isZielObjekteZielobjekte() {
		return zielObjekteZielobjekte;
	}

	public boolean isSchutzbedarf() {
		return schutzbedarf;
	}

	public boolean isBausteine() {
		return bausteine;
	}

	public boolean isRollen() {
		return rollen;
	}

	public boolean isKosten() {
		return kosten;
	}

	public boolean isUmsetzung() {
		return umsetzung;
	}

	public boolean isBausteinPersonen() {
		return bausteinPersonen;
	}

}
