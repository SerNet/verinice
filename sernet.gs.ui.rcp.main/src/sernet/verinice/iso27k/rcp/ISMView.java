/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropPerformer;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.filter.LebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.ObjektLebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.rcp.action.CollapseAction;
import sernet.verinice.iso27k.rcp.action.ControlDropPerformer;
import sernet.verinice.iso27k.rcp.action.ExpandAction;
import sernet.verinice.iso27k.rcp.action.ISMViewFilter;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.iso27k.rcp.action.TagFilter;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class ISMView extends ViewPart {

	private static final Logger LOG = Logger.getLogger(ISMView.class);
	
	public static final String ID = "sernet.verinice.iso27k.rcp.ISMView";
	
	private static Transfer[] types = new Transfer[] { TextTransfer.getInstance(),FileTransfer.getInstance() };
	private static int operations = DND.DROP_COPY | DND.DROP_MOVE;
	
	private TreeViewer viewer;
	
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
	
	private MetaDropAdapter metaDropAdapter;

	private ControlDropPerformer controlDropAdapter;

	private BSIModelViewDropPerformer bsiDropAdapter;
	
	private ISelectionListener selectionListener;

	private boolean bsiListenerAdded = false;

	private boolean catalogListenerAdded = false;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		try {
			contentProvider = new ISMViewContentProvider(cache);
			viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			drillDownAdapter = new DrillDownAdapter(viewer);
			viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
			viewer.setContentProvider(contentProvider);
			viewer.setLabelProvider(new ISMViewLabelProvider(cache));
			
			getSite().setSelectionProvider(viewer);
			
			Activator.showDerbyWarning(this.getSite().getShell());
			CnAWorkspace.getInstance().createDatabaseConfig();
			
			Activator.inheritVeriniceContextState();
			Activator.checkDbVersion();
			
			CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(new ISO27KModelViewUpdate(viewer,cache));
			
			
			setInput(CnAElementFactory.getInstance().getISO27kModel());
			
			hookContextMenu();
			makeActions();
			addActions();
			fillToolBar();
			hookDNDListeners();
		} catch (Exception e) {
			LOG.error("Error while creating organiozation view", e);
		}
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
		
		bulkEditAction = new ShowBulkEditAction(getViewSite().getWorkbenchWindow(), "Bulk Edit...");
	
		expandAction = new ExpandAction(viewer, contentProvider);
		expandAction.setText("Expand Children");
		expandAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

		collapseAction = new CollapseAction(viewer);
		collapseAction.setText("Collapse Children");
		collapseAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
	
		expandAllAction = new Action() {
			@Override
			public void run() {
				expandAll();
			}
		};
		expandAllAction.setText("Expand All");
		expandAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

		collapseAllAction = new Action() {
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAllAction.setText("Collapse All");
		collapseAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));

		filterAction = new ISMViewFilter(viewer,
				Messages.BsiModelView_3,
				new TagFilter(viewer));
		
		metaDropAdapter = new MetaDropAdapter(viewer);
		controlDropAdapter = new ControlDropPerformer(this);
		bsiDropAdapter = new BSIModelViewDropPerformer();
		metaDropAdapter.addAdapter(controlDropAdapter);
		metaDropAdapter.addAdapter(bsiDropAdapter);	
	}
	
	private void fillToolBar() {
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
	
	private void expandAll() {
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
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(bulkEditAction);
		manager.add(expandAction);
		manager.add(collapseAction);
		drillDownAdapter.addNavigationActions(manager);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}
	
	static class ViewContentProviderOld implements IStructuredContentProvider, ITreeContentProvider {

		ICommandService commandService;
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		public Object[] getChildren(Object parentElement) {
			Object[] children = new Object[] {};
			if(parentElement instanceof List) {
				children = ((List)parentElement).toArray();
			} else if(parentElement instanceof Organization) {
				children = getChildrenOfOrganization((Organization)parentElement);
			} else if( parentElement instanceof Group) {
				children = getChildrenOfGroup((Group)parentElement);
			} else if( parentElement instanceof CnATreeElement) {
				children = getChildrenOfCnATreeElement((CnATreeElement)parentElement);
			}
			return children;
		}

		/**
		 * @param organization
		 * @return
		 */
		private CnATreeElement[] getChildrenOfOrganization(Organization organization) {
			RetrieveCnATreeElement retrieveElement = RetrieveCnATreeElement.getOrganizationISMViewInstance(organization.getDbId());
			try {
				retrieveElement = getCommandService().executeCommand(retrieveElement);
			} catch (CommandException e) {
				LOG.error("Error while getting children of organization.", e);
			}
			CnATreeElement[] children = new CnATreeElement[] {};
			if(retrieveElement.getElement()!=null) {
				Set<CnATreeElement> set = retrieveElement.getElement().getChildren();
				children = (CnATreeElement[])set.toArray(new CnATreeElement[set.size()]);
			}
			return children;
		} 
		
		/**
		 * @param group
		 */
		@SuppressWarnings("unchecked")
		private CnATreeElement[] getChildrenOfGroup(Group group) {
			RetrieveCnATreeElement retrieveElement = RetrieveCnATreeElement.getGroupISMViewInstance(group.getDbId(), (Class<Group>) group.getClass());
			try {
				retrieveElement = getCommandService().executeCommand(retrieveElement);
			} catch (CommandException e) {
				LOG.error("Error while getting children of organization.", e);
			}
			CnATreeElement[] children = new CnATreeElement[] {};
			if(retrieveElement.getElement()!=null) {
				Set<CnATreeElement> set = retrieveElement.getElement().getChildren();
				children = (CnATreeElement[])set.toArray(new CnATreeElement[set.size()]);
			}
			return children;
		}
		
		/**
		 * @param element
		 * @return
		 */
		private CnATreeElement[] getChildrenOfCnATreeElement(CnATreeElement element) {
			RetrieveCnATreeElement retrieveElement = RetrieveCnATreeElement.getElementISMViewInstance(element.getDbId(), element.getClass());
			try {
				retrieveElement = getCommandService().executeCommand(retrieveElement);
			} catch (CommandException e) {
				LOG.error("Error while getting children of organization.", e);
			}
			CnATreeElement[] children = new CnATreeElement[] {};
			if(retrieveElement.getElement()!=null) {
				Set<CnATreeElement> set = retrieveElement.getElement().getChildren();
				children = (CnATreeElement[])set.toArray(new CnATreeElement[set.size()]);
			}
			return children;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			// TODO Check if there is a better way to do this
			return getChildren(element).length>0;
		}
		
		
		
		private ICommandService getCommandService() {
			if(commandService==null) {
				commandService = createCommandService();
			}
			return commandService;
		}
		
		private ICommandService createCommandService() {
			commandService = ServiceFactory.lookupCommandService();
			return commandService;
		}
	}
	
	/**
	 * Implementation of {@link ISchedulingRule} which enforces
	 * that two jobs containing an instance of this rule cannot be
	 * run at the same time.
	 * 
	 * <p>In short this enforces that the scheduler runs the jobs
	 * in the order they are scheduled.</p>
	 * 
	 */
	static class Mutex implements ISchedulingRule
	{

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
		
	}
}
