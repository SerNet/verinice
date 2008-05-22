package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.ChooseGefaehrdungPage.GefaehrdungenFilter;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.ChooseGefaehrdungPage.OwnGefaehrdungenFilter;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.ChooseGefaehrdungPage.SearchFilter;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;


/**
 * WizardPage - pick the Gefährdungen, which need processing.
 * 
 * @author ahanekop@sernet.de
 *
 */

public class EstimateGefaehrdungPage extends WizardPage {

	private Composite container;
	private TableColumn checkboxColumn;
	private TableColumn imgColumn;
	private TableColumn numberColumn;
	private TableColumn nameColumn;
	private TableColumn descrColumn;
	private CheckboxTableViewer viewer;
	private OwnGefaehrdungenFilter ownGefaehrdungFilter = new OwnGefaehrdungenFilter();
	private GefaehrdungenFilter gefaehrdungFilter = new GefaehrdungenFilter();
	private SearchFilter searchFilter = new SearchFilter();
	
	protected EstimateGefaehrdungPage() {
		super("Gefährdungsbewertung");
		setTitle("Gefährdungsbewertung");
		setDescription("Wählen Sie die Gefährdungen aus, denen NICHT ausreichend Rechnung getragen wurde.");
	}
	
	/* must be implemented - content of wizard page! */
	public void createControl(Composite parent) {

		container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		container.setLayout(gridLayout);
		setControl(container);

		/* table viewer */
		viewer = CheckboxTableViewer.newCheckList(
		        container, SWT.BORDER);
		final Table table = viewer.getTable();
		
		GridData data1 = new GridData();
	    data1.grabExcessHorizontalSpace = true;
	    data1.grabExcessVerticalSpace = true;
	    data1.horizontalAlignment = SWT.FILL;
	    data1.verticalAlignment = SWT.FILL;
	    table.setLayoutData(data1);
		
	    table.setHeaderVisible(true);
		table.setLinesVisible(true);
	    
		checkboxColumn = new TableColumn(table, SWT.LEFT);
		checkboxColumn.setText("");
		checkboxColumn.setWidth(35);
		
		imgColumn = new TableColumn(table, SWT.LEFT);
		imgColumn.setText("");
		imgColumn.setWidth(35);
		
		numberColumn = new TableColumn(table, SWT.LEFT);
		numberColumn.setText("Nummer");
		numberColumn.setWidth(100);
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(100);
		
		descrColumn = new TableColumn(table, SWT.LEFT);
		descrColumn.setText("Beschreibung");
		descrColumn.setWidth(200);
		
		/* dies sollte überflüssig sein */
		viewer.setColumnProperties(new String[] {
				"_checkbox",
				"_img",
				"_number",
				"_name",
				"_descr"
		});

		/* group the Filter checkboxes with composite */
		Composite compositeFilter = new Composite(container, SWT.NULL);
		GridLayout gridLayoutFilters = new GridLayout();
        gridLayoutFilters.numColumns = 2;
        compositeFilter.setLayout(gridLayoutFilters);
        GridData data6 = new GridData();
        data6.horizontalAlignment = SWT.LEFT;
        data6.verticalAlignment = SWT.TOP;
	    compositeFilter.setLayoutData(data6);
	    
	    /* filter button - OwnGefaehrdungen only */
	    Button button5 = new Button(compositeFilter, SWT.CHECK);
	    button5.setText("nur eigene Gefährdungen anzeigen");
	    GridData data7 = new GridData();
	    data7.horizontalSpan = 2;
	    button5.setLayoutData(data7);
	    button5.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		Button thisButton = (Button) event.widget;
	    		if(thisButton.getSelection()){
	    			viewer.addFilter(ownGefaehrdungFilter);
	    			viewer.refresh();
	    		} else {
	    			viewer.removeFilter(ownGefaehrdungFilter);
	    			viewer.refresh();
	    		}
	    	}

	    });
	    
	    /* filter button - Gefaehrdungen only */
	    Button button6 = new Button(compositeFilter, SWT.CHECK);
	    button6.setText("nur BSI Gefährdungen anzeigen");
	    GridData data8 = new GridData();
	    data8.horizontalSpan = 2;
	    button6.setLayoutData(data8);
	    button6.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		Button thisButton = (Button) event.widget;
	    		if(thisButton.getSelection()){
	    			viewer.addFilter(gefaehrdungFilter);
	    			viewer.refresh();
	    		} else {
	    			viewer.removeFilter(gefaehrdungFilter);
	    			viewer.refresh();
	    		}
	    	}

	    });
	    
	    /* filter button - search */
	    new Label(compositeFilter, SWT.NULL).setText("suche:");
	    Text search = new Text(compositeFilter, SWT.SINGLE | SWT.BORDER);
	    GridData data9 = new GridData();
	    data9.horizontalAlignment = SWT.FILL;
	    search.setLayoutData(data9);
	    
	    search.addModifyListener(new ModifyListener(){
	    	public void modifyText(ModifyEvent event) {
	    		Text text = (Text) event.widget;
	    		if (text.getText().length() > 0) {
	    			searchFilter.setPattern(text.getText());
	    			viewer.addFilter(searchFilter);
	    			viewer.refresh();
	    		} else {
	    			viewer.removeFilter(searchFilter);
	    			viewer.refresh();
	    		}
	    	}
	    });

	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			initContents();
		}
	}

	/**
	 * fills the CheckboxTableViewer with all Gefaehrdungen available
	 */
	private void initContents() {
		ArrayList<Gefaehrdung> selectedArrayList = ((RisikoanalyseWizard)getWizard()).getAssociatedGefaehrdungen();
		
		/* map a domain model object into multiple images and text labels */
		viewer.setLabelProvider(new CheckboxTableViewerLabelProvider());
		/* map domain model into array */
		viewer.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewer.setInput(selectedArrayList);
		
		viewer.setSorter(new GefaehrdungenSorter());
		
		/**
		 * listener adds/removes Gefaehrdungen to Arrays of Gefaehrdungen
		 */
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Gefaehrdung currentGefaehrdung = (Gefaehrdung) event
						.getElement();
				ArrayList<Gefaehrdung> notOKArrayList = ((RisikoanalyseWizard) getWizard())
						.getNotOKGefaehrdungen();
				ArrayList<GefaehrdungsUmsetzung> umsetzungen = ((RisikoanalyseWizard) getWizard())
						.getGefaehrdungsUmsetzungen();

				// TODO statt contains: auf gleiche ID überprüfen
				// -> vergl. ChooseGefaehrdungPage
				if (event.getChecked()) {
					/* in liste notOkay hinzufügen */
					if (!notOKArrayList.contains(currentGefaehrdung)) {
						notOKArrayList.add(currentGefaehrdung);
					}
					/* in Liste der Umsetzungen hinzufügen */
					GefaehrdungsUmsetzung newGefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(
							currentGefaehrdung);
					newGefaehrdungsUmsetzung.setOkay(false);
					umsetzungen.add(newGefaehrdungsUmsetzung);
					/*
					 * for (GefaehrdungsUmsetzung gefaehrdung : umsetzungen) {
					 * if (currentGefaehrdung.getId().equals(
					 * gefaehrdung.getId())) { gefaehrdung.setOkay(false);
					 * break; } }
					 */
				} else {
					/* aus Liste der Umsetzungen entfernen */
					for (GefaehrdungsUmsetzung gefaehrdung : umsetzungen) {
						// Logger.getLogger(this.getClass()).debug("test");
						if (currentGefaehrdung.getId().equals(
								gefaehrdung.getId())) {
							gefaehrdung.setOkay(true);
							umsetzungen.remove(gefaehrdung);
							break;
						}
					}

					((RisikoanalyseWizard) getWizard())
							.setGefaehrdungsUmsetzungen(umsetzungen);
					/*
					 * for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung :
					 * umsetzungen) { if
					 * (gefaehrdungsUmsetzung.getId().equals(currentGefaehrdung.getId())) {
					 * umsetzungen.remove(gefaehrdungsUmsetzung); // TODO
					 * klären, ob gefaehrdungsUmsetzung dispose()d werden
					 * kann/muss break; } }
					 */
					/* aus liste notOkay entferen */
					notOKArrayList.remove(currentGefaehrdung);
				}

				checkPageComplete();
			}
		});
		
		packAllColumns();
		
		// vs. overriding method WizardPage.canFilpToNextPage
		checkPageComplete();
	}
	
	/**
	 * packs all columns of Table containing Gefaehrdungen
	 */
	private void packAllColumns() {
		checkboxColumn.pack();
		imgColumn.pack();
		numberColumn.pack();
		nameColumn.pack();
		descrColumn.pack();
	}
	
	private void checkPageComplete() {
		if (((RisikoanalyseWizard)getWizard()).getNotOKGefaehrdungen().isEmpty()) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}
	
	class OwnGefaehrdungenFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof OwnGefaehrdung)
				return true;
			return false;
		}
	}
	
	class GefaehrdungenFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (!(element instanceof OwnGefaehrdung)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	class SearchFilter extends ViewerFilter {
		private Pattern pattern; 
		
		void setPattern(String searchString) {
			pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
		}
		
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			Gefaehrdung gefaehrdung = (Gefaehrdung) element;
			String gefTitle = gefaehrdung.getTitel();
			Matcher matcher = pattern.matcher(gefTitle);
			
			if (matcher.find()) {
				return true;
			} else {
				return false;
			}
		}
	}
}