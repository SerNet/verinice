package sernet.gs.ui.rcp.main.bsi.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.actions.TodoViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.FindMassnahmenForListView;

/**
 * Shows controls that still have to be implemented.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov 2007) $ 
 * $LastChangedBy: koderman $
 *
 */
public class TodoView extends ViewPart {
	
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.todoview"; //$NON-NLS-1$

	private static class TodoLabelProvider extends LabelProvider implements ITableLabelProvider {

		private SimpleDateFormat dateFormat =  new SimpleDateFormat("dd.MM.yy, EE"); //$NON-NLS-1$
		
		public Image getColumnImage(Object element, int columnIndex) {
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) element;
			if (columnIndex == 0) {
				return CnAImageProvider.getImage(mn);
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) element;
			switch(columnIndex) {
			case 0: // icon
				return ""; //$NON-NLS-1$
			case 1: // date
				Date date = mn.getUmsetzungBis();
				if (date == null)
					return Messages.TodoView_3;
				return dateFormat.format(date);
			case 2: // bearbeiter
				return mn.getUmsetzungDurch();
			case 3: // siegelstufe
				return "" + mn.getStufe(); //$NON-NLS-1$
			case 4: // zielobjekt
				if (mn.getParent() instanceof GefaehrdungsUmsetzung)
					return "RA: " + 
						(mn.getParent().getParent().getParent()).getTitel(); // mn -> gefaehrdung -> risikoanalyse -> ziel
				else
					return (mn.getParent().getParent()).getTitel(); // mn -> baustein -> ziel
			case 5: // title
				return mn.getTitel();
			}
			return ""; //$NON-NLS-1$
		}
	}
	
	private static class TodoSorter extends ViewerSorter {
		public boolean isSorterProperty(Object arg0, String arg1) {
			return arg1.equals("_date"); //$NON-NLS-1$
		}
		
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (o1 == null || o2 == null)
				return 0;
			MassnahmenUmsetzung mn1 = (MassnahmenUmsetzung) o1;
			MassnahmenUmsetzung mn2 = (MassnahmenUmsetzung) o2;
			return sortByDate(mn1.getUmsetzungBis(), mn2.getUmsetzungBis());
		}

		private int sortByDate(Date date1, Date date2) {
	        if (date1 == null)
	            return 1;
	        
	        if (date2 == null)
	            return -1;
	        
			int comp = date1.compareTo(date2);
			// reverse order:
			comp = (comp < 0) ? 1 : (comp > 0) ? -1 : 0;
			return comp;
	        
		}
		

	}

	private IModelLoadListener loadListener = new IModelLoadListener() {
		public void closed(BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.setInput(new ArrayList());
				}
			});
		}
		
		public void loaded(final BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setInput();
				}
			});
		}
	};
	
	
	private TableViewer viewer;
	private TableColumn iconColumn;
	private TableColumn titleColumn;
	private TableColumn siegelColumn;
	private TableColumn dateColumn;
	private TableColumn zielColumn;
	private Action doubleClickAction;
	private TableColumn bearbeiterColumn;
	private TodoViewFilterAction filterAction;
	private MassnahmenUmsetzungFilter umsetzungFilter;
	private MassnahmenSiegelFilter siegelFilter;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | 
				SWT.MULTI | SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		
		iconColumn = new TableColumn(table, SWT.LEFT);
		iconColumn.setText(" "); //$NON-NLS-1$
		iconColumn.setWidth(25);
		
		dateColumn = new TableColumn(table, SWT.LEFT);
		dateColumn.setText(Messages.TodoView_8);
		dateColumn.setWidth(200);
		
		bearbeiterColumn = new TableColumn(table, SWT.LEFT);
		bearbeiterColumn.setText(Messages.TodoView_9);
		bearbeiterColumn.setWidth(100);
		
		siegelColumn = new TableColumn(table, SWT.LEFT);
		siegelColumn.setText(Messages.TodoView_10);
		siegelColumn.setWidth(20);
		
		zielColumn = new TableColumn(table, SWT.LEFT);
		zielColumn.setText(Messages.TodoView_11);
		zielColumn.setWidth(150);
		
		titleColumn = new TableColumn(table, SWT.LEFT);
		titleColumn.setText(Messages.TodoView_12);
		titleColumn.setWidth(250);
		
		viewer.setColumnProperties(new String[] {
				"_icon", //$NON-NLS-1$
				"_date", //$NON-NLS-1$
				"_bearbeiter", //$NON-NLS-1$
				"_siegel", //$NON-NLS-1$
				"_ziel", //$NON-NLS-1$
				"_title" //$NON-NLS-1$
		});
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createFilters();
		createPullDownMenu();

		viewer.setContentProvider(new MassnahmenUmsetzungContentProvider());
		viewer.setLabelProvider(new TodoLabelProvider());
		setInput();
		CnAElementFactory.getInstance().addLoadListener(loadListener);
		
		viewer.setSorter(new TodoSorter());
		makeActions();
		hookActions();
		fillLocalToolBar();
		
		getSite().setSelectionProvider(viewer);
		
		packColumns();
	}
	
	protected void setInput() {
		if (CnAElementHome.getInstance().isOpen()) {
			FindMassnahmenForListView command = new FindMassnahmenForListView();
			ServiceFactory.lookupCommandService().executeCommand(command);
			final List<MassnahmenUmsetzung> allMassnahmen = command.getAll();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.setInput(allMassnahmen);
				}
			});
		}
	}
	
	private void packColumns() {
		dateColumn.pack();
	}

	private void createPullDownMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		filterAction = new TodoViewFilterAction(viewer, 
				Messages.TodoView_2,
				this.umsetzungFilter,
				this.siegelFilter);
		menuManager.add(filterAction);
	}
	
	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.filterAction);
	}
	
	private void createFilters() {
		umsetzungFilter = new MassnahmenUmsetzungFilter(viewer);
		siegelFilter = new MassnahmenSiegelFilter(viewer);
		umsetzungFilter.setUmsetzungPattern(new String[] {
				MassnahmenUmsetzung.P_UMSETZUNG_NEIN,
				MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE,
				MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET
		});
	}

	private void makeActions() {
		doubleClickAction = new Action() {
			public void run() {
				Object sel = ((IStructuredSelection)viewer.getSelection())
					.getFirstElement();
				EditorFactory.getInstance().openEditor(sel);
			}
		};
	}
	
	@Override
	public void dispose() {
		CnAElementFactory.getInstance().removeLoadListener(loadListener);
	}

	private void hookActions() {
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					doubleClickAction.run();
				}
			});
	}

	@Override
	public void setFocus() {
	 viewer.getTable().setFocus();
	}

}
