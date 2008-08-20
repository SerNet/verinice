package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;


/**
 * WizardPage - pick the Gefährdungen, which need processing.
 * 
 * @author ahanekop@sernet.de
 *
 */

public class ChooseGefaehrdungPage extends WizardPage {

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
	
	protected ChooseGefaehrdungPage() {
		super("Gefährdungsübersicht");
		setTitle("Gefährdungsübersicht");
		setDescription("Wählen Sie die Gefährdungen aus, die behandelt werden sollen.");
	}
	
	/* must be implemented - content of wizard page! */
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		
		/* TODO brauche ich das control (s.u.)? */
		setControl(container);

		/* table viewer */
		viewer = CheckboxTableViewer.newCheckList(
		        container, SWT.BORDER);
		
		
		
		final Table table = viewer.getTable();
		
		GridData data1 = new GridData();
	    data1.grabExcessHorizontalSpace = true;
	    data1.grabExcessVerticalSpace = true;
	    data1.horizontalSpan = 2;
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
		
		
		/**
		 *  listener adds/removes Gefaehrdungen to Array of selected Gefaehrdungen
		 */
		viewer.addCheckStateListener(new ICheckStateListener() {
		    public void checkStateChanged(CheckStateChangedEvent event) {
		    	Gefaehrdung currentGefaehrdung = (Gefaehrdung) event.getElement();
		    	ArrayList<Gefaehrdung> selectedArrayList = 
		    		((RisikoanalyseWizard)getWizard()).getAssociatedGefaehrdungen();
		    	ArrayList<GefaehrdungsUmsetzung> umsetzungenArrayList = 
		    		((RisikoanalyseWizard)getWizard()).getGefaehrdungsUmsetzungen();
		    	
		    	// TODO statt contains: auf gleiche ID überprüfen
		    	// -> warum ??
		        if (event.getChecked()) {
		        	if (!selectedArrayList.contains(currentGefaehrdung))
		        		/* Add to List of Associated Gefaehrdungen */
		        		selectedArrayList.add(currentGefaehrdung);
		        		/* create GefaehrungsUmsetzung an add to List of GefaehrdungsUmstzezungen
		        		GefaehrdungsUmsetzung newGefaehrdungsUmsetzung =
		        				new GefaehrdungsUmsetzung(currentGefaehrdung);
		        		umsetzungenArrayList.add(newGefaehrdungsUmsetzung);
		        		*/
		        } else {
		        	/* remove from List of Associated Gefaehrdungen */
		        	selectedArrayList.remove(currentGefaehrdung);
		        	/* remove from List of GefaehrdungsUmstzezungen an dispose GefaehrungsUmsetzung
		        	for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : umsetzungenArrayList) {
						if (gefaehrdungsUmsetzung.getId().equals(currentGefaehrdung.getId())) {
							umsetzungenArrayList.remove(gefaehrdungsUmsetzung);
							// TODO klären, ob gefaehrdungsUmsetzung dispose()d werden kann/muss
							break;
						}
					}
		        	*/
		        }
		          
		        checkPageComplete();
		    }
		});
		
		/**
		 *  listener opens edit Dialog for selected Gefaehrdung
		 */
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
	    		Gefaehrdung selectedGefaehrdung = (Gefaehrdung) selection.getFirstElement();
	    		if (selectedGefaehrdung instanceof OwnGefaehrdung) {
	    			Logger.getLogger(this.getClass()).debug("edit own gef");
	    			OwnGefaehrdung ownGefSelected = (OwnGefaehrdung) selectedGefaehrdung;
	    			final EditGefaehrdungDialog dialog = 
	    				new EditGefaehrdungDialog(container.getShell(), ownGefSelected);
	    			dialog.open();
	    			viewer.refresh();
	    		} else {
	    			Logger.getLogger(this.getClass()).debug("no edit");
	    		}
		    }
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
		
		/* group the buttons with composite */
		Composite composite = new Composite(container, SWT.NULL);
		GridLayout gridLayoutButtons = new GridLayout();
        gridLayoutButtons.numColumns = 5;
        composite.setLayout(gridLayoutButtons);
        GridData data2 = new GridData();
        data2.horizontalAlignment = SWT.RIGHT;
        data2.verticalAlignment = SWT.TOP;
	    composite.setLayoutData(data2);

	    /* group the buttons with Group
	    Group buttons = new Group(container, SWT.SHADOW_ETCHED_OUT);
	    buttons.setText("Actions");
        GridLayout gridLayoutButtons = new GridLayout();
        gridLayoutButtons.numColumns = 1;
        buttons.setLayout(gridLayoutButtons);
        GridData data2 = new GridData();
	    data2.verticalAlignment = SWT.BOTTOM;
	    buttons.setLayoutData(data2);
	    */

	    /* new button */
	    Button button2 = new Button(composite, SWT.PUSH);
	    button2.setText("neu");
	    GridData data3 = new GridData();
	    button2.setLayoutData(data3);
	    button2.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		ArrayList<OwnGefaehrdung> arrListOwnGefaehrdungen = 
	    			((RisikoanalyseWizard)getWizard()).getOwnGefaehrdungen();
	    		final NewGefaehrdungDialog dialog = 
	    			new NewGefaehrdungDialog(container.getShell(), arrListOwnGefaehrdungen);
	    		dialog.open();
	    		((RisikoanalyseWizard)getWizard()).addOwnGefaehrdungen();
	    		viewer.refresh();
	    		selectAssignedGefaehrdungen();
	    	}
	    });
	    
	    /* delete button */
	    Button button3 = new Button(composite, SWT.PUSH);
	    button3.setText("löschen");
	    GridData data4 = new GridData();
	    button3.setLayoutData(data4);
	    button3.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Gefaehrdung selectedGefaehrdung = (Gefaehrdung) selection.getFirstElement();
				if (selectedGefaehrdung instanceof OwnGefaehrdung) {
					/* ask user to confirm */
					boolean confirmed = MessageDialog.openQuestion(container
							.getShell(), "Bestätigung",
							"Wollen Sie die Gefährdung mit dem Titel \""
									+ selectedGefaehrdung.getTitel()
									+ "\" wirklich löschen?");
					if (confirmed) {
						deleteOwnGefaehrdung(selectedGefaehrdung);
						viewer.refresh();
						selectAssignedGefaehrdungen();
					}
				}
			}
		});
	    
	    /* edit button */
	    Button button4 = new Button(composite, SWT.PUSH);
	    button4.setText("bearbeiten");
	    GridData data5 = new GridData();
	    button4.setLayoutData(data5);
	    button4.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
	    		Gefaehrdung selectedGefaehrdung = (Gefaehrdung) selection.getFirstElement();
	    		if (selectedGefaehrdung instanceof OwnGefaehrdung) {
	    			OwnGefaehrdung ownGefSelected = (OwnGefaehrdung) selectedGefaehrdung;
	    			final EditGefaehrdungDialog dialog = 
	    				new EditGefaehrdungDialog(container.getShell(), ownGefSelected);
	    		dialog.open();
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
		
		ArrayList<Gefaehrdung> arrListAllGefaehrdungen = 
			((RisikoanalyseWizard)getWizard()).getAllGefaehrdungen();

		/* map a domain model object into multiple images and text labels */
		viewer.setLabelProvider(new CheckboxTableViewerLabelProvider());
		/* map domain model into array */
		viewer.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewer.setInput(arrListAllGefaehrdungen);
		
		viewer.setSorter(new GefaehrdungenSorter());
	    
		selectAssignedGefaehrdungen();
		packAllColumns();
		
		// vs. overriding method WizardPage.canFilpToNextPage 
		checkPageComplete();
	}

	/**
	 * marks all checkboxes of Gefaehrdungen associated to the selected Baustein
	 */
	private void selectAssignedGefaehrdungen() {
		ArrayList<Gefaehrdung> list = ((RisikoanalyseWizard)getWizard()).getAssociatedGefaehrdungen();
		viewer.setCheckedElements((Gefaehrdung[]) list.toArray(new Gefaehrdung[list.size()]));
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
	
	/**
	 * activates next button if List of selected Gefaehrdungen is not empty
	 */
	private void checkPageComplete() {
		if (((RisikoanalyseWizard)getWizard()).getAssociatedGefaehrdungen().isEmpty()) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}
	
	
	
	private void deleteOwnGefaehrdung(Gefaehrdung delGefaehrdung) {
		ArrayList<Gefaehrdung> allList = ((RisikoanalyseWizard)getWizard()).getAllGefaehrdungen();
 		ArrayList<Gefaehrdung> list = ((RisikoanalyseWizard)getWizard()).getAssociatedGefaehrdungen();
 		ArrayList<OwnGefaehrdung> ownList =
 			((RisikoanalyseWizard)getWizard()).getOwnGefaehrdungen();
 		
 		try {
			if (ownList.contains(delGefaehrdung)) {
				OwnGefaehrdungHome.getInstance().remove((OwnGefaehrdung)delGefaehrdung);
				
				/* delete from List of OwnGefaehrdungen */
				ownList.remove(delGefaehrdung);
				((RisikoanalyseWizard)getWizard()).setOwnGefaehrdungen(ownList);
				
				/* delete from List of selected Gefaehrdungen */
				if (list.contains(delGefaehrdung)) { 
					list.remove(delGefaehrdung);
				}
				
				/* delete from list of all Gefaehrdungen */
				if (allList.contains(delGefaehrdung)) { 
					allList.remove(delGefaehrdung);
				}
			}
		} catch (Exception e) {
			ExceptionUtil.log(e, "Gefährdung konnte nicht gelöscht werden.");
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