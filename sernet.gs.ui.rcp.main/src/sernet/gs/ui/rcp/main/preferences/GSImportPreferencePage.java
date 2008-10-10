package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;

/**
 * GS Tool Import database settings
 * 
 * @author akoderman@sernet.de
 *
 */
public class GSImportPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {


	private RadioGroupFieldEditor dbDriver;
	private StringFieldEditor dialect;
	private StringFieldEditor url;
	private StringFieldEditor user;
	private StringFieldEditor pass;

	public GSImportPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Hier konfigurieren Sie sie MS-SQL Datenbank des GSTool 4.5 für die Datenübernahme.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		
		url = new StringFieldEditor(PreferenceConstants.GS_DB_URL,
				"GSTOOL JDBC URL",
				getFieldEditorParent());
		addField(url);
		
		
		user = new StringFieldEditor(PreferenceConstants.GS_DB_USER,
				"GSTOOL DB User",
				getFieldEditorParent());
		addField(user);
		
		pass = new StringFieldEditor(PreferenceConstants.GS_DB_PASS,
				"GSTOOL DB Passwort",
				getFieldEditorParent());
		addField(pass);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}
