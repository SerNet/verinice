package sernet.gs.ui.rcp.main.bsi.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
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

import com.sun.star.ucb.CommandFailedException;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.actions.AuditViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.FindMassnahmenForTodoView;

/**
 * Shows implemented controls to be reviewed by the auditor.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov 2007) $ 
 * $LastChangedBy: koderman $
 *
 */
public class AuditView extends ViewPart implements IMassnahmenListView {
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.auditview"; //$NON-NLS-1$


	private static class AuditLabelProvider extends LabelProvider implements ITableLabelProvider {

		private SimpleDateFormat dateFormat =  new SimpleDateFormat("dd.MM.yy, EE"); //$NON-NLS-1$
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof PlaceHolder) {
				return null;
			}
			
			TodoViewItem mn = (TodoViewItem) element;
			if (columnIndex == 0) {
				return CnAImageProvider.getImage(mn);
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {

			if (element instanceof PlaceHolder) {
				if (columnIndex == 1) {
					PlaceHolder ph = (PlaceHolder) element;
					return ph.getTitle();
				}
				return "";
			}
			
			TodoViewItem mn = (TodoViewItem) element;
			switch(columnIndex) {
			case 0: // icon
				return ""; //$NON-NLS-1$
			case 1: // date
				Date date = mn.getNaechsteRevision();
				if (date == null)
					return Messages.TodoView_3;
				return dateFormat.format(date);
			case 2: // bearbeiter
				return mn.getRevisionDurch();
			case 3: // siegelstufe
				return "" + mn.getStufe(); //$NON-NLS-1$
			case 4: // zielobjekt
				return mn.getParentTitle();
			case 5: // title
				return mn.getTitel();
			}
			return ""; //$NON-NLS-1$
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
					setInput(true);
				}
			});
		}
	};
	
	
	private static class AuditSorter extends ViewerSorter {
		public boolean isSorterProperty(Object arg0, String arg1) {
			return arg1.equals("_date"); //$NON-NLS-1$
		}
		
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (o1 == null || o2 == null)
				return 0;
			TodoViewItem mn1 = (TodoViewItem) o1;
			TodoViewItem mn2 = (TodoViewItem) o2;
			return sortByDate(mn1.getNaechsteRevision(), mn2.getNaechsteRevision());
		}

		private int sortByDate(Date date1, Date date2) {
	        if (date1 == null)
	            return 1;
	        
	        if (date2 == null)
	            return -1;
	        
			int comp = date1.compareTo(date2);
			// reverse order:
			//comp = (comp < 0) ? 1 : (comp > 0) ? -1 : 0;
			return comp;
	        
		}
		

	}
	
	private TableViewer viewer;
	private TableColumn iconColumn;
	private TableColumn titleColumn;
	private TableColumn siegelColumn;
	private TableColumn dateColumn;
	private TableColumn zielColumn;
	private Action doubleClickAction;
	private TableColumn bearbeiterColumn;
	private AuditViewFilterAction filterAction;
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
		dateColumn.setText(Messages.AuditView_8);
		dateColumn.setWidth(200);
		
		bearbeiterColumn = new TableColumn(table, SWT.LEFT);
		bearbeiterColumn.setText(Messages.AuditView_9);
		bearbeiterColumn.setWidth(100);
		
		siegelColumn = new TableColumn(table, SWT.LEFT);
		siegelColumn.setText(Messages.AuditView_10);
		siegelColumn.setWidth(20);
		
		zielColumn = new TableColumn(table, SWT.LEFT);
		zielColumn.setText(Messages.AuditView_11);
		zielColumn.setWidth(150);
		
		titleColumn = new TableColumn(table, SWT.LEFT);
		titleColumn.setText(Messages.AuditView_12);
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
		
		viewer.setContentProvider(new MassnahmenUmsetzungContentProvider(this));
		viewer.setLabelProvider(new AuditLabelProvider());
		try {
			setInput(true);
		} catch (RuntimeException e) {
			ExceptionUtil.log(e, "Fehler beim Datenzugriff.");
		}
		
		CnAElementFactory.getInstance().addLoadListener(loadListener);
		
		createFilters();
		viewer.setSorter(new AuditSorter());
		makeActions();
		createPullDownMenu();
		hookActions();
		fillLocalToolBar();
		
		getSite().setSelectionProvider(viewer);
		packColumns();
	}
	
	public void setInput(boolean showLoadingMessage) {
		if (!CnAElementHome.getInstance().isOpen())
			return;
		
		if (showLoadingMessage)
			viewer.setInput(new PlaceHolder("Lade Maßnahmen..."));
		
		WorkspaceJob job = new WorkspaceJob("Lade alle Massnahmen für Realisierungsplan...") {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				try {
					FindMassnahmenForTodoView command = new FindMassnahmenForTodoView();
					command = ServiceFactory.lookupCommandService().executeCommand(command);
					final List<TodoViewItem> allMassnahmen = command.getAll();
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							viewer.setInput(allMassnahmen);
						}
					});
				} catch (Exception e) {
					ExceptionUtil.log(e, "Fehler beim Erstellen des Realisierungsplans.");
				}
				return Status.OK_STATUS; 
			}
		};
		job.setUser(false);
		job.schedule();
	}
	
	@Override
	public void dispose() {
		CnAElementFactory.getInstance().removeLoadListener(loadListener);
	}
	
	private void makeActions() {
		doubleClickAction = new Action() {
			public void run() {
				Object sel = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
				EditorFactory.getInstance().openEditor(sel);
			}
		};
		
		filterAction = new AuditViewFilterAction(viewer, 
				Messages.AuditView_19,
				this.umsetzungFilter,
				this.siegelFilter);
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
	
	private void packColumns() {
		dateColumn.pack();
	}
	
	
	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.filterAction);
	}
	
	
	private void createPullDownMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		menuManager.add(filterAction);
	}
	
	private void createFilters() {
		umsetzungFilter = new MassnahmenUmsetzungFilter(viewer);
		siegelFilter = new MassnahmenSiegelFilter(viewer);
		umsetzungFilter.setUmsetzungPattern(new String[] {
				MassnahmenUmsetzung.P_UMSETZUNG_JA,
				MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH
		});
	}

}
