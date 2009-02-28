package sernet.gs.ui.rcp.main.preferences;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;

import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.ui.rcp.gsimport.AttachDbFileTask;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.views.Messages;

/**
 * GS Tool Import database settings
 * 
 * @author akoderman@sernet.de
 * 
 */
public class GSImportAttachPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public static final String ID = "sernet.gs.ui.rcp.main.page6";

	private FileFieldEditor gstoolDumpFile;

	private boolean showWarning = true;
	
	private StringFieldEditor attachDb;



	public GSImportAttachPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Anhängen einer exportierten GSTOOL Datenbank im .MDF Format für den Import.");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		

		gstoolDumpFile = new FileFieldEditor(PreferenceConstants.GSTOOL_FILE,
				"GSTOOL-Datei anhängen", getFieldEditorParent()) {
			public boolean isValid() {
				if (gstoolDumpFile.getStringValue() != null
						&& gstoolDumpFile.getStringValue().length() > 0) {

					String url = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.GS_DB_URL);
					if (showWarning && !(url.indexOf("localhost") > -1 || url
							.indexOf("127.0.0.1") > -1)) {
						showWarning = false;
						MessageDialog
								.openWarning(
										getShell(),
										"Anzuhängende Datei",
										"Sie haben eine Datei zum Anhängen angegeben. Der Datenbankserver läuft "
												+ "auf einem entfernten Rechner. Bitte beachten Sie, dass sich die anzuhängende Datei auf dem "
												+ "SERVER befinden muss! Das heißt, der hier eingestellte Pfad muss vom Datenbank-Server aus erreichbar sein!");
					}
				}

				// always return true, file may be on different host and entered
				// manually
				return true;
			}
		};
		
		// set dbName to filename as default:
		gstoolDumpFile.getTextControl(getFieldEditorParent()).addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				Pattern pat = Pattern.compile("[/\\\\](\\w*?)(.MDF|.mdf)");
				Matcher matcher = pat.matcher(gstoolDumpFile
						.getStringValue());
				if (matcher.find()) {
					String dbName = matcher.group(1);
					attachDb.setStringValue(dbName);
				}
			}
		});
		
		gstoolDumpFile.setFileExtensions(new String[] { "*.MDF;*.mdf", "*.*" });
		addField(gstoolDumpFile);

		attachDb = new StringFieldEditor(PreferenceConstants.GS_DB_ATTACHDB,
				"Datenbank anhängen als Name", getFieldEditorParent());
		addField(attachDb);

		createAttachButton();

	}

	private void createAttachButton() {
		Button button = new Button((Composite) getControl(), SWT.PUSH);
		button.setText("Datenbank anhängen");
		button.setLayoutData(new GridData(GridData.END, GridData.BEGINNING,
				true, true));
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String url = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.GS_DB_URL);
				Pattern pat = Pattern.compile("/(\\w*?)$");
				Matcher matcher = pat.matcher(url);
				boolean found = matcher.find();
				if (!found) {
					Logger.getLogger(this.getClass()).debug(
							"No database defined.");
					return;
				}
				String oldDbName = matcher.group(1);

				final String urlString = url.replace(oldDbName, "master");
				final String userString = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.GS_DB_USER);
				final String passString = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.GS_DB_PASS);
				final String fileName = gstoolDumpFile.getStringValue();
				final String newDbName = attachDb.getStringValue();

				boolean doIt = MessageDialog
						.openConfirm(
								getShell(),
								"Datenbank anhängen",
								"Verbindung wird hergestellt mit "
										+ urlString
										+ "\n\nDie Datei '"
										+ fileName
										+ "' wird angehängt als Datenbank '"
										+ newDbName
										+ "'.\nDiese Datenbank sollte nicht existieren, alle evtl. existierenden Daten werden überschrieben."
										+ "\n\nWollen Sie fortfahren?");
				if (!doIt)
					return;

				try {
					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
							new IRunnableWithProgress() {
								public void run(IProgressMonitor monitor)
										throws InvocationTargetException,
										InterruptedException {
									
									AttachDbFileTask task = new AttachDbFileTask();
									try {
										task.attachDBFile(urlString, userString,
												passString, fileName, newDbName);
										
										Display.getDefault().syncExec(new Runnable() {
											public void run() {
												MessageDialog
												.openInformation(
														getShell(),
														"Hurra",
														"Die Datenbank wurde angehängt. Probieren Sie nun die " +
														"Verbindung zur Datenbank '"
														+ newDbName	+ "' zu testen.");
											}
										});
									} catch (SQLException e) {
										ExceptionUtil.log(e,
												"Konnte Datenbankdatei nicht anhängen "
												+ fileName);
									} catch (ClassNotFoundException e) {
										ExceptionUtil.log(e,
												"Konnte Datenbankdatei nicht anhängen "
												+ fileName);
									}
								}
							});
				} catch (InvocationTargetException e1) {
					ExceptionUtil.log(e1, "Fehler beim Anhängen der DB");
				} catch (InterruptedException e1) {
					ExceptionUtil.log(e1, "Fehler beim Anhängen der DB");
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
