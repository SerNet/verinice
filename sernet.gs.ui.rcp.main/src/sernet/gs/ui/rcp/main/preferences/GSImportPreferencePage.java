package sernet.gs.ui.rcp.main.preferences;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;

/**
 * GS Tool Import database settings
 * 
 * @author akoderman@sernet.de
 * 
 */
public class GSImportPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private RadioGroupFieldEditor dbDriver;
	private StringFieldEditor dialect;
	private StringFieldEditor url;
	private StringFieldEditor user;
	private StringFieldEditor pass;

	public GSImportPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("GSImportPreferencePage.0")); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {

		url = new StringFieldEditor(PreferenceConstants.GS_DB_URL,
				"GSTOOL JDBC URL", //$NON-NLS-1$
				getFieldEditorParent());
		addField(url);

		user = new StringFieldEditor(PreferenceConstants.GS_DB_USER,
				"GSTOOL DB User", //$NON-NLS-1$
				getFieldEditorParent());
		addField(user);

		pass = new StringFieldEditor(PreferenceConstants.GS_DB_PASS,
				"GSTOOL DB Passwort", //$NON-NLS-1$
				getFieldEditorParent());
		addField(pass);

		Button button = new Button((Composite) getControl(), SWT.PUSH);
		button.setText("Teste Verbindung");
		button.setLayoutData(new GridData(GridData.END, GridData.BEGINNING,
				true, true));
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					Class.forName("net.sourceforge.jtds.jdbc.Driver");
		            Connection con = DriverManager.getConnection(url.getStringValue(),
		            		user.getStringValue(),
		            		pass.getStringValue());
		            Statement stmt = con.createStatement();
		            stmt.executeQuery("select top 1 * from N_Zielobjekt");
					stmt.close();
					con.close();
					MessageDialog.openInformation(getShell(), "Hurra", "Die Einstellungen sind korrekt.");
				} catch (Exception e1) {
					ExceptionUtil.log(e1, "Die Verbindung konnte nicht hergestellt werden mit "
							+ url.getStringValue());
				}
			}

		});

	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
		}
	}

	@Override
	protected void checkState() {
		super.checkState();
		if (!isValid())
			return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
