package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;

/**
 * WizardPage which lists all Gefaehrdungen from BSI IT-Grundschutz-Kataloge and
 * additionally all self-defined OwnGefaehrdungen in a CheckboxTableViewer.
 * 
 * @author ahanekop@sernet.de
 * 
 */

public class ChooseGefaehrdungPage extends WizardPage {

	private Composite composite;
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

	/**
	 * Adds widgets to the wizardPage.
	 * Called once at startup of Wizard.
	 * 
	 *  @param parent parent Composite  
	 */
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);
		
		/* TODO brauche ich das control (s.u.)? */
		setControl(composite);

		/* CheckboxTableViewer */
		viewer = CheckboxTableViewer.newCheckList(
		        composite, SWT.BORDER);
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
		
		/* listener adds/removes Gefaehrdungen to Array of selected Gefaehrdungen */
		viewer.addCheckStateListener(new ICheckStateListener() {
		    public void checkStateChanged(CheckStateChangedEvent event) {
		    	Gefaehrdung currentGefaehrdung = (Gefaehrdung) event.getElement();
		    	ArrayList<Gefaehrdung> selectedArrayList = 
		    		((RisikoanalyseWizard)getWizard()).getAssociatedGefaehrdungen();
		    	ArrayList<GefaehrdungsUmsetzung> umsetzungenArrayList = 
		    		((RisikoanalyseWizard)getWizard()).getGefaehrdungsUmsetzungen();
		    	
		    	// TODO statt contains: auf gleiche ID überprüfen (warum ??)
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
		
		/* listener opens edit Dialog for the selected Gefaehrdung */
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				Gefaehrdung selectedGefaehrdung = (Gefaehrdung) selection
						.getFirstElement();
				if (selectedGefaehrdung instanceof OwnGefaehrdung) {
					OwnGefaehrdung selectedOwnGefaehrdung = (OwnGefaehrdung) selectedGefaehrdung;
					final EditGefaehrdungDialog dialog = new EditGefaehrdungDialog(
							composite.getShell(), selectedOwnGefaehrdung);
					dialog.open();
					viewer.refresh();
				}
			}
		});
		
	    /* group the Filter checkboxes with composite */
		Composite compositeFilter = new Composite(composite, SWT.NULL);
		GridLayout gridLayoutFilters = new GridLayout();
        gridLayoutFilters.numColumns = 2;
        compositeFilter.setLayout(gridLayoutFilters);
        GridData gridCompositeFilter = new GridData();
        gridCompositeFilter.horizontalAlignment = SWT.LEFT;
        gridCompositeFilter.verticalAlignment = SWT.TOP;
	    compositeFilter.setLayoutData(gridCompositeFilter);
	    
	    /* filter button - OwnGefaehrdungen only */
	    Button buttonOwnGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
	    buttonOwnGefaehrdungen.setText("nur eigene Gefährdungen anzeigen");
	    GridData gridOwnGefaehrdungen = new GridData();
	    gridOwnGefaehrdungen.horizontalSpan = 2;
	    buttonOwnGefaehrdungen.setLayoutData(gridOwnGefaehrdungen);
	    
	    /* Listener adds/removes Filter ownGefaehrdungFilter */
	    buttonOwnGefaehrdungen.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		Button button = (Button) event.widget;
	    		if(button.getSelection()){
	    			viewer.addFilter(ownGefaehrdungFilter);
	    			viewer.refresh();
	    		} else {
	    			viewer.removeFilter(ownGefaehrdungFilter);
	    			viewer.refresh();
	    		}
	    	}

	    });
	    
	    /* filter button - BSI Gefaehrdungen only */
	    Button buttonGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
	    buttonGefaehrdungen.setText("nur BSI Gefährdungen anzeigen");
	    GridData gridGefaehrdungen = new GridData();
	    gridGefaehrdungen.horizontalSpan = 2;
	    buttonGefaehrdungen.setLayoutData(gridGefaehrdungen);
	    
	    /* Listener adds/removes Filter gefaehrdungFilter*/
	    buttonGefaehrdungen.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		Button button = (Button) event.widget;
	    		if(button.getSelection()){
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
	    Text textSearch = new Text(compositeFilter, SWT.SINGLE | SWT.BORDER);
	    GridData gridSearch = new GridData();
	    gridSearch.horizontalAlignment = SWT.FILL;
	    textSearch.setLayoutData(gridSearch);

	    /* Listener adds/removes Filter searchFilter */
	    textSearch.addModifyListener(new ModifyListener(){
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
		Composite compositeButtons = new Composite(composite, SWT.NULL);
		GridLayout gridLayoutButtons = new GridLayout();
        gridLayoutButtons.numColumns = 5;
        compositeButtons.setLayout(gridLayoutButtons);
        GridData data2 = new GridData();
        data2.horizontalAlignment = SWT.RIGHT;
        data2.verticalAlignment = SWT.TOP;
	    compositeButtons.setLayoutData(data2);

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
	    Button buttonNew = new Button(compositeButtons, SWT.PUSH);
	    buttonNew.setText("neu");
	    GridData gridNew = new GridData();
	    buttonNew.setLayoutData(gridNew);
	    
	    /* Listener opens Dialog for creation of new OwnGefaehrdung */
	    buttonNew.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		ArrayList<OwnGefaehrdung> arrListOwnGefaehrdungen = 
	    			((RisikoanalyseWizard)getWizard()).getOwnGefaehrdungen();
	    		final NewGefaehrdungDialog dialog = 
	    			new NewGefaehrdungDialog(composite.getShell(), arrListOwnGefaehrdungen);
	    		dialog.open();
	    		((RisikoanalyseWizard)getWizard()).addOwnGefaehrdungen();
	    		viewer.refresh();
	    		selectAssignedGefaehrdungen();
	    	}
	    });
	    
	    /* delete button */
	    Button button3 = new Button(compositeButtons, SWT.PUSH);
	    button3.setText("löschen");
	    GridData data4 = new GridData();
	    button3.setLayoutData(data4);
	    button3.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Gefaehrdung selectedGefaehrdung = (Gefaehrdung) selection.getFirstElement();
				if (selectedGefaehrdung instanceof OwnGefaehrdung) {
					/* ask user to confirm */
					boolean confirmed = MessageDialog.openQuestion(composite
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
	    Button button4 = new Button(compositeButtons, SWT.PUSH);
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
	    				new EditGefaehrdungDialog(composite.getShell(), ownGefSelected);
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
		((RisikoanalyseWizard)getWizard()).setCanFinish(false);
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