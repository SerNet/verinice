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
 * Preference page to switch between client / server settings.
 * 
 * @author akoderman@sernet.de
 *
 */
public class ClientServerPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private RadioGroupFieldEditor operationMode;
	private StringFieldEditor serverURI;
	

	public ClientServerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Umstellen zwischen Betrieb als Arbeitsplatzrechner (Standalone) mit direkter" +
				"Verbindung zu einer Datenbank oder Mehrbenutzerbetrieb unter Verwendung des Verinice-Servers." +
				"HINWEIS: Die Umstellung auf Serverbetrieb erfordert einen Neustart des Clients!");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		createRadioGroup();
		
		serverURI = new StringFieldEditor(PreferenceConstants.VNSERVER_URI, 
				"Server URI", 
				getFieldEditorParent());
		addField(serverURI);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			boolean standalone = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_STANDALONE);
			serverURI.setEnabled(standalone, getFieldEditorParent());
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			if (event.getSource() == operationMode) {
				change on property change
				boolean standalone = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_STANDALONE);
				serverURI.setEnabled(standalone, getFieldEditorParent());
			}
			checkState();
		}
	}
	
	private void createRadioGroup() {
		operationMode = new RadioGroupFieldEditor(PreferenceConstants.OPERATION_MODE, 
				"Betriebsmodus",
				1,
				new String[][] {
				{"Standalone", PreferenceConstants.OPERATION_MODE_STANDALONE},
				{"Mehrbenutzer", PreferenceConstants.OPERATION_MODE_WITHSERVER}
		}, getFieldEditorParent());
		addField(operationMode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}
