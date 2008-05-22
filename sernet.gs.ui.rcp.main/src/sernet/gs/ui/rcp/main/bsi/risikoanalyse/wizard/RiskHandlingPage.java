package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.CellEditor;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
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
import org.eclipse.swt.widgets.Item;
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

public class RiskHandlingPage extends WizardPage {

	private Composite container;
	private TableColumn checkboxColumn;
	private TableColumn imgColumn;
	private TableColumn numberColumn;
	private TableColumn nameColumn;
	private TableColumn choiceColumn;
	private TableViewer viewer;
	private SearchFilter searchFilter = new SearchFilter();
	
	public static final String IMG_COLUMN_ID = "image";
	public static final String NUMBER_COLUMN_ID = "number";
	public static final String NAME_COLUMN_ID = "name";
	public static final String CHOICE_COLUMN_ID = "choice";
	
	protected RiskHandlingPage() {
		super("Risikobehandlung");
		setTitle("Risikobehandlung");
		setDescription("Entscheiden Sie, wie mit den Gefährdungen umgegangen werden soll.");
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
		viewer = new TableViewer(container);
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
	    
		imgColumn = new TableColumn(table, SWT.LEFT);
		imgColumn.setText("");
		imgColumn.setWidth(35);
		
		numberColumn = new TableColumn(table, SWT.LEFT);
		numberColumn.setText("Nummer");
		numberColumn.setWidth(100);
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(100);
		
		choiceColumn = new TableColumn(table, SWT.LEFT);
		choiceColumn.setText("Risikoalternative");
		choiceColumn.setWidth(200);
		
		viewer.setColumnProperties(new String[] {
				IMG_COLUMN_ID,
				NUMBER_COLUMN_ID,
				NAME_COLUMN_ID,
				CHOICE_COLUMN_ID
		});
		
		
		final ComboBoxCellEditor choiceEditor = new ComboBoxCellEditor(table, 
				GefaehrdungsUmsetzung.ALTERNATIVEN, 
				SWT.READ_ONLY | SWT.DROP_DOWN);
		
	    viewer.setCellEditors(new CellEditor[] {null,null,null,choiceEditor});

	    viewer.setCellModifier(new PropertiesComboBoxCellModifier());
	    
		/* group the Filter checkboxes with composite */
		Composite compositeFilter = new Composite(container, SWT.NULL);
		GridLayout gridLayoutFilters = new GridLayout();
        gridLayoutFilters.numColumns = 2;
        
        compositeFilter.setLayout(gridLayoutFilters);
        GridData data6 = new GridData();
        data6.horizontalAlignment = SWT.LEFT;
        data6.verticalAlignment = SWT.TOP;
	    compositeFilter.setLayoutData(data6);
	    
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
		ArrayList<GefaehrdungsUmsetzung> arrListRiskGefaehrdungen = 
			((RisikoanalyseWizard)getWizard()).getGefaehrdungsUmsetzungen();

		/* map a domain model object into multiple images and text labels */
		viewer.setLabelProvider(new TableViewerLabelProvider());
		/* map domain model into array */
		viewer.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewer.setInput(arrListRiskGefaehrdungen);
		
		viewer.setSorter(new GefaehrdungenSorter());

		packAllColumns();
		checkPageComplete();
	}

	/**
	 * packs all columns of Table containing Gefaehrdungen
	 */
	private void packAllColumns() {
		imgColumn.pack();
		numberColumn.pack();
		nameColumn.pack();
		choiceColumn.pack();
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