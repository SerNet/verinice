package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.actions.ShowKonsolidatorAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropListener;
import sernet.gs.ui.rcp.main.bsi.dnd.CopyBSIModelViewAction;
import sernet.gs.ui.rcp.main.bsi.dnd.PasteBsiModelViewAction;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.filter.LebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.ObjektLebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.TagFilter;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewCloseDBAction;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewFilterAction;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewOpenDBAction;
import sernet.gs.ui.rcp.main.common.model.ChangeLogWatcher;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.common.model.ObjectDeletedException;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class BsiModelView extends ViewPart {
	/**
	 * Content provider for BSI model elements.
	 * 
	 * @author koderman@sernet.de
	 * 
	 */
	class ViewContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof CnATreeElement) {
				CnATreeElement el = (CnATreeElement) parent;
				CnATreeElement[] children = el.getChildrenAsArray();
				if (el.getLinksDown().size() > 0) {
					Object[] result = new Object[children.length + 1];
					System.arraycopy(children, 0, result, 0, children.length);
					result[children.length] = el.getLinks();
					return result;
				} else {
					return children;
				}
			}
			else if (parent instanceof LinkKategorie) {
				return ((LinkKategorie)parent).getChildren().toArray();
			}
			return null;
		}

		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof CnATreeElement) {
				CnATreeElement el = (CnATreeElement) child;
				return el.getParent();
			}
			else if (child instanceof LinkKategorie) {
				LinkKategorie kat = (LinkKategorie) child;
				return kat.getParent();
			}
			else if (child instanceof CnALink) {
				CnALink link = (CnALink) child;
				return link.getParent();
			}
			return null;
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof CnATreeElement) {
				CnATreeElement el = (CnATreeElement) parent;
				return el.getChildren().size() > 0
					|| el.getLinksDown().size() > 0;
			}
			if (parent instanceof LinkKategorie) {
				LinkKategorie kat = (LinkKategorie) parent;
				return kat.getChildren().size() > 0;
			}
			return false;
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

	}

	/**
	 * Label provider fpr BSI model elements.
	 * 
	 * @author koderman@sernet.de
	 * 
	 */
	class ViewLabelProvider extends LabelProvider {

		public Image getImage(Object obj) {
			if (obj instanceof BausteinUmsetzung) {
				BausteinUmsetzung bu = (BausteinUmsetzung) obj;
				switch (bu.getErreichteSiegelStufe()) {
				case 'A':
					return ImageCache.getInstance().getImage(
							ImageCache.BAUSTEIN_UMSETZUNG_A);
				case 'B':
					return ImageCache.getInstance().getImage(
							ImageCache.BAUSTEIN_UMSETZUNG_B);
				case 'C':
					return ImageCache.getInstance().getImage(
							ImageCache.BAUSTEIN_UMSETZUNG_C);
				}
				// else return default image
				return ImageCache.getInstance().getImage(
						ImageCache.BAUSTEIN_UMSETZUNG);
			}
			else if (obj instanceof LinkKategorie) {
				return ImageCache.getInstance().getImage(
						ImageCache.LINKS);
			}
			else if (obj instanceof CnALink) {
				CnALink link = (CnALink) obj;
				return CnAImageProvider.getImage(link.getDependency());
			}
			CnATreeElement el = (CnATreeElement) obj;
			return CnAImageProvider.getImage(el);
		}

		public String getText(Object obj) {
			if (obj == null)
				return "<null>";
			if (obj instanceof IBSIStrukturElement) {
				IBSIStrukturElement el = (IBSIStrukturElement) obj;
				CnATreeElement el2 = (CnATreeElement) obj;
				return el.getKuerzel() + " " + el2.getTitel();
			}
			else if (obj instanceof LinkKategorie)
				return ((LinkKategorie)obj).getTitle();
			else if (obj instanceof CnALink) {
				CnALink link = (CnALink) obj;
				return link.getTitle(); 
			}
			CnATreeElement el = (CnATreeElement) obj;
			return el.getTitel();
		}
	}

	public static final String ID = "sernet.gs.ui.rcp.main.views.bsimodelview"; //$NON-NLS-1$

	private Action doubleClickAction;

	private DrillDownAdapter drillDownAdapter;

	private BSIModel model;

	/**
	 * Check for model changes and update our display.
	 */
	private class BSIModelViewUpdater implements IBSIModelListener {

		private ThreadSafeViewerUpdate updater = new ThreadSafeViewerUpdate(
				viewer);

		public void childAdded(CnATreeElement category, CnATreeElement child) {
			updater.add(category, child);
		}

		public void childChanged(CnATreeElement category, CnATreeElement child) {
			updater.refresh(child);
		}

		public void childRemoved(CnATreeElement category, CnATreeElement child) {
			updater.refresh();
		}

		public void modelRefresh() {
			updater.refresh();
		}
	};

	private TreeViewer viewer;

	private BSIModelViewFilterAction filterAction;

	private final IPropertyChangeListener prefChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if ((event.getProperty().equals(PreferenceConstants.DB_URL)
					|| event.getProperty().equals(PreferenceConstants.DB_USER)
					|| event.getProperty()
							.equals(PreferenceConstants.DB_DRIVER) || event
					.getProperty().equals(PreferenceConstants.DB_PASS))) {
				CnAElementFactory.getInstance().closeModel();
				setNullModel();
			}
		}
	};

	private Action openDBAction;

	private BSIModelViewCloseDBAction closeDBAction;

	private PasteBsiModelViewAction pasteAction;

	private Action expandAllAction;

	private Action collapseAction;

	private ShowBulkEditAction bulkEditAction;

	private Action selectEqualsAction;

	private ShowKonsolidatorAction konsolidatorAction;

	private CopyBSIModelViewAction copyAction;

	public void setNullModel() {
		model = new NullModel();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.setInput(model);
				viewer.refresh();
			}
		});
	}

	/**
	 * The constructor.
	 */
	public BsiModelView() {
	}

	private void addBSIFilter() {
		viewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IDatenschutzElement)
					return false;
				return true;
			}

		});
	}
	
	@Override
	public void dispose() {
		CnAElementFactory.getInstance().closeModel();
		super.dispose();
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());

		getSite().setSelectionProvider(viewer);
		makeActions();
		createPullDownMenu();
		hookContextMenu();
		hookDoubleClickAction();
		hookDNDListeners();
		hookGlobalActions();
		addBSIFilter();
		fillLocalToolBar();
		Activator.getDefault().getPluginPreferences()
				.addPropertyChangeListener(this.prefChangeListener);
		setNullModel();

	}

	private void hookGlobalActions() {
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.PASTE.getId(), pasteAction);
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.COPY.getId(), copyAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new GroupMarker("content")); //$NON-NLS-1$
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		manager.add(new Separator());
		manager.add(copyAction);
		manager.add(pasteAction);
		manager.add(bulkEditAction);
		manager.add(selectEqualsAction);
		selectEqualsAction.setEnabled(bausteinSelected());
		manager.add(konsolidatorAction);

		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		manager.add(expandAllAction);
		manager.add(collapseAction);

		manager.add(new Separator());
		manager.add(this.openDBAction);
		manager.add(closeDBAction);

	}

	private boolean bausteinSelected() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (!sel.isEmpty() 
				&& sel.size() == 1 
				&& sel.getFirstElement() instanceof BausteinUmsetzung)
			return true;
		return false;
	}

	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.openDBAction);
		manager.add(this.closeDBAction);
		manager.add(this.filterAction);
		manager.add(expandAllAction);

		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDNDListeners() {
		Transfer[] types = new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDropSupport(operations, types, new BSIModelViewDropListener(
				viewer));
		viewer.addDragSupport(operations, types, new BSIModelViewDragListener(viewer));
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void makeActions() {

		selectEqualsAction = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Object o =sel.getFirstElement();
				if (o instanceof BausteinUmsetzung) {
					BausteinUmsetzung sourceBst = (BausteinUmsetzung) o;
					ArrayList newsel = new ArrayList(10);
					newsel.add(sourceBst);
					BSIModel model = CnAElementFactory.getCurrentModel();
					ArrayList<BausteinUmsetzung> alleBausteine = model.getBausteine();
					for (BausteinUmsetzung bst : alleBausteine) {
							if (bst.getKapitel().equals(sourceBst.getKapitel())
									)
								newsel.add(bst);
					}
					viewer.setSelection(new StructuredSelection(newsel));
				}
			}
		};
		selectEqualsAction.setText("Gleiche Bausteine selektieren");

		bulkEditAction = new ShowBulkEditAction(getViewSite()
				.getWorkbenchWindow(), "Bulk Edit...");
		
		konsolidatorAction = new ShowKonsolidatorAction(getViewSite().getWorkbenchWindow(),
				"Konsolidator...");

		doubleClickAction = new Action() {
			public void run() {
				Object sel = ((IStructuredSelection) viewer.getSelection())
						.getFirstElement();
				EditorFactory.getInstance().updateAndOpenObject(sel);
			}
		};

		
		filterAction = new BSIModelViewFilterAction(viewer,
				Messages.BsiModelView_3, new MassnahmenUmsetzungFilter(viewer),
				new MassnahmenSiegelFilter(viewer),
				new LebenszyklusPropertyFilter(viewer),
				new ObjektLebenszyklusPropertyFilter(viewer),
				new BSIModelElementFilter(viewer),
				new TagFilter(viewer));

		expandAllAction = new Action() {
			@Override
			public void run() {
				viewer.expandAll();
			}
		};
		expandAllAction.setText("Alle aufklappen");
		expandAllAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.EXPANDALL));

		collapseAction = new Action() {
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAction.setText("Alle zuklappen");
		collapseAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.COLLAPSEALL));

		copyAction = new CopyBSIModelViewAction(this, "Kopieren");
		pasteAction = new PasteBsiModelViewAction(this.viewer, "Einfügen");

		openDBAction = new BSIModelViewOpenDBAction(this, viewer);

		closeDBAction = new BSIModelViewCloseDBAction(this, viewer);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void createPullDownMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		menuManager.add(openDBAction);
		menuManager.add(closeDBAction);
		menuManager.add(filterAction);
		menuManager.add(expandAllAction);
		menuManager.add(collapseAction);

		menuManager.add(new Separator());
		menuManager.add(copyAction);
		menuManager.add(pasteAction);

	}

	public void setModel(BSIModel model2) {
		this.model = model2;
		model.addBSIModelListener(new BSIModelViewUpdater());

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.setInput(model);
				viewer.refresh();
			}
		});
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) viewer.getSelection();
	}

}