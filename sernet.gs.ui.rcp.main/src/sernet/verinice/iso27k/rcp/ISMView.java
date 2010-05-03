/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ShowAccessControlEditAction;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropPerformer;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.rcp.action.CollapseAction;
import sernet.verinice.iso27k.rcp.action.ControlDropPerformer;
import sernet.verinice.iso27k.rcp.action.ExpandAction;
import sernet.verinice.iso27k.rcp.action.HideEmptyFilter;
import sernet.verinice.iso27k.rcp.action.ISMViewFilter;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.iso27k.rcp.action.TagFilter;
import sernet.verinice.rcp.IAttachedToPerspective;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ISMView extends ViewPart implements IAttachedToPerspective {

	private static final Logger LOG = Logger.getLogger(ISMView.class);
	
	public static final String ID = "sernet.verinice.iso27k.rcp.ISMView"; //$NON-NLS-1$
	
	private static Transfer[] types = new Transfer[] { TextTransfer.getInstance(),FileTransfer.getInstance() };
	private static int operations = DND.DROP_COPY | DND.DROP_MOVE;

	protected TreeViewer viewer;
	
	TreeViewerCache cache = new TreeViewerCache();
	
	ISMViewContentProvider contentProvider;

	private DrillDownAdapter drillDownAdapter;

	private Action doubleClickAction; 
	
	private ShowBulkEditAction bulkEditAction;
	
	private ExpandAction expandAction;
	
	private CollapseAction collapseAction;
	
	private Action expandAllAction;

	private Action collapseAllAction;
	
	private ISMViewFilter filterAction;
	
	protected HideEmptyFilter hideEmptyFilter;
	
	private MetaDropAdapter metaDropAdapter;

	private ControlDropPerformer controlDropAdapter;

	private BSIModelViewDropPerformer bsiDropAdapter;
	
	private ShowAccessControlEditAction accessControlEditAction;
	
	private IModelLoadListener modelLoadListener;

	private ISO27KModelViewUpdate modelUpdateListener;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {
		try {
			initView(parent);
			startInitDataJob();
		} catch (Exception e) {
			LOG.error("Error while creating organization view", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.ISMView_2);
		}
		
	}

	protected void initView(Composite parent) {
		contentProvider = new ISMViewContentProvider(cache);
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ISMViewLabelProvider(cache));
		getSite().setSelectionProvider(viewer);
		hookContextMenu();
		makeActions();
		addActions();
		fillToolBar();
		hookDNDListeners();
	}
	
	/**
	 * 
	 */
	protected void startInitDataJob() {
		WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
					initData();
				} catch (Exception e) {
					LOG.error("Error while loading data.", e); //$NON-NLS-1$
					status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.ISMView_4,e); //$NON-NLS-1$
				} finally {
					monitor.done();
				}
				return status;
			}
		};
		JobScheduler.scheduleInitJob(initDataJob);		
	}

	protected void initData() {	
		if(CnAElementFactory.isIsoModelLoaded()) {
			if (modelUpdateListener == null ) {
				// modellistener should only be created once!
				if (LOG.isDebugEnabled())
					Logger.getLogger(this.getClass()).debug("Creating modelUpdateListener for ISMView."); //$NON-NLS-1$
				modelUpdateListener = new ISO27KModelViewUpdate(viewer,cache);
				CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(modelUpdateListener);
				Display.getDefault().syncExec(new Runnable(){
					public void run() {
						setInput(CnAElementFactory.getInstance().getISO27kModel());
					}
				});
			}
		} else if(modelLoadListener==null) {
			// model is not loaded yet: add a listener to load data when it's laoded
			modelLoadListener = new IModelLoadListener() {

				public void closed(BSIModel model) {
					// nothing to do
				}

				public void loaded(BSIModel model) {
					synchronized (modelLoadListener) {
						startInitDataJob();
					}
				}
				
			};
			CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(modelUpdateListener);
		CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
		super.dispose();
	}

	public void setInput(ISO27KModel model) {
		viewer.setInput(model);
	}
	
	public void setInput(List<Organization> organizationList) {
		viewer.setInput(organizationList);
	}
	
	public void setInput(Organization organization) {
		viewer.setInput(organization);
	}
	
	private void makeActions() {
		doubleClickAction = new Action() {
			public void run() {
				if(viewer.getSelection() instanceof IStructuredSelection) {
					Object sel = ((IStructuredSelection) viewer.getSelection()).getFirstElement();		
					EditorFactory.getInstance().updateAndOpenObject(sel);
				}
			}
		};
		
		bulkEditAction = new ShowBulkEditAction(getViewSite().getWorkbenchWindow(), Messages.ISMView_6);
	
		expandAction = new ExpandAction(viewer, contentProvider);
		expandAction.setText(Messages.ISMView_7);
		expandAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

		collapseAction = new CollapseAction(viewer);
		collapseAction.setText(Messages.ISMView_8);
		collapseAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
	
		expandAllAction = new Action() {
			@Override
			public void run() {
				expandAll();
			}
		};
		expandAllAction.setText(Messages.ISMView_9);
		expandAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

		collapseAllAction = new Action() {
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAllAction.setText(Messages.ISMView_10);
		collapseAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
		
		hideEmptyFilter = createHideEmptyFilter();
		filterAction = new ISMViewFilter(viewer,
				Messages.ISMView_12,
				new TagFilter(viewer),
				hideEmptyFilter);
		
		metaDropAdapter = new MetaDropAdapter(viewer);
		controlDropAdapter = new ControlDropPerformer(this);
		bsiDropAdapter = new BSIModelViewDropPerformer();
		metaDropAdapter.addAdapter(controlDropAdapter);
		metaDropAdapter.addAdapter(bsiDropAdapter);	
		
		accessControlEditAction = new ShowAccessControlEditAction(getViewSite().getWorkbenchWindow(), Messages.ISMView_11);
	}

    /**
     * Override this in subclasses to hide empty groups
     * on startup.
     * 
     * @return a HideEmptyFilter
     */
    protected HideEmptyFilter createHideEmptyFilter() {
        return new HideEmptyFilter(viewer);
    }
	
	protected void fillToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(expandAllAction);
		manager.add(collapseAllAction);
		drillDownAdapter.addNavigationActions(manager);
		manager.add(filterAction);
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
		viewer.addDragSupport(operations, types, new BSIModelViewDragListener(viewer));
		viewer.addDropSupport(operations, types, metaDropAdapter);
		
	}
	
	protected void expandAll() {
		// TODO: do this a new thread and show user a progress bar
		viewer.expandAll();
	}
	
	private void addActions() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
		viewer.addSelectionChangedListener(expandAction);
		viewer.addSelectionChangedListener(collapseAction);
	}

	/**
	 * @param manager
	 */
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(new GroupMarker("content")); //$NON-NLS-1$
		manager.add(new Separator());
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator());
		manager.add(new GroupMarker("special")); //$NON-NLS-1$
		manager.add(bulkEditAction);
		manager.add(accessControlEditAction);
		manager.add(new Separator());
		manager.add(expandAction);
		manager.add(collapseAction);
		drillDownAdapter.addNavigationActions(manager);

		
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public ISMViewContentProvider getContentProvider() {
		return contentProvider;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
	 */
	public String getPerspectiveId() {
		return Iso27kPerspective.ID;
	}

}
