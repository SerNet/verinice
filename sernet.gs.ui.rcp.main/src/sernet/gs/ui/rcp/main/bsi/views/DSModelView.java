/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAPlaceholder;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelForTreeView;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.IDatenschutzElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.validation.CnAValidation;
import sernet.verinice.rcp.tree.ElementManager;
import sernet.verinice.rcp.tree.TreeContentProvider;

/**
 * View that allows editing of applications' privacy ("Datenschutz") properties.
 * 
 * @author koderman[at]sernet[dot]de
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
						ExceptionUtil.log(e, Messages.DSModelView_0);
					}
				}
			});
		}

        public void loaded(ISO27KModel model) {
            // work is done in loaded( BSIModel model)    
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

		public void childChanged(CnATreeElement child) {
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

		public void linkChanged(CnALink old, CnALink link, Object source) {
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

		public void modelReload(BSIModel newModel) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildRemoved(java.lang.Integer)
		 */
		public void databaseChildRemoved(ChangeLogEntry id) {
			// TODO Auto-generated method stub
			
		}
		
	    @Override
	    public void validationAdded(Integer scopeId){};
	    
	    @Override
	    public void validationRemoved(Integer scopeId){};
	    
	    @Override
	    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation){};
	};

	private DSModelViewUpdater viewUpdater;

	private BSIModel model;

	private ElementManager elementManager;

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
	    elementManager = new ElementManager();
	}
	
	public String getRightID(){
	    return ActionRightIDs.DSMODELVIEW;
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewUpdater = new DSModelViewUpdater();
		drillDownAdapter = new DrillDownAdapter(viewer);
		
		viewer.setContentProvider(new TreeContentProvider(elementManager));
		viewer.setLabelProvider(new DSViewLabelProvider());
		viewer.setSorter(new NameSorter());

		getSite().setSelectionProvider(viewer);
		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();
		addDSFilter();
		try {
			setInput();
		} catch (CommandException e) {
			ExceptionUtil.log(e, Messages.DSModelView_6);
		}

		CnAElementFactory.getInstance().addLoadListener(loadListener);

	}

	@Override
	public void dispose() {
		CnAElementFactory.getInstance().removeLoadListener(loadListener);
		model.removeBSIModelListener(viewUpdater);
	}

	private void setInput() throws CommandException {
		if (!CnAElementFactory.isModelLoaded() ) {
			setNullModel();
			return;
		}
		
		LoadBSIModelForTreeView command = new LoadBSIModelForTreeView();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		BSIModel newModel = command.getModel();

		if (model != null){
			model.removeBSIModelListener(viewUpdater);
		}
		this.model = newModel;
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
						|| element instanceof IDatenschutzElement
						|| element instanceof CnAPlaceholder){
					return true;
				}
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
