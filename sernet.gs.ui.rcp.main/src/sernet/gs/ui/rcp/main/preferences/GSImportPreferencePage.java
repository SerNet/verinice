/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;

/**
 * GS Tool Import database settings
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class GSImportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final Logger log = Logger.getLogger(GSImportRestorePreferencePage.class);

	public static final String ID = "sernet.gs.ui.rcp.main.page5"; //$NON-NLS-1$

	private StringFieldEditor url;
	private StringFieldEditor user;
	private StringFieldEditor pass;

	private static final String TEST_QUERY = "select top 1 * from N_Zielobjekt"; //$NON-NLS-1$

	public GSImportPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("GSImportPreferencePage_9")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createContents
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		final Link link = new Link(parent, SWT.NONE);
		link.setText(Messages.getString("GSImportPreferencePage_10")); //$NON-NLS-1$
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Program.launch(event.text);
			}

		});
		return super.createContents(parent);
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {

		url = new StringFieldEditor(PreferenceConstants.GS_DB_URL, Messages.getString("GSImportPreferencePage.17"),  //$NON-NLS-1$
				getFieldEditorParent());
		addField(url);

		user = new StringFieldEditor(PreferenceConstants.GS_DB_USER, Messages.getString("GSImportPreferencePage.18"),  //$NON-NLS-1$
				getFieldEditorParent());
		addField(user);

		pass = new StringFieldEditor(PreferenceConstants.GS_DB_PASS, Messages.getString("GSImportPreferencePage.19"),  //$NON-NLS-1$
				getFieldEditorParent());
		addField(pass);

		createTestButton();

	}

	private void createTestButton() {
		Button button = new Button((Composite) getControl(), SWT.PUSH);
		button.setText(Messages.getString("GSImportPreferencePage_0")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, true));
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final String urlString = url.getStringValue();
				final String userString = user.getStringValue();
				final String passString = pass.getStringValue();

				WorkspaceJob job = new WorkspaceJob(Messages.getString("GSImportPreferencePage_2")) { //$NON-NLS-1$

					@Override
					public IStatus runInWorkspace(final IProgressMonitor monitor) {

						monitor.beginTask(Messages.getString("GSImportPreferencePage_1"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
						monitor.setTaskName(Messages.getString("GSImportPreferencePage_1")); //$NON-NLS-1$
						try {
							log.debug("Loading MSSQL JDBC driver."); //$NON-NLS-1$
							Class.forName("net.sourceforge.jtds.jdbc.Driver"); //$NON-NLS-1$
							log.debug("Establishing database connection"); //$NON-NLS-1$
							Connection con = DriverManager.getConnection(urlString, userString, passString);
							log.debug("Running test query."); //$NON-NLS-1$
							Statement stmt = con.createStatement();
							stmt.executeQuery(TEST_QUERY);
							stmt.close();
							con.close();
							log.debug("Finished MSSQL connection test."); //$NON-NLS-1$

							// success:
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(getShell(), Messages.getString("GSImportPreferencePage_5"), Messages.getString("GSImportPreferencePage_6")); //$NON-NLS-1$ //$NON-NLS-2$
								}
							});
						} catch (Exception e1) {
							if (e1.getMessage().indexOf("N_Zielobj") > -1) { //$NON-NLS-1$
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										MessageDialog.openInformation(getShell(), Messages.getString("GSImportPreferencePage_15"), Messages.getString("GSImportPreferencePage_16")); //$NON-NLS-1$ //$NON-NLS-2$
									}
								});
							} else {
								ExceptionUtil.log(e1, Messages.getString("GSImportPreferencePage_7") + urlString); //$NON-NLS-1$
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
		if (!isValid()) {
			return;
		}
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
