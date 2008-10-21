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
		setDescription(Messages.getString("GeneralSettingsPage.0")); //$NON-NLS-1$
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		derbyWarning = new BooleanFieldEditor(PreferenceConstants.FIRSTSTART,
				Messages.getString("GeneralSettingsPage.1"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(derbyWarning);
		
		errorPopups = new BooleanFieldEditor(PreferenceConstants.ERRORPOPUPS, 
				Messages.getString("GeneralSettingsPage.2"), //$NON-NLS-1$
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