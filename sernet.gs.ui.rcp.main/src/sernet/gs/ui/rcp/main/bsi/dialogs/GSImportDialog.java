package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openoffice.java.accessibility.ComboBox;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.preferences.GSImportAttachPreferencePage;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.preferences.ShowPreferencesAction;

/**
 * 
 * @author koderman@sernet.de
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
		
		String mdbFileName = Activator.getDefault().getPluginPreferences()
		.getString(PreferenceConstants.GS_MDBFILE);
		
		if (mdbFileName != null && mdbFileName.length() > 0) {
			Label intro = new Label(container, SWT.NONE);
			intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
					false, false, 1, 1));
			intro
			.setText("Der Import wird aus folgender Datei durchgeführt: \n"
					+ mdbFileName);
		}
		else {
			String importDb = Activator.getDefault().getPluginPreferences()
			.getString(PreferenceConstants.GS_DB_URL);
			
			Label intro = new Label(container, SWT.NONE);
			intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
					false, false, 1, 1));
			intro
			.setText("Der Import wird aus folgender GSTOOL-Datenbank durchgeführt: \n"
					+ importDb);
			
		}
		
		Link prefLink = new Link(container, SWT.NONE);
		prefLink.setText("<a>Ändern...</a>");
		prefLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
				ShowPreferencesAction action = new ShowPreferencesAction(GSImportAttachPreferencePage.ID);
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
