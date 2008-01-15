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
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman@sernet.de
 *
 */
public class DatenbankPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {


	private RadioGroupFieldEditor dbDriver;
	private StringFieldEditor dialect;
	private StringFieldEditor url;
	private StringFieldEditor user;
	private StringFieldEditor pass;

	public DatenbankPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Hier konfigurieren Sie die Datenbank, in der " +
				"SerNet verinice ihr GS-Modell speichert. Wenn Sie keine Datenbank " +
				"zur Verfügung haben, benutzt verinice die integrierte " +
				"Derby-DB und speichert das Modell in ihrem Arbeitsverzeichnis. " +
				"Eine bessere Performance erzielen Sie aber in jedem Fall " +
				"mit einer externen Datenbank, so dass Sie " +
				"bei ernsthafter Nutzung auf Postgres oder MySQL umsteigen sollten.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		createRadioGroup();
		dialect = new StringFieldEditor(PreferenceConstants.DB_DIALECT,
				"SQL Dialekt",
				getFieldEditorParent());
		addField(dialect);
		
		url = new StringFieldEditor(PreferenceConstants.DB_URL,
				"JDBC URL",
				getFieldEditorParent());
		addField(url);
		
		
		user = new StringFieldEditor(PreferenceConstants.DB_USER,
				"DB User",
				getFieldEditorParent());
		addField(user);
		
		pass = new StringFieldEditor(PreferenceConstants.DB_PASS,
				"DB Passwort",
				getFieldEditorParent());
		addField(pass);
	}
	
	private void createRadioGroup() {
		dbDriver = new RadioGroupFieldEditor(PreferenceConstants.DB_DRIVER,
				"Verwendete Datenbank",
				1,
				new String[][] {
					{"Derby (integriert)", PreferenceConstants.DB_DRIVER_DERBY}, 
					{"Postgres", PreferenceConstants.DB_DRIVER_POSTGRES},
					{"MySQL", PreferenceConstants.DB_DRIVER_MYSQL}
				},
				getFieldEditorParent());
		addField(dbDriver);
		
		
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			if (event.getSource() == dbDriver) {
				setDefaults((String)event.getNewValue());
			}
			checkState();
		}
	}

	private void setDefaults(String newValue) {
		if (newValue.equals(PreferenceConstants.DB_DRIVER_DERBY)) {
			dialect.setStringValue(PreferenceConstants.DB_DIALECT_derby);
			String derbyUrl = PreferenceConstants.DB_URL_DERBY.replace("%s",CnAWorkspace
					.getInstance().getWorkdir() );
			url.setStringValue(derbyUrl);
			user.setStringValue("");
			pass.setStringValue("");
		}
		else if (newValue.equals(PreferenceConstants.DB_DRIVER_POSTGRES)) {
			dialect.setStringValue(PreferenceConstants.DB_DIALECT_postgres);
			url.setStringValue(PreferenceConstants.DB_URL_POSTGRES);
			user.setStringValue("");
			pass.setStringValue("");
		}
		else if (newValue.equals(PreferenceConstants.DB_DRIVER_MYSQL)) {
			dialect.setStringValue(PreferenceConstants.DB_DIALECT_mysql);
			url.setStringValue(PreferenceConstants.DB_URL_MYSQL);
			user.setStringValue("");
			pass.setStringValue("");
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
