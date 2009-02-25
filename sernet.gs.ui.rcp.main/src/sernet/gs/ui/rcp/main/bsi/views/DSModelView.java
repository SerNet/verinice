package sernet.gs.ui.rcp.main.bsi.views;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.taskcommands.FindMassnahmenForTodoView;

/**
 * View that allows editing of applications' privacy ("Datenschutz") properties.
 * 
 * @author koderman@sernet.de
 * 
 */
public class DSModelView extends ViewPart {
	public static final String ID = "sernet.gs.ui.rcp.main.views.dsmodelview"; //$NON-NLS-1$

	protected TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action doubleClickAction;

	/**
	 * Check model load / unload and update view.
	 */
	private IModelLoadListener loadListener = new IModelLoadListener() {
		public void closed(BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setNullModel();
				}
			});
		}

		public void loaded(final BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						setInput();
					} catch (CommandException e) {
						ExceptionUtil.log(e, "Kann Datenschutzmodell nicht anzeigen.");
					}
				}
			});
		}
	};

	/**
	 * Check for model changes and update our display.
	 */
	private class DSModelViewUpdater implements IBSIModelListener {

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

		/**
		 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
		 */
		public void modelRefresh() {
			modelRefresh(null);
		}

		public void modelRefresh(Object source) {
			updater.refresh();
		}

		public void linkChanged(CnALink link) {
			// do nothing
		}
		
		public void linkRemoved(CnALink link) {
			// do nothing
			
		}
		
		public void linkAdded(CnALink link) {
			// do nothing
		}

		public void databaseChildAdded(CnATreeElement child) {
			// TODO Auto-generated method stub
			
		}

		public void databaseChildChanged(CnATreeElement child) {
			// TODO Auto-generated method stub
			
		}

		public void databaseChildRemoved(CnATreeElement child) {
			// TODO Auto-generated method stub
			
		}
	};

	private DSModelViewUpdater viewUpdater;

	private BSIModel model;

	/**
	 * Content provider for BSI model elements.
	 * 
	 * @author koderman@sernet.de
	 * 
	 */
	class ViewContentProvider implements ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof CnATreeElement) {
				CnATreeElement el = (CnATreeElement) child;
				return el.getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof CnATreeElement) {
				CnATreeElement el = (CnATreeElement) parent;
				return el.getChildrenAsArray();
			}
			return null;
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof CnATreeElement) {
				CnATreeElement el = (CnATreeElement) parent;
				return el.getChildren().size() > 0;
			}
			return false;
		}

	}

	/**
	 * Label provider für BSI model elements.
	 * 
	 * @author koderman@sernet.de
	 * 
	 */
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			if (obj instanceof ITVerbund)
				return Messages.DSModelView_1;

			if (obj instanceof AnwendungenKategorie)
				return Messages.DSModelView_2;

			CnATreeElement el = (CnATreeElement) obj;
			return el.getTitel();
		}

		public Image getImage(Object obj) {
			CnATreeElement el = (CnATreeElement) obj;
			return CnAImageProvider.getImage(el);
		}
	}

	class NameSorter extends ViewerSorter {
		@Override
		public int category(Object element) {
			return element instanceof BausteinUmsetzung ? 0 : 1;
		}

		// @Override
		// public int compare(Viewer viewer, Object e1, Object e2) {
		// BSITreeElement elmt1 = (BSITreeElement) e1;
		// BSITreeElement elmt2 = (BSITreeElement) e2;
		// return elmt1.getTitle().compareToIgnoreCase(elmt2.getTitle());
		// }

	}

	/**
	 * The constructor.
	 */
	public DSModelView() {
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewUpdater = new DSModelViewUpdater();
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());

		getSite().setSelectionProvider(viewer);
		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();
		addDSFilter();
		try {
			setInput();
		} catch (CommandException e) {
			ExceptionUtil.log(e, "Kann Datenschutzmodell nicht anzeigen.");
		}

		CnAElementFactory.getInstance().addLoadListener(loadListener);

	}

	@Override
	public void dispose() {
		CnAElementFactory.getInstance().removeLoadListener(loadListener);
		model.removeBSIModelListener(viewUpdater);
	}

	private void setInput() throws CommandException {
		LoadBSIModel command = new LoadBSIModel();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		BSIModel model2 = command.getModel();

		model.removeBSIModelListener(viewUpdater);
		this.model = model2;
		model.addBSIModelListener(this.viewUpdater);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.setInput(model);
				viewer.refresh();
			}
		});
	}

	public void setNullModel() {
		model = new NullModel();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.setInput(model);
				viewer.refresh();
			}
		});
	}

	private void addDSFilter() {
		viewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof ITVerbund
						|| element instanceof AnwendungenKategorie
						|| element instanceof Anwendung
						|| element instanceof IDatenschutzElement)
					return true;
				return false;
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {

		doubleClickAction = new Action() {
			public void run() {
				Object sel = ((IStructuredSelection) viewer.getSelection())
						.getFirstElement();
				if (sel instanceof CnATreeElement
						&& !(sel instanceof ITVerbund)) {
					EditorFactory.getInstance().openEditor(sel);
				}
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

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}