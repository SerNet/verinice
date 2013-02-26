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

import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.RemoveMassnahmeFromGefaherdung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsBaumRoot;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.IGefaehrdungsBaumElement;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.SWTElementFactory;

/**
 * Add security measures (Massnahmen) to the risks (Gefaehrdungen). New security
 * measures may be created.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class AdditionalSecurityMeasuresPage extends WizardPage {

	private Composite composite;
	private TableViewer viewerMassnahme;
	private TableColumn imageColumnMassnahme;
	private TableColumn numberColumnMassnahme;
	private TableColumn nameColumnMassnahme;
	private TableColumn descriptionColumnMassnahme;
	private TreeViewer viewerGefaehrdung;

	private MassnahmenUmsetzungenFilter massnahmenUmsetzungenFilter = new MassnahmenUmsetzungenFilter();
	private RisikoMassnahmenUmsetzungenFilter risikoMassnahmenUmsetzungenFilter = new RisikoMassnahmenUmsetzungenFilter();
	private SearchFilter searchFilter = new SearchFilter();

	/**
	 * Constructor sets title an description of WizardPage.
	 */
	protected AdditionalSecurityMeasuresPage() {
		super(Messages.AdditionalSecurityMeasuresPage_0);
		setTitle(Messages.AdditionalSecurityMeasuresPage_1);
		setDescription(Messages.AdditionalSecurityMeasuresPage_2);
	}

	/**
	 * Adds widgets to the wizardPage. Called once at startup of Wizard.
	 * 
	 * @param parent
	 *            the parent Composite
	 */
	public void createControl(Composite parent) {
	    
	    final int gridColumnAmount = 4;
	    final int gefaehrdungGridColumnAmount = 3;
	    final int buttonMnGridColumnAmount = gefaehrdungGridColumnAmount;
	    final int gridCompositeHorizontalSpan = gridColumnAmount;
	    final int imageCMWidth = 35;
	    final int numberCMWidth = 100;
	    final int nameCMWidth = numberCMWidth;
	    final int descriptionCMWidth = 200;
	    
		composite = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = gridColumnAmount;
		composite.setLayout(gridLayout);
		setControl(composite);

		/* TreeViewer: Gefaehrdungen */
		viewerGefaehrdung = new TreeViewer(composite, SWT.MULTI);
		GridData gridViewerGefaehrdung = new GridData();
		gridViewerGefaehrdung.grabExcessHorizontalSpace = true;
		gridViewerGefaehrdung.grabExcessVerticalSpace = true;
		gridViewerGefaehrdung.horizontalSpan = 2;
		gridViewerGefaehrdung.horizontalAlignment = SWT.FILL;
		gridViewerGefaehrdung.verticalAlignment = SWT.FILL;
		viewerGefaehrdung.getTree().setLayoutData(gridViewerGefaehrdung);

		/* TableViewer: Massnahmen */
		viewerMassnahme = new TableViewer(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
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
		imageColumnMassnahme.setText(""); //$NON-NLS-1$
		imageColumnMassnahme.setWidth(imageCMWidth);

		numberColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		numberColumnMassnahme.setText(Messages.AdditionalSecurityMeasuresPage_4);
		numberColumnMassnahme.setWidth(numberCMWidth);

		nameColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		nameColumnMassnahme.setText(Messages.AdditionalSecurityMeasuresPage_5);
		nameColumnMassnahme.setWidth(nameCMWidth);

		descriptionColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		descriptionColumnMassnahme.setText(Messages.AdditionalSecurityMeasuresPage_6);
		descriptionColumnMassnahme.setWidth(descriptionCMWidth);

		/* listener opens edit Dialog for selected Massnahme */
		viewerMassnahme.addDoubleClickListener(new IDoubleClickListener() {

			/**
			 * Notifies of a double click.
			 * 
			 * @param event
			 *            event object describing the double-click
			 */
			public void doubleClick(DoubleClickEvent event) {
				/* retrieve selected Massnahme and open edit dialog with it */
				editMassnahme();
			}
		});

		/* add drag and drop support */
		CnATreeElement cnaElement = ((RiskAnalysisWizard) getWizard()).getFinishedRiskAnalysis();
		/*
		 * note: this Transfer is not used for data transfer, but for fulfilling
		 * parameter needs of addDropSupport and addDragSupport. The actual data
		 * transfer ins realized through DNDItems in
		 * RisikoMassnahmenUmsetzungDragListener and
		 * RisikoMassnahmenUmsetzungDropListener
		 */
		Transfer[] types = new Transfer[] { RisikoMassnahmenUmsetzungTransfer.getInstance() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;

		viewerGefaehrdung.addDropSupport(operations, types, new RisikoMassnahmenUmsetzungDropListener(viewerGefaehrdung));

		viewerMassnahme.addDragSupport(operations, types, new RisikoMassnahmenUmsetzungDragListener(viewerMassnahme, cnaElement));

		/* group the buttons for viewerGefaehrdung with group */
		Group groupButtonsGefaehrdung = new Group(composite, SWT.SHADOW_ETCHED_OUT);
		groupButtonsGefaehrdung.setText(Messages.AdditionalSecurityMeasuresPage_7);
		GridLayout gridLayoutButtonsGefaehrdung = new GridLayout();
		gridLayoutButtonsGefaehrdung.numColumns = gefaehrdungGridColumnAmount;
		groupButtonsGefaehrdung.setLayout(gridLayoutButtonsGefaehrdung);
		GridData gridGroupButtonsGefaehrdung = new GridData();
		gridGroupButtonsGefaehrdung.horizontalSpan = 2;
		gridGroupButtonsGefaehrdung.horizontalAlignment = SWT.LEFT;
		gridGroupButtonsGefaehrdung.verticalAlignment = SWT.TOP;
		groupButtonsGefaehrdung.setLayoutData(gridGroupButtonsGefaehrdung);

		/* listener deletes the selected Massnahme after confirmation */
		SelectionAdapter deleteGefaehrdungAdapter = new SelectionAdapter() {
		    
		    /**
		     * Deletes the selected Massnahme after confirmation.
		     * 
		     * @param event
		     *            event containing information about the selection
		     */
		    @Override
		    public void widgetSelected(SelectionEvent event) {
		        IStructuredSelection selection = (IStructuredSelection) viewerGefaehrdung.getSelection();
		        RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung = (RisikoMassnahmenUmsetzung) selection.getFirstElement();
		        
		        /* ask user to confirm */
		        boolean confirmed = MessageDialog.openQuestion(composite.getShell(), 
		                Messages.AdditionalSecurityMeasuresPage_9, 
		                NLS.bind(Messages.AdditionalSecurityMeasuresPage_10, selectedRisikoMassnahmenUmsetzung.getTitle()));
		        if (confirmed) {
		            deleteTreeViewerRisikoMassnahmenUmsetzung(selectedRisikoMassnahmenUmsetzung);
		            viewerGefaehrdung.refresh();
		        }
		    }
		};
		/* delete button for viewerGefaehrdung */
		Button buttonDeleteGefaehrdung = SWTElementFactory.generatePushButton(groupButtonsGefaehrdung, Messages.AdditionalSecurityMeasuresPage_8, null, deleteGefaehrdungAdapter);
		GridData gridDeleteGefaehrdung = new GridData();
		buttonDeleteGefaehrdung.setLayoutData(gridDeleteGefaehrdung);


		/* group the buttons for viewerMassnahme with group */
		Group groupButtonsMassnahme = new Group(composite, SWT.SHADOW_ETCHED_OUT);
		groupButtonsMassnahme.setText(Messages.AdditionalSecurityMeasuresPage_12);
		GridLayout gridLayoutButtonsMassnahme = new GridLayout();
		gridLayoutButtonsMassnahme.numColumns = buttonMnGridColumnAmount;
		groupButtonsMassnahme.setLayout(gridLayoutButtonsMassnahme);
		GridData gridGroupButtonsMassnahme = new GridData();
		gridGroupButtonsMassnahme.horizontalSpan = 2;
		gridGroupButtonsMassnahme.horizontalAlignment = SWT.RIGHT;
		gridGroupButtonsMassnahme.verticalAlignment = SWT.TOP;
		groupButtonsMassnahme.setLayoutData(gridGroupButtonsMassnahme);

		/* listener opens Dialog for creating a new Massnahme */
		SelectionAdapter newMassnahmeAdapter = new SelectionAdapter() {
		    
		    /**
		     * Opens dialog for creating an new Massnahme of type
		     * RisikoMassnahmenUmsetzung and adds it to the List of
		     * RisikoMassnahmenUmsetzungen and the viewer.
		     * 
		     * @param event
		     *            event containing information about the selection
		     */
		    @Override
		    public void widgetSelected(SelectionEvent event) {
		        
		        /* create new RisikoMassnahmenUmsetzung */
		        final NewRisikoMassnahmeDialog dialog = new NewRisikoMassnahmeDialog(composite.getShell());
		        int result = dialog.open();
		        
		        if (result == Window.OK) {
		            /* add new RisikoMassnahmenUmsetzung to List and viewer */
		            ((RiskAnalysisWizard) getWizard()).addRisikoMassnahmeUmsetzung(dialog.getNewRisikoMassnahme());
		            viewerMassnahme.refresh();
		            packAllMassnahmeColumns();
		        }
		    }
		};
		/* new button */
		Button buttonNewMassnahme = SWTElementFactory.generatePushButton(groupButtonsMassnahme, Messages.AdditionalSecurityMeasuresPage_13, null, newMassnahmeAdapter);
		GridData gridButtonMassnahmeNew = new GridData();
		buttonNewMassnahme.setLayoutData(gridButtonMassnahmeNew);


		/* listener deletes the selected Massnahme after confirmation */
		SelectionAdapter deleteMassnahmeAdapter = new SelectionAdapter() {
		    
		    /**
		     * Deletes the selected Massnahme after confirmation.
		     * 
		     * @param event
		     *            event containing information about the selection
		     */
		    @Override
		    public void widgetSelected(SelectionEvent event) {
		        IStructuredSelection selection = (IStructuredSelection) viewerMassnahme.getSelection();
		        MassnahmenUmsetzung selectedMassnahmenUmsetzung = (MassnahmenUmsetzung) selection.getFirstElement();
		        
		        /* only RisikoMassnahmenUmsetzungen can be deleted */
		        if (selectedMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) {
		            RisikoMassnahmenUmsetzung rsUmsetzung = (RisikoMassnahmenUmsetzung) selectedMassnahmenUmsetzung;
		            
		            /* ask user to confirm */
		            boolean confirmed = MessageDialog.openQuestion(composite.getShell(), 
		                    Messages.AdditionalSecurityMeasuresPage_15,
		                    NLS.bind(Messages.AdditionalSecurityMeasuresPage_10, selectedMassnahmenUmsetzung.getTitle()));
		            /* delete */
		            if (confirmed) {
		                deleteRisikoMassnahmenUmsetzung(rsUmsetzung);
		                viewerMassnahme.refresh();
		            }
		        }
		    }
		};
		/* delete button */
		Button buttonDeleteMassnahme = SWTElementFactory.generatePushButton(groupButtonsMassnahme, Messages.AdditionalSecurityMeasuresPage_14, null, deleteMassnahmeAdapter);
		GridData gridButtonMassnahmeDelete = new GridData();
		buttonDeleteMassnahme.setLayoutData(gridButtonMassnahmeDelete);

		/* listener opens edit Dialog for the selected Massnahme */
		SelectionAdapter editMassnahmeAdapter = new SelectionAdapter() {
		    
		    @Override
		    public void widgetSelected(SelectionEvent event) {
		        editMassnahme();
		    }
		};
		/* edit button */
		Button buttonEditMassnahme = SWTElementFactory.generatePushButton(groupButtonsGefaehrdung, Messages.AdditionalSecurityMeasuresPage_18, null, editMassnahmeAdapter);
		GridData gridButtonEditMassnahme = new GridData();
		buttonEditMassnahme.setLayoutData(gridButtonEditMassnahme);


		/* group the Filter checkboxes with composite */
		Composite compositeFilter = new Composite(composite, SWT.NULL);
		GridLayout gridLayoutSearch = new GridLayout();
		gridLayoutSearch.numColumns = 2;
		compositeFilter.setLayout(gridLayoutSearch);
		GridData gridCompositeFilter = new GridData();
		gridCompositeFilter.horizontalSpan = gridCompositeHorizontalSpan;
		gridCompositeFilter.horizontalAlignment = SWT.RIGHT;
		gridCompositeFilter.verticalAlignment = SWT.TOP;
		compositeFilter.setLayoutData(gridCompositeFilter);

		/*
		 * listener adds/removes Filter for own Massnahmen
		 * (RisikoMassnahmenUmsetzungen)
		 */
		SelectionAdapter filterOwnMassnahmenAdapter = new SelectionAdapter() {
		    
		    /**
		     * Adds or removes the Filter for own Massnahmen, depending on the
		     * event.
		     * 
		     * @param event
		     *            event containing information about the selection
		     */
		    @Override
		    public void widgetSelected(SelectionEvent event) {
		        
		        Button thisButton = (Button) event.widget;
		        
		        if (thisButton.getSelection()) {
		            viewerMassnahme.addFilter(risikoMassnahmenUmsetzungenFilter);
		            viewerMassnahme.refresh();
		        } else {
		            viewerMassnahme.removeFilter(risikoMassnahmenUmsetzungenFilter);
		            viewerMassnahme.refresh();
		        }
		    }
		};
		/* filter button - RisikoMassnahmenUmsetzungen only */
		Button buttonFilterOwnMassnahmen = SWTElementFactory.generateCheckboxButton(compositeFilter, Messages.AdditionalSecurityMeasuresPage_19, null, filterOwnMassnahmenAdapter);
		GridData gridButtonFilterMassnahmen = new GridData();
		gridButtonFilterMassnahmen.horizontalSpan = 2;
		buttonFilterOwnMassnahmen.setLayoutData(gridButtonFilterMassnahmen);

		/*
		 * listener adds/removes Filter for BSI Massnahmen
		 * (MassnahmenUmsetzungen)
		 */
		SelectionAdapter filterBSIMassnahmenAdapter = new SelectionAdapter() {
		    
		    /**
		     * Adds or removes the Filter for BSI Massnahmen, depending on the
		     * event.
		     * 
		     * @param event
		     *            event containing information about the selection
		     */
		    @Override
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
		};

		/* filter button - MassnahmenUmsetzungen only */
		Button buttonFilterBSIMassnahmen = SWTElementFactory.generateCheckboxButton(compositeFilter, Messages.AdditionalSecurityMeasuresPage_20, null, filterBSIMassnahmenAdapter);
		GridData gridButtonFilterBSIMassnahmen = new GridData();
		gridButtonFilterBSIMassnahmen.horizontalSpan = 2;
		buttonFilterBSIMassnahmen.setLayoutData(gridButtonFilterBSIMassnahmen);

		/* filter button - search */
		new Label(compositeFilter, SWT.NULL).setText(Messages.AdditionalSecurityMeasuresPage_21);
		Text textFilterSearch = new Text(compositeFilter, SWT.SINGLE | SWT.BORDER);
		GridData gridTextFilterSearch = new GridData();
		gridTextFilterSearch.horizontalAlignment = SWT.FILL;
		textFilterSearch.setLayoutData(gridTextFilterSearch);

		/* listener adds/removes search Filter */
		textFilterSearch.addModifyListener(new ModifyListener() {

			/**
			 * Adds/removes search Filter when Text is modified.
			 * 
			 * @param event
			 *            event containing information about the modify
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
	 * @param visible
	 *            boolean indicating if content should be visible
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
	 * Massnahmen. Is processed each time the WizardPage is set visible.
	 */
	private void initContents() {

		List<GefaehrdungsUmsetzung> arrListGefaehrdungsUmsetzungen = ((RiskAnalysisWizard) getWizard()).getNotOKGefaehrdungsUmsetzungen();

		/* root of TreeViewer */
		IGefaehrdungsBaumElement baum = new GefaehrdungsBaumRoot(arrListGefaehrdungsUmsetzungen);

		viewerGefaehrdung.setLabelProvider(new GefaehrdungTreeViewerLabelProvider());
		viewerGefaehrdung.setContentProvider(new GefaehrdungTreeViewerContentProvider());
		viewerGefaehrdung.setInput(baum);
		viewerGefaehrdung.expandAll();

		ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzungen = (ArrayList<MassnahmenUmsetzung>)((RiskAnalysisWizard) getWizard()).getAllMassnahmenUmsetzungen();

		/* map a domain model object into multiple images and text labels */
		viewerMassnahme.setLabelProvider(new MassnahmeTableViewerLabelProvider());
		/* map domain model into array */
		viewerMassnahme.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewerMassnahme.setInput(arrListMassnahmenUmsetzungen);
		viewerMassnahme.setSorter(new MassnahmenSorter());
		packAllMassnahmeColumns();

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
	 * @param massnahme
	 *            the Massnahme to delete
	 */
	private void deleteTreeViewerRisikoMassnahmenUmsetzung(RisikoMassnahmenUmsetzung massnahme) {

		try {
			GefaehrdungsUmsetzung parent = (GefaehrdungsUmsetzung) massnahme.getParent();

			if (massnahme instanceof RisikoMassnahmenUmsetzung && parent instanceof GefaehrdungsUmsetzung) {

				RemoveMassnahmeFromGefaherdung command = new RemoveMassnahmeFromGefaherdung(parent, massnahme);
				command = ServiceFactory.lookupCommandService().executeCommand(command);
				parent = command.getParent();

				parent.getChildren().remove(massnahme);
				/* refresh viewer */
				GefaehrdungsBaumRoot baumElement = (GefaehrdungsBaumRoot) viewerGefaehrdung.getInput();
				baumElement.replaceChild(parent);
				viewerGefaehrdung.refresh();
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug(e.toString());
		}
	}

	/**
	 * Deletes a self-defined Massnahme from the TableViewer.
	 * 
	 * @param massnahme
	 *            the Massnahme to delete
	 */
	private void deleteRisikoMassnahmenUmsetzung(RisikoMassnahmenUmsetzung risikoMassnahmenUmsetzung) {

		ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzungen = (ArrayList<MassnahmenUmsetzung>)((RiskAnalysisWizard) getWizard()).getAllMassnahmenUmsetzungen();

		try {
			/* delete from List of MassnahmenUmsetzungen */
			if (arrListMassnahmenUmsetzungen.contains(risikoMassnahmenUmsetzung)) {
				arrListMassnahmenUmsetzungen.remove(risikoMassnahmenUmsetzung);
				RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(risikoMassnahmenUmsetzung);
				RisikoMassnahmeHome.getInstance().remove(risikoMassnahmenUmsetzung.getRisikoMassnahme());
			}

			// TODO an dieser Stelle müssten eigentlich auch die
			// RisikoMassnahmenUmsetzungen,
			// die duch DND dieser RisikoMassnahmenUmsetzungen in den
			// TreeViewer
			// gelangt sind, geloescht werden..
		} catch (Exception e) {
			ExceptionUtil.log(e, Messages.AdditionalSecurityMeasuresPage_22);
		}
	}

	protected void editMassnahme() {
		IStructuredSelection selection = (IStructuredSelection) viewerMassnahme.getSelection();
		MassnahmenUmsetzung selectedMassnahmenUmsetzung = (MassnahmenUmsetzung) selection.getFirstElement();
		if (selectedMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) {
			RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung = (RisikoMassnahmenUmsetzung) selectedMassnahmenUmsetzung;
			final EditRisikoMassnahmenUmsetzungDialog dialog = new EditRisikoMassnahmenUmsetzungDialog(composite.getShell(), selectedRisikoMassnahmenUmsetzung);
			int result = dialog.open();
			if (result == Window.OK) {
				((RiskAnalysisWizard) getWizard()).replaceMassnahmenUmsetzung(dialog.getRisikoMassnahmenUmsetzung());
				viewerMassnahme.refresh();
				packAllMassnahmeColumns();
			}

		}
	}

	/**
	 * Filter to extract all self-defined Massnahmen in the TableViewer.
	 * 
	 * @author ahanekop[at]sernet[dot]de
	 */
	class RisikoMassnahmenUmsetzungenFilter extends ViewerFilter {

		/**
		 * Returns true, if the given element is a self-defined Massnahme.
		 * 
		 * @param viewer
		 *            the Viewer to operate on
		 * @param parentElement
		 *            not used
		 * @param element
		 *            given element
		 * @return true if element passes test, false else
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return element instanceof RisikoMassnahmenUmsetzung;
		}
	}

	/**
	 * Filter to extract all BSI-Massnahmen in the TableViewer.
	 * 
	 * @author ahanekop[at]sernet[dot]de
	 */
	class MassnahmenUmsetzungenFilter extends ViewerFilter {

		/**
		 * Returns true, if the given element is a BSI-Massnahme from
		 * BSI-Standard 100-3.
		 * 
		 * @param viewer
		 *            the Viewer to operate on
		 * @param parentElement
		 *            not used
		 * @param element
		 *            given element
		 * @return true if element passes test, false else
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return !(element instanceof RisikoMassnahmenUmsetzung);
		}
	}

	/**
	 * Filter to extract all Massnahmen matching a given String.
	 * 
	 * @author ahanekop[at]sernet[dot]de
	 */
	class SearchFilter extends ViewerFilter {

		private Pattern pattern;

		/**
		 * Updates the Pattern.
		 * 
		 * @param searchString
		 *            the String to search for
		 */
		void setPattern(String searchString) {

			pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
		}

		/**
		 * Selects all Massnahmen matching the Pattern.
		 * 
		 * @param viewer
		 *            the Viewer to operate on
		 * @param parentElement
		 *            not used
		 * @param element
		 *            given element
		 * @return true if element passes test, false else
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			MassnahmenUmsetzung massnahmeUmsetzung = (MassnahmenUmsetzung) element;
			String title = massnahmeUmsetzung.getTitle();
			Matcher matcher = pattern.matcher(title);

			return matcher.find();
		}
	}
}
