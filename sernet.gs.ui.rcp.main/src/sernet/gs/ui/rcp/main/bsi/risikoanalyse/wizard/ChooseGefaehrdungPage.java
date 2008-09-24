package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;

/**
 * WizardPage which lists all Gefaehrdungen from BSI IT-Grundschutz-Kataloge and
 * additionally all self-defined OwnGefaehrdungen in a CheckboxTableViewer.
 * 
 * @author ahanekop@sernet.de
 */
public class ChooseGefaehrdungPage extends WizardPage {

	private Composite composite;
	private TableColumn checkboxColumn;
	private TableColumn imageColumn;
	private TableColumn numberColumn;
	private TableColumn nameColumn;
	private TableColumn descriptionColumn;
	private CheckboxTableViewer viewer;
	private OwnGefaehrdungenFilter ownGefaehrdungFilter = new OwnGefaehrdungenFilter();
	private GefaehrdungenFilter gefaehrdungFilter = new GefaehrdungenFilter();
	private SearchFilter searchFilter = new SearchFilter();
	private RiskAnalysisWizard wizard;

	/**
	 * Constructor sets title an description of WizardPage.
	 */
	protected ChooseGefaehrdungPage() {
		super("Gefährdungsübersicht");
		setTitle("Gefährdungsübersicht");
		setDescription("Wählen Sie die Gefährdungen aus, die behandelt werden sollen.");
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
		
		/* CheckboxTableViewer */
		viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
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

		imageColumn = new TableColumn(table, SWT.LEFT);
		imageColumn.setText("");
		imageColumn.setWidth(35);

		numberColumn = new TableColumn(table, SWT.LEFT);
		numberColumn.setText("Nummer");
		numberColumn.setWidth(100);

		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(100);

		descriptionColumn = new TableColumn(table, SWT.LEFT);
		descriptionColumn.setText("Beschreibung");
		descriptionColumn.setWidth(200);

		/* listener adds/removes Gefaehrdungen to Array of selected Gefaehrdungen */
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Gefaehrdung currentGefaehrdung = (Gefaehrdung) event
						.getElement();

				if (event.getChecked())
					associateGefaehrdung(currentGefaehrdung, true);
				else 
					associateGefaehrdung(currentGefaehrdung, false);
			}

		});

		/* listener opens edit Dialog for the selected Gefaehrdung */
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			/**
			 * Notifies of a double click.
			 *  
			 * @param event event object describing the double-click
			 */
			public void doubleClick(DoubleClickEvent event) {
				/* retrieve selected Gefaehrdung and open edit dialog with it */
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				IGSModel selectedGefaehrdung = (IGSModel) selection
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
			
			/**
	    	 * Adds/removes Filter depending on event.
	    	 * 
	    	 * @param event event containing information about the selection
	    	 */
			public void widgetSelected(SelectionEvent event) {
				Button button = (Button) event.widget;
				if (button.getSelection()) {
					viewer.addFilter(ownGefaehrdungFilter);
					viewer.refresh();
				} else {
					viewer.removeFilter(ownGefaehrdungFilter);
					viewer.refresh();
					assignBausteinGefaehrdungen();
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
			
			/**
	    	 * Adds/removes Filter depending on event.
	    	 * 
	    	 * @param event event containing information about the selection
	    	 */
			public void widgetSelected(SelectionEvent event) {
				Button button = (Button) event.widget;
				if (button.getSelection()) {
					viewer.addFilter(gefaehrdungFilter);
					viewer.refresh();
				} else {
					viewer.removeFilter(gefaehrdungFilter);
					viewer.refresh();
					assignBausteinGefaehrdungen();
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
		textSearch.addModifyListener(new ModifyListener() {
			
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
					assignBausteinGefaehrdungen();
				}
			}
		});

		/* group the buttons with composite
		Composite compositeButtons = new Composite(composite, SWT.NULL);
		GridLayout gridLayoutButtons = new GridLayout();
		gridLayoutButtons.numColumns = 5;
		compositeButtons.setLayout(gridLayoutButtons);
		GridData data2 = new GridData();
		data2.horizontalAlignment = SWT.RIGHT;
		data2.verticalAlignment = SWT.TOP;
		compositeButtons.setLayoutData(data2);
		*/

		/* group the buttons with Group */
		Group groupButtons = new Group(composite, SWT.SHADOW_ETCHED_OUT);
		groupButtons.setText("eigene Gefährdungen");
		GridLayout gridLayoutButtons = new GridLayout();
		gridLayoutButtons.numColumns = 5;
		groupButtons.setLayout(gridLayoutButtons);
		GridData gridButtons = new GridData();
		gridButtons.horizontalAlignment = SWT.RIGHT;
		gridButtons.verticalAlignment = SWT.TOP;
		groupButtons.setLayoutData(gridButtons);

		/* new button */
		Button buttonNew = new Button(groupButtons, SWT.PUSH);
		buttonNew.setText("neu");
		GridData gridNew = new GridData();
		buttonNew.setLayoutData(gridNew);

		/* Listener opens Dialog for creation of new OwnGefaehrdung */
		buttonNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ArrayList<OwnGefaehrdung> arrListOwnGefaehrdungen = ((RiskAnalysisWizard) getWizard())
						.getAllOwnGefaehrdungen();
				final NewGefaehrdungDialog dialog = new NewGefaehrdungDialog(
						composite.getShell(), arrListOwnGefaehrdungen);
				dialog.open();
				((RiskAnalysisWizard) getWizard()).addOwnGefaehrdungen();
				viewer.refresh();
				assignBausteinGefaehrdungen();
			}
		});

		/* delete button */
		Button buttonDelete = new Button(groupButtons, SWT.PUSH);
		buttonDelete.setText("löschen");
		GridData gridDelete = new GridData();
		buttonDelete.setLayoutData(gridDelete);

		/* Listener opens MessageDialog and deletes selected Gefaehrdung */
		buttonDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				Gefaehrdung selectedGefaehrdung = (Gefaehrdung) selection
						.getFirstElement();
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
						assignBausteinGefaehrdungen();
					}
				}
			}
		});

		/* edit button */
		Button buttonEdit = new Button(groupButtons, SWT.PUSH);
		buttonEdit.setText("bearbeiten");
		GridData gridEdit = new GridData();
		buttonEdit.setLayoutData(gridEdit);

		/* Listener opens Dialog for editing the selected Gefaehrdung */
		buttonEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				IGSModel selectedGefaehrdung = (IGSModel) selection
						.getFirstElement();
				if (selectedGefaehrdung instanceof OwnGefaehrdung) {
					OwnGefaehrdung ownGefSelected = (OwnGefaehrdung) selectedGefaehrdung;
					final EditGefaehrdungDialog dialog = new EditGefaehrdungDialog(
							composite.getShell(), ownGefSelected);
					dialog.open();
					viewer.refresh();
				}
			}
		});
	}

	protected void associateGefaehrdung(Gefaehrdung currentGefaehrdung, boolean select) {
			RiskAnalysisWizard wizard = ((RiskAnalysisWizard) getWizard());
			List<GefaehrdungsUmsetzung> selectedArrayList = wizard
					.getAssociatedGefaehrdungen();

			if (select) {
				if (!GefaehrdungsUtil.listContainsById(selectedArrayList, currentGefaehrdung)) {
					/* Add to List of Associated Gefaehrdungen */
					selectedArrayList.add(
							GefaehrdungsUmsetzungFactory.build(wizard.getFinishedRiskAnalysis(), currentGefaehrdung)
							);
				}
			} else {
				/* remove from List of Associated Gefaehrdungen */
				GefaehrdungsUtil.removeBySameId(selectedArrayList, currentGefaehrdung);
			}
			checkPageComplete();
	
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
	 * Fills the CheckboxTableViewer with all Gefaehrdungen available.
	 * Is processed each time the WizardPage is set visible.
	 */
	private void initContents() {
		wizard = ((RiskAnalysisWizard) getWizard());
		ArrayList<Gefaehrdung> arrListAllGefaehrdungen = wizard
				.getAllGefaehrdungen();

		/* map a domain model object into multiple images and text labels */
		viewer.setLabelProvider(new CheckboxTableViewerLabelProvider());
		/* map domain model into array */
		viewer.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewer.setInput(arrListAllGefaehrdungen);
		viewer.setSorter(new GefaehrdungenSorter());
		assignBausteinGefaehrdungen();
		checkAllSelectedGefaehrdungen();
		packAllColumns();

		((RiskAnalysisWizard) getWizard()).setCanFinish(false);
		checkPageComplete();
	}


	private void checkAllSelectedGefaehrdungen() {
		List<Gefaehrdung> toCheck = new ArrayList<Gefaehrdung>();
		for (GefaehrdungsUmsetzung associatedGefaehrdung: wizard.getAssociatedGefaehrdungen()) {
			for (Gefaehrdung gefaehrdung: wizard.getAllGefaehrdungen()) {
				if (gefaehrdung.getId().equals(associatedGefaehrdung.getId()))
					toCheck.add(gefaehrdung);
			}
		}
		
		Gefaehrdung[] checkarray = (Gefaehrdung[]) toCheck.toArray(new Gefaehrdung[toCheck
				.size()]);
		viewer.setCheckedElements(checkarray);
	}

	/**
	 * Marks all checkboxes of Gefaehrdungen associated to the selected Baustein.
	 */
	private void assignBausteinGefaehrdungen() {
		List<GefaehrdungsUmsetzung> list = ((RiskAnalysisWizard) getWizard())
				.getAssociatedGefaehrdungen();
		
		ArrayList<Gefaehrdung> toCheck = new ArrayList<Gefaehrdung>(50);
		for (GefaehrdungsUmsetzung selectedGefaehrdung : list) {
			for (Gefaehrdung gefaehrdung : wizard.getAllGefaehrdungen()) {
				if (gefaehrdung.getId().equals(selectedGefaehrdung.getId())) {
					associateGefaehrdung(gefaehrdung, true);
				}
			}
		}
	}

	/**
	 * Adjusts all columns of the CheckboxTableViewer.
	 */
	private void packAllColumns() {
		checkboxColumn.pack();
		imageColumn.pack();
		numberColumn.pack();
		nameColumn.pack();
		descriptionColumn.pack();
	}

	/**
	 * Activates the next button, if the List of selected Gefaehrdungen is not empty.
	 */
	private void checkPageComplete() {
		if (((RiskAnalysisWizard) getWizard()).getAssociatedGefaehrdungen()
				.isEmpty()) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}
	
	

	/**
	 * Deletes a OwnGefaehrdung.
	 * 
	 * @param delGefaehrdung the (Own)Gefaehrdung to delete
	 */
	private void deleteOwnGefaehrdung(Gefaehrdung delGefaehrdung) {
		ArrayList<Gefaehrdung> arrListAllGefaehrdungen = ((RiskAnalysisWizard) getWizard())
				.getAllGefaehrdungen();
		List<GefaehrdungsUmsetzung> arrListAssociatedGefaehrdungen = ((RiskAnalysisWizard) getWizard())
				.getAssociatedGefaehrdungen();
		ArrayList<OwnGefaehrdung> arrListOwnGefaehrdungen = ((RiskAnalysisWizard) getWizard())
				.getAllOwnGefaehrdungen();

		try {
			if (arrListOwnGefaehrdungen.contains(delGefaehrdung)) {
				/* delete OwnGefaehrdung from Database */
				OwnGefaehrdungHome.getInstance().remove(
						(OwnGefaehrdung) delGefaehrdung);

				/* delete OwnGefaehrdung from List of OwnGefaehrdungen */
				arrListOwnGefaehrdungen.remove(delGefaehrdung);

				/* delete OwnGefaehrdung from List of selected Gefaehrdungen */
				GefaehrdungsUtil.removeBySameId(arrListAssociatedGefaehrdungen, delGefaehrdung);

				/* delete OwnGefaehrdung from list of all Gefaehrdungen */
				if (arrListAllGefaehrdungen.contains(delGefaehrdung)) {
					arrListAllGefaehrdungen.remove(delGefaehrdung);
				}
			}
		} catch (Exception e) {
			ExceptionUtil.log(e, "Gefährdung konnte nicht gelöscht werden.");
		}
	}

	/**
	 * Filter to extract all OwnGefaehrdungen in CheckboxTableViewer.
	 * 
	 * @author ahanekop@sernet.de
	 */
	class OwnGefaehrdungenFilter extends ViewerFilter {
		
		/**
		 * Returns true, if the given element is an OwnGefaehrdung.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof OwnGefaehrdung)
				return true;
			return false;
		}
	}

	/**
	 * Filter to extract all Gefaehrdungen in CheckboxTableViewer.
	 * 
	 * @author ahanekop@sernet.de
	 */
	class GefaehrdungenFilter extends ViewerFilter {
		
		/**
		 * Returns true, if the given element is a Gefaehrdung.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (!(element instanceof OwnGefaehrdung)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Filter to extract all (Own)Gefaehrdungen matching a given String. 
	 *
	 * @author ahanekop@sernet.de
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
		 * Selects all (Own)Gefaehrdungen matching the Pattern.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
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
