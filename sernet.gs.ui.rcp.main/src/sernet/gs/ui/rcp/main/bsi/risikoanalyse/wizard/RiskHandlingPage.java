/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah[at]sernet[dot]de>
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
 *     Anne Hanekop <ah[at]sernet[dot]de> 	- initial API and implementation
 *     ak[at]sernet[dot]de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.model.Gefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;

/**
 * Choose an alternative, how to deal with the Gefaerdungen.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class RiskHandlingPage extends WizardPage {

	private Composite composite;
	private TableColumn imgColumn;
	private TableColumn numberColumn;
	private TableColumn nameColumn;
	private TableColumn choiceColumn;
	private TableViewer viewer;
	private SearchFilter searchFilter = new SearchFilter();
	public static final String IMG_COLUMN_ID = "image"; //$NON-NLS-1$
	public static final String NUMBER_COLUMN_ID = "number"; //$NON-NLS-1$
	public static final String NAME_COLUMN_ID = "name"; //$NON-NLS-1$
	public static final String CHOICE_COLUMN_ID = "choice"; //$NON-NLS-1$

	/**
	 * Constructor sets title an description of WizardPage.
	 */
	protected RiskHandlingPage() {
		super(Messages.RiskHandlingPage_4);
		setTitle(Messages.RiskHandlingPage_5);
		setDescription(Messages.RiskHandlingPage_6);
	}
	
	/**
	 * Adds widgets to the wizardPage.
	 * Called once at startup of Wizard.
	 * 
	 *  @param parent the parent Composite
	 */
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);
		setControl(composite);

		/* TableViewer */
		viewer = new TableViewer(composite, SWT.FULL_SELECTION);
		final Table table = viewer.getTable();
		GridData gridTable = new GridData();
	    gridTable.grabExcessHorizontalSpace = true;
	    gridTable.grabExcessVerticalSpace = true;
	    gridTable.horizontalSpan = 2;
	    gridTable.horizontalAlignment = SWT.FILL;
	    gridTable.verticalAlignment = SWT.FILL;
	    table.setLayoutData(gridTable);
	    table.setHeaderVisible(true);
		table.setLinesVisible(true);
	    
		imgColumn = new TableColumn(table, SWT.LEFT);
		imgColumn.setText(""); //$NON-NLS-1$
		imgColumn.setWidth(35);
		
		numberColumn = new TableColumn(table, SWT.LEFT);
		numberColumn.setText(Messages.RiskHandlingPage_8);
		numberColumn.setWidth(100);
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText(Messages.RiskHandlingPage_9);
		nameColumn.setWidth(100);
		
		choiceColumn = new TableColumn(table, SWT.LEFT);
		choiceColumn.setText(Messages.RiskHandlingPage_10);
		choiceColumn.setWidth(200);
		
		/* needed for PropertiesComboBoxCellModifier */
		viewer.setColumnProperties(new String[] {
				IMG_COLUMN_ID,
				NUMBER_COLUMN_ID,
				NAME_COLUMN_ID,
				CHOICE_COLUMN_ID
		});
		
		final ComboBoxCellEditor choiceEditor = new ComboBoxCellEditor(table, 
				GefaehrdungsUmsetzung.ALTERNATIVEN_TEXT, 
				SWT.READ_ONLY);
		choiceEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		
		// allow modification of risk treatment:
	    viewer.setCellEditors(new CellEditor[] {null,null,null,choiceEditor});
	    viewer.setCellModifier(new PropertiesComboBoxCellModifier(viewer, (RiskAnalysisWizard)getWizard(), this));
	    
		/* group the Filter checkboxes with composite */
		Composite compositeFilter = new Composite(composite, SWT.NULL);
		GridLayout gridLayoutFilters = new GridLayout();
        gridLayoutFilters.numColumns = 2;
        compositeFilter.setLayout(gridLayoutFilters);
        GridData gridCompositeFilter = new GridData();
        gridCompositeFilter.horizontalAlignment = SWT.LEFT;
        gridCompositeFilter.verticalAlignment = SWT.TOP;
	    compositeFilter.setLayoutData(gridCompositeFilter);
	    
	    /* filter button - search */
	    new Label(compositeFilter, SWT.NULL).setText(Messages.RiskHandlingPage_11);
	    Text search = new Text(compositeFilter, SWT.SINGLE | SWT.BORDER);
	    GridData gridSearch = new GridData();
	    gridSearch.horizontalAlignment = SWT.FILL;
	    search.setLayoutData(gridSearch);
	    
	    /* Listener adds/removes Filter searchFilter */
	    search.addModifyListener(new ModifyListener(){
	    	
	    	/**
			 * Adds/removes Filter when Text is modified depending on event.
			 * 
			 * @param event event containing information about the selection
			 */
	    	public void modifyText(ModifyEvent event) {
				Text text = (Text) event.widget;
				if (text.getText().length() > 0) {
					
					ViewerFilter[] filters = viewer.getFilters();
					SearchFilter thisFilter = null;
					boolean contains = false;
					
					for (ViewerFilter item : filters) {
						if (item instanceof SearchFilter) {
							contains = true;
							thisFilter = (SearchFilter) item;
						}
					}
					if (contains) {
						/* filter is already active - update filter */
						thisFilter.setPattern(text.getText());
						viewer.refresh();
						
					} else {
						/* filter is not active - add */
						searchFilter.setPattern(text.getText());
						viewer.addFilter(searchFilter);
						viewer.refresh();
					}
				} else {
					viewer.removeFilter(searchFilter);
					viewer.refresh();
					packAllColumns();
				}
			}
	    });
	}
	
	/**
	 * Sets the control to the given visibility state.
	 * 
	 * @param visible boolean indicating if content should be visible
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			initContents();
		}
	}

	/**
	 * Fills the TableViewer with all previously selected
	 * Gefaehrdungen in order to choose an alternate
	 * proceeding.
	 * Is processed each time the WizardPage is set visible.
	 */
	private void initContents() {
		List<GefaehrdungsUmsetzung> arrListAllGefaehrdungsUmsetzungen = 
			((RiskAnalysisWizard)getWizard()).getAllGefaehrdungsUmsetzungen();

		/* map a domain model object into multiple images and text labels */
		viewer.setLabelProvider(new TableViewerLabelProvider());
		/* map domain model into array */
		viewer.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewer.setInput(arrListAllGefaehrdungsUmsetzungen);
		/* sort elements */
		viewer.setSorter(new GefaehrdungenSorter());
		packAllColumns();
		
		/* Wizard may be finished at this page */
		((RiskAnalysisWizard)getWizard()).setCanFinish(true);
		
		checkPageComplete();
	}

	/**
	 * Adjusts all columns of the TableViewer.
	 */
	private void packAllColumns() {
		imgColumn.pack();
		numberColumn.pack();
		nameColumn.pack();
		choiceColumn.pack();
	}
	
	/**
	 * Activates the next button, if any of the Gefaehrdungen's alternative is "A" .
	 */
	private void checkPageComplete() {
		List<GefaehrdungsUmsetzung> arrListAllGefaehrdungsUmsetzungen = 
			((RiskAnalysisWizard)getWizard()).getAllGefaehrdungsUmsetzungen();
		Boolean complete = false;
		
		/* setPageComplete(false) if no GefaehrdungsUmsetzung is of alternative "A" */
		for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : arrListAllGefaehrdungsUmsetzungen) {
			if (gefaehrdungsUmsetzung.getAlternative().equals("A")) { //$NON-NLS-1$
				complete = true;
				break;
			}
		}
		
		if (complete) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}
	
	/**
	 * Filter to extract all GefaehrdungsUmsetzungen matching a given String. 
	 *
	 * @author ahanekop[at]sernet[dot]de
	 */
	class SearchFilter extends ViewerFilter {
		
		private Pattern pattern; 
		
		/**
		 * Updates the Pattern.
		 * 
		 * @param searchString the String to search for
		 */
		void setPattern(String searchString) {
			pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
		}
		
		/**
		 * Selects all GefaehrdungsUmsetzungen matching the Pattern.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			Gefaehrdung gefaehrdung = (Gefaehrdung) element;
			String gefaehrdungTitle = gefaehrdung.getTitel();
			Matcher matcher = pattern.matcher(gefaehrdungTitle);
			
			if (matcher.find()) {
				return true;
			} else {
				return false;
			}
		}
	}
}
