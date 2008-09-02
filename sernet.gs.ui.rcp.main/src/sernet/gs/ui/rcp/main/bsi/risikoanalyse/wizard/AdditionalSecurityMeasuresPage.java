package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
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
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.ChooseGefaehrdungPage.SearchFilter;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Add security measures (Massnahmen) to the risks (Gefaehrdungen).
 * New security measures may be created. 
 * 
 * @author ahanekop@sernet.de
 */
public class AdditionalSecurityMeasuresPage extends WizardPage {

	private Composite composite;
	private TableViewer viewerMassnahme;
	private TableColumn imageColumnMassnahme;
	private TableColumn numberColumnMassnahme;
	private TableColumn nameColumnMassnahme;
	private TableColumn descriptionColumnMassnahme;
	private TreeViewer viewerGefaehrdung;
	
	private MassnahmenUmsetzungenFilter massnahmenUmsetzungenFilter =
		new MassnahmenUmsetzungenFilter();
	private RisikoMassnahmenUmsetzungenFilter risikoMassnahmenUmsetzungenFilter =
			new RisikoMassnahmenUmsetzungenFilter();
	private SearchFilter searchFilter = new SearchFilter();

	/**
	 * Constructor sets title an description of WizardPage.
	 */
	protected AdditionalSecurityMeasuresPage() {
		super("Zusätzliche IT-Sicherheitsmaßnahmen");
		setTitle("Zusätzliche IT-Sicherheitsmaßnahmen");
		setDescription("Fügen Sie den Gefährdungen weitere"
				+ " IT-Sicherheitsmaßnahmen hinzu. Legen Sie ggf."
				+ " zusätzlich eigene Maßnahmen an.");
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
		gridLayout.numColumns = 4;
		composite.setLayout(gridLayout);
		setControl(composite);

		/* TreeViewer: Gefaehrdungen */
		viewerGefaehrdung = new TreeViewer(composite, SWT.SINGLE);
		GridData gridViewerGefaehrdung = new GridData();
		gridViewerGefaehrdung.grabExcessHorizontalSpace = true;
		gridViewerGefaehrdung.grabExcessVerticalSpace = true;
		gridViewerGefaehrdung.horizontalSpan = 2;
		gridViewerGefaehrdung.horizontalAlignment = SWT.FILL;
		gridViewerGefaehrdung.verticalAlignment = SWT.FILL;
		viewerGefaehrdung.getTree().setLayoutData(gridViewerGefaehrdung);

		/* TableViewer: Massnahmen */
		viewerMassnahme = new TableViewer(composite, SWT.BORDER | SWT.SINGLE
				| SWT.FULL_SELECTION);
		final Table tableMassnahme = viewerMassnahme.getTable();
		GridData gridTableMassnahme = new GridData();
		gridTableMassnahme.grabExcessHorizontalSpace = true;
		gridTableMassnahme.grabExcessVerticalSpace = true;
		gridTableMassnahme.horizontalSpan = 2;
		gridTableMassnahme.horizontalAlignment = SWT.FILL;
		gridTableMassnahme.verticalAlignment = SWT.FILL;
		tableMassnahme.setLayoutData(gridTableMassnahme);
		tableMassnahme.setHeaderVisible(true);
		tableMassnahme.setLinesVisible(true);

		imageColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		imageColumnMassnahme.setText("");
		imageColumnMassnahme.setWidth(35);

		numberColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		numberColumnMassnahme.setText("Nummer");
		numberColumnMassnahme.setWidth(100);

		nameColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		nameColumnMassnahme.setText("Name");
		nameColumnMassnahme.setWidth(100);

		descriptionColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		descriptionColumnMassnahme.setText("Beschreibung");
		descriptionColumnMassnahme.setWidth(200);

		/* listener opens edit Dialog for selected Massnahme */
		viewerMassnahme.addDoubleClickListener(new IDoubleClickListener() {
			
			/**
			 * Notifies of a double click.
			 *  
			 * @param event event object describing the double-click
			 */
			public void doubleClick(DoubleClickEvent event) {
				/* retrieve selected Massnahme and open edit dialog with it */
				IStructuredSelection selection = (IStructuredSelection) viewerMassnahme
						.getSelection();
				MassnahmenUmsetzung selectedMassnahmenUmsetzung = (MassnahmenUmsetzung) selection
						.getFirstElement();
				if (selectedMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) {
					RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung =
							(RisikoMassnahmenUmsetzung) selectedMassnahmenUmsetzung;
					final EditRisikoMassnahmenUmsetzungDialog dialog =
						new EditRisikoMassnahmenUmsetzungDialog(
							composite.getShell(),
							selectedRisikoMassnahmenUmsetzung);
					dialog.open();
					viewerMassnahme.refresh();
					packAllMassnahmeColumns();
				}
			}
		});

		/* add drag and drop support */
		CnATreeElement cnaElement = ((RiskAnalysisWizard) getWizard())
				.getCnaElement();
		/* note: this Transfer is not used for data transfer, but for fulfilling
		 * parameter needs of addDropSupport and addDragSupport. The actual
		 * data transfer ins realized through DNDItems in
		 * RisikoMassnahmenUmsetzungDragListener and
		 * RisikoMassnahmenUmsetzungDropListener */
		Transfer[] types = new Transfer[] { RisikoMassnahmenUmsetzungTransfer
				.getInstance() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		viewerGefaehrdung.addDropSupport(operations, types,
				new RisikoMassnahmenUmsetzungDropListener(viewerGefaehrdung));
		viewerMassnahme.addDragSupport(operations, types,
				new RisikoMassnahmenUmsetzungDragListener(viewerMassnahme,
						cnaElement));

		/* group the buttons for viewerGefaehrdung with group */
		Group groupButtonsGefaehrdung = new Group(composite,
				SWT.SHADOW_ETCHED_OUT);
		groupButtonsGefaehrdung.setText("Maßnahmen");
		GridLayout gridLayoutButtonsGefaehrdung = new GridLayout();
		gridLayoutButtonsGefaehrdung.numColumns = 3;
		groupButtonsGefaehrdung.setLayout(gridLayoutButtonsGefaehrdung);
		GridData gridGroupButtonsGefaehrdung = new GridData();
		gridGroupButtonsGefaehrdung.horizontalSpan = 2;
		gridGroupButtonsGefaehrdung.horizontalAlignment = SWT.LEFT;
		gridGroupButtonsGefaehrdung.verticalAlignment = SWT.TOP;
		groupButtonsGefaehrdung.setLayoutData(gridGroupButtonsGefaehrdung);

		/* delete button for viewerGefaehrdung */
		Button buttonDeleteGefaehrdung = new Button(groupButtonsGefaehrdung,
				SWT.PUSH);
		buttonDeleteGefaehrdung.setText("löschen");
		GridData gridDeleteGefaehrdung = new GridData();
		buttonDeleteGefaehrdung.setLayoutData(gridDeleteGefaehrdung);
		
		/* listener deletes the selected Massnahme after confirmation */
		buttonDeleteGefaehrdung.addSelectionListener(new SelectionAdapter() {
			
			/**
	    	 * Deletes the selected Massnahme after confirmation.
	    	 * 
	    	 * @param event event containing information about the selection
	    	 */
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection)
						viewerGefaehrdung.getSelection();
				RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung =
						(RisikoMassnahmenUmsetzung) selection.getFirstElement();

				/* ask user to confirm */
				boolean confirmed = MessageDialog.openQuestion(composite
						.getShell(), "Bestätigung",
						"Wollen Sie die Massnahme mit dem Titel \""
								+ selectedRisikoMassnahmenUmsetzung.getTitle()
								+ "\" wirklich löschen?");
				if (confirmed) {
					deleteTreeViewerRisikoMassnahmenUmsetzung(
							selectedRisikoMassnahmenUmsetzung);
					viewerGefaehrdung.refresh();
				}
			}
		});

		/* group the buttons for viewerMassnahme with group */
		Group groupButtonsMassnahme = new Group(composite,
				SWT.SHADOW_ETCHED_OUT);
		groupButtonsMassnahme.setText("eigene Maßnahmen");
		GridLayout gridLayoutButtonsMassnahme = new GridLayout();
		gridLayoutButtonsMassnahme.numColumns = 3;
		groupButtonsMassnahme.setLayout(gridLayoutButtonsMassnahme);
		GridData gridGroupButtonsMassnahme = new GridData();
		gridGroupButtonsMassnahme.horizontalSpan = 2;
		gridGroupButtonsMassnahme.horizontalAlignment = SWT.RIGHT;
		gridGroupButtonsMassnahme.verticalAlignment = SWT.TOP;
		groupButtonsMassnahme.setLayoutData(gridGroupButtonsMassnahme);

		/*
		Composite composite = new Composite(container, SWT.NULL);
		GridLayout gridLayoutButtons = new GridLayout();
		gridLayoutButtons.numColumns = 3;
		composite.setLayout(gridLayoutButtons);
		GridData data3 = new GridData();
		data3.horizontalSpan = 4;
		data3.horizontalAlignment = SWT.RIGHT;
		data3.verticalAlignment = SWT.TOP;
		composite.setLayoutData(data3);
		 */

		/* new button */
		Button buttonNewMassnahme = new Button(groupButtonsMassnahme, SWT.PUSH);
		buttonNewMassnahme.setText("neu");
		GridData gridButtonMassnahmeNew = new GridData();
		buttonNewMassnahme.setLayoutData(gridButtonMassnahmeNew);
		
		/* listener opens Dialog for creating a new Massnahme */
		buttonNewMassnahme.addSelectionListener(new SelectionAdapter() {
			
			/**
	    	 * Opens dialog for creating an new Massnahme of type
	    	 * RisikoMassnahmenUmsetzung and adds it to the List of
	    	 * RisikoMassnahmenUmsetzungen and the viewer.
	    	 * 
	    	 * @param event event containing information about the selection
	    	 */
			public void widgetSelected(SelectionEvent event) {

				/* get all RisikoMassnahmenUmsetzung */
				ArrayList<RisikoMassnahmenUmsetzung> arrListRisikoMassnahmenUmsetzung =
						((RiskAnalysisWizard) getWizard())
								.getAllRisikoMassnahmenUmsetzungen();

				/* create new RisikoMassnahmenUmsetzung */
				final NewRisikoMassnahmenUmsetzungDialog dialog =
						new NewRisikoMassnahmenUmsetzungDialog(
							composite.getShell(), arrListRisikoMassnahmenUmsetzung,
							((RiskAnalysisWizard) getWizard()).getCnaElement());
				dialog.open();
				
				/* add new RisikoMassnahmenUmsetzung to List and viewer */
				((RiskAnalysisWizard) getWizard())
						.addRisikoMassnahmenUmsetzungen();
				viewerMassnahme.refresh();
				packAllMassnahmeColumns();
			}
		});

		/* delete button */
		Button buttonDeleteMassnahme = new Button(groupButtonsMassnahme, SWT.PUSH);
		buttonDeleteMassnahme.setText("löschen");
		GridData gridButtonMassnahmeDelete = new GridData();
		buttonDeleteMassnahme.setLayoutData(gridButtonMassnahmeDelete);
		
		/* listener deletes the selected Massnahme after confirmation */
		buttonDeleteMassnahme.addSelectionListener(new SelectionAdapter() {
			
			/**
	    	 * Deletes the selected Massnahme after confirmation.
	    	 * 
	    	 * @param event event containing information about the selection
	    	 */
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection)
						viewerMassnahme.getSelection();
				MassnahmenUmsetzung selectedMassnahmenUmsetzung =
						(MassnahmenUmsetzung) selection.getFirstElement();

				/* only RisikoMassnahmenUmsetzungen can be deleted */
				if (selectedMassnahmenUmsetzung instanceof
							RisikoMassnahmenUmsetzung) {

					/* ask user to confirm */
					boolean confirmed = MessageDialog.openQuestion(composite
							.getShell(), "Bestätigung",
							"Wollen Sie die Massnahme mit dem Titel \""
									+ selectedMassnahmenUmsetzung.getTitle()
									+ "\" wirklich löschen?");
					/* delete */
					if (confirmed) {
						deleteRisikoMassnahmenUmsetzung(selectedMassnahmenUmsetzung);
						viewerMassnahme.refresh();
					}
				}
			}
		});

		/* edit button */
		Button buttonEditMassnahme = new Button(groupButtonsMassnahme, SWT.PUSH);
		buttonEditMassnahme.setText("bearbeiten");
		GridData gridButtonEditMassnahme = new GridData();
		buttonEditMassnahme.setLayoutData(gridButtonEditMassnahme);
		
		/* listener opens edit Dialog for the selected Massnahme */
		buttonEditMassnahme.addSelectionListener(new SelectionAdapter() {
			
			/**
	    	 * Opens edit Dialog for the selected Massnahme and refreshes the
	    	 * viewer.
	    	 * 
	    	 * @param event event containing information about the selection
	    	 */
			public void widgetSelected(SelectionEvent event) {
				
				/* retrieve selected element and open edit dialog with it */
				IStructuredSelection selection = (IStructuredSelection)
						viewerMassnahme.getSelection();
				MassnahmenUmsetzung selectedMassnahmenUmsetzung =
						(MassnahmenUmsetzung) selection.getFirstElement();
				
				/* only RisikoMassnahmenUmsetzung can be edited */
				if (selectedMassnahmenUmsetzung instanceof
							RisikoMassnahmenUmsetzung) {
					RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung =
							(RisikoMassnahmenUmsetzung) selectedMassnahmenUmsetzung;
					final EditRisikoMassnahmenUmsetzungDialog dialog =
							new EditRisikoMassnahmenUmsetzungDialog(
									composite.getShell(),
									selectedRisikoMassnahmenUmsetzung);
					dialog.open();
					
					viewerMassnahme.refresh();
					packAllMassnahmeColumns();
				}
			}
		});

		/* group the Filter checkboxes with composite */
		Composite compositeFilter = new Composite(composite, SWT.NULL);
		GridLayout gridLayoutSearch = new GridLayout();
		gridLayoutSearch.numColumns = 2;
		compositeFilter.setLayout(gridLayoutSearch);
		GridData gridCompositeFilter = new GridData();
		gridCompositeFilter.horizontalSpan = 4;
		gridCompositeFilter.horizontalAlignment = SWT.RIGHT;
		gridCompositeFilter.verticalAlignment = SWT.TOP;
		compositeFilter.setLayoutData(gridCompositeFilter);

		/* filter button - RisikoMassnahmenUmsetzungen only */
		Button buttonFilterOwnMassnahmen = new Button(compositeFilter, SWT.CHECK);
		buttonFilterOwnMassnahmen.setText("nur eigene Maßnahmen anzeigen");
		GridData gridButtonFilterMassnahmen = new GridData();
		gridButtonFilterMassnahmen.horizontalSpan = 2;
		buttonFilterOwnMassnahmen.setLayoutData(gridButtonFilterMassnahmen);
		
		/* listener adds/removes Filter for own Massnahmen
		 * (RisikoMassnahmenUmsetzungen) */
		buttonFilterOwnMassnahmen.addSelectionListener(new SelectionAdapter() {
			
			/**
	    	 * Adds or removes the Filter for own Massnahmen, depending on the
	    	 * event.
	    	 * 
	    	 * @param event event containing information about the selection
	    	 */
			public void widgetSelected(SelectionEvent event) {
				
				Button thisButton = (Button) event.widget;
				
				if (thisButton.getSelection()) {
					viewerMassnahme
							.addFilter(risikoMassnahmenUmsetzungenFilter);
					viewerMassnahme.refresh();
				} else {
					viewerMassnahme
							.removeFilter(risikoMassnahmenUmsetzungenFilter);
					viewerMassnahme.refresh();
				}
			}
		});

		/* filter button - MassnahmenUmsetzungen only */
		Button buttonFilterBSIMassnahmen = new Button(compositeFilter, SWT.CHECK);
		buttonFilterBSIMassnahmen.setText("nur BSI Maßnahmen anzeigen");
		GridData gridButtonFilterBSIMassnahmen = new GridData();
		gridButtonFilterBSIMassnahmen.horizontalSpan = 2;
		buttonFilterBSIMassnahmen.setLayoutData(gridButtonFilterBSIMassnahmen);
		
		/* listener adds/removes Filter for BSI Massnahmen
		 * (MassnahmenUmsetzungen) */
		buttonFilterBSIMassnahmen.addSelectionListener(new SelectionAdapter() {
			
			/**
	    	 * Adds or removes the Filter for BSI Massnahmen, depending on the
	    	 * event.
	    	 * 
	    	 * @param event event containing information about the selection
	    	 */
			public void widgetSelected(SelectionEvent event) {
				
				Button thisButton = (Button) event.widget;
				
				if (thisButton.getSelection()) {
					viewerMassnahme.addFilter(massnahmenUmsetzungenFilter);
					viewerMassnahme.refresh();
				} else {
					viewerMassnahme.removeFilter(massnahmenUmsetzungenFilter);
					viewerMassnahme.refresh();
				}
			}
		});

		/* filter button - search */
		new Label(compositeFilter, SWT.NULL).setText("suche:");
		Text textFilterSearch = new Text(compositeFilter, SWT.SINGLE | SWT.BORDER);
		GridData gridTextFilterSearch = new GridData();
		gridTextFilterSearch.horizontalAlignment = SWT.FILL;
		textFilterSearch.setLayoutData(gridTextFilterSearch);

		/* listener adds/removes search Filter */
		textFilterSearch.addModifyListener(new ModifyListener() {
			
			/**
	    	 * Adds/removes search Filter when Text is modified.
	    	 * 
	    	 * @param event event containing information about the modify
	    	 */
			public void modifyText(ModifyEvent event) {
				Text text = (Text) event.widget;
				if (text.getText().length() > 0) {
					
					ViewerFilter[] filters = viewerMassnahme.getFilters();
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
						viewerMassnahme.refresh();
						
					} else {
						/* filter is not active - add */
						searchFilter.setPattern(text.getText());
						viewerMassnahme.addFilter(searchFilter);
						viewerMassnahme.refresh();
					}
				} else {
					viewerMassnahme.removeFilter(searchFilter);
					viewerMassnahme.refresh();
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
	 * fills the CheckboxTableViewer with all Gefaehrdungen available
	 */
	
	/**
	 * Fills the TreeViewer with Gefaehrdungen and the TableViewer with
	 * Massnahmen.
	 * Is processed each time the WizardPage is set visible.
	 */
	private void initContents() {

		((RiskAnalysisWizard) getWizard()).addRisikoGefaehrdungsUmsetzungen();
		ArrayList<GefaehrdungsUmsetzung> arrListGefaehrdungsUmsetzungen =
				((RiskAnalysisWizard) getWizard())
						.getNotOKGefaehrdungsUmsetzungen();

		/* root of TreeViewer */
		IGefaehrdungsBaumElement baum = new GefaehrdungsBaumRoot(
				arrListGefaehrdungsUmsetzungen);

		viewerGefaehrdung
				.setLabelProvider(new GefaehrdungTreeViewerLabelProvider());
		viewerGefaehrdung
				.setContentProvider(new GefaehrdungTreeViewerContentProvider());
		viewerGefaehrdung.setInput(baum);
		viewerGefaehrdung.expandAll();

		ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzungen =
				((RiskAnalysisWizard) getWizard())
						.getAllMassnahmenUmsetzungen();

		/* map a domain model object into multiple images and text labels */
		viewerMassnahme
				.setLabelProvider(new MassnahmeTableViewerLabelProvider());
		/* map domain model into array */
		viewerMassnahme.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewerMassnahme.setInput(arrListMassnahmenUmsetzungen);
		packAllMassnahmeColumns();

		// TODO viewer.setSorter(new GefaehrdungenSorter());

		((RiskAnalysisWizard) getWizard()).setCanFinish(true);
	}

	/**
	 * Adjusts all columns of the TableViewer.
	 */
	private void packAllMassnahmeColumns() {
		imageColumnMassnahme.pack();
		numberColumnMassnahme.pack();
		nameColumnMassnahme.pack();
		descriptionColumnMassnahme.pack();
	}

	/**
	 * Deletes a Massnahme from the TreeViewer.
	 * 
	 * @param massnahme the Massnahme to delete
	 */
	private void deleteTreeViewerRisikoMassnahmenUmsetzung(
			RisikoMassnahmenUmsetzung massnahme) {
		
		try {
			GefaehrdungsUmsetzung parent = (GefaehrdungsUmsetzung) massnahme
					.getGefaehrdungsBaumParent();

			if (massnahme != null
					&& massnahme instanceof RisikoMassnahmenUmsetzung
					&& parent != null
					&& parent instanceof GefaehrdungsUmsetzung) {

				/* delete child from List of Children in parent */
				parent.removeGefaehrdungsBaumChild(massnahme);

				/* refresh viewer */
				viewerGefaehrdung.refresh();
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug(e.toString());
		}
	}

	/**
	 * Deletes a self-defined Massnahme from the TableViewer.
	 * 
	 * @param massnahme the Massnahme to delete
	 */
	private void deleteRisikoMassnahmenUmsetzung(
			MassnahmenUmsetzung massnahmenUmsetzung) {
		
		ArrayList<RisikoMassnahmenUmsetzung> arrListRisikoMassnahmenUmsetzungen =
				((RiskAnalysisWizard) getWizard())
						.getAllRisikoMassnahmenUmsetzungen();
		
		ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzungen =
				((RiskAnalysisWizard) getWizard())
					.getAllMassnahmenUmsetzungen();

		try {
			if (arrListRisikoMassnahmenUmsetzungen
					.contains(massnahmenUmsetzung)) {
				// TODO
				// OwnGefaehrdungHome.getInstance().remove((OwnGefaehrdung)delGefaehrdung);

				/* delete from List of RisikoMassnahmenUmsetzungen */
				arrListRisikoMassnahmenUmsetzungen.remove(massnahmenUmsetzung);

				/* delete from List of MassnahmenUmsetzungen */
				if (arrListMassnahmenUmsetzungen.contains(massnahmenUmsetzung)) {
					arrListMassnahmenUmsetzungen.remove(massnahmenUmsetzung);
				}

				// TODO an dieser Stelle müssten eigentlich auch die
				// RisikoMassnahmenUmsetzungen,
				// die duch DND dieser RisikoMassnahmenUmsetzungen in den
				// TreeViewer
				// gelangt sind, geloescht werden..
			}
		} catch (Exception e) {
			ExceptionUtil.log(e,
					"RisikoMassnahmenUmsetzung konnte nicht gelöscht werden.");
		}
	}

	/**
	 * Filter to extract all self-defined Massnahmen in the TableViewer.
	 * 
	 * @author ahanekop@sernet.de
	 */
	class RisikoMassnahmenUmsetzungenFilter extends ViewerFilter {
		
		/**
		 * Returns true, if the given element is a self-defined Massnahme.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof RisikoMassnahmenUmsetzung) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Filter to extract all BSI-Massnahmen in the TableViewer.
	 * 
	 * @author ahanekop@sernet.de
	 */
	class MassnahmenUmsetzungenFilter extends ViewerFilter {
		
		/**
		 * Returns true, if the given element is a BSI-Massnahme from
		 * BSI-Standard 100-3.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (!(element instanceof RisikoMassnahmenUmsetzung)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Filter to extract all Massnahmen matching a given String. 
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
		 * Selects all Massnahmen matching the Pattern.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			
			MassnahmenUmsetzung massnahmeUmsetzung = (MassnahmenUmsetzung) element;
			String title = massnahmeUmsetzung.getTitle();
			Matcher matcher = pattern.matcher(title);

			if (matcher.find()) {
				return true;
			} else {
				return false;
			}
		}
	}
}