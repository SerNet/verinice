package sernet.gs.ui.rcp.main.preferences;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
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
public class GSImportPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public static final String ID = "sernet.gs.ui.rcp.main.page5";

	private RadioGroupFieldEditor dbDriver;
	private StringFieldEditor dialect;
	private StringFieldEditor url;
	private StringFieldEditor user;
	private StringFieldEditor pass;


	private boolean showWindowsWarning = true;
	


	private static final String TEST_QUERY = "select top 1 * from N_Zielobjekt";

	public GSImportPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("\nHier können Sie eine bestehende GSTOOL\u2122-Datenbank importieren. " +
				"Verinice unterstützt drei Möglichkeiten für den Import: direkt aus der GSTOOL-Datenbank," +
				" aus einer exportierten .MDB Datei oder aus einer Datenbanksicherung (.MDF-Datei)."
				//+ " Eine genauere Anleitung finden Sie unter Hilfe -> Spickzettel -> GSTOOL-Import");
				);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		
		

		url = new StringFieldEditor(PreferenceConstants.GS_DB_URL,
				"GSTOOL DB JDBC URL", //$NON-NLS-1$
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

		
		createTestButton();

	}


	private void createTestButton() {
		Button button = new Button((Composite) getControl(), SWT.PUSH);
		button.setText(Messages.GSImportPreferencePage_0);
		button.setLayoutData(new GridData(GridData.END, GridData.BEGINNING,
				true, true));
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final String urlString = url.getStringValue();
				final String userString = user.getStringValue();
				final String passString = pass.getStringValue();

				WorkspaceJob job = new WorkspaceJob(
						Messages.GSImportPreferencePage_2) {

					public IStatus runInWorkspace(final IProgressMonitor monitor) {
						monitor.beginTask(Messages.GSImportPreferencePage_1,
								IProgressMonitor.UNKNOWN);
						monitor.setTaskName(Messages.GSImportPreferencePage_1);
						try {
							Class.forName("net.sourceforge.jtds.jdbc.Driver"); //$NON-NLS-1$
							Connection con = DriverManager.getConnection(
									urlString, userString, passString);
							Statement stmt = con.createStatement();
							stmt.executeQuery(TEST_QUERY); //$NON-NLS-1$
							stmt.close();
							con.close();

							// success:
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(getShell(),
											Messages.GSImportPreferencePage_5,
											Messages.GSImportPreferencePage_6);
								}
							});
						} catch (Exception e1) {
							if (e1.getMessage().indexOf("N_Zielobj") > -1) {
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										MessageDialog.openInformation(getShell(), "Soweit so gut", 
												"Die Verbindung wurde erfolgreich mit der Datenbank hergestellt. In dieser Datenbank wurden keine GSTOOL-Daten gefunden. Sie können eine der beiden Importfunktionen" +
												" nutzen, um Daten von externen Quellen zu importieren. Sie finden diese hier in den Einstellungen " +
										"wenn Sie den Knoten 'GSTool Import' aufklappen.");
									}
								});
							}
							else {
								ExceptionUtil.log(e1,
										Messages.GSImportPreferencePage_7
										+ urlString);
							}
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
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
