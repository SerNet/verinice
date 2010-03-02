/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
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
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnATreeElementTitles;

/**
 * Wizard page to allow the user to choose the ITVerbund for the report about to be created.
 * 
 * @author koderman@sernet.de
 *
 */
public class ChooseITVerbundPage extends WizardPage {



	
	
	
	private Combo itverbundCombo;
	private List<ITVerbund> itverbuende;
	private ITVerbund selectedITVerbund = null;
	
	public ITVerbund getSelectedITVerbund() {
		return selectedITVerbund;
	}


	protected ChooseITVerbundPage() {
		super("chooseITVerbund");
		setTitle("Informationsverbund auswählen");
		setDescription("Wählen Sie den Informationsverbund, für den der Report erstellt werden soll.");
	}
	
	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
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

		loadITVerbuende();
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
		boolean complete = selectedITVerbund != null;
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
	
}
