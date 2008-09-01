package sernet.gs.ui.rcp.main.preferences;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import sernet.gs.scraper.ZIPGSSource;
import sernet.gs.ui.rcp.main.Activator;

/**
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman@sernet.de
 *
 */
public class KatalogePreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private StringFieldEditor zipfilePath;
	private StringFieldEditor datenschutzZipPath;
	private RadioGroupFieldEditor gsAccessMethod;
	private DirectoryFieldEditor bsiUrl;

	public KatalogePreferencePage() {
		super(GRID);
		
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Hier konfigurieren Sie die Datenquelle für die" +
				"Grundschutz-Kataloge des BSI. Sie können diese kostenlos von der Webseite" +
				" des BSI downloaden. " +
				"Tragen Sie dann den Speicherort der ZIP-Datei ein, " +
				"oder das Verzeichnis mit den entpackten HTML-Dateien.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		gsAccessMethod = new RadioGroupFieldEditor(PreferenceConstants.GSACCESS,
				"Datenquelle für GS-Kataloge",
				1,
				new String[][] {
					{"Ordner", PreferenceConstants.GSACCESS_DIR}, 
					{"ZIP-File", PreferenceConstants.GSACCESS_ZIP}
				},
				getFieldEditorParent());
		addField(gsAccessMethod);
		
		bsiUrl = new DirectoryFieldEditor(PreferenceConstants.BSIDIR,
				"Verzeichnis mit GS-Katalogen (HTML-Format)",
				getFieldEditorParent());
		addField(bsiUrl);
		
		zipfilePath = new FileFieldEditor(PreferenceConstants.BSIZIPFILE, 
				"ZIP-Datei mit GS-Katalogen",
				getFieldEditorParent());
		addField(zipfilePath);

		datenschutzZipPath = new FileFieldEditor(PreferenceConstants.DSZIPFILE, 
				"ZIP-Datei mit Datenschutzbaustein",
				getFieldEditorParent());
		addField(datenschutzZipPath);
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