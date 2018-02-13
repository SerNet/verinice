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
import org.eclipse.jface.dialogs.MessageDialog;
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

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.CnAPlaceholder;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NullModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.IDatenschutzElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.validation.CnAValidation;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.tree.TreeContentProvider;
import sernet.verinice.service.commands.crud.LoadBSIModelForTreeView;
import sernet.verinice.service.tree.ElementManager;

/**
 * View that allows editing of applications' privacy ("Datenschutz") properties.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class DSModelView extends RightsEnabledView {
	public static final String ID = "sernet.gs.ui.rcp.main.views.dsmodelview"; //$NON-NLS-1$

	protected TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action doubleClickAction;

	/**
	 * Check model load / unload and update view.
	 */
	private IModelLoadListener loadListener = new IModelLoadListener() {
		@Override
        public void closed(BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
                public void run() {
					setNullModel();
				}
			});
		}

		@Override
        public void loaded(final BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
                public void run() {
					try {
						setInput();
					} catch (CommandException e) {
						ExceptionUtil.log(e, Messages.DSModelView_0);
					}
				}
			});
		}

        @Override
        public void loaded(ISO27KModel model) {
            // work is done in loaded( BSIModel model)    
        }

        @Override
        public void loaded(BpModel model) {
         // work is done in loaded( BSIModel model)   
            
        }

        @Override
        public void loaded(CatalogModel model) {
            // nothing to do
        }
	};

	/**
	 * Check for model changes and update our display.
	 */
	private class DSModelViewUpdater implements IBSIModelListener {

		private ThreadSafeViewerUpdate updater = new ThreadSafeViewerUpdate(
				viewer);

		@Override
        public void childAdded(CnATreeElement category, CnATreeElement child) {
			updater.add(category, child);
		}

		@Override
        public void childChanged(CnATreeElement child) {
			updater.refresh(child);
		}

		@Override
        public void childRemoved(CnATreeElement category, CnATreeElement child) {
			updater.refresh();
		}

		/**
		 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
		 */
		@Deprecated
        @Override
        public void modelRefresh() {
			modelRefresh(null);
		}

		@Override
        public void modelRefresh(Object source) {
			updater.refresh();
		}

		@Override
        public void linkChanged(CnALink old, CnALink link, Object source) {
			// do nothing
		}
		
		@Override
        public void linkRemoved(CnALink link) {
			// do nothing
			
		}
		
		@Override
        public void linkAdded(CnALink link) {
			// do nothing
		}

		@Override
        public void databaseChildAdded(CnATreeElement child) {
			// TODO Auto-generated method stub
			
		}

		@Override
        public void databaseChildChanged(CnATreeElement child) {
			// TODO Auto-generated method stub
			
		}

		@Override
        public void databaseChildRemoved(CnATreeElement child) {
			// TODO Auto-generated method stub
			
		}

		@Override
        public void modelReload(BSIModel newModel) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildRemoved(java.lang.Integer)
		 */
		@Override
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
	
	@Override
    public String getRightID(){
	    return ActionRightIDs.DSMODELVIEW;
	}
	
	/* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }

	@Override
    public void createPartControl(Composite parent) {
	    super.createPartControl(parent);
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
        MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                Messages.DSModelView_7,
                Messages.DSModelView_8);

	}

	@Override
	public void dispose() {
		CnAElementFactory.getInstance().removeLoadListener(loadListener);
		model.removeBSIModelListener(viewUpdater);
		super.dispose();
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
			@Override
            public void run() {
				viewer.setInput(model);
				viewer.refresh();
			}
		});
	}

	public void setNullModel() {
		model = new NullModel();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
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
			@Override
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
			@Override
            public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
    public void setFocus() {
		viewer.getControl().setFocus();
	}

}
