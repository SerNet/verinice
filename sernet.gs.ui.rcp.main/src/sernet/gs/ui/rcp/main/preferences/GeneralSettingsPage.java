package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import sernet.gs.ui.rcp.main.Activator;

/**
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman@sernet.de
 *
 */
public class GeneralSettingsPage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {


	private BooleanFieldEditor errorPopups;
	private BooleanFieldEditor derbyWarning;

	public GeneralSettingsPage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Allgemeine Einstellungen für das CnA Tool");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		derbyWarning = new BooleanFieldEditor(PreferenceConstants.FIRSTSTART,
				"Hinweise beim ersten Start erneut anzeigen.",
				getFieldEditorParent());
		addField(derbyWarning);
		
		errorPopups = new BooleanFieldEditor(PreferenceConstants.ERRORPOPUPS, 
				"Auftretende Fehler als Popup anzeigen (sonst nur im Log-File)",
				getFieldEditorParent());
		addField(errorPopups);
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE))
			checkState();
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