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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.Transfer;
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
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


/**
 * WizardPage - add security measures to risks.
 * 
 * @author ahanekop@sernet.de
 *
 */

public class AdditionalSecurityMeasuresPage extends WizardPage {

	private Composite container;
	private TreeViewer viewerGefaehrdung;
	private TableViewer viewerMassnahme;
	
	private TableColumn imgColumnGefaehrdung;
	private TableColumn numberColumnGefaehrdung;
	private TableColumn nameColumnGefaehrdung;
	private TableColumn descrColumnGefaehrdung;
	
	private TableColumn imgColumnMassnahme;
	private TableColumn numberColumnMassnahme;
	private TableColumn nameColumnMassnahme;
	private TableColumn descrColumnMassnahme;
	
	private RisikoMassnahmenUmsetzungenFilter risikoMassnahmenUmsetzungenFilter = new RisikoMassnahmenUmsetzungenFilter();
	private MassnahmenUmsetzungenFilter massnahmenUmsetzungenFilter = new MassnahmenUmsetzungenFilter();
	private SearchFilter searchFilter = new SearchFilter();
	
	protected AdditionalSecurityMeasuresPage() {
		super("Zusätzliche IT-Sicherheitsmaßnahmen");
		setTitle("Zusätzliche IT-Sicherheitsmaßnahmen");
		setDescription("Fügen Sie den Gefährdungen weitere" +
				" IT-Sicherheitsmaßnahmen hinzu. Legen Sie ggf." +
				" zusätzlich eigene Maßnahmen an.");
	}
	
	/* must be implemented - content of wizard page! */
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		container.setLayout(gridLayout);
		setControl(container);
		
		/* TreeViewer: Gefaehrdungen */
		viewerGefaehrdung = new TreeViewer(container, SWT.SINGLE);
		GridData data14 = new GridData();
		data14.grabExcessHorizontalSpace = true;
		data14.grabExcessVerticalSpace = true;
		data14.horizontalSpan = 2;
		data14.horizontalAlignment = SWT.FILL;
		data14.verticalAlignment = SWT.FILL;
		viewerGefaehrdung.getTree().setLayoutData(data14);
		
		/* TableViewer: Massnahmen */
		viewerMassnahme = new TableViewer(container, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		
		final Table tableMassnahme = viewerMassnahme.getTable();
		GridData data2 = new GridData();
		data2.grabExcessHorizontalSpace = true;
		data2.grabExcessVerticalSpace = true;
		data2.horizontalSpan = 2;
		data2.horizontalAlignment = SWT.FILL;
		data2.verticalAlignment = SWT.FILL;
		tableMassnahme.setLayoutData(data2);
		tableMassnahme.setHeaderVisible(true);
		tableMassnahme.setLinesVisible(true);
		
		imgColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		imgColumnMassnahme.setText("");
		imgColumnMassnahme.setWidth(35);
		
		nameColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		nameColumnMassnahme.setText("Name");
		nameColumnMassnahme.setWidth(100);
		
		descrColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
		descrColumnMassnahme.setText("Beschreibung");
		descrColumnMassnahme.setWidth(200);
		
		/**
		 *  listener opens edit Dialog for selected Gefaehrdung
		 */
		viewerMassnahme.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				/* retrieve selected element and open edit dialog with it */
				IStructuredSelection selection = (IStructuredSelection) viewerMassnahme
						.getSelection();
				MassnahmenUmsetzung selectedMassnahmenUmsetzung = (MassnahmenUmsetzung) selection
						.getFirstElement();
				if (selectedMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) {
					RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung =
						(RisikoMassnahmenUmsetzung) selectedMassnahmenUmsetzung;
					final EditRisikoMassnahmenUmsetzungDialog dialog = new EditRisikoMassnahmenUmsetzungDialog(
							container.getShell(),
							selectedRisikoMassnahmenUmsetzung);
					dialog.open();
					viewerMassnahme.refresh();
					packAllMassnahmeColumns();
				}
		    }
		});
		
		/* add drag and drop support */
		CnATreeElement cnaElement = ((RisikoanalyseWizard)getWizard()).getCnaElement();
		Transfer[] types = new Transfer[] { RisikoMassnahmenUmsetzungTransfer.getInstance() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		viewerGefaehrdung.addDropSupport(operations, types,
				new RisikoMassnahmenUmsetzungDropListener(viewerGefaehrdung));
		viewerMassnahme.addDragSupport(operations, types,
				new RisikoMassnahmenUmsetzungDragListener(viewerMassnahme,cnaElement));
		
		/* group the buttons for viewerGefaehrdung with group */
	    Group groupButtonsGefaehrdung = new Group(container, SWT.SHADOW_ETCHED_OUT);
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
	    Button buttonDeleteGefaehrdung = new Button(groupButtonsGefaehrdung, SWT.PUSH);
	    buttonDeleteGefaehrdung.setText("löschen");
	    GridData gridDeleteGefaehrdung = new GridData();
	    buttonDeleteGefaehrdung.setLayoutData(gridDeleteGefaehrdung);
	    buttonDeleteGefaehrdung.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewerGefaehrdung
						.getSelection();
				RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung = (RisikoMassnahmenUmsetzung) selection
						.getFirstElement();

				/* ask user to confirm */
				boolean confirmed = MessageDialog.openQuestion(container
						.getShell(), "Bestätigung",
						"Wollen Sie die Massnahme mit dem Titel \""
								+ selectedRisikoMassnahmenUmsetzung.getTitle()
								+ "\" wirklich löschen?");
				if (confirmed) {
					deleteTreeViewerRisikoMassnahmenUmsetzung(selectedRisikoMassnahmenUmsetzung);
					viewerGefaehrdung.refresh();
				}
			}
		});
		
		/* group the buttons for viewerMassnahme with group */
	    Group groupButtonsMassnahme = new Group(container, SWT.SHADOW_ETCHED_OUT);
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
	    Button button2 = new Button(groupButtonsMassnahme, SWT.PUSH);
	    button2.setText("neu");
	    GridData data4 = new GridData();
	    button2.setLayoutData(data4);
	    button2.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		
	    		ArrayList<RisikoMassnahmenUmsetzung> arrListRisikoMassnahmenUmsetzung = 
	    			((RisikoanalyseWizard)getWizard()).getAllRisikoMassnahmenUmsetzungen();
	    		
	    		/* Add new RisikoMassnahmenUmsetzungen to viewer */
	    		
	    		final NewRisikoMassnahmenUmsetzungDialog dialog = new NewRisikoMassnahmenUmsetzungDialog(
						container.getShell(), arrListRisikoMassnahmenUmsetzung,
						((RisikoanalyseWizard)getWizard()).getCnaElement());
	    		dialog.open();
	    		((RisikoanalyseWizard)getWizard()).addRisikoMassnahmenUmsetzungen();
	    		viewerMassnahme.refresh();
	    		packAllMassnahmeColumns();
	    	}
	    });
	    
	    /* delete button */
	    Button button3 = new Button(groupButtonsMassnahme, SWT.PUSH);
	    button3.setText("löschen");
	    GridData data5 = new GridData();
	    button3.setLayoutData(data5);
	    button3.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewerMassnahme
						.getSelection();
				MassnahmenUmsetzung selectedMassnahmenUmsetzung = (MassnahmenUmsetzung) selection
				.getFirstElement();
				
				if (selectedMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) {
				/* only RisikoMassnahmenUmsetzungen can be deleted */

				/* ask user to confirm */
				boolean confirmed = MessageDialog.openQuestion(container
						.getShell(), "Bestätigung",
						"Wollen Sie die Massnahme mit dem Titel \""
								+ selectedMassnahmenUmsetzung.getTitle()
								+ "\" wirklich löschen?");
				if (confirmed) {
					deleteRisikoMassnahmenUmsetzung(selectedMassnahmenUmsetzung);
					viewerMassnahme.refresh();
				}
				ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzung = 
	    			((RisikoanalyseWizard)getWizard()).getAllMassnahmenUmsetzungen();
				ArrayList<RisikoMassnahmenUmsetzung> arrListRisikoMassnahmenUmsetzung = 
	    			((RisikoanalyseWizard)getWizard()).getAllRisikoMassnahmenUmsetzungen();
				Logger.getLogger(this.getClass()).debug(
						"#MU: " + arrListMassnahmenUmsetzung.size() +
						" #RMU: " + arrListRisikoMassnahmenUmsetzung.size());
				}
			}
		});
	    
	    /* edit button */
	    Button button4 = new Button(groupButtonsMassnahme, SWT.PUSH);
	    button4.setText("bearbeiten");
	    GridData data10 = new GridData();
	    button4.setLayoutData(data10);
	    button4.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
				/* retrieve selected element and open edit dialog with it */
				IStructuredSelection selection = (IStructuredSelection) viewerMassnahme
						.getSelection();
				MassnahmenUmsetzung selectedMassnahmenUmsetzung = (MassnahmenUmsetzung) selection
						.getFirstElement();
				if (selectedMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) {
					RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung =
						(RisikoMassnahmenUmsetzung) selectedMassnahmenUmsetzung;
					final EditRisikoMassnahmenUmsetzungDialog dialog = new EditRisikoMassnahmenUmsetzungDialog(
							container.getShell(),
							selectedRisikoMassnahmenUmsetzung);
					dialog.open();
					viewerMassnahme.refresh();
					packAllMassnahmeColumns();
				}
			}
	    });
	    
	    /* group the Filter checkboxes with composite */
		Composite compositeFilter = new Composite(container, SWT.NULL);
		GridLayout gridLayoutSearch = new GridLayout();
        gridLayoutSearch.numColumns = 2;
        compositeFilter.setLayout(gridLayoutSearch);
        GridData data12 = new GridData();
        data12.horizontalSpan = 4;
        data12.horizontalAlignment = SWT.RIGHT;
        data12.verticalAlignment = SWT.TOP;
	    compositeFilter.setLayoutData(data12);
		
	    /* filter button - RisikoMassnahmenUmsetzungen only */
	    Button button5 = new Button(compositeFilter, SWT.CHECK);
	    button5.setText("nur eigene Maßnahmen anzeigen");
	    GridData data7 = new GridData();
	    data7.horizontalSpan = 2;
	    button5.setLayoutData(data7);
	    button5.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		Button thisButton = (Button) event.widget;
	    		if(thisButton.getSelection()){
	    			viewerMassnahme.addFilter(risikoMassnahmenUmsetzungenFilter);
	    			viewerMassnahme.refresh();
	    		} else {
	    			viewerMassnahme.removeFilter(risikoMassnahmenUmsetzungenFilter);
	    			viewerMassnahme.refresh();
	    		}
	    	}
	    });
	    
	    /* filter button - MassnahmenUmsetzungen only */
	    Button button6 = new Button(compositeFilter, SWT.CHECK);
	    button6.setText("nur BSI Maßnahmen anzeigen");
	    GridData data8 = new GridData();
	    data8.horizontalSpan = 2;
	    button6.setLayoutData(data8);
	    button6.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		Button thisButton = (Button) event.widget;
	    		if(thisButton.getSelection()){
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
	    Text search = new Text(compositeFilter, SWT.SINGLE | SWT.BORDER);
	    GridData data9 = new GridData();
	    data9.horizontalAlignment = SWT.FILL;
	    search.setLayoutData(data9);
	    
	    search.addModifyListener(new ModifyListener(){
	    	public void modifyText(ModifyEvent event) {
	    		Text text = (Text) event.widget;
	    		if (text.getText().length() > 0) {
	    			searchFilter.setPattern(text.getText());
	    			viewerMassnahme.addFilter(searchFilter);
	    			viewerMassnahme.refresh();
	    		} else {
	    			viewerMassnahme.removeFilter(searchFilter);
	    			viewerMassnahme.refresh();
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
		
		((RisikoanalyseWizard)getWizard()).addRisikoGefaehrdungsUmsetzungen();
		
		ArrayList<GefaehrdungsUmsetzung> arrListGefaehrdungsUmsetzungen = 
			((RisikoanalyseWizard)getWizard()).getRisikoGefaehrdungsUmsetzungen();
		
		/* root of TreeViewer */
		IGefaehrdungsBaumElement baum = new GefaehrdungsBaumRoot(arrListGefaehrdungsUmsetzungen);
		
		viewerGefaehrdung.setLabelProvider(new GefaehrdungTreeViewerLabelProvider());
		viewerGefaehrdung.setContentProvider(new GefaehrdungTreeViewerContentProvider());
		viewerGefaehrdung.setInput(baum);
		// viewerGefaehrdung.expandAll();
		
		ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzungen = 
			((RisikoanalyseWizard)getWizard()).getAllMassnahmenUmsetzungen();
		
		/* map a domain model object into multiple images and text labels */
		viewerMassnahme.setLabelProvider(new MassnahmeTableViewerLabelProvider());
		/* map domain model into array */
		viewerMassnahme.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewerMassnahme.setInput(arrListMassnahmenUmsetzungen);
		packAllMassnahmeColumns();
		
		// TODO viewer.setSorter(new GefaehrdungenSorter());
		
		// vs. overriding method WizardPage.canFilpToNextPage
		((RisikoanalyseWizard)getWizard()).setCanFinish(true);
		// TODO wird das benötigt? s.u.
		// checkPageComplete();
	}

	/**
	 * packs all columns of TableViewer containing MassnahmeUmsetzungen
	 */
	private void packAllMassnahmeColumns() {
		imgColumnMassnahme.pack();
		nameColumnMassnahme.pack();
		descrColumnMassnahme.pack();
	}
	
	/**
	 * activates next button if List of selected Gefaehrdungen is not empty
	 */
	/*
	// TODO klären, ob das benötigt wird
	private void checkPageComplete() {
		if (((RisikoanalyseWizard)getWizard()).getAssociatedGefaehrdungen().isEmpty()) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}
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
	
	private void deleteRisikoMassnahmenUmsetzung(
			MassnahmenUmsetzung massnahmenUmsetzung) {
		ArrayList<RisikoMassnahmenUmsetzung> arrListRisikoMassnahmenUmsetzungen = ((RisikoanalyseWizard) getWizard())
				.getAllRisikoMassnahmenUmsetzungen();

		ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzungen = ((RisikoanalyseWizard) getWizard())
				.getAllMassnahmenUmsetzungen();

		try {
			if (arrListRisikoMassnahmenUmsetzungen
					.contains(massnahmenUmsetzung)) {
				// TODO
				// OwnGefaehrdungHome.getInstance().remove((OwnGefaehrdung)delGefaehrdung);

				/* delete from List of RisikoMassnahmenUmsetzungen */
				arrListRisikoMassnahmenUmsetzungen
				.remove(massnahmenUmsetzung);
				
				/* delete from List of MassnahmenUmsetzungen */
				if (arrListMassnahmenUmsetzungen.contains(massnahmenUmsetzung)) {
					arrListMassnahmenUmsetzungen.remove(massnahmenUmsetzung);
				}

				// TODO an dieser Stelle müssten eigentlich auch die
				// RisikoMassnahmenUmsetzungen,
				// die duch DND diser RisikoMassnahmenUmsetzungen in den
				// TreeViewer
				// gelangt sind, geloescht werden..
			}
		} catch (Exception e) {
			ExceptionUtil.log(e,
					"RisikoMassnahmenUmsetzung konnte nicht gelöscht werden.");
		}
	}
	
	class RisikoMassnahmenUmsetzungenFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof RisikoMassnahmenUmsetzung) {
				return true;
			} else {
				return false;
			}
		}
	}

	class MassnahmenUmsetzungenFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (!(element instanceof RisikoMassnahmenUmsetzung)) {
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