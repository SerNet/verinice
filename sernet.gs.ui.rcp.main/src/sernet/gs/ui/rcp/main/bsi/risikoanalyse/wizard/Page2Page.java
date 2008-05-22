package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;


/**
 * WizardPage - pick the Gefährdungen, which need processing.
 * 
 * @author ahanekop@sernet.de
 *
 */

public class Page2Page extends WizardPage {

	private Composite container;
	private TableColumn checkboxColumn;
	private TableColumn numberColumn;
	private TableColumn nameColumn;
	private TableColumn descrColumn;
	
	protected Page2Page() {
		super("Gefährdung auswählen");
		setTitle("Gefährdung auswählen");
		setDescription("Wählen Sie die Gefährdungen aus, die behandelt werden sollen.");
	}
	
	/* must be implemented - content of wizard page! */
	public void createControl(Composite parent) {

		container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		container.setLayout(gridLayout);
		setControl(container);

		/* table viewer */
		final CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(
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
		checkboxColumn.setWidth(25);
		
		numberColumn = new TableColumn(table, SWT.LEFT);
		numberColumn.setText("Nummer");
		numberColumn.setWidth(100);
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(100);
		
		descrColumn = new TableColumn(table, SWT.LEFT);
		descrColumn.setText("Beschreibung");
		descrColumn.setWidth(200);
		
		viewer.setColumnProperties(new String[] {
				"_checkbox",
				"_number",
				"_name",
				"_descr"
		});
		
		String[] inhalt = new String[] {
				"eins", "zwei", "drei"
		};
		
		viewer.setLabelProvider(new ItemTableLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(inhalt);
		
		/* group the buttons with composite */
		Composite composite = new Composite(container, SWT.NULL);
		// composite.setBounds(100,100,100,100);
		GridLayout gridLayoutButtons = new GridLayout();
        gridLayoutButtons.numColumns = 2;
        composite.setLayout(gridLayoutButtons);
        GridData data2 = new GridData();
	    // data2.verticalAlignment = SWT.BOTTOM;
        data2.horizontalAlignment = SWT.RIGHT;
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
	    // data3.grabExcessHorizontalSpace = true;
	    // data3.grabExcessVerticalSpace = true;
	    // data3.horizontalAlignment = SWT.FILL;
	    // data3.verticalAlignment = SWT.BOTTOM;
	    button2.setLayoutData(data3);
	    
	    /* delete button */
	    Button button3 = new Button(composite, SWT.PUSH);
	    button3.setText("löschen");
	    GridData data4 = new GridData();
	    // data4.grabExcessHorizontalSpace = true;
	    // data4.grabExcessVerticalSpace = true;
	    // data4.horizontalAlignment = SWT.FILL;
	    // data4.verticalAlignment = SWT.BOTTOM;
	    button3.setLayoutData(data4);
	    
		// setPageComplete(false);
	}

	public class ItemTableLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String string = (String) element;
			switch (columnIndex) {
			case 0:
				return "";
			case 1:
				return string;
			case 2:
				return string.substring(0, 1);
			default:
				return "";
			}
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}
	}
	
	/* noch zu implementieren: setPageComplete() - Freigabe next und finish */
	
}