package sernet.gs.ui.rcp.main.bsi.views;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.PropertiesComboBoxCellModifier;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.ChangeLinkType;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRelationsFor;
import sernet.hui.common.connect.HuiRelation;


/**
 * This view displays all relations (links) for a slected element and allows the user to change the link type.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RelationView extends ViewPart {

	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.RelationView"; //$NON-NLS-1$
	
	private TableViewer viewer;
	private Action jumpToAction;
	private Action action2;
	private Action doubleClickAction;
	private ISelectionListener selectionListener;
	private CnATreeElement inputElmt;
	private TableColumn col1;
	private TableColumn col2;
	private TableColumn col3;
	
	private String[] currentLinkTypeNames = new String[] {};
	private String[] currentLinkTypeIDs;

	private static final String COLUMN_IMG = "_img";
	private static final String COLUMN_TYPE = "_type";
	private static final String COLUMN_TITLE = "_title";

	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput instanceof PlaceHolder)
				return;
			inputElmt = (CnATreeElement) newInput;
			viewer.refresh();
		}
		
		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			if (obj instanceof PlaceHolder) {
				return new Object[] {obj};
			}
			
			HashSet<CnALink> result = new HashSet<CnALink>();
			result.addAll(inputElmt.getLinksDown());
			result.addAll(inputElmt.getLinksUp());
			return (CnALink[]) result.toArray(new CnALink[result.size()]);
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof PlaceHolder) {
				if (index != 1)
					return "";
				PlaceHolder pl = (PlaceHolder) obj;
				return pl.getTitle();
			}
			
			CnALink link = (CnALink) obj;
			HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(link.getTypeId());
			
			switch (index) {
			case 0:
				return "";
			case 1:
				// if we can't find a real name for the relation, we just display "depends on" or "necessary for":
				if (isDownwardLink(link))
					return (relation != null) ? relation.getName() : "hängt ab von";
				else
					return (relation != null) ? relation.getReversename() : "ist nötig für";
			case 2:
				if (isDownwardLink(link))
					return link.getDependency().getTitel();
				else
					return link.getDependant().getTitel();
			default:
				return "";
			}
		}
		
		public Image getColumnImage(Object obj, int index) {
			if (index != 0)
				return null;
			if (obj instanceof PlaceHolder)
				return null;
			
			CnALink link = (CnALink) obj;
			if (isDownwardLink(link))
				return ImageCache.getInstance().getImage(ImageCache.LINK_DOWN);
			else
				return ImageCache.getInstance().getImage(ImageCache.LINK_UP);
		}
		
	}
	class NameSorter extends ViewerSorter {
		public boolean isSorterProperty(Object arg0, String arg1) {
			return arg1.equals(COLUMN_TITLE); //$NON-NLS-1$
		}
		
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (o1 == null || o2 == null)
				return 0;
			CnALink link1 = (CnALink) o1;
			CnALink link2 = (CnALink) o2;
			if (link1.getTypeId() == null)
				return 0;
			return link1.getTypeId().compareTo(link2.getTypeId());
		}
	}


	/**
	 * The constructor.
	 */
	public RelationView() {
	}

	/**
	 * @param elmt
	 */
	public void loadLinks(final CnATreeElement elmt) {

		if (!CnAElementHome.getInstance().isOpen()) {
			return;
		}

		viewer.setInput(new PlaceHolder("Lade Relationen..."));

		WorkspaceJob job = new WorkspaceJob("Lade Relationen...") {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				Activator.inheritVeriniceContextState();

				try {
					monitor.setTaskName("Lade Relationen...");

					FindRelationsFor command = new FindRelationsFor(elmt);
					command = ServiceFactory.lookupCommandService()
							.executeCommand(command);
					final CnATreeElement linkElmt = command.getElmt();

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							viewer.setInput(linkElmt);
						}
					});
				} catch (Exception e) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							viewer.setInput(new PlaceHolder("Fehler beim Laden."));
						}
					});

					ExceptionUtil.log(e, "Fehler beim Laden von Beziehungen.");
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());

		Table table = viewer.getTable();
		
		col1 = new TableColumn(table, SWT.LEFT);
		col1.setText("");
		col1.setWidth(25);
		col1.setResizable(false);
		
		col2 = new TableColumn(table, SWT.LEFT);
		col2.setText("Relation");
		col2.setWidth(100);

		col3 = new TableColumn(table, SWT.LEFT);
		col3.setText("Titel");
		col3.setWidth(250);
		
		viewer.setColumnProperties(new String[] {
				COLUMN_IMG, //$NON-NLS-1$
				COLUMN_TYPE, //$NON-NLS-1$
				COLUMN_TITLE //$NON-NLS-1$
		});
		
		final ComboBoxCellEditor typeEditor = new ComboBoxCellEditor(table, 
				currentLinkTypeNames, 
				SWT.READ_ONLY);
		typeEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);

		viewer.setCellEditors(new CellEditor[] {null,typeEditor,null});
	    viewer.setCellModifier(new ICellModifier() {


			public boolean canModify(Object element, String property) {
				if (!property.equals(COLUMN_TYPE)
						|| 	!(element instanceof CnALink))
					return false;
				
				
				CnALink link = (CnALink) element;
				String currentName = getCurrentName(link);
				Set<HuiRelation> possibleRelations = HitroUtil.getInstance()
						.getTypeFactory().getPossibleRelations(
								link.getDependant().getEntityType().getId(),
								link.getDependency().getEntityType().getId());

				return (possibleRelations != null && possibleRelations.size() > 0);
				
			}

			public Object getValue(Object element, String property) {
				// build array for combobox and return index of currently selected element:
				if (!(element instanceof CnALink))
					return null;
				CnALink link = (CnALink) element;
				String currentName = getCurrentName(link);
				Set<HuiRelation> possibleRelations = HitroUtil.getInstance()
						.getTypeFactory().getPossibleRelations(
								link.getDependant().getEntityType().getId(),
								link.getDependency().getEntityType().getId());
				Set<String> names = new HashSet<String>();
				Set<String> IDs = new HashSet<String>();

				for (HuiRelation huiRelation : possibleRelations) {
					String id = huiRelation.getId();
					String name = (isDownwardLink(link)) ? huiRelation
							.getName() : huiRelation.getReversename();
					names.add(name);
					IDs.add(id);
				}
				currentLinkTypeIDs = (String[]) IDs.toArray(new String[IDs.size()]);
				currentLinkTypeNames = (String[]) names
						.toArray(new String[names.size()]);
				return getIndex(currentName);
			}

			private int getIndex(String currentName) {
				int i=0;
				for (String name : currentLinkTypeNames) {
					if (name.equals(currentName))
						return i;
					++i;
				}
				return -1;
			}

			public void modify(Object element, String property, Object value) {
				if (element == null)
					return;

				CnALink link = (CnALink) ((TableItem) element).getData();
				int index = (Integer) value;
				String linkTypeID = currentLinkTypeIDs[index];
				ChangeLinkType command = new ChangeLinkType(link.getId(), linkTypeID, "");
				try {
					command = ServiceFactory.lookupCommandService().executeCommand(
							command);
				} catch (CommandException e) {
					ExceptionUtil.log(e, "Fehler beim Ändern der Relation.");
				}
			}

		});
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		hookPageSelection();
	}

	/**
	 * @param link
	 * @return
	 */
	protected String getCurrentName(CnALink link) {
		HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(link.getTypeId());
		if (relation == null) {
			String name = isDownwardLink(link) ? "" : "";
		}
		
		String name = isDownwardLink(link) ? relation.getName() : relation.getReversename();
		return name;
	}

	/**
	 * @param link
	 * @return
	 */
	protected boolean isDownwardLink(CnALink link) {
		return this.inputElmt.getLinksDown().contains(link);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RelationView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void hookPageSelection() {
		selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				pageSelectionChanged(part, selection);
			}
		};
		getSite().getPage().addPostSelectionListener(selectionListener);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		getSite().getPage().removePostSelectionListener(selectionListener);
	}
	
	protected void pageSelectionChanged(IWorkbenchPart part,
			ISelection selection) {
		if (part == this)
			return;

		if (!(selection instanceof IStructuredSelection))
			return;

		if (((IStructuredSelection) selection).size() != 1)
			return;

		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (element instanceof CnATreeElement) {
			setNewInput((CnATreeElement)element);
		}
	}
	
	/**
	 * @param element
	 */
	private void setNewInput(CnATreeElement elmt) {
		loadLinks(elmt);
		setViewTitle("Relationen für: " + elmt.getTitel());
	}

	private void setViewTitle(String title) {
		this.setContentDescription(title);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(jumpToAction);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(jumpToAction);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(jumpToAction);
		manager.add(action2);
	}

	private void makeActions() {
		jumpToAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj == null)
					return;
				
				CnALink link = (CnALink) obj;
				if (isDownwardLink(link))
					setNewInput(link.getDependency());
				else
					setNewInput(link.getDependant());
			}
		};
		jumpToAction.setText("Springe zu...");
		jumpToAction.setToolTipText("Springe zum Ziel der markierten Relation");
		jumpToAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ARROW_IN));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				CnALink link = (CnALink) obj;

				// open the object on the other side of the link:
				if (isDownwardLink(link))
					EditorFactory.getInstance().updateAndOpenObject(link.getDependency());
				else
					EditorFactory.getInstance().updateAndOpenObject(link.getDependant());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Relations",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}