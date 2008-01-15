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
public class ReportPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {


	private DirectoryFieldEditor ooDir;
	private FileFieldEditor ooTemplate;
	private FileFieldEditor ooDocumentTemplate;

	public ReportPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Hier konfigurieren Sie die Schnittstelle zu OpenOffice," +
				" um Reports und die vom BSI geforderten Referenzdokumnte zu erstellen. ");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		ooDir = new DirectoryFieldEditor(PreferenceConstants.OODIR,
				"Pfad zu OpenOffice)",
				getFieldEditorParent());
		addField(ooDir);
		
		ooTemplate = new FileFieldEditor(PreferenceConstants.OOTEMPLATE, 
				"OO Calc Vorlagendatei",
				getFieldEditorParent());
		addField(ooTemplate);

		ooDocumentTemplate = new FileFieldEditor(PreferenceConstants.OOTEMPLATE_TEXT, 
				"OO Writer Vorlagendatei",
				getFieldEditorParent());
		addField(ooDocumentTemplate);
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
		
		setValid(true);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}