package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.awt.Container;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.openoffice.java.accessibility.ComboBox;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;

/**
 * 
 * @author koderman@sernet.de
 *
 */
public class BSIModelFilterDialog extends FilterDialog {

	private String lebenszyklus = "";
	private Combo combo;
	private boolean[] filterAusblenden;
	private Combo comboObjektLZ;
	private String objektLebenszyklus = "";
	private CheckboxTableViewer viewer;
	private String[] tagPattern;
	private Composite container;
	private Group tagGroup;
	
	private static final String[] LZ_ITEMS = new String[] {
		"<alle>",
		"Planung",
		"Beschaffung",
		"Umsetzung",
		"Betrieb",
		"Aussonderung",
		"Notfallvorsorge"
	};
	
	private static final String[] LZ_ZIELOBJEKTE_ITEMS = new String[] {
		"<alle>",
         "Betrieb",
         "Planung" ,
         "Grundinstallation", 
         "Konfiguration" ,
         "Test" ,
         "Auslieferung", 
         "Reparatur",
         "Standby",
         "Reserve"
	};
	private String[] checkedElements;


	public BSIModelFilterDialog(Shell parent,
			String[] umsetzung, 
			String[] siegel,
			String lebenszyklus,
			String objektLebenszyklus,
			boolean[] filterAusblenden, 
			String[] tags) {
		super(parent, umsetzung, siegel, null);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
		this.lebenszyklus = lebenszyklus;
		this.objektLebenszyklus = objektLebenszyklus;
		this.filterAusblenden = filterAusblenden;
		if (this.filterAusblenden == null)
			this.filterAusblenden = new boolean[] {false, false};
		this.tagPattern = tags;
		
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		Label intro = new Label(container, SWT.NONE);
		intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 2, 1));
		intro.setText("Filtern nach folgenden Kriterien:");
		
		Group boxesComposite = createUmsetzungGroup(container);
		Group boxesComposite2 = createSiegelGroup(container);
		
		createLebenszyklusDropDown(container);
		createObjektLebenszyklusDropDown(container);
		
		createUmsetzungCheckboxes(boxesComposite);
		createSiegelCheckboxes(boxesComposite2);
		
		Group group = createAusblendenGroup(container);
		createAusblendenCheckboxes(group);
		
		tagGroup = createTagfilterGroup(container);
		
		initContent();
		container.layout();
		return container;
	}
	
	private Group createTagfilterGroup(Composite parent) {
		Group groupComposite = new Group(parent, SWT.BORDER);
		groupComposite.setText("Nach Tag selektieren");
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER,
				true, false, 2, 1);
		groupComposite.setLayoutData(gridData);
		groupComposite.setLayout(new GridLayout(1, false));
		
		ScrolledComposite comp = new ScrolledComposite(groupComposite, SWT.V_SCROLL);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setExpandHorizontal(true);

		viewer = CheckboxTableViewer.newCheckList(comp, SWT.BORDER);
		Table table = viewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		
		comp.setContent(viewer.getControl());
		
		//FIXME workaround to prevent tableviewer size from exceeding shell size:
		comp.setMinSize(100,100);

		TableColumn checkboxColumn = new TableColumn(table, SWT.LEFT);
		checkboxColumn.setText("");
		checkboxColumn.setWidth(35);

		TableColumn imageColumn = new TableColumn(table, SWT.LEFT);
		imageColumn.setText("Tag");
		imageColumn.setWidth(100);
		
		viewer.setContentProvider(new ArrayContentProvider());
		
		viewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 1)
					return (String) element;
				return null;
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		});
		
		return groupComposite;
	}
	
	public String[] getCheckedElements() {
		return checkedElements;
	}


	private Group createAusblendenGroup(Composite parent) {
		Group boxesComposite = new Group(parent, SWT.BORDER);
		boxesComposite.setText("Ausblenden");
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER,
				true, false, 2, 1);
		boxesComposite.setLayoutData(gridData);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns = 2;
		boxesComposite.setLayout(layout2);
		return boxesComposite;
	
	}
	
private void createAusblendenCheckboxes(Group parent) {
		
		final Button button1 = new Button(parent, SWT.CHECK);
		button1.setText("Bausteinzuordnungen");
		button1.setSelection(filterAusblenden[0]);
		button1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button1.getSelection())
					filterAusblenden[0] = true;
				else
					filterAusblenden[0] = false;
			}
		});
		
		final Button button2 = new Button(parent, SWT.CHECK);
		button2.setText("Maßnahmenumsetzungen");
		button2.setSelection(filterAusblenden[1]);
		button2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button2.getSelection())
					filterAusblenden[1] = true;
				else
					filterAusblenden[1] = false;
			}
		});
		
			
	}

	private void createLebenszyklusDropDown(Composite container) {
		Label label = new Label(container, SWT.None);
		label.setText("Maßnahmen für Lebenszyklus");
		label.pack();
		
		combo = new Combo(container, SWT.NONE);
		combo.setItems(LZ_ITEMS);
		combo.setText(lebenszyklus==null ? "" : lebenszyklus);
		combo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setLZ();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createObjektLebenszyklusDropDown(Composite container) {
		Label label = new Label(container, SWT.None);
		label.setText("Objekte in Lebenszyklus");
		label.pack();
		
		comboObjektLZ = new Combo(container, SWT.NONE);
		comboObjektLZ.setItems(LZ_ZIELOBJEKTE_ITEMS);
		comboObjektLZ.setText(objektLebenszyklus==null ? "" : objektLebenszyklus);
		comboObjektLZ.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setObjektLZ();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void setLZ() {
		if (combo.getSelectionIndex() == 0)
			lebenszyklus = "";
		else 
			this.lebenszyklus = LZ_ITEMS[combo.getSelectionIndex()];
	}

	private void setObjektLZ() {
		if (comboObjektLZ.getSelectionIndex() == 0)
			objektLebenszyklus = "";
		else 
			objektLebenszyklus = LZ_ZIELOBJEKTE_ITEMS[comboObjektLZ.getSelectionIndex()];
	}
	
	public String getLebenszyklus() {
		return lebenszyklus;
	}
	
	
	protected void initContent() {
		super.initContent();
		if (CnAElementFactory.getCurrentModel() != null) {
			viewer.setInput(CnAElementFactory.getCurrentModel().getTags());
			
			//FIXME workaround to prevent tableviewer size from exceeding shell size:
			viewer.getTable().setSize(200,200);
			
			if (tagPattern != null)
				viewer.setCheckedElements(tagPattern);
			tagGroup.getParent().layout(true);
		}
		
	}
	
	@Override
	public boolean close() {
		// get checked objects, cast to string:
		List<Object> tagList = Arrays.asList(viewer.getCheckedElements());
		this.checkedElements = (String[]) tagList.toArray(new String[tagList.size()]);
		return super.close();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Maßnahmen Filter Einstellungen");
		
		// FIXME workaround to prevent tableviewer size from exceeding shell size:
		newShell.setSize(400,500);
	}


	public boolean[] getAusblendenSelection() {
		return this.filterAusblenden;
	}


	public String getObjektLebenszyklus() {
		return objektLebenszyklus;
	}
}
