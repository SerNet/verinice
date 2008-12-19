package sernet.gs.ui.rcp.main.preferences;

import org.apache.log4j.Logger;
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
		setDescription(Messages.getString("DatenbankPreferencePage.0") + //$NON-NLS-1$
				Messages.getString("DatenbankPreferencePage.1") + //$NON-NLS-1$
				Messages.getString("DatenbankPreferencePage.2") + //$NON-NLS-1$
				Messages.getString("DatenbankPreferencePage.3") + //$NON-NLS-1$
				Messages.getString("DatenbankPreferencePage.4") + //$NON-NLS-1$
				Messages.getString("DatenbankPreferencePage.5") + //$NON-NLS-1$
				Messages.getString("DatenbankPreferencePage.6")); //$NON-NLS-1$
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
				Messages.getString("DatenbankPreferencePage.7"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(dialect);
		
		url = new StringFieldEditor(PreferenceConstants.DB_URL,
				Messages.getString("DatenbankPreferencePage.8"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(url);
		
		
		user = new StringFieldEditor(PreferenceConstants.DB_USER,
				Messages.getString("DatenbankPreferencePage.9"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(user);
		
		pass = new StringFieldEditor(PreferenceConstants.DB_PASS,
				Messages.getString("DatenbankPreferencePage.10"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(pass);
	}
	
	private void createRadioGroup() {
		dbDriver = new RadioGroupFieldEditor(PreferenceConstants.DB_DRIVER,
				Messages.getString("DatenbankPreferencePage.11"), //$NON-NLS-1$
				1,
				new String[][] {
					{Messages.getString("DatenbankPreferencePage.12"), PreferenceConstants.DB_DRIVER_DERBY},  //$NON-NLS-1$
					{"Postgres", PreferenceConstants.DB_DRIVER_POSTGRES}, //$NON-NLS-1$
					{"MySQL", PreferenceConstants.DB_DRIVER_MYSQL} //$NON-NLS-1$
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
			
			// replace backslashes
			// derby db url looks like this on windows: c:/Programme/Verinice...
			String derbyUrl = PreferenceConstants.DB_URL_DERBY.replace("%s",CnAWorkspace //$NON-NLS-1$
					.getInstance().getWorkdir().replaceAll("\\\\", "/") );
			Logger.getLogger(this.getClass()).debug("Derby url is " + derbyUrl);
			url.setStringValue(derbyUrl);
			user.setStringValue(""); //$NON-NLS-1$
			pass.setStringValue(""); //$NON-NLS-1$
		}
		else if (newValue.equals(PreferenceConstants.DB_DRIVER_POSTGRES)) {
			dialect.setStringValue(PreferenceConstants.DB_DIALECT_postgres);
			url.setStringValue(PreferenceConstants.DB_URL_POSTGRES);
			user.setStringValue(""); //$NON-NLS-1$
			pass.setStringValue(""); //$NON-NLS-1$
		}
		else if (newValue.equals(PreferenceConstants.DB_DRIVER_MYSQL)) {
			dialect.setStringValue(PreferenceConstants.DB_DIALECT_mysql);
			url.setStringValue(PreferenceConstants.DB_URL_MYSQL);
			user.setStringValue(""); //$NON-NLS-1$
			pass.setStringValue(""); //$NON-NLS-1$
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
