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
package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.StatusLine;
import sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.reports.ISMReport;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnATreeElementTitles;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.iso27k.Organization;

/**
 * Wizard page to allow the user to choose the ITVerbund or organization
 * for the report about to be created.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class ChooseRootObjectPage extends WizardPage {


	private Combo itverbundCombo;
	private List<ITVerbund> itverbuende;
	private List<Organization> scopes;
	private ITVerbund selectedITVerbund = null;
	private Organization selectedScope = null;
	private Combo scopeCombo;
	
	public Organization getSelectedOrganization() {
		return selectedScope;
	}


	public ITVerbund getSelectedITVerbund() {
		return selectedITVerbund;
	}


	protected ChooseRootObjectPage() {
		super("chooseITVerbund");
		setTitle("Geltungsbereich auswählen");
		setDescription("Wählen Sie den Informationsverbund oder den Scope, für den der Report erstellt werden soll.");
	}
	
	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		setControl(container);

		final Label label2 = new Label(container, SWT.NULL);
		GridData gridData7 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		label2.setLayoutData(gridData7);
		label2.setText("Informationsverbund:");

		itverbundCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		itverbundCombo.setEnabled(false);
		
		itverbundCombo.addSelectionListener(new SelectionListener() {


			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				int s = itverbundCombo.getSelectionIndex();
				selectedITVerbund = itverbuende.get(s);
				updatePageComplete();
			}

		});
		
		final Label label3 = new Label(container, SWT.NULL);
		GridData gridData8 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		label3.setLayoutData(gridData8);
		label3.setText("Scope:");

		scopeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		scopeCombo.setEnabled(false);
		
		scopeCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				int s = scopeCombo.getSelectionIndex();
				selectedScope = scopes.get(s);
				updatePageComplete();
			}

		});

		loadITVerbuende();
		loadScopes();
	}

	private void loadScopes() {
		LoadCnATreeElementTitles<Organization> compoundLoader = new LoadCnATreeElementTitles<Organization>(
				Organization.class);
		try {
			compoundLoader = ServiceFactory.lookupCommandService()
					.executeCommand(compoundLoader);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Laden der Scopes.");
		}
		
		this.scopes = compoundLoader
				.getElements();
		
			scopeCombo.removeAll();

			for (Organization c : scopes)
				scopeCombo.add(c.getTitle());
			scopeCombo.setEnabled(true);
			scopeCombo.pack();
	
	}


	/**
	 * 
	 */
	private void loadITVerbuende() {
		LoadCnATreeElementTitles<ITVerbund> compoundLoader = new LoadCnATreeElementTitles<ITVerbund>(
				ITVerbund.class);
		try {
			compoundLoader = ServiceFactory.lookupCommandService()
					.executeCommand(compoundLoader);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Laden der IT-Verbunde");
		}
		
		this.itverbuende = compoundLoader
				.getElements();
		
			itverbundCombo.removeAll();

			for (ITVerbund c : itverbuende)
				itverbundCombo.add(c.getTitle());
			itverbundCombo.setEnabled(true);
			itverbundCombo.pack();
	}


	ExportWizard getExportWizard() {
		return ((ExportWizard) getWizard());
		
	}
	
	

	private void updatePageComplete() {
		disableWrongCombo();
		boolean complete = selectedITVerbund != null || selectedScope != null;
		if ( !complete) {
			setMessage(null);
			setPageComplete(false);
			return;
		}
		
		setPageComplete(true);
	}

	
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		updatePageComplete();
	}


	private void disableWrongCombo() {
		if (getExportWizard().getReport() instanceof ISMReport) {
			scopeCombo.setEnabled(true);
			itverbundCombo.setEnabled(false);
		}
		else {
			itverbundCombo.setEnabled(true);
			scopeCombo.setEnabled(false);
		}
	}
	
}
